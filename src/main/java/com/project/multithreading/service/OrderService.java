package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Transactional
    public void createOrdersConcurrently() {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            // 10 thread başlatılıyor
            for (int i = 0; i < 10; i++) {
                futures.add(executorService.submit(() -> {
                    String threadName = Thread.currentThread().getName();
                    try {
                        if (threadName.endsWith("thread-2")) {
                            System.out.println("...Thread-2 REJECTED...");
                            throw new RuntimeException("Thread-2 çalışırken hata oluştu!");
                        }

                        //Order oluşturuluyor
                        for (int j = 0; j < 10; j++) {
                            Order order = new Order();
                            order.setDescription("Order from " + threadName + " - Record " + j);
                            orderRepository.save(order);
                            System.out.println(order.getDescription());
                        }

                        return true; // İşlem başarılıysa true döndür
                    } catch (Exception e) {
                        System.out.println("Hata oluştu: " + e.getMessage());
                        return false; // Hata olursa false döndür
                    }
                }));
            }

            // Threadlerin tamamlanmasını bekle
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);

            // İşlemler kontrol ediliyor, bir hata varsa rollback yapılacak
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    throw new RuntimeException("Bir thread hata verdi, rollback yapılıyor.");
                }
            }

            transactionManager.commit(status); // Başarılıysa commit
        } catch (Exception e) {
            System.out.println("Toplu rollback yapılıyor: " + e.getMessage());
            transactionManager.rollback(status); // Hata varsa rollback
        }
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }
}
