package com.dinelock.controller;

import com.dinelock.entity.Restaurant;
import com.dinelock.service.RestaurantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantQueryService restaurantQueryService;

	@GetMapping("/top")
	public ResponseEntity<List<Restaurant>> getTopRestaurants(@RequestParam String cuisineType) {
		return ResponseEntity.ok(restaurantQueryService.getTopRestaurantsByCuisine(cuisineType));
	}
}
