package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public void createOrdersConcurrently() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    String threadName = Thread.currentThread().getName();

                    if ("thread-2".equals(threadName)) {
                        throw new RuntimeException("Thread-2 çalışırken hata oluştu!");
                    }

                    for (int j = 0; j < 10; j++) {
                        Order order = Order.builder()
                                .description("Order from " + threadName + " - Record " + j)
                                .build();
                        orderRepository.save(order);
                    }
                });
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    public void deleteAll(){
        orderRepository.deleteAll();
    }
}

