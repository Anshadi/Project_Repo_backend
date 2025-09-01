package com.voice.shopping.controller;

import com.voice.shopping.model.PurchaseHistory;
import com.voice.shopping.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@CrossOrigin(origins = { "https://project-repo-0.onrender.com", "http://localhost:*", "http://127.0.0.1:*" })
public class HistoryController {

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<PurchaseHistory>> getUserHistory(@PathVariable String userId) {
        log.info("Fetching purchase history for user: {}", userId);

        try {
            List<PurchaseHistory> history = purchaseHistoryRepository.findByUserIdOrderByPurchaseDateDesc(userId);
            log.info("Retrieved {} history items for user {}", history.size(), userId);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching purchase history: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<PurchaseHistory> addToHistory(@RequestBody PurchaseHistory historyItem) {
        log.info("Adding item to purchase history for user: {}", historyItem.getUserId());

        try {
            PurchaseHistory savedItem = purchaseHistoryRepository.save(historyItem);
            log.info("Added item to history: {}", savedItem.getId());
            return ResponseEntity.ok(savedItem);

        } catch (Exception e) {
            log.error("Error adding item to history: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearHistory(@RequestParam(required = false) String userId) {
        log.info("Clearing purchase history for user: {}", userId != null ? userId : "all users");

        try {
            if (userId != null) {
                // Clear history for specific user
                purchaseHistoryRepository.deleteByUserId(userId);
                log.info("Cleared history for user: {}", userId);
            } else {
                // Clear all history (for demo purposes)
                purchaseHistoryRepository.deleteAll();
                log.info("Cleared all purchase history");
            }
            
            return ResponseEntity.ok("History cleared successfully");

        } catch (Exception e) {
            log.error("Error clearing history: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to clear history");
        }
    }
}
