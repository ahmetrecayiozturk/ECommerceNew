package com.ecommerce.deliveryservice.api;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DeliveryUpdateRequest {
    private long paymentId;
    private Long orderId;
    private Long userId;
    private String status;
    private boolean success;
    private ZonedDateTime createdAt;
}
