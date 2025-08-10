package com.ecommerce.productservice.application;


import com.ecommerce.productservice.api.ProductCreateRequest;
import com.ecommerce.productservice.api.ProductUpdateRequest;
import com.ecommerce.productservice.domain.aggregate.Product;
import com.ecommerce.productservice.domain.model.Category;
import com.ecommerce.productservice.infrastructure.events.ProductCreatedEvent;
import com.ecommerce.productservice.infrastructure.repository.ProductRepository;
import com.ecommerce.productservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProductApplicationService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ProductApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, ProductRepository productRepository, PasswordEncoder passwordEncoder,JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void addProduct(ProductCreateRequest productCreateRequest) throws IOException {
        try {
            Product product = new Product();
            product.setProductName(productCreateRequest.getProductName());
            product.setDescription(productCreateRequest.getDescription());
            product.setPrice(productCreateRequest.getPrice());
            product.setCategory(Category.valueOf(productCreateRequest.getCategory()));
            product.setStockQuantity(productCreateRequest.getStockQuantity());
            product.setImageUrl(productCreateRequest.getImageUrl());
            productRepository.save(product);
            ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
            productCreatedEvent.setProductId(product.getId());
            productCreatedEvent.setProductName(product.getProductName());
            productCreatedEvent.setDescription(product.getDescription());
            productCreatedEvent.setPrice(product.getPrice());
            productCreatedEvent.setCategory(product.getCategory().name());
            productCreatedEvent.setStockQuantity(product.getStockQuantity());
            String payload = objectMapper.writeValueAsString(productCreatedEvent);
            kafkaTemplate.send("product-created-topic", payload);
            System.out.println("Product added and event sent to Kafka: " + payload);
        }
        catch (Exception e) {
            System.err.println("Error while adding product: " + e.getMessage());
            throw new RuntimeException("Product adding failed", e);
        }
    }
    @Transactional
    public void updateProduct(ProductUpdateRequest productUpdateRequest) {
        try{
            Product product = productRepository.findById(productUpdateRequest.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setProductName(productUpdateRequest.getProductName());
            product.setDescription(productUpdateRequest.getDescription());
            product.setPrice(productUpdateRequest.getPrice());
            product.setCategory(Category.valueOf(productUpdateRequest.getCategory()));
            product.setStockQuantity(productUpdateRequest.getStockQuantity());
            product.setImageUrl(productUpdateRequest.getImageUrl());
            productRepository.save(product);
            ProductCreatedEvent productUpdatedEvent = new ProductCreatedEvent();
            productUpdatedEvent.setProductId(product.getId());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Product> getAllProduct(){
        try{
            List<Product> products = productRepository.findAll();
            if(products.isEmpty()){
                throw new RuntimeException("No products found");
            }
            return products;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }





    public Object getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error while retrieving products: " + e.getMessage());
            throw new RuntimeException("Product retrieval failed", e);
        }
    }

}
