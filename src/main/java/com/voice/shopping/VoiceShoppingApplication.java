package com.voice.shopping;

import com.voice.shopping.service.ProductService;
import com.voice.shopping.repository.ShoppingItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@SpringBootApplication
@EnableMongoRepositories
@RequiredArgsConstructor
public class VoiceShoppingApplication implements CommandLineRunner {

    private final ProductService productService;
    private final ShoppingItemRepository shoppingItemRepository;

    public static void main(String[] args) {
        SpringApplication.run(VoiceShoppingApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("https://spontaneous-sherbet-a72ea7.netlify.app")
                    .allowedMethods("*");
            }
        };
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Voice Shopping Assistant Backend Started Successfully!");
        log.info("📡 API Server running on http://localhost:8082");
        log.info("📋 API Documentation available at http://localhost:8082/api");

        // Clear all shopping lists to ensure fresh start
        long deletedItems = shoppingItemRepository.count();
        if (deletedItems > 0) {
            shoppingItemRepository.deleteAll();
            log.info("Cleared {} existing shopping list items for fresh start", deletedItems);
        }

        // Initialize product database for search functionality
        productService.initializeSampleProducts();

        log.info("Voice commands ready for processing!");
        log.info(" Shopping list operations ready!");
        log.info(" Product search functionality active!");
        log.info(" AI-powered recommendations enabled!");

        // Display API endpoints
        log.info("\n Available API Endpoints:");
        log.info("   POST /api/voice/process - Process voice commands");
        log.info("   GET  /api/list/{userId} - Get shopping list");
        log.info("   POST /api/list/add - Add item to list");
        log.info("   PUT  /api/list/update - Update item quantity");
        log.info("   DELETE /api/list/{userId}/{itemId} - Remove item");
        log.info("   GET  /api/recommendations/{userId} - Get recommendations");
        log.info("   GET  /api/search - Search products");
        log.info("   POST /api/search/initialize - Initialize sample data");
    }
}
