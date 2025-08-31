package com.voice.shopping.service;

import com.voice.shopping.model.Product;
import com.voice.shopping.model.PurchaseHistory;
import com.voice.shopping.model.ShoppingItem;
import com.voice.shopping.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final ShoppingListService shoppingListService;
    private final GeminiService geminiService;
    private final ProductService productService;

    @Value("${app.recommendations.max-items}")
    private Integer maxRecommendations;

    public Map<String, Object> getRecommendations(String userId) {
        log.debug("Generating recommendations for user: {}", userId);

        try {
            
            List<ShoppingItem> currentItems = shoppingListService.getShoppingList(userId);
            String currentItemsStr = currentItems.stream()
                    .map(ShoppingItem::getName)
                    .collect(Collectors.joining(", "));

            // Get user purchase history (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<PurchaseHistory> recentHistory = purchaseHistoryRepository
                    .findByUserIdAndPurchaseDateAfter(userId, thirtyDaysAgo);

            String historyStr = recentHistory.stream()
                    .map(PurchaseHistory::getItemName)
                    .distinct()
                    .limit(10)
                    .collect(Collectors.joining(", "));

            // Generate AI-powered recommendations
            List<String> suggestions = new ArrayList<>();

            if (!currentItemsStr.isEmpty() || !historyStr.isEmpty()) {
                // Get all available products from database
                List<Product> allProducts = productService.getAllProducts();
                String availableProducts = allProducts.stream()
                    .map(Product::getName)
                    .collect(Collectors.joining(", "));
                
                log.info("Calling Gemini for recommendations - Current items: {}, History: {}, Available products: {}", currentItemsStr, historyStr, availableProducts);
                String aiRecommendations = geminiService.generateRecommendations(currentItemsStr, historyStr, availableProducts);
                log.info("Gemini response: '{}'", aiRecommendations);
                
                if (aiRecommendations != null && !aiRecommendations.trim().isEmpty()) {
                    // Get current item names for filtering
                    Set<String> currentItemNames = currentItems.stream()
                            .map(item -> item.getName().toLowerCase())
                            .collect(Collectors.toSet());
                    
                    suggestions = Arrays.stream(aiRecommendations.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .filter(s -> !currentItemNames.contains(s.toLowerCase())) // Filter out current items
                            .distinct() // Remove duplicates
                            .limit(maxRecommendations)
                            .collect(Collectors.toList());
                    log.info("Parsed suggestions (after filtering): {}", suggestions);
                } else {
                    log.warn("Gemini returned empty/null response");
                }
            }

            // Fallback to frequency-based recommendations if AI fails
            if (suggestions.isEmpty()) {
                suggestions = getFrequencyBasedRecommendations(userId);
                
                // Filter fallback suggestions too
                Set<String> currentItemNames = currentItems.stream()
                        .map(item -> item.getName().toLowerCase())
                        .collect(Collectors.toSet());
                
                suggestions = suggestions.stream()
                        .filter(item -> !currentItemNames.contains(item.toLowerCase()))
                        .distinct()
                        .collect(Collectors.toList());
            }


            Map<String, Object> response = new HashMap<>();
            response.put("suggestions", suggestions);
            response.put("based_on", !currentItemsStr.isEmpty() ? "current_list" : "history");

            return response;

        } catch (Exception e) {
            log.error("Error generating recommendations: {}", e.getMessage());
            return getDefaultRecommendations();
        }
    }

    private List<String> getFrequencyBasedRecommendations(String userId) {
        // Get frequently bought items
        List<PurchaseHistory> allHistory = purchaseHistoryRepository.findFrequentItemsByUserId(userId);

        // Count frequency of each item
        Map<String, Long> itemFrequency = allHistory.stream()
                .collect(Collectors.groupingBy(
                        PurchaseHistory::getItemName,
                        Collectors.counting()));

        // Return top frequent items
        return itemFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(maxRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Object> getDefaultRecommendations() {
        Map<String, Object> response = new HashMap<>();
        response.put("suggestions", Arrays.asList()); // Empty list - no hardcoded items
        response.put("based_on", "no_data");
        return response;
    }

    public void recordPurchase(String userId, ShoppingItem item) {
        try {
            PurchaseHistory history = new PurchaseHistory(
                    userId,
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory(),
                    item.getUnit(),
                    item.getPrice(), // Use actual item price
                    item.getBrand(), // Use actual item brand
                    "Online Store" // Set store consistently
            );

            purchaseHistoryRepository.save(history);
            log.debug("Recorded purchase history for user {} - item: {} with price: {}", userId, item.getName(), item.getPrice());

        } catch (Exception e) {
            log.error("Error recording purchase history: {}", e.getMessage());
        }
    }
}
