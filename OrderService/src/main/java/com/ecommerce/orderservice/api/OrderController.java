package com.ecommerce.orderservice.api;

import com.ecommerce.orderservice.application.service.OrderService;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService, OutboxRepository outboxRepository) {
        this.orderService = orderService;

    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequest orderCreateRequest) {
        try{
            orderService.createOrder(
                    orderCreateRequest.userId,
                    orderCreateRequest.quantity,
                    orderCreateRequest.productId);
            return ResponseEntity.ok("Order created successfully");
        }
        catch(Exception e){
            return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
        }
    }

}
