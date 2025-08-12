package com.ecommerce.productservice.api;

import com.ecommerce.productservice.application.dto.ProductDto;
import com.ecommerce.productservice.application.service.OrderService;
import com.ecommerce.productservice.domain.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
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
