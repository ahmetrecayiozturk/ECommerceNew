package com.ecommerce.orderservice.domain.events;

import lombok.Data;

@Data
public class DeliveryEvent {
    private Long orderId;
    private Long userId;
    private Long productId;
    private int quantity;
    private boolean success;
}
