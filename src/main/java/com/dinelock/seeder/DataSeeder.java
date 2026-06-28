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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReviewRepository reviewRepository;

    // Java 21 Record yapısı: Deterministik verileri tutmak için
    record Profile(double expectedScore, int taste, int speed, int price, int ambience, String template) {}

    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepository.count() == 0) {
            log.info("Veritabanı boş. Deterministik veri üretimi başlıyor...");
            Faker faker = new Faker();
            String[] cuisines = {"Italian", "Turkish", "Asian", "Mexican", "Seafood", "Steakhouse"};

            // LLM'in kesin değerler çıkarması için manipüle edilmiş (kalibre) profiller
            List<Profile> profiles = List.of(
                    // Ortalaması tam 4.75 olan profil
                    new Profile(4.8, 5, 5, 4, 5, "Amazing experience! Taste: %d/5. Speed: %d/5. Price is fair: %d/5. Ambience is perfect: %d/5."),
                    // Ortalaması tam 3.5 olan profil
                    new Profile(3.5, 3, 4, 3, 4, "Average dining. Taste is %d/5. Speed was %d/5. Price: %d/5. Ambience: %d/5."),
                    // Ortalaması tam 2.0 olan profil
                    new Profile(2.0, 2, 2, 2, 2, "Disappointing. Taste: %d/5. Speed: %d/5. Price: %d/5. Ambience: %d/5.")
            );

            for (int i = 0; i < 20; i++) {
                Profile profile = profiles.get(faker.random().nextInt(profiles.size()));

                Restaurant restaurant = Restaurant.builder()
                        .name(faker.restaurant().name())
                        .address(faker.address().fullAddress())
                        .phoneNumber(faker.phoneNumber().cellPhone())
                        .cuisineType(cuisines[faker.random().nextInt(cuisines.length)])
                        .aiSentimentScore(profile.expectedScore()) // Arayüzde görünecek statik puan
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                restaurant = restaurantRepository.save(restaurant);

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

                for (int k = 0; k < 15; k++) {
                    // LLM'in tereddütsüz parsing yapmasını sağlayan sentetik yorum
                    String content = String.format(profile.template(), profile.taste(), profile.speed(), profile.price(), profile.ambience());

                    Review review = Review.builder()
                            .restaurant(restaurant)
                            .reviewerName(faker.name().fullName())
                            .content(content)
                            .sentimentLabel(profile.expectedScore() > 4 ? "POSITIVE" : profile.expectedScore() > 3 ? "NEUTRAL" : "NEGATIVE")
                            .sentimentScore(profile.expectedScore())
                            .createdAt(LocalDateTime.now())
                            .build();
                    reviewRepository.save(review);
                }
            }
            log.info("Deterministik veri üretimi başarıyla tamamlandı!");
        }
    }
}