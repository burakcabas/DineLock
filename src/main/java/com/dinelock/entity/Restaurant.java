package com.dinelock.entity;
import jakarta.persistence.*;
import lombok.*; import java.time.LocalDateTime; import java.util.ArrayList; import java.util.List;
@Entity
@Table(name = "restaurants") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString @EqualsAndHashCode public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private String phoneNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Restoranın mutfak türü (Örn: "İtalyan", "Asya", "Türk")
    private String cuisineType;

    // AI tarafından yorumlara bakılarak hesaplanan ortalama güvenilirlik puanı (1.0 - 5.0)
    private Double aiSentimentScore;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<RestaurantTable> tables = new ArrayList<>();
}