package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    private void customRollback(List<Integer> idList) {
        deleteAll(idList.stream().map(this::getById).toList());
    }

    public void createOrdersConcurrently() {
        List<Thread> threads = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();
        int threadCount = 10;
        int taskCount = 10;
        AtomicInteger finishedThreadCount = new AtomicInteger();
        AtomicInteger successThreadCount = new AtomicInteger();

        try {

            for (int i = 0; i < threadCount; i++) {
                String threadName = "Thread-" + (i + 1);

                Thread thread = new Thread(() -> {
                    try {
                        if (Thread.currentThread().getName().equals("Thread-2")) {
                            System.out.println("...Thread-2 REJECTED...");
                            throw new RuntimeException("Thread-2 çalışırken hata oluştu!");
                        }

                        if (Thread.currentThread().getName().equals("Thread-3")) {
                            System.out.println("Thread-3 is sleeping for 5 seconds...");
                            Thread.sleep(5000);
                        }

                        // Siparişler
                        for (int j = 0; j < taskCount; j++) {
                            Order order = new Order();
                            order.setDescription("Order from " + Thread.currentThread().getName() + " - Record " + j);
                            repository.save(order);
                            idList.add(order.getId());
                            System.out.println(order.getDescription());
                        }
                        successThreadCount.getAndAdd(1);

                    } catch (Exception e) {
                        System.out.println("Hata oluştu: " + e.getMessage());
                        throw new RuntimeException(e);
                    } finally {
                        finishedThreadCount.getAndAdd(1);
                    }
                });


                thread.setName(threadName);
                threads.add(thread);
                thread.start();
            }


            for (Thread thread : threads) {
                thread.join();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Finished Thread: " + finishedThreadCount.get() +
                "\nSuccess Thread: " + successThreadCount.get() + "/" + threadCount +
                "\nSuccess task count: " + idList.size() + "/" + taskCount * threadCount);

        if ((successThreadCount.get() != threadCount) & (finishedThreadCount.get() == threadCount)) {
            System.out.println("Rolling back...");
            customRollback(idList);
        }
    }


    public List<Order> getAll() {
        return repository.findAll();
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteAll(List<Order> orders) {
        repository.deleteAll(orders);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public Order getById(int id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));
    }
}