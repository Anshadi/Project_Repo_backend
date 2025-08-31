package com.voice.shopping.controller;

import com.voice.shopping.dto.ApiResponse;
import com.voice.shopping.model.Product;
import com.voice.shopping.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:*", "http://127.0.0.1:*" })
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double maxPrice) {

        log.info("Searching products - Query: {}, Brand: {}, MaxPrice: {}", query, brand, maxPrice);

        try {
            List<Product> products = productService.searchProducts(query, brand, maxPrice);
            log.info("Found {} products", products.size());
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("Fetching products by category: {}", category);

        try {
            List<Product> products = productService.searchProductsByCategory(category);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("Error fetching products by category: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<Product>> getProductSuggestions(@RequestParam String item) {
        log.info("Getting product suggestions for: {}", item);

        try {
            List<Product> suggestions = productService.getProductSuggestions(item);
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            log.error("Error getting product suggestions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all products");

        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("Error fetching all products: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<String>> initializeSampleData() {
        log.info("Initializing sample product data");

        try {
            // Sample product initialization disabled - no hardcoded data
            return ResponseEntity.ok(ApiResponse.success("Sample product initialization disabled"));

        } catch (Exception e) {
            log.error("Error initializing sample data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to initialize sample data"));
        }
    }
}
