package com.voice.shopping.controller;

import com.voice.shopping.dto.VoiceCommandRequest;
import com.voice.shopping.dto.VoiceCommandResponse;
import com.voice.shopping.service.VoiceProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@CrossOrigin(origins = { "https://project-repo-0.onrender.com", "http://localhost:*", "http://127.0.0.1:*" })
public class VoiceController {

    private final VoiceProcessingService voiceProcessingService;

    @PostMapping("/process")
    public ResponseEntity<VoiceCommandResponse> processVoiceCommand(
            @Valid @RequestBody VoiceCommandRequest request) {

        log.info("Processing voice command for user {}: {}", request.getUserId(), request.getQuery());

        try {
            VoiceCommandResponse response = voiceProcessingService.processVoiceCommand(
                    request.getUserId(),
                    request.getQuery());

            log.info("Voice command processed successfully: {}", response.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing voice command: {}", e.getMessage());
            VoiceCommandResponse errorResponse = new VoiceCommandResponse(
                    "error",
                    "Sorry, there was an error processing your command. Please try again.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
