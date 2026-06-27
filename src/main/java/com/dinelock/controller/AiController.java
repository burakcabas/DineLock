package com.dinelock.controller;

import com.dinelock.dto.AiAnalysisResult;
import com.dinelock.entity.Review;
import com.dinelock.repository.ReviewRepository;
import com.dinelock.service.GeminiAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiAiService geminiAiService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/analyze/{restaurantId}")
    public ResponseEntity<AiAnalysisResult> analyzeRestaurant(@PathVariable Long restaurantId) {
        // 1. İlgili restoranın yorumlarını veritabanından çek
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);

        // 2. Token tasarrufu ve I/O hızı için sadece ilk 10 yorumu birleştirip Gemini'ye veriyoruz
        String combinedReviews = reviews.stream()
                .limit(10)
                .map(Review::getContent)
                .collect(Collectors.joining(" | "));

        // 3. Servis çağrısı ve JSON dönüşü
        AiAnalysisResult result = geminiAiService.analyzeReview(combinedReviews);

        return ResponseEntity.ok(result);
    }
}