package com.ecommerce.userservice.domain.events;

import lombok.Data;

@Data
public class CompensationEvent {
    private Long userId; // Optional, if specific user compensation is needed
    private Long orderId;
    private Long productId; // Optional, if specific product compensation is needed
    private int quantity; // Optional, if specific quantity compensation is needed
    private String failedStep; // "PAYMENT", "PRODUCT", "DELIVERY" vs.
    private String reason;}
