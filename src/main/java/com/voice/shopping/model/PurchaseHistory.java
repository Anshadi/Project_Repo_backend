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
@Document(collection = "purchase_history")
public class PurchaseHistory {
    @Id
    private String id;

    @Indexed
    private String userId;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String category;
    private String unit;
    private Double price;
    private String brand;
    private String store;

    @Indexed
    private LocalDateTime purchaseDate = LocalDateTime.now();

    // Constructor for quick history creation
    public PurchaseHistory(String userId, String itemName, Integer quantity, String category) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.category = category;
        this.purchaseDate = LocalDateTime.now();
    }

    // Constructor with full details including store
    public PurchaseHistory(String userId, String itemName, Integer quantity, String category,
            String unit, Double price, String store) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.store = store;
        this.purchaseDate = LocalDateTime.now();
    }

    // Constructor with full details including brand
    public PurchaseHistory(String userId, String itemName, Integer quantity, String category,
            String unit, Double price, String brand, String store) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.brand = brand;
        this.store = store;
        this.purchaseDate = LocalDateTime.now();
    }
}
