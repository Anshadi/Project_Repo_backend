package com.voice.shopping.controller;

import com.voice.shopping.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:*", "http://127.0.0.1:*" })
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable String userId) {
        log.info("Generating recommendations for user: {}", userId);

        try {
            Map<String, Object> recommendations = recommendationService.getRecommendations(userId);
            log.info("Generated {} recommendations for user {}",
                    ((java.util.List<?>) recommendations.get("suggestions")).size(), userId);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            log.error("Error generating recommendations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
