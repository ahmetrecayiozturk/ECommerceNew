package com.ecommerce.productservice.domain.model;

import lombok.Data;

@Data
public class ProductSnapshot {
    private Long productId;
    private String name;
    private Double price;
}