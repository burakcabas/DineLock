package com.dinelock.service;

import com.dinelock.entity.Restaurant;
import com.dinelock.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantQueryService {

    private final RestaurantRepository restaurantRepository;

    // "top-restaurants" adında bir önbellek (cache) bölgesi oluşturur.
    // Aynı mutfak türü (cuisineType) ikinci kez sorulduğunda, metodun içine HİÇ GİRMEZ, sonucu direkt RAM'den döner.
    @Cacheable(value = "topRestaurants", key = "#cuisineType")
    public List<Restaurant> getTopRestaurantsByCuisine(String cuisineType) {
        log.warn("DİKKAT: Veritabanına gidiliyor! Ağır SQL sorgusu çalışıyor... Mutfak: {}", cuisineType);
        return restaurantRepository.findTop10ByCuisineTypeOrderByAiSentimentScoreDesc(cuisineType);
    }
}