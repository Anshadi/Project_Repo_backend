package com.voice.shopping.service;

import com.voice.shopping.model.SharedList;
import com.voice.shopping.repository.SharedListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedListService {

    private final SharedListRepository sharedListRepository;

    public SharedList createSharedList(String ownerId, String name, String description) {
        log.info("Creating shared list '{}' for owner: {}", name, ownerId);
        
        SharedList sharedList = new SharedList(name, ownerId, description);
        return sharedListRepository.save(sharedList);
    }

    public List<SharedList> getUserSharedLists(String userId) {
        log.debug("Fetching shared lists for user: {}", userId);
        return sharedListRepository.findAccessibleLists(userId);
    }

    public List<SharedList> getOwnedLists(String ownerId) {
        log.debug("Fetching owned lists for user: {}", ownerId);
        return sharedListRepository.findByOwnerId(ownerId);
    }

    public List<SharedList> getListsSharedWithUser(String userId) {
        log.debug("Fetching lists shared with user: {}", userId);
        return sharedListRepository.findListsSharedWithUser(userId);
    }

    public SharedList shareListWithUser(String listId, String ownerId, String targetUserId) {
        log.info("Sharing list {} from owner {} with user: {}", listId, ownerId, targetUserId);
        
        Optional<SharedList> listOpt = sharedListRepository.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Shared list not found");
        }

        SharedList sharedList = listOpt.get();
        if (!sharedList.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only the list owner can share the list");
        }

        if (!sharedList.getSharedWithUsers().contains(targetUserId)) {
            sharedList.getSharedWithUsers().add(targetUserId);
            sharedList.setUpdatedAt(LocalDateTime.now());
            return sharedListRepository.save(sharedList);
        }

        return sharedList;
    }

    public SharedList shareListByCode(String shareCode, String userId) {
        log.info("User {} joining shared list with code: {}", userId, shareCode);
        
        Optional<SharedList> listOpt = sharedListRepository.findByShareCode(shareCode);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Invalid share code");
        }

        SharedList sharedList = listOpt.get();
        
        // Check if share code is expired
        if (sharedList.getShareExpiresAt() != null && 
            sharedList.getShareExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Share code has expired");
        }

        if (!sharedList.getSharedWithUsers().contains(userId)) {
            sharedList.getSharedWithUsers().add(userId);
            sharedList.setUpdatedAt(LocalDateTime.now());
            return sharedListRepository.save(sharedList);
        }

        return sharedList;
    }

    public void removeUserFromSharedList(String listId, String ownerId, String targetUserId) {
        log.info("Removing user {} from shared list {} by owner: {}", targetUserId, listId, ownerId);
        
        Optional<SharedList> listOpt = sharedListRepository.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Shared list not found");
        }

        SharedList sharedList = listOpt.get();
        if (!sharedList.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only the list owner can remove users");
        }

        sharedList.getSharedWithUsers().remove(targetUserId);
        sharedList.setUpdatedAt(LocalDateTime.now());
        sharedListRepository.save(sharedList);
    }

    public void deleteSharedList(String listId, String ownerId) {
        log.info("Deleting shared list {} by owner: {}", listId, ownerId);
        
        Optional<SharedList> listOpt = sharedListRepository.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Shared list not found");
        }

        SharedList sharedList = listOpt.get();
        if (!sharedList.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only the list owner can delete the list");
        }

        sharedListRepository.deleteById(listId);
    }

    public SharedList updateListPermissions(String listId, String ownerId, boolean allowEdit, boolean allowAdd, boolean allowDelete) {
        log.info("Updating permissions for shared list {} by owner: {}", listId, ownerId);
        
        Optional<SharedList> listOpt = sharedListRepository.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Shared list not found");
        }

        SharedList sharedList = listOpt.get();
        if (!sharedList.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only the list owner can update permissions");
        }

        sharedList.setAllowEdit(allowEdit);
        sharedList.setAllowAdd(allowAdd);
        sharedList.setAllowDelete(allowDelete);
        sharedList.setUpdatedAt(LocalDateTime.now());
        
        return sharedListRepository.save(sharedList);
    }

    public String regenerateShareCode(String listId, String ownerId) {
        log.info("Regenerating share code for list {} by owner: {}", listId, ownerId);
        
        Optional<SharedList> listOpt = sharedListRepository.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("Shared list not found");
        }

        SharedList sharedList = listOpt.get();
        if (!sharedList.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only the list owner can regenerate share code");
        }

        String newShareCode = generateShareCode();
        sharedList.setShareCode(newShareCode);
        sharedList.setUpdatedAt(LocalDateTime.now());
        sharedListRepository.save(sharedList);
        
        return newShareCode;
    }

    private String generateShareCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    public boolean hasUserAccess(String listId, String userId) {
        Optional<SharedList> listOpt = sharedListRepository.findAccessibleListByUserAndId(userId, listId);
        return listOpt.isPresent();
    }
}
