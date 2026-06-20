package com.dinelock.entity;
import jakarta.persistence.*;
import lombok.*; import java.time.LocalDateTime;
@Entity
@Table(name = "reservations") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString @EqualsAndHashCode public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String customerContact;

    /**
     * Reservation start time (required to be LocalDateTime per constraints).
     */
    private LocalDateTime startTime;

    /**
     * Reservation end time (also LocalDateTime).
     */
    private LocalDateTime endTime;

    /**
     * Number of guests for this reservation.
     */
    private Integer numberOfGuests;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RestaurantTable restaurantTable;

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }
}