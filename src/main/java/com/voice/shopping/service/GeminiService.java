package com.voice.shopping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GeminiService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.max-tokens}")
    private Integer maxTokens;

    public GeminiService() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> processVoiceCommand(String query) {
        log.info("Attempting Gemini API call for voice command: {}", query);
        try {
            String prompt = buildVoiceProcessingPrompt(query);
            String response = callGeminiAPI(prompt);
            
            log.info("Gemini API response received: {}", response);
            return parseAIResponse(response);

        } catch (Exception e) {
            log.error("Gemini API call failed - using fallback parsing", e);
            log.error("Error details: {}", e.getMessage());
            return fallbackProcessing(query);
        }
    }

    public String generateRecommendations(String currentItems, String userHistory, String availableProducts) {
        try {
            String prompt = String.format(
                    "STRICT INSTRUCTIONS: You are a smart shopping assistant. You must ONLY recommend products from the provided database list.\n\n" +
                            "Current shopping list: %s\n" +
                            "User purchase history: %s\n" +
                            "Available products in database: [%s]\n\n" +
                            "RULES:\n" +
                            "1. ONLY suggest products that appear EXACTLY in the available products list above\n" +
                            "2. Copy product names EXACTLY as they appear (including capitalization)\n" +
                            "3. Suggest 4-5 diverse complementary items that go well with current list items\n" +
                            "4. CRITICAL: Do NOT suggest items already in the current shopping list\n" +
                            "5. Use smart food pairing and meal planning logic:\n" +
                            "   - Butter -> suggest Bread, Eggs, Jam, Honey\n" +
                            "   - Milk -> suggest Cereal, Cookies, Bread, Bananas\n" +
                            "   - Chicken -> suggest Rice, Broccoli, Onions, Garlic\n" +
                            "   - Pasta -> suggest Tomato Sauce, Cheese, Basil, Olive Oil\n" +
                            "   - Eggs -> suggest Bacon, Bread, Cheese, Spinach\n" +
                            "   - Beef -> suggest Potatoes, Carrots, Onions, Mushrooms\n" +
                            "6. Think about complete meals, breakfast combinations, cooking needs\n" +
                            "7. Prioritize variety - suggest items from different categories when possible\n" +
                            "8. Return only exact product names separated by commas\n" +
                            "9. If no suitable recommendations from database, return 'none'\n\n" +
                            "Examples:\n" +
                            "- Current list: 'Butter', Available: 'Bread, Eggs, Jam' -> suggest: Bread, Eggs, Jam\n" +
                            "- Current list: 'Chicken Breast', Available: 'Rice, Broccoli, Garlic' -> suggest: Rice, Broccoli, Garlic\n" +
                            "- Current list: 'Pasta', Available: 'Tomato Sauce, Parmesan Cheese' -> suggest: Tomato Sauce, Parmesan Cheese\n\n" +
                            "Response (exact product names only):",
                    currentItems, userHistory, availableProducts);

            log.info("Calling Gemini for recommendations - Current items: {}, History: {}, Available: {}", currentItems, userHistory, availableProducts);
            String response = callGeminiAPI(prompt);
            log.info("Gemini response: '{}'", response);
            
            return response != null ? response.trim() : "";

        } catch (Exception e) {
            log.error("Error generating recommendations with Gemini: {}", e.getMessage());
            log.error("Gemini API call failed - check API key and quota", e);
            return ""; // Empty fallback - no hardcoded items
        }
    }

    public String generateProductSuggestions(String itemName, String availableProducts) {
        try {
            String prompt = String.format(
                    "STRICT INSTRUCTIONS: You must ONLY return exact product names from the provided list.\n\n" +
                            "User requested: '%s'\n" +
                            "Available products in database: [%s]\n\n" +
                            "RULES:\n" +
                            "1. ONLY suggest products that appear EXACTLY in the available products list above\n" +
                            "2. Copy the product names EXACTLY as they appear (including capitalization)\n" +
                            "3. If '%s' relates to any products in the list, suggest 1-2 most relevant ones\n" +
                            "4. If '%s' is not a grocery item, return 'none'\n" +
                            "5. Separate multiple suggestions with commas\n\n" +
                            "Examples:\n" +
                            "- If user wants 'milk' and list contains 'Whole Milk', return: Whole Milk\n" +
                            "- If user wants 'bread' and list contains 'Whole Wheat Bread', return: Whole Wheat Bread\n" +
                            "- If user wants 'cat', return: none\n\n" +
                            "Response (exact product names only):",
                    itemName, availableProducts, itemName, itemName);

            return callGeminiAPI(prompt);

        } catch (Exception e) {
            log.error("Error generating product suggestions: {}", e.getMessage());
            log.error("Gemini API call failed - check API key and quota", e);
            return "none"; // Fallback
        }
    }

    private String callGeminiAPI(String prompt) throws IOException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", new Object[]{parts});
        requestBody.put("contents", new Object[]{contents});

        // Add generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("temperature", 0.7); // Higher temperature for more creative suggestions
        generationConfig.put("topP", 0.8);
        requestBody.put("generationConfig", generationConfig);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("Gemini API request body: {}", jsonBody);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Gemini API call failed with status: {}", response.code());
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            log.debug("Gemini API raw response: {}", responseBody);

            // Parse Gemini response
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonResponse.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode contentParts = content.get("parts");
                    if (contentParts != null && contentParts.isArray() && contentParts.size() > 0) {
                        JsonNode text = contentParts.get(0).get("text");
                        if (text != null) {
                            return text.asText().trim();
                        }
                    }
                }
            }
            
            log.warn("Could not parse Gemini response properly");
            return "";
        }
    }

    private String getSystemPrompt() {
        return "You are a shopping assistant. Parse voice commands and respond with ONLY valid JSON. " +
                "Format: {\"intent\":\"add\", \"item\":\"milk\", \"quantity\":2, \"unit\":\"bottles\", \"category\":\"Dairy\"} " +
                "Rules: " +
                "1. Extract the actual food/product name, not containers " +
                "2. 'add 2 bottles of milk' -> item='milk', quantity=2, unit='bottles' " +
                "3. 'add bread' -> item='bread', quantity=1, unit='items' " +
                "4. 'remove milk' -> intent='remove', item='milk' " +
                "5. Categories: Dairy, Meat, Vegetables, Fruits, Bakery, Beverages, Snacks, Other " +
                "6. Return ONLY the JSON object, no markdown, no explanation.";
    }

    private String buildVoiceProcessingPrompt(String query) {
        return getSystemPrompt() + "\n\nProcess this shopping command: \"" + query + "\"";
    }

    private Map<String, Object> parseAIResponse(String response) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Clean up response - remove markdown formatting
            response = response.trim();
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            
            if (response.startsWith("{") && response.endsWith("}")) {
                // Parse basic JSON manually for reliability
                result.put("intent", extractJsonValue(response, "intent"));
                result.put("item", extractJsonValue(response, "item"));
                result.put("quantity", Integer.parseInt(extractJsonValue(response, "quantity", "1")));
                result.put("unit", extractJsonValue(response, "unit", "item"));
                result.put("category", extractJsonValue(response, "category", "Other"));
            } else {
                log.warn("Response doesn't look like JSON, using fallback: {}", response);
                return fallbackProcessing(response);
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return fallbackProcessing(response);
        }

        return result;
    }

    private String extractJsonValue(String json, String key) {
        return extractJsonValue(json, key, "");
    }

    private String extractJsonValue(String json, String key, String defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"|\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return defaultValue;
    }

    private Map<String, Object> fallbackProcessing(String query) {
        Map<String, Object> result = new HashMap<>();
        query = query.toLowerCase();

        // Determine intent
        if (query.contains("add") || query.contains("put") || query.contains("need")) {
            result.put("intent", "add");
        } else if (query.contains("remove") || query.contains("delete") || query.contains("take off")) {
            result.put("intent", "remove");
        } else if (query.contains("change") || query.contains("update") || query.contains("modify")) {
            result.put("intent", "update");
        } else if (query.contains("find") || query.contains("search") || query.contains("show")) {
            result.put("intent", "search");
        } else {
            result.put("intent", "add"); // Default to add
        }

        // Extract quantity
        Pattern quantityPattern = Pattern.compile("(\\d+)");
        Matcher quantityMatcher = quantityPattern.matcher(query);
        result.put("quantity", quantityMatcher.find() ? Integer.parseInt(quantityMatcher.group(1)) : 1);

        // Extract item name with improved logic
        String[] words = query.split("\\s+");
        String extractedItem = null;
        
        // Look for pattern "X of Y" where Y is the actual item
        for (int i = 0; i < words.length - 2; i++) {
            if (words[i + 1].equals("of")) {
                extractedItem = words[i + 2];
                break;
            }
        }
        
        // If no "of" pattern, look for item after quantity
        if (extractedItem == null) {
            for (int i = 0; i < words.length; i++) {
                if (words[i].matches("\\d+") && i + 1 < words.length) {
                    // Skip container words like bottles, cans, boxes
                    String candidate = words[i + 1];
                    if (!java.util.Arrays.asList("bottles", "cans", "boxes", "jars", "bags", "packs").contains(candidate)) {
                        extractedItem = candidate;
                        break;
                    } else if (i + 3 < words.length && words[i + 2].equals("of")) {
                        // Handle "2 bottles of milk"
                        extractedItem = words[i + 3];
                        break;
                    }
                }
            }
        }

        // Fallback: take last meaningful word
        if (extractedItem == null) {
            for (int i = words.length - 1; i >= 0; i--) {
                if (!java.util.Arrays.asList("add", "remove", "delete", "put", "my", "list", "to", "from", "bottles", "cans", "boxes").contains(words[i])) {
                    extractedItem = words[i];
                    break;
                }
            }
        }
        
        result.put("item", extractedItem != null ? extractedItem : "unknown");
        result.put("unit", "item");
        result.put("category", "Other");

        return result;
    }
}
