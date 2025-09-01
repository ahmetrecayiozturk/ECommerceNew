package com.ecommerce.orderservice.api;

import com.ecommerce.orderservice.application.service.OrderService;
import com.ecommerce.orderservice.domain.aggregate.Order;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final Environment environment;
    public OrderController(OrderService orderService, OutboxRepository outboxRepository, Environment environment) {
        this.orderService = orderService;

        this.environment = environment;
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
    @GetMapping("/get-all")
    public ResponseEntity<List<Order>> getOrders() {
        List<Order> order = orderService.getAllOrders();
        return ResponseEntity.ok(order);
    }
    @PostMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        String port = environment.getProperty("local.server.port");
        String msg = "Order Service is up and running on port: " + port;
        System.out.println(msg);
        return ResponseEntity.ok(msg);
    }

}
