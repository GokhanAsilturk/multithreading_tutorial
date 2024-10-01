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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    Pattern pattern = Pattern.compile("pool-\\d+-thread-(\\d+)");
                    Matcher matcher = pattern.matcher(threadName);

                    if (matcher.find()) {
                        String threadNumber = matcher.group(1);

                        if ("2".equals(threadNumber)) {
                            System.out.println("...Thread-2 REJECTED...");
                            throw new RuntimeException("Thread-2 çalışırken hata oluştu!");
                        }
                    }

                    for (int j = 0; j < 10; j++) {
                        Order order = new Order();
                        order.setDescription("Order from " + threadName + " - Record " + j);
                        orderRepository.save(order);
                        System.out.println(order.getDescription());
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

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }
}

