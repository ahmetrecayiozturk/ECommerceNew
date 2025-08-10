package com.ecommerce.productservice.domain.aggregate;

import com.ecommerce.productservice.domain.model.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "e_commerce_products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private String productName;
    private String description;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;
    @NotNull
    private double price;
    @NotNull
    private int stockQuantity;
    @ElementCollection
    private List<String> imageUrl;
    private ZonedDateTime createdAt;
    public Product() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public Product(String productName, String description, String category, Double price, int stockQuantity) {
        this.productName = productName;
        this.description = description;
        this.category = Category.valueOf(category);
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
}
