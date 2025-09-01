package com.voice.shopping.controller;

import com.voice.shopping.dto.ApiResponse;
import com.voice.shopping.model.SharedList;
import com.voice.shopping.service.SharedListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@CrossOrigin(origins = { "https://project-repo-0.onrender.com", "http://localhost:*", "http://127.0.0.1:*" })
public class SharedListController {

    private final SharedListService sharedListService;

    @GetMapping("/{userId}/lists")
    public ResponseEntity<List<SharedList>> getUserSharedLists(@PathVariable String userId) {
        log.info("Fetching shared lists for user: {}", userId);
        
        try {
            List<SharedList> lists = sharedListService.getUserSharedLists(userId);
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            log.error("Error fetching shared lists: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SharedList>> createSharedList(@RequestBody Map<String, String> request) {
        log.info("Creating shared list: {}", request);
        
        try {
            String ownerId = request.get("ownerId");
            String name = request.get("name");
            String description = request.get("description");
            
            if (ownerId == null || name == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Owner ID and name are required"));
            }

            SharedList sharedList = sharedListService.createSharedList(ownerId, name, description);
            return ResponseEntity.ok(ApiResponse.success("Shared list created successfully", sharedList));
            
        } catch (Exception e) {
            log.error("Error creating shared list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create shared list: " + e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<SharedList>> joinSharedList(@RequestBody Map<String, String> request) {
        log.info("User joining shared list: {}", request);
        
        try {
            String userId = request.get("userId");
            String shareCode = request.get("shareCode");
            
            if (userId == null || shareCode == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID and share code are required"));
            }

            SharedList sharedList = sharedListService.shareListByCode(shareCode, userId);
            return ResponseEntity.ok(ApiResponse.success("Successfully joined shared list", sharedList));
            
        } catch (Exception e) {
            log.error("Error joining shared list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to join shared list: " + e.getMessage()));
        }
    }

    @PostMapping("/{listId}/invite")
    public ResponseEntity<ApiResponse<SharedList>> inviteUserToList(
            @PathVariable String listId,
            @RequestBody Map<String, String> request) {
        log.info("Inviting user to shared list {}: {}", listId, request);
        
        try {
            String ownerId = request.get("ownerId");
            String targetUserId = request.get("targetUserId");
            
            if (ownerId == null || targetUserId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Owner ID and target user ID are required"));
            }

            SharedList sharedList = sharedListService.shareListWithUser(listId, ownerId, targetUserId);
            return ResponseEntity.ok(ApiResponse.success("User invited successfully", sharedList));
            
        } catch (Exception e) {
            log.error("Error inviting user to shared list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to invite user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{listId}/remove/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> removeUserFromList(
            @PathVariable String listId,
            @PathVariable String targetUserId,
            @RequestParam String ownerId) {
        log.info("Removing user {} from shared list {} by owner: {}", targetUserId, listId, ownerId);
        
        try {
            sharedListService.removeUserFromSharedList(listId, ownerId, targetUserId);
            return ResponseEntity.ok(ApiResponse.success("User removed from shared list successfully", (String) null));
            
        } catch (Exception e) {
            log.error("Error removing user from shared list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to remove user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<String>> deleteSharedList(
            @PathVariable String listId,
            @RequestParam String ownerId) {
        log.info("Deleting shared list {} by owner: {}", listId, ownerId);
        
        try {
            sharedListService.deleteSharedList(listId, ownerId);
            return ResponseEntity.ok(ApiResponse.success("Shared list deleted successfully", (String) null));
            
        } catch (Exception e) {
            log.error("Error deleting shared list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete shared list: " + e.getMessage()));
        }
    }

    @PutMapping("/{listId}/permissions")
    public ResponseEntity<ApiResponse<SharedList>> updateListPermissions(
            @PathVariable String listId,
            @RequestBody Map<String, Object> request) {
        log.info("Updating permissions for shared list {}: {}", listId, request);
        
        try {
            String ownerId = (String) request.get("ownerId");
            Boolean allowEdit = (Boolean) request.get("allowEdit");
            Boolean allowAdd = (Boolean) request.get("allowAdd");
            Boolean allowDelete = (Boolean) request.get("allowDelete");
            
            if (ownerId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Owner ID is required"));
            }

            SharedList sharedList = sharedListService.updateListPermissions(
                    listId, ownerId, 
                    allowEdit != null ? allowEdit : true,
                    allowAdd != null ? allowAdd : true,
                    allowDelete != null ? allowDelete : false);
                    
            return ResponseEntity.ok(ApiResponse.success("Permissions updated successfully", sharedList));
            
        } catch (Exception e) {
            log.error("Error updating list permissions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update permissions: " + e.getMessage()));
        }
    }

    @PostMapping("/{listId}/regenerate-code")
    public ResponseEntity<ApiResponse<String>> regenerateShareCode(
            @PathVariable String listId,
            @RequestBody Map<String, String> request) {
        log.info("Regenerating share code for list {}: {}", listId, request);
        
        try {
            String ownerId = request.get("ownerId");
            
            if (ownerId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Owner ID is required"));
            }

            String newShareCode = sharedListService.regenerateShareCode(listId, ownerId);
            return ResponseEntity.ok(ApiResponse.success("Share code regenerated successfully", newShareCode));
            
        } catch (Exception e) {
            log.error("Error regenerating share code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to regenerate share code: " + e.getMessage()));
        }
    }
}
