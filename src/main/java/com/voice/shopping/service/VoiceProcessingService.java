package com.voice.shopping.service;

import com.voice.shopping.dto.VoiceCommandResponse;
import com.voice.shopping.model.ShoppingItem;
import com.voice.shopping.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceProcessingService {

    private final GeminiService geminiService;
    private final ShoppingListService shoppingListService;
    private final ProductService productService;

    public VoiceCommandResponse processVoiceCommand(String userId, String query) {
        log.info("Processing voice command for user {}: {}", userId, query);

        try {
            // Use Gemini API for intelligent parsing
            Map<String, Object> parsedCommand = geminiService.processVoiceCommand(query);

            String intent = (String) parsedCommand.get("intent");
            String itemName = (String) parsedCommand.get("item");
            Integer quantity = (Integer) parsedCommand.get("quantity");
            String category = (String) parsedCommand.get("category");
            String unit = (String) parsedCommand.get("unit");

            log.info("Gemini parsed command - Intent: {}, Item: {}, Quantity: {}", intent, itemName, quantity);

            return executeIntent(userId, intent, itemName, quantity, category, unit);

        } catch (Exception e) {
            log.error("Error processing voice command with Gemini: {}", e.getMessage());
            // Fallback to local parsing only if Gemini fails
            try {
                Map<String, Object> parsedCommand = parseCommandLocally(query);
                String intent = (String) parsedCommand.get("intent");
                String itemName = (String) parsedCommand.get("item");
                Integer quantity = (Integer) parsedCommand.get("quantity");
                String category = (String) parsedCommand.get("category");
                String unit = (String) parsedCommand.get("unit");
                
                log.warn("Using local parsing fallback - Intent: {}, Item: {}, Quantity: {}", intent, itemName, quantity);
                return executeIntent(userId, intent, itemName, quantity, category, unit);
            } catch (Exception fallbackError) {
                log.error("Both Gemini and local parsing failed: {}", fallbackError.getMessage());
                return new VoiceCommandResponse("error", "Sorry, I couldn't understand that command. Please try again.");
            }
        }
    }

    private Map<String, Object> parseCommandLocally(String query) {
        Map<String, Object> result = new HashMap<>();
        String lowerQuery = query.toLowerCase().trim();
        
        // Default values
        result.put("intent", "add");
        result.put("quantity", 1);
        result.put("category", "Other");
        result.put("unit", "item");
        
        // Extract quantity
        Pattern quantityPattern = Pattern.compile("(\\d+)\\s+");
        Matcher quantityMatcher = quantityPattern.matcher(lowerQuery);
        if (quantityMatcher.find()) {
            result.put("quantity", Integer.parseInt(quantityMatcher.group(1)));
        }
        
        // Extract item name - everything after "add" and quantity
        String itemName = lowerQuery;
        itemName = itemName.replaceAll("add\\s+", "");
        itemName = itemName.replaceAll("\\d+\\s+", "");
        itemName = itemName.replaceAll("to\\s+my\\s+list", "");
        itemName = itemName.replaceAll("to\\s+my\\s+shopping\\s+list", "");
        itemName = itemName.replaceAll("bottles?\\s+of\\s+", "");
        itemName = itemName.replaceAll("pieces?\\s+of\\s+", "");
        itemName = itemName.trim();
        
        result.put("item", itemName);
        
        log.info("Local parsing result: {}", result);
        return result;
    }

    private VoiceCommandResponse executeIntent(String userId, String intent, String itemName,
            Integer quantity, String category, String unit) {
        switch (intent.toLowerCase()) {
            case "add":
                return handleAddIntent(userId, itemName, quantity, category, unit);

            case "remove":
                return handleRemoveIntent(userId, itemName);

            case "update":
                return handleUpdateIntent(userId, itemName, quantity);

            case "search":
                return handleSearchIntent(itemName);

            case "list":
                return handleListIntent(userId);

            default:
                return new VoiceCommandResponse("error",
                        "I'm not sure what you want me to do. Try saying 'add', 'remove', 'search', or 'list'.");
        }
    }

    private VoiceCommandResponse handleAddIntent(String userId, String itemName, Integer quantity,
            String category, String unit) {
        try {
            // Validate item name first
            if (itemName == null || itemName.trim().isEmpty() || "unknown".equalsIgnoreCase(itemName)) {
                return new VoiceCommandResponse("error", "I couldn't identify a valid item to add. Please try again with a specific food or grocery item.");
            }
            
            // First, try to find a matching product to get price information
            List<Product> products = productService.getProductSuggestions(itemName);
            
            ShoppingItem item;
            String message;
            
            if (!products.isEmpty()) {
                // Found matching product - use it to preserve price info
                Product matchingProduct = products.get(0); // Use best match
                item = shoppingListService.addItem(userId, matchingProduct.getName(), quantity, 
                    matchingProduct.getCategory(), unit != null ? unit : "item", 
                    null, "medium", matchingProduct.getPrice(), matchingProduct.getBrand());
                
                message = String.format("Added %d %s %s to your list (Price: $%.2f each)",
                        quantity, unit != null ? unit : "item" + (quantity > 1 ? "s" : ""), 
                        matchingProduct.getName(), matchingProduct.getPrice());
                        
                log.info("Voice command added item with price: {} - ${}", matchingProduct.getName(), matchingProduct.getPrice());
            } else {
                // No matching product found - get AI suggestions from actual database products
                List<Product> allProducts = productService.getAllProducts();
                String availableProducts = allProducts.stream()
                    .map(Product::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
                
                String aiSuggestions = geminiService.generateProductSuggestions(itemName, availableProducts);
                
                if (aiSuggestions != null && !aiSuggestions.trim().isEmpty() && !"none".equalsIgnoreCase(aiSuggestions)) {
                    // Parse AI suggestions and get actual Product objects
                    List<String> suggestedNames = java.util.Arrays.stream(aiSuggestions.split(","))
                        .map(String::trim)
                        .collect(java.util.stream.Collectors.toList());
                    
                    log.info("AI suggested product names: {}", suggestedNames);
                    
                    List<Product> suggestedProducts = productService.getProductsByNames(suggestedNames);
                    log.info("Found matching products: {}", suggestedProducts.stream().map(Product::getName).collect(java.util.stream.Collectors.toList()));
                    
                    if (!suggestedProducts.isEmpty()) {
                        // Use the first suggested product and add it directly with price
                        Product suggestedProduct = suggestedProducts.get(0);
                        item = shoppingListService.addItem(userId, suggestedProduct.getName(), quantity, 
                            suggestedProduct.getCategory(), unit != null ? unit : "item", 
                            null, "medium", suggestedProduct.getPrice(), suggestedProduct.getBrand());
                        
                        message = String.format("I found '%s' for you and added %d %s to your list (Price: $%.2f each). Other suggestions: %s",
                                suggestedProduct.getName(), quantity, 
                                unit != null ? unit : "item" + (quantity > 1 ? "s" : ""), 
                                suggestedProduct.getPrice(),
                                suggestedProducts.stream().skip(1).map(Product::getName).collect(java.util.stream.Collectors.joining(", ")));
                                
                        log.info("Voice command added suggested product with price: {} - ${}", suggestedProduct.getName(), suggestedProduct.getPrice());
                    } else {
                        log.warn("AI suggestions '{}' did not match any database products", aiSuggestions);
                        return new VoiceCommandResponse("suggestion", 
                            String.format("I couldn't find '%s' in our products. AI suggested: %s, but these don't match our inventory. Please try a different item.", 
                            itemName, aiSuggestions));
                    }
                } else {
                    // Don't add items that don't exist in database
                    log.warn("Item '{}' not found in database and AI couldn't suggest alternatives", itemName);
                    return new VoiceCommandResponse("error", 
                        String.format("I couldn't find '%s' in our product database. Please try a different item or add it to the product catalog first.", itemName));
                }
            }

            return new VoiceCommandResponse("added", "add", item.getName(), quantity, message);

        } catch (Exception e) {
            log.error("Error adding item: {}", e.getMessage());
            return new VoiceCommandResponse("error", "Sorry, I couldn't add that item to your list.");
        }
    }

    private VoiceCommandResponse handleRemoveIntent(String userId, String itemName) {
        try {
            Optional<ShoppingItem> itemOpt = shoppingListService.findItemByName(userId, itemName);

            if (itemOpt.isPresent()) {
                ShoppingItem item = itemOpt.get();
                boolean removed = shoppingListService.removeItem(userId, item.getId());

                if (removed) {
                    String message = String.format("Removed %s from your list", itemName);
                    return new VoiceCommandResponse("removed", "remove", itemName, null, message);
                }
            }

            String message = String.format("I couldn't find %s in your shopping list", itemName);
            return new VoiceCommandResponse("not_found", message);

        } catch (Exception e) {
            log.error("Error removing item: {}", e.getMessage());
            return new VoiceCommandResponse("error", "Sorry, I couldn't remove that item from your list.");
        }
    }

    private VoiceCommandResponse handleUpdateIntent(String userId, String itemName, Integer quantity) {
        try {
            Optional<ShoppingItem> itemOpt = shoppingListService.findItemByName(userId, itemName);

            if (itemOpt.isPresent()) {
                ShoppingItem item = itemOpt.get();
                shoppingListService.updateItemQuantity(userId, item.getId(), quantity);

                String message = String.format("Updated %s quantity to %d", itemName, quantity);
                return new VoiceCommandResponse("updated", "update", itemName, quantity, message);
            }

            String message = String.format("I couldn't find %s in your shopping list to update", itemName);
            return new VoiceCommandResponse("not_found", message);

        } catch (Exception e) {
            log.error("Error updating item: {}", e.getMessage());
            return new VoiceCommandResponse("error", "Sorry, I couldn't update that item.");
        }
    }

    private VoiceCommandResponse handleSearchIntent(String query) {
        try {
            List<Product> products = productService.searchProducts(query, null, null);

            String message = String.format("Found %d products for '%s'", products.size(), query);
            VoiceCommandResponse response = new VoiceCommandResponse("searched", message);
            response.setData(products);
            response.setAction("search");

            return response;

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage());
            return new VoiceCommandResponse("error", "Sorry, I couldn't search for products right now.");
        }
    }

    private VoiceCommandResponse handleListIntent(String userId) {
        try {
            List<ShoppingItem> items = shoppingListService.getShoppingList(userId);
            long itemCount = shoppingListService.getItemCount(userId);

            String message = String.format("You have %d items in your shopping list", itemCount);
            VoiceCommandResponse response = new VoiceCommandResponse("listed", message);
            response.setData(items);
            response.setAction("list");

            return response;

        } catch (Exception e) {
            log.error("Error getting shopping list: {}", e.getMessage());
            return new VoiceCommandResponse("error", "Sorry, I couldn't get your shopping list.");
        }
    }
}
