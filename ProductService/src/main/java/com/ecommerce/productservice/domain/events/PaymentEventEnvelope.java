package com.ecommerce.productservice.domain.events;

import lombok.Data;

@Data
public class PaymentEventEnvelope {
    private Long id;
    private Long orderId;
    private Long userId;
    private String aggregateType;
    private String eventType;
    private String payload;
    private String timestamp;
    private Boolean success;
    private Boolean published;
    private String paymentStatus;
}