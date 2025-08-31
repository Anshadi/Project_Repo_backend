package com.voice.shopping.repository;

import com.voice.shopping.model.PurchaseHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseHistoryRepository extends MongoRepository<PurchaseHistory, String> {

    List<PurchaseHistory> findByUserId(String userId);

    List<PurchaseHistory> findByUserIdOrderByPurchaseDateDesc(String userId);

    // Find recent purchases (last 30 days)
    @Query("{ userId: ?0, purchaseDate: { $gte: ?1 } }")
    List<PurchaseHistory> findByUserIdAndPurchaseDateAfter(String userId, LocalDateTime date);

    // Find frequently bought items
    @Query("{ userId: ?0 }")
    List<PurchaseHistory> findFrequentItemsByUserId(String userId);

    // Find items bought together with a specific item
    @Query("{ userId: ?0, itemName: { $ne: ?1 } }")
    List<PurchaseHistory> findItemsBoughtWith(String userId, String itemName);

    // Get category purchase statistics
    @Query("{ userId: ?0 }")
    List<PurchaseHistory> findByUserIdForCategoryStats(String userId);
    
    // Delete all history for a specific user
    void deleteByUserId(String userId);
}
