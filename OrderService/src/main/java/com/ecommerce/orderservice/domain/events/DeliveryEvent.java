package com.ecommerce.orderservice.domain.events;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DeliveryEvent {
    private Long orderId;
    private Long userId;
    private Long productId;
    private int quantity;
    private String status;
    private boolean success;
    private ZonedDateTime createdAt;
}
