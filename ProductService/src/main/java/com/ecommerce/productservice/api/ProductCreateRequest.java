package com.ecommerce.productservice.api;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ProductCreateRequest {
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
    private List<String> imageUrl = Collections.singletonList("https://example.com/default-image.jpg");
}
