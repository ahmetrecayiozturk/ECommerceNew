package com.ecommerce.orderservice.api;

import lombok.Data;

@Data
public class OrderCreateRequest {
    Long userId;
    int quantity;
    Long productId;
}
