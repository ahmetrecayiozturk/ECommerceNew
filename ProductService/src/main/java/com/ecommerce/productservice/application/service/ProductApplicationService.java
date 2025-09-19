package com.ecommerce.productservice.application.service;


import com.ecommerce.productservice.api.ProductCreateRequest;
import com.ecommerce.productservice.api.ProductUpdateRequest;
import com.ecommerce.productservice.domain.aggregate.Product;
import com.ecommerce.productservice.domain.model.Category;
import com.ecommerce.productservice.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductApplicationService {

    private final ProductRepository productRepository;

    public ProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void addProduct(ProductCreateRequest productCreateRequest){
        try {
            Product product = new Product();
            product.setProductName(productCreateRequest.getProductName());
            product.setDescription(productCreateRequest.getDescription());
            product.setPrice(productCreateRequest.getPrice());
            product.setCategory(Category.valueOf(productCreateRequest.getCategory()));
            product.setStockQuantity(productCreateRequest.getStockQuantity());
            product.setImageUrl(productCreateRequest.getImageUrl());
            productRepository.save(product);
            System.out.println("Product added successfully: " + product.getProductName());
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
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Product> getProductsByFilter(String category){
        try{
            List<Product> products = productRepository.findAll();
            if(category != null && !category.isEmpty()){
                products = products.stream()
                        .filter(product -> product.getCategory().name().equalsIgnoreCase(category))
                        .toList();
            }
            if(products.isEmpty()){
                throw new RuntimeException("No products found");
            }
            return products;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice){
        try{
            List<Product> products = productRepository.findAll();
            if(minPrice != null && maxPrice != null){
                products = products.stream()
                        .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
                        .toList();
            }
            if(products.isEmpty()){
                throw new RuntimeException("No products found");
            }
            return products;
        }
        catch(Exception e){
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

    public Product getProductById(Long id){
        try{
            Optional<Product> productFinded  = productRepository.findById(id);
            if(productFinded.isPresent()){
                return productFinded.get();
            }
            else{
                throw new RuntimeException("Product not found");
            }
        }
        catch(Exception e){
            System.err.println("Error while retrieving product by ID: " + e.getMessage());
            throw new RuntimeException("Product retrieval failed", e);
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
