package com.voice.shopping.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class EnvironmentConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            // Load .env file from the current directory
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Set environment variables for the current process
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Only set if not already set by system environment
                if (System.getenv(key) == null) {
                    System.setProperty(key, value);
                    System.out.println("Loaded environment variable: " + key);
                }
            });
            
            System.out.println("✅ Environment variables loaded successfully from .env file");
            
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not load .env file: " + e.getMessage());
            System.err.println("Make sure your .env file exists in the backend directory");
        }
    }
}
