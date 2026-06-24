package com.dinelock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Yorumu yapan kullanıcının adı (Sentetik veri ile dolacak)
    private String reviewerName;

    // Yorumun tam metni
    @Column(columnDefinition = "TEXT")
    private String content;

    // AI'ın belirleyeceği duygu durumu (Örn: POSITIVE, NEGATIVE, NEUTRAL)
    private String sentimentLabel;

    // AI'ın yorum için verdiği 1 ile 5 arası spesifik puan
    private Double sentimentScore;

    // Yorumun hangi restorana ait olduğunu belirten ilişki
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    private LocalDateTime createdAt;
}