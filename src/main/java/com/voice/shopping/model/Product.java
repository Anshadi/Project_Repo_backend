package com.voice.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    @TextIndexed(weight = 3) 
    private String name;

    @TextIndexed(weight = 2)
    private String brand;

    @Positive(message = "Price must be positive")
    private Double price;

    @Indexed
    private String category;

    @TextIndexed
    private String description;

    private String imageUrl;
    private Boolean inStock = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructor for quick product creation
    public Product(String name, String brand, Double price, String category, String description) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
