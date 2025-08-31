package com.voice.shopping.service;

import com.voice.shopping.model.User;
import com.voice.shopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        // Create default user if not found
        log.info("User not found, creating default user with ID: {}", userId);
        return createDefaultUser(userId);
    }

    public User createUser(User user) {
        log.info("Creating new user: {}", user.getEmail());
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    public User updateUser(String userId, User updatedUser) {
        log.info("Updating user: {}", userId);
        
        User existingUser = getUserById(userId);
        
        // Update fields if provided
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getLanguage() != null) {
            existingUser.setLanguage(updatedUser.getLanguage());
        }
        if (updatedUser.getDiet() != null) {
            existingUser.setDiet(updatedUser.getDiet());
        }
        if (updatedUser.getFavoriteCategories() != null) {
            existingUser.setFavoriteCategories(updatedUser.getFavoriteCategories());
        }
        if (updatedUser.getFavoriteBrands() != null) {
            existingUser.setFavoriteBrands(updatedUser.getFavoriteBrands());
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }

    public void updateUserPreferences(String userId, Map<String, String> preferences) {
        log.info("Updating preferences for user: {} with preferences: {}", userId, preferences);
        
        User user = getUserById(userId);
        
        // Update preferences
        if (preferences.containsKey("language")) {
            user.setLanguage(preferences.get("language"));
        }
        if (preferences.containsKey("diet")) {
            user.setDiet(preferences.get("diet"));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.debug("Updated preferences for user: {}", userId);
    }

    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("User deleted successfully: {}", userId);
        } else {
            log.warn("Attempted to delete non-existent user: {}", userId);
            throw new RuntimeException("User not found: " + userId);
        }
    }

    public boolean userExists(String userId) {
        return userRepository.existsById(userId);
    }

    private User createDefaultUser(String userId) {
        User defaultUser = new User();
        defaultUser.setId(userId);
        defaultUser.setName("Shopping User");
        defaultUser.setEmail(userId + "@demo.com");
        defaultUser.setLanguage("en");
        defaultUser.setDiet("none");
        defaultUser.setCreatedAt(LocalDateTime.now());
        defaultUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(defaultUser);
    }
}
