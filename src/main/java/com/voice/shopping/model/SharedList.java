package com.voice.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shared_lists")
public class SharedList {
    @Id
    private String id;

    @NotBlank(message = "List name is required")
    private String name;

    @NotBlank(message = "Owner ID is required")
    @Indexed
    private String ownerId;

    private String description;

    // List of user IDs who have access to this list
    private List<String> sharedWithUsers = new ArrayList<>();

    // Sharing permissions
    private boolean allowEdit = true; // Allow shared users to edit
    private boolean allowAdd = true;  // Allow shared users to add items
    private boolean allowDelete = false; // Allow shared users to delete items

    // Sharing metadata
    @Indexed
    private String shareCode; // Unique code for sharing
    private LocalDateTime shareExpiresAt;
    private boolean isPubliclyShared = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructor for quick creation
    public SharedList(String name, String ownerId, String description) {
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.shareCode = generateShareCode();
    }

    private String generateShareCode() {
        // Generate a random 8-character share code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    public boolean isUserAllowed(String userId) {
        return ownerId.equals(userId) || sharedWithUsers.contains(userId);
    }

    public boolean canUserEdit(String userId) {
        if (ownerId.equals(userId)) return true;
        return sharedWithUsers.contains(userId) && allowEdit;
    }

    public boolean canUserAdd(String userId) {
        if (ownerId.equals(userId)) return true;
        return sharedWithUsers.contains(userId) && allowAdd;
    }

    public boolean canUserDelete(String userId) {
        if (ownerId.equals(userId)) return true;
        return sharedWithUsers.contains(userId) && allowDelete;
    }
}
