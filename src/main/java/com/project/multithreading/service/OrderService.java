package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // İşlemleri toplu bir transaction içinde çalıştırıyoruz.
    @Transactional
    public void createOrdersConcurrently() {
        // ExecutorService ile 10 iş parçacığı oluşturuyoruz
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 10; i++) {
                futures.add(executorService.submit(() -> {
                    String threadName = Thread.currentThread().getName();
                    try {
                        // Thread-2 bir hata atıyor
                        if (threadName.endsWith("thread-2")) {
                            System.out.println("...Thread-2 REJECTED...");
                            throw new RuntimeException("Thread-2 çalışırken hata oluştu!"); // Hata simülasyonu
                        }

                        // Siparişleri oluşturuyoruz
                        for (int j = 0; j < 10; j++) {
                            Order order = new Order();
                            order.setDescription("Order from " + threadName + " - Record " + j);
                            orderRepository.save(order);
                            System.out.println(order.getDescription());
                        }
                        return true; // Başarılıysa true döndür
                    } catch (Exception e) {
                        System.out.println("Hata oluştu: " + e.getMessage());
                        return false; // Hata varsa false döndür
                    }
                }));
            }

            // Tüm iş parçacıklarının tamamlanmasını bekliyoruz
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);

            // Herhangi bir iş parçacığında hata olup olmadığını kontrol ediyoruz
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    throw new RuntimeException("Bir iş parçacığında hata meydana geldi, rollback yapılıyor.");
                }
            }

        } catch (Exception e) {
            System.out.println("Hata tespit edildi: " + e.getMessage());
            throw new RuntimeException("Tüm işlemler rollback yapılacak!"); // Hata varsa ana thread rollback tetikler
        } finally {
            if (!executorService.isShutdown()) {
                executorService.shutdownNow();
            }
        }
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }
}
