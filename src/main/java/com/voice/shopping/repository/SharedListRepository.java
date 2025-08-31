package com.voice.shopping.repository;

import com.voice.shopping.model.SharedList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedListRepository extends MongoRepository<SharedList, String> {
    
    // Find lists owned by a user
    List<SharedList> findByOwnerId(String ownerId);
    
    // Find lists shared with a user
    @Query("{'sharedWithUsers': ?0}")
    List<SharedList> findListsSharedWithUser(String userId);
    
    // Find all lists accessible by a user (owned + shared)
    @Query("{'$or': [{'ownerId': ?0}, {'sharedWithUsers': ?0}]}")
    List<SharedList> findAccessibleLists(String userId);
    
    // Find by share code
    Optional<SharedList> findByShareCode(String shareCode);
    
    // Find publicly shared lists
    List<SharedList> findByIsPubliclySharedTrue();
    
    // Check if user has access to a list
    @Query("{'$and': [{'_id': ?1}, {'$or': [{'ownerId': ?0}, {'sharedWithUsers': ?0}]}]}")
    Optional<SharedList> findAccessibleListByUserAndId(String userId, String listId);
}
