package com.project.multithreading.service;

import com.project.multithreading.repository.Order;
import com.project.multithreading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private void customRollback(List<Integer> idList) {
        idList.forEach(this::deleteById);
    }

    public void createOrdersConcurrently() {
        List<Thread> threads = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();

        try {

            for (int i = 0; i < 10; i++) {
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
                        for (int j = 0; j < 10; j++) {
                            Order order = new Order();
                            order.setDescription("Order from " + Thread.currentThread().getName() + " - Record " + j);
                            orderRepository.save(order);
                            idList.add(order.getId());
                            System.out.println(order.getDescription());
                        }
                    } catch (Exception e) {
                        System.out.println("Hata oluştu: " + e.getMessage());
                        throw new RuntimeException(e);
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
            customRollback(idList);
            throw new RuntimeException("Tüm işlemler rollback yapılacak!", e);
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