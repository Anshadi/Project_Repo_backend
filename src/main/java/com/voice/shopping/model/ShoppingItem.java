package com.voice.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shopping_items")
public class ShoppingItem {
    @Id
    private String id;

    @Indexed
    private String userId;

    @NotBlank(message = "Item name is required")
    private String name;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String category;
    private String unit = "pcs";
    
    // Price information for analytics
    private Double price; // Price per unit
    private String brand; // Product brand
    
  
    private String notes; 
    private String priority = "medium"; 
    private boolean completed = false; 

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    
    public ShoppingItem(String userId, String name, Integer quantity, String category) {
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with price and brand for analytics
    public ShoppingItem(String userId, String name, Integer quantity, String category, String unit, Double price, String brand) {
        this(userId, name, quantity, category, unit);
        this.price = price;
        this.brand = brand;
    }

    
    public ShoppingItem(String userId, String name, Integer quantity, String category, String unit) {
        this(userId, name, quantity, category);
        this.unit = unit;
    }
}
