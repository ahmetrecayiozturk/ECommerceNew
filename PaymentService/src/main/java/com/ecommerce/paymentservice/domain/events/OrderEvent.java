package com.ecommerce.paymentservice.domain.events;

import com.ecommerce.paymentservice.domain.model.ProductSnapshot;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class OrderEvent {
    private Long id;
    private Long orderId;
    private Long userId;
    private int quantity;
    private ProductSnapshot productSnapshot; // <-- Bunu ekle!

    public Long getOrderId() {
        return orderId != null ? orderId : id;
    }
    public Long getProductId() {
        // Hem productId doğrudan varsa onu, yoksa productSnapshot içinden al
        return productSnapshot != null ? productSnapshot.getProductId() : null;
    }
}