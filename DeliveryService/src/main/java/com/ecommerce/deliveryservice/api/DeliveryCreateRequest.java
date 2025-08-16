package com.ecommerce.deliveryservice.api;

import lombok.Data;

@Data
public class DeliveryCreateRequest {
    private Long orderId;
    private Long userId;
    private String status;
    private boolean success;
}
