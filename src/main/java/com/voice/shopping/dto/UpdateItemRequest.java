package com.voice.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull(message = "Item ID is required")
    private String itemId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    private String notes;
    private String priority;
    private Boolean completed;
}
