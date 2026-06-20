package com.dinelock.service;
import com.dinelock.entity.Reservation; import com.dinelock.exception.ReservationConflictException; import com.dinelock.repository.ReservationRepository; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        Long tableId = reservation.getRestaurantTable().getId();
        if (reservationRepository.findConflictingReservationForUpdate(
                tableId,
                reservation.getStartTime(),
                reservation.getEndTime()
        ).isPresent()) {
            throw new ReservationConflictException(
                    "Reservation conflict: the requested time slot overlaps with an existing reservation for table id " + tableId + "."
            );
        }

        return reservationRepository.save(reservation);
    }
}