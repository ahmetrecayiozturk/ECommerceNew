package com.ecommerce.productservice.api;

import com.ecommerce.productservice.application.service.ProductApplicationService;
import com.ecommerce.productservice.domain.aggregate.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody ProductCreateRequest productCreateRequest) throws IOException {
            productApplicationService.addProduct(productCreateRequest);
            return ResponseEntity.ok("Product added successfully");
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody ProductUpdateRequest productUpdateRequest) throws IOException {
        productApplicationService.updateProduct(productUpdateRequest);
        return ResponseEntity.ok("Product updated successfully");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok((List<Product>) productApplicationService.getAllProducts());
    }
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productApplicationService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
