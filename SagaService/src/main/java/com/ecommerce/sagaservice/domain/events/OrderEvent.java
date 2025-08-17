package com.ecommerce.sagaservice.domain.events;

import com.ecommerce.sagaservice.domain.model.ProductSnapshot;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class OrderEvent {
    @JsonAlias({"id"})
    private Long orderId;
    private Long userId;
    private Long productId;
    private int quantity;
    private ProductSnapshot productSnapshot;

    public Long getProductId() {
        if (productId != null) return productId;
        if (productSnapshot != null && productSnapshot.getProductId() != null) return productSnapshot.getProductId();
        return null;
    }
}