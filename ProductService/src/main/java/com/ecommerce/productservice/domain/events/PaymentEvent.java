package com.ecommerce.productservice.domain.events;

import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public class PaymentEvent {
    private Long orderId;
    private Long userId;
    private Long productId;
    private String status;
    private int quantity;
    private boolean success;
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
}
