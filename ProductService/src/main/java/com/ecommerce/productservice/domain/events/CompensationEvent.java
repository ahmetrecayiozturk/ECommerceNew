package com.ecommerce.productservice.domain.events;

import lombok.Data;

@Data
public class CompensationEvent {
    private Long userId; 
    private Long orderId;
    private Long productId; 
    private int quantity; 
    private String failedStep;
    private String reason;}
