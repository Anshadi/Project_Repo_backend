package com.voice.shopping.service;

import com.voice.shopping.model.ShoppingItem;
import com.voice.shopping.model.Product;
import com.voice.shopping.repository.ShoppingItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ProductService productService;

    public List<ShoppingItem> getShoppingList(String userId) {
        log.debug("Fetching shopping list for user: {}", userId);
        return shoppingItemRepository.findByUserId(userId);
    }

    public ShoppingItem addItem(String userId, String itemName, Integer quantity, String category, String unit) {
        return addItem(userId, itemName, quantity, category, unit, null, "medium");
    }

    public ShoppingItem addItem(String userId, String itemName, Integer quantity, String category, String unit, String notes, String priority) {
        return addItem(userId, itemName, quantity, category, unit, notes, priority, null, null);
    }

    public ShoppingItem addItem(String userId, String itemName, Integer quantity, String category, String unit, String notes, String priority, Double price, String brand) {
        log.debug("Adding item: {} (qty: {}) for user: {} with notes: {}, priority: {}, price: {}, brand: {}", 
                 itemName, quantity, userId, notes, priority, price, brand);

        // MANDATORY: Only add items that exist in product database with valid price
        if (price == null || brand == null) {
            List<Product> matchingProducts = productService.getProductSuggestions(itemName);
            if (!matchingProducts.isEmpty()) {
                Product product = matchingProducts.get(0);
                if (price == null) price = product.getPrice();
                if (brand == null) brand = product.getBrand();
                log.info("Found matching product for '{}': {} - ${}", itemName, product.getName(), product.getPrice());
            } else {
                // Reject items not found in database
                throw new RuntimeException("Product '" + itemName + "' not found in database. Only database products can be added.");
            }
        }
        
        // Ensure price is available - reject items without price
        if (price == null || price <= 0) {
            throw new RuntimeException("Product '" + itemName + "' has no valid price. Cannot add items without pricing information.");
        }

        // Check if item already exists
        Optional<ShoppingItem> existingItem = shoppingItemRepository.findByUserIdAndNameIgnoreCase(userId, itemName);

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            ShoppingItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (notes != null) item.setNotes(notes);
            if (priority != null) item.setPriority(priority);
            // Update price and brand if provided (from product search)
            if (price != null) item.setPrice(price);
            if (brand != null) item.setBrand(brand);
            item.setUpdatedAt(LocalDateTime.now());
            return shoppingItemRepository.save(item);
        } else {
            // Create new item
            ShoppingItem newItem = new ShoppingItem(userId, itemName, quantity, category, unit != null ? unit : "item", price, brand);
            if (notes != null) newItem.setNotes(notes);
            if (priority != null) newItem.setPriority(priority);
            return shoppingItemRepository.save(newItem);
        }
    }

    public ShoppingItem updateItemQuantity(String userId, String itemId, Integer quantity) {
        log.debug("Updating item {} quantity to {} for user: {}", itemId, quantity, userId);

        Optional<ShoppingItem> itemOpt = shoppingItemRepository.findByUserIdAndId(userId, itemId);
        if (itemOpt.isPresent()) {
            ShoppingItem item = itemOpt.get();
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            return shoppingItemRepository.save(item);
        } else {
            throw new RuntimeException("Item not found in user's shopping list");
        }
    }

    public boolean removeItem(String userId, String itemId) {
        log.debug("Removing item {} for user: {}", itemId, userId);

        Optional<ShoppingItem> itemOpt = shoppingItemRepository.findByUserIdAndId(userId, itemId);
        if (itemOpt.isPresent()) {
            shoppingItemRepository.deleteByUserIdAndId(userId, itemId);
            return true;
        }
        return false;
    }

    public Optional<ShoppingItem> findItemByName(String userId, String itemName) {
        return shoppingItemRepository.findByUserIdAndNameIgnoreCase(userId, itemName);
    }

    public long getItemCount(String userId) {
        return shoppingItemRepository.countByUserId(userId);
    }

    public ShoppingItem updateItem(String userId, String itemId, Integer quantity, String notes, String priority, Boolean completed) {
        log.debug("Updating item {} for user: {} - quantity: {}, notes: {}, priority: {}, completed: {}", 
                 itemId, userId, quantity, notes, priority, completed);

        Optional<ShoppingItem> itemOpt = shoppingItemRepository.findByUserIdAndId(userId, itemId);
        if (itemOpt.isPresent()) {
            ShoppingItem item = itemOpt.get();
            
            if (quantity != null) item.setQuantity(quantity);
            if (notes != null) item.setNotes(notes);
            if (priority != null) item.setPriority(priority);
            if (completed != null) item.setCompleted(completed);
            
            item.setUpdatedAt(LocalDateTime.now());
            return shoppingItemRepository.save(item);
        } else {
            throw new RuntimeException("Item not found in user's shopping list");
        }
    }

    public ShoppingItem updateItemNotes(String userId, String itemId, String notes) {
        return updateItem(userId, itemId, null, notes, null, null);
    }

    public ShoppingItem updateItemPriority(String userId, String itemId, String priority) {
        return updateItem(userId, itemId, null, null, priority, null);
    }

    public ShoppingItem markItemCompleted(String userId, String itemId, boolean completed) {
        return updateItem(userId, itemId, null, null, null, completed);
    }

    public List<ShoppingItem> getCompletedItems(String userId) {
        log.debug("Fetching completed items for user: {}", userId);
        return shoppingItemRepository.findByUserIdAndCompleted(userId, true);
    }

    public List<ShoppingItem> getPendingItems(String userId) {
        log.debug("Fetching pending items for user: {}", userId);
        return shoppingItemRepository.findByUserIdAndCompleted(userId, false);
    }

    public List<ShoppingItem> getItemsByPriority(String userId, String priority) {
        log.debug("Fetching {} priority items for user: {}", priority, userId);
        return shoppingItemRepository.findByUserIdAndPriority(userId, priority);
    }

    public List<ShoppingItem> getItemsSortedByPriority(String userId) {
        log.debug("Fetching items sorted by priority for user: {}", userId);
        return shoppingItemRepository.findByUserIdOrderByPriorityDesc(userId);
    }

    public void clearList(String userId) {
        log.debug("Clearing shopping list for user: {}", userId);
        List<ShoppingItem> items = shoppingItemRepository.findByUserId(userId);
        shoppingItemRepository.deleteAll(items);
    }

    public ShoppingItem addItemFromProduct(String userId, String productId, Integer quantity) {
        log.debug("Adding product {} (qty: {}) to shopping list for user: {}", productId, quantity, userId);
        
        // Get product details
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found: " + productId);
        }
        
        Product product = productOpt.get();
        
        // Add item with product details including price and brand
        return addItem(userId, product.getName(), quantity, product.getCategory(), 
                      "item", null, "medium", product.getPrice(), product.getBrand());
    }
}
