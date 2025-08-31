package com.voice.shopping.controller;

import com.voice.shopping.dto.AddItemRequest;
import com.voice.shopping.dto.ApiResponse;
import com.voice.shopping.dto.UpdateItemRequest;
import com.voice.shopping.model.ShoppingItem;
import com.voice.shopping.service.ShoppingListService;
import com.voice.shopping.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/list")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:*", "http://127.0.0.1:*" })
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final RecommendationService recommendationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<ShoppingItem>> getShoppingList(@PathVariable String userId) {
        log.info("Fetching shopping list for user: {}", userId);

        try {
            List<ShoppingItem> items = shoppingListService.getShoppingList(userId);
            log.info("Retrieved {} items for user {}", items.size(), userId);
            return ResponseEntity.ok(items);

        } catch (Exception e) {
            log.error("Error fetching shopping list: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ShoppingItem>> addItem(@Valid @RequestBody AddItemRequest request) {
        log.info("Adding item '{}' (qty: {}) for user: {} with price: {} and brand: {}",
                request.getItem(), request.getQuantity(), request.getUserId(), request.getPrice(), request.getBrand());

        try {
            ShoppingItem item = shoppingListService.addItem(
                    request.getUserId(),
                    request.getItem(),
                    request.getQuantity(),
                    request.getCategory(),
                    request.getUnit(),
                    request.getNotes(),
                    request.getPriority(),
                    request.getPrice(),
                    request.getBrand());

            log.info("Item added successfully: {} with price: {}", item.getId(), item.getPrice());
            return ResponseEntity.ok(ApiResponse.success("Item added successfully", item));

        } catch (Exception e) {
            log.error("Error adding item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add item: " + e.getMessage()));
        }
    }

    @PostMapping("/add-from-product")
    public ResponseEntity<ApiResponse<ShoppingItem>> addItemFromProduct(
            @RequestParam String userId,
            @RequestParam String productId,
            @RequestParam Integer quantity) {
        
        log.info("Adding product {} (qty: {}) to shopping list for user: {}", productId, quantity, userId);

        try {
            ShoppingItem item = shoppingListService.addItemFromProduct(userId, productId, quantity);
            log.info("Product added to shopping list successfully: {} with price: {}", item.getId(), item.getPrice());
            return ResponseEntity.ok(ApiResponse.success("Product added to shopping list successfully", item));

        } catch (Exception e) {
            log.error("Error adding product to shopping list: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add product: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ShoppingItem>> updateItem(@Valid @RequestBody UpdateItemRequest request) {
        log.info("Updating item {} for user: {} - quantity: {}, notes: {}, priority: {}, completed: {}",
                request.getItemId(), request.getUserId(), request.getQuantity(), 
                request.getNotes(), request.getPriority(), request.getCompleted());

        try {
            // Check if only quantity is being updated (for backward compatibility)
            if (request.getNotes() == null && request.getPriority() == null && request.getCompleted() == null) {
                // Use existing updateItemQuantity method
                ShoppingItem item = shoppingListService.updateItemQuantity(
                        request.getUserId(),
                        request.getItemId(),
                        request.getQuantity());
                return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
            } else {
                // Use enhanced update method (we'll add this to service)
                ShoppingItem item = shoppingListService.updateItem(
                        request.getUserId(),
                        request.getItemId(),
                        request.getQuantity(),
                        request.getNotes(),
                        request.getPriority(),
                        request.getCompleted());
                return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
            }

        } catch (Exception e) {
            log.error("Error updating item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/{itemId}")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @PathVariable String userId,
            @PathVariable String itemId) {

        log.info("Removing item {} for user: {}", itemId, userId);

        try {
            boolean removed = shoppingListService.removeItem(userId, itemId);

            if (removed) {
                log.info("Item removed successfully: {}", itemId);
                return ResponseEntity.ok(ApiResponse.success("Item removed successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error removing item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove item: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getItemCount(@PathVariable String userId) {
        try {
            long count = shoppingListService.getItemCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error getting item count: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to get item count"));
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearList(@PathVariable String userId) {
        log.info("Clearing shopping list for user: {}", userId);

        try {
            // Record purchases before clearing
            List<ShoppingItem> items = shoppingListService.getShoppingList(userId);
            items.forEach(item -> recommendationService.recordPurchase(userId, item));

            shoppingListService.clearList(userId);
            log.info("Shopping list cleared for user: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("Shopping list cleared successfully"));

        } catch (Exception e) {
            log.error("Error clearing shopping list: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to clear shopping list"));
        }
    }

    // Enhanced endpoints for new features
    
    @GetMapping("/{userId}/completed")
    public ResponseEntity<List<ShoppingItem>> getCompletedItems(@PathVariable String userId) {
        log.info("Fetching completed items for user: {}", userId);
        
        try {
            List<ShoppingItem> items = shoppingListService.getCompletedItems(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching completed items: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{userId}/pending")
    public ResponseEntity<List<ShoppingItem>> getPendingItems(@PathVariable String userId) {
        log.info("Fetching pending items for user: {}", userId);
        
        try {
            List<ShoppingItem> items = shoppingListService.getPendingItems(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching pending items: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{userId}/priority/{priority}")
    public ResponseEntity<List<ShoppingItem>> getItemsByPriority(
            @PathVariable String userId, 
            @PathVariable String priority) {
        log.info("Fetching {} priority items for user: {}", priority, userId);
        
        try {
            List<ShoppingItem> items = shoppingListService.getItemsByPriority(userId, priority);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching items by priority: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{userId}/{itemId}/complete")
    public ResponseEntity<ApiResponse<ShoppingItem>> markItemCompleted(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestParam boolean completed) {
        log.info("Marking item {} as {} for user: {}", itemId, completed ? "completed" : "pending", userId);
        
        try {
            ShoppingItem item = shoppingListService.markItemCompleted(userId, itemId, completed);
            return ResponseEntity.ok(ApiResponse.success("Item status updated successfully", item));
        } catch (Exception e) {
            log.error("Error updating item completion status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update item status"));
        }
    }
    
    @PutMapping("/{userId}/{itemId}/notes")
    public ResponseEntity<ApiResponse<ShoppingItem>> updateItemNotes(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestParam String notes) {
        log.info("Updating notes for item {} for user: {}", itemId, userId);
        
        try {
            ShoppingItem item = shoppingListService.updateItemNotes(userId, itemId, notes);
            return ResponseEntity.ok(ApiResponse.success("Item notes updated successfully", item));
        } catch (Exception e) {
            log.error("Error updating item notes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update item notes"));
        }
    }
    
    @PutMapping("/{userId}/{itemId}/priority")
    public ResponseEntity<ApiResponse<ShoppingItem>> updateItemPriority(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestParam String priority) {
        log.info("Updating priority for item {} to {} for user: {}", itemId, priority, userId);
        
        try {
            ShoppingItem item = shoppingListService.updateItemPriority(userId, itemId, priority);
            return ResponseEntity.ok(ApiResponse.success("Item priority updated successfully", item));
        } catch (Exception e) {
            log.error("Error updating item priority: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update item priority"));
        }
    }
}
