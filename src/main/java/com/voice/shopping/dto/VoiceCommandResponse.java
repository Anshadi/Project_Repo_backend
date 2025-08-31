package com.voice.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCommandResponse {
    private String status; // "added", "removed", "updated", "searched", "error"
    private String item; // Item name that was processed
    private Integer quantity; // Quantity processed
    private String message; // Human-readable response message
    private String action; // Specific action taken
    private Object data; // Additional data (e.g., search results)

    // Constructor for simple responses
    public VoiceCommandResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Constructor for item operations
    public VoiceCommandResponse(String status, String item, Integer quantity, String message) {
        this.status = status;
        this.item = item;
        this.quantity = quantity;
        this.message = message;
    }

    // Constructor with action
    public VoiceCommandResponse(String status, String action, String item, Integer quantity, String message) {
        this.status = status;
        this.action = action;
        this.item = item;
        this.quantity = quantity;
        this.message = message;
    }
}
