package com.dinelock.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*; import java.time.LocalDateTime; import java.util.ArrayList; import java.util.List;
@Entity @Table(name = "restaurant_tables") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString @EqualsAndHashCode public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Logical table number or identifier (e.g., "T1", or numeric index).
     */
    private String tableNumber;

    /**
     * Seating capacity for this table.
     */
    private Integer capacity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Restaurant restaurant;

    @OneToMany(mappedBy = "restaurantTable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}