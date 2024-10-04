package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private void customRollback(List<Integer> idList) {
        idList.forEach(this::deleteById);
    }

    public void createOrdersConcurrently() {
        // ExecutorService ile 10 iş parçacığı oluşturuyoruz
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();

        try {

            for (int i = 0; i < 10; i++) {

                futures.add(executorService.submit(() -> {
                    String threadName = Thread.currentThread().getName();
                    try {
                        if (threadName.endsWith("thread-2")) {
                            System.out.println("...Thread-2 REJECTED...");
                            throw new RuntimeException("Thread-2 çalışırken hata oluştu!"); // Hata simülasyonu
                        }

                        // Siparişleri oluşturuyoruz
                        for (int j = 0; j < 10; j++) {
                            Order order = new Order();
                            order.setDescription("Order from " + threadName + " - Record " + j);
                            orderRepository.save(order);
                            idList.add(order.getId());
                            System.out.println(order.getDescription());
                        }
                        return true;
                    } catch (Exception e) {
                        System.out.println("Hata oluştu: " + e.getMessage());
                        return false;
                    }
                }));
                for (Future<Boolean> future : futures) {
                    if (!future.get()) {
                        throw new RuntimeException();
                    }
                }
            }

            // Tüm iş parçacıklarının tamamlanmasını bekliyoruz
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS)


        } catch (Exception e) {
            customRollback(idList);
            throw new RuntimeException("Tüm işlemler rollback yapılacak!");
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

    public void deleteById(Integer id) {
        orderRepository.deleteById(id);
    }
}