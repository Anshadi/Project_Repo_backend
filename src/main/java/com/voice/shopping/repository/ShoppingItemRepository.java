package com.voice.shopping.repository;

import com.voice.shopping.model.ShoppingItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingItemRepository extends MongoRepository<ShoppingItem, String> {
    List<ShoppingItem> findByUserId(String userId);

    Optional<ShoppingItem> findByUserIdAndId(String userId, String itemId);

    @Query("{'userId': ?0, 'name': {$regex: ?1, $options: 'i'}}")
    Optional<ShoppingItem> findByUserIdAndNameIgnoreCase(String userId, String name);

    void deleteByUserIdAndId(String userId, String itemId);

    long countByUserId(String userId);
    
    // Enhanced queries for new fields
    List<ShoppingItem> findByUserIdAndCompleted(String userId, boolean completed);
    
    List<ShoppingItem> findByUserIdAndPriority(String userId, String priority);
    
    List<ShoppingItem> findByUserIdOrderByPriorityDesc(String userId);
    
    @Query("{'userId': ?0, 'completed': false}")
    List<ShoppingItem> findPendingItemsByUserId(String userId);
}
