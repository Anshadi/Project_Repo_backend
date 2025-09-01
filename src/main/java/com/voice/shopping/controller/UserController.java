package com.voice.shopping.controller;

import com.voice.shopping.dto.ApiResponse;
import com.voice.shopping.model.User;
import com.voice.shopping.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = { "https://project-repo-0.onrender.com", "http://localhost:*", "http://127.0.0.1:*" })
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        log.info("Fetching user profile for: {}", userId);
        
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody User user) {
        log.info("Registering new user: {}", user.getEmail());
        
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", savedUser));
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to register user: " + e.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<String>> updateUserPreferences(@RequestBody Map<String, String> requestBody) {
        log.info("Updating user preferences: {}", requestBody);
        
        try {
            // The frontend doesn't send userId, so we need to extract it differently
            // For now, let's assume it's passed in the request body or use a default user
            String userId = requestBody.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                // If no userId provided, we could use a default or return error
                // For this demo, let's use a default user ID
                userId = "default_user";
                log.warn("No userId provided in preferences update, using default: {}", userId);
            }

            // Create a new map without userId for actual preferences
            Map<String, String> preferences = new HashMap<>(requestBody);
            preferences.remove("userId");

            userService.updateUserPreferences(userId, preferences);
            return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", (String) null));
        } catch (Exception e) {
            log.error("Error updating user preferences: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update preferences: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String userId, @RequestBody User user) {
        log.info("Updating user profile for: {}", userId);
        
        try {
            User updatedUser = userService.updateUser(userId, user);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        log.info("Deleting user: {}", userId);
        
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", (String) null));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@PathVariable String userId) {
        log.debug("Checking if user exists: {}", userId);
        
        boolean exists = userService.userExists(userId);
        return ResponseEntity.ok(ApiResponse.success("User existence checked", exists));
    }
}
