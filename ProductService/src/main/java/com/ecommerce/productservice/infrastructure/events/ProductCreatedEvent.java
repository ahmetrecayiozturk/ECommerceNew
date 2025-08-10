package com.ecommerce.productservice.infrastructure.events;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Data
public class ProductCreatedEvent {
    private Long productId;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
    private ZonedDateTime createdAt;
}
