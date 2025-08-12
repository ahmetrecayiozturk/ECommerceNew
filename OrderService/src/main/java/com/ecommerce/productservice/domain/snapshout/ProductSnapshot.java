package com.ecommerce.productservice.domain.snapshout;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProductSnapshot {
    private Long productId;
    private String name;
    private Double price;

    public ProductSnapshot(Long productId, String name, Double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }
    public ProductSnapshot() {}
}
