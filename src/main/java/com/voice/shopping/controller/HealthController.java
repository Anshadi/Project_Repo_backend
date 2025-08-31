package com.voice.shopping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:*", "http://127.0.0.1:*" })
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Voice Shopping Assistant Backend is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
}
