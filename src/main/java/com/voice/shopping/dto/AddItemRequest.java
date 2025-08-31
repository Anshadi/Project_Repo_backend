package com.voice.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {
    @NotNull(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Item name is required")
    private String item;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String category;
    private String unit;
    private String notes;
    private String priority = "medium";
    
    // Price information for analytics
    private Double price; // Price per unit
    private String brand; // Product brand
}
