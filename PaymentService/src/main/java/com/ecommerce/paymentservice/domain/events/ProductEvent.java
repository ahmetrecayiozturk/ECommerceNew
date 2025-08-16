package com.ecommerce.paymentservice.domain.events;

import lombok.Data;

@Data
public class ProductEvent {
    private Long userId;
    private Long orderId;
    private Long productId;
    private int quantity;
    private boolean success;
}
