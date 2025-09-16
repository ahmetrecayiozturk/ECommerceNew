package com.ecommerce.orderservice.application;

import com.ecommerce.orderservice.domain.dto.ProductDto;
import com.ecommerce.orderservice.domain.snapshout.ProductSnapshot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductSnapshotProvider {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String myProducturl = "http://localhost:8080/api/product/get-by-id/";

    public ProductSnapshot getProductSnapshot(Long productId){
        ProductDto productDto = restTemplate.getForObject(myProducturl + productId, ProductDto.class);
        ProductSnapshot productSnapshot = new ProductSnapshot(productDto.getId(), productDto.getName(), productDto.getPrice());
        return productSnapshot;
    }
}
