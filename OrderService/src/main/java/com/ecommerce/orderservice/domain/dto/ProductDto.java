package com.ecommerce.orderservice.domain.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String productName;
    private double price;
}