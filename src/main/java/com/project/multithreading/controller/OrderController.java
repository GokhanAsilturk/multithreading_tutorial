package com.project.multithreading.controller;

import com.project.multithreading.repository.Order;
import com.project.multithreading.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> createOrders() {
        try {
            orderService.createOrdersConcurrently();
            return ResponseEntity.ok("Siparişler başarıyla oluşturuldu.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAll());
        } catch (Exception e) {
            throw new InternalError("Hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllOrders() {
        try {
            orderService.deleteAll();
            return ResponseEntity.ok("Siparişler silindi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hata oluştu: " + e.getMessage());
        }
    }
}
