package com.voice.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCommandRequest {
    @NotNull(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Query is required")
    private String query;

    // Optional context for better processing
    private String context;
}
