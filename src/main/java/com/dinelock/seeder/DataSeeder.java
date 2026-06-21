package com.dinelock.seeder;

import com.dinelock.entity.Restaurant;
import com.dinelock.entity.RestaurantTable;
import com.dinelock.entity.Review;
import com.dinelock.repository.RestaurantRepository;
import com.dinelock.repository.RestaurantTableRepository;
import com.dinelock.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepository.count() == 0) {
            log.info("Veritabanı boş. Sentetik veri üretimi başlıyor...");
            Faker faker = new Faker();
            String[] cuisines = {"Italian", "Turkish", "Asian", "Mexican", "Seafood", "Steakhouse"};
            String[] reviewTemplates = {
                    "The %s was absolutely fantastic! Highly recommended.",
                    "Great atmosphere, but the %s was a bit too salty for my taste.",
                    "Best dining experience I've had in a while. You must try the %s.",
                    "Service was slow, but the %s made up for it.",
                    "Average place. The %s was okay, nothing special."
            };

            for (int i = 0; i < 20; i++) {
                // 1. Restoran Üretimi (Gerçekçi İsimler)
                Restaurant restaurant = Restaurant.builder()
                        .name(faker.restaurant().name())
                        .address(faker.address().fullAddress())
                        .phoneNumber(faker.phoneNumber().cellPhone())
                        .cuisineType(cuisines[faker.random().nextInt(cuisines.length)])
                        .aiSentimentScore(faker.number().randomDouble(1, 3, 5))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                restaurant = restaurantRepository.save(restaurant);

                // 2. Masa Üretimi
                for (int j = 1; j <= 5; j++) {
                    RestaurantTable table = RestaurantTable.builder()
                            .restaurant(restaurant)
                            .tableNumber("T-" + j)
                            .capacity(faker.number().numberBetween(2, 8))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    restaurantTableRepository.save(table);
                }

                // 3. Yorum Üretimi (Yemek isimleriyle zenginleştirilmiş)
                for (int k = 0; k < 15; k++) {
                    String randomDish = faker.food().dish();
                    String template = reviewTemplates[faker.random().nextInt(reviewTemplates.length)];
                    String realisticReview = String.format(template, randomDish);

                    String sentiment = realisticReview.contains("fantastic") || realisticReview.contains("Best") ? "POSITIVE" :
                            realisticReview.contains("salty") || realisticReview.contains("slow") ? "NEGATIVE" : "NEUTRAL";

                    Review review = Review.builder()
                            .restaurant(restaurant)
                            .reviewerName(faker.name().fullName())
                            .content(realisticReview)
                            .sentimentLabel(sentiment)
                            .sentimentScore(faker.number().randomDouble(1, 1, 5))
                            .createdAt(LocalDateTime.now())
                            .build();
                    reviewRepository.save(review);
                }
            }
            log.info("Sentetik veri üretimi başarıyla tamamlandı!");
        }
    }
}