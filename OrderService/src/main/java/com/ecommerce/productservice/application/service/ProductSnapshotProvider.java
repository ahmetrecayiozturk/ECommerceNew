package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.ProductDto;
import com.ecommerce.productservice.domain.snapshout.ProductSnapshot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductSnapshotProvider {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String myPRoducturl = "http://localhost:8080/api/v1/products/";

    public ProductSnapshot getProductSnapshot(Long productId){
        ProductDto productDto = restTemplate.getForObject(myPRoducturl + productId, ProductDto.class);
        ProductSnapshot productSnapshot = new ProductSnapshot(productDto.getId(), productDto.getName(), productDto.getPrice());
        return productSnapshot;
    }
}
