package com.ecommerce.sagaservice.domain.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class OrderEvent {
    @JsonAlias({"id"})
    private Long orderId;
    private Long userId;
    private Long productId;
    private int quantity;
}