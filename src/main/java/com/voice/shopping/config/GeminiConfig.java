package com.voice.shopping.config;

import com.voice.shopping.service.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Bean
    public GeminiService geminiService() {
        try {
            // Try to get the API key from environment variables first
            String envApiKey = System.getenv("GEMINI_API_KEY");
            if (envApiKey != null && !envApiKey.isEmpty()) {
                apiKey = envApiKey;
            }
            
            // Try to get from system properties if still not found
            if (apiKey == null || apiKey.isEmpty()) {
                String propApiKey = System.getProperty("GEMINI_API_KEY");
                if (propApiKey != null && !propApiKey.isEmpty()) {
                    apiKey = propApiKey;
                }
            }
            
            System.out.println("DEBUG: Initializing Gemini service with key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "NULL"));
            
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-gemini-api-key-here")) {
                System.out.println("WARNING: Gemini API key not configured - using fallback mode");
                return null; // Return null to enable fallback mode
            }
            
            // Log API key status (first 10 chars only for security)
            String maskedKey = apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "***";
            System.out.println("Gemini API Key loaded: " + maskedKey);
            
            GeminiService service = new GeminiService();
            System.out.println("SUCCESS: Gemini service initialized successfully");
            return service;
        } catch (Exception e) {
            System.out.println("ERROR: Failed to initialize Gemini service - " + e.getMessage());
            e.printStackTrace();
            return null; // Return null to enable fallback mode
        }
    }
}
