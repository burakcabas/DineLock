package com.dinelock.repository;

import com.dinelock.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Belirli bir mutfak türündeki restoranları, AI puanına göre en yüksekten düşüğe sıralar
    List<Restaurant> findTop10ByCuisineTypeOrderByAiSentimentScoreDesc(String cuisineType);
    
}