package com.ecommerce.orderservice.domain.events;

import lombok.Data;

@Data
public class ProductEvent {
    private Long orderId;
    private Long productId;
    private int quantity;
    private boolean success;
}
