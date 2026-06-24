package com.dinelock.controller;

import com.dinelock.entity.Reservation;
import com.dinelock.entity.RestaurantTable;
import com.dinelock.exception.ReservationConflictException;
import com.dinelock.repository.RestaurantTableRepository;
import com.dinelock.service.ReservationService;
import com.dinelock.service.RestaurantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final RestaurantQueryService restaurantQueryService;
    private final ReservationService reservationService;
    private final RestaurantTableRepository restaurantTableRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("restaurants", restaurantQueryService.getTopRestaurantsByCuisine("Italian"));
        return "index";
    }

    @PostMapping("/reserve")
    public String reserve(
            @RequestParam Long tableId,
            @RequestParam String customerName,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam Integer numberOfGuests
    ) {
        try {
            RestaurantTable restaurantTable = restaurantTableRepository.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant table not found with id: " + tableId));

            Reservation reservation = Reservation.builder()
                    .restaurantTable(restaurantTable)
                    .customerName(customerName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .numberOfGuests(numberOfGuests)
                    .status(Reservation.ReservationStatus.PENDING)
                    .build();

            reservationService.createReservation(reservation);
            return "redirect:/?success=true";
        } catch (ReservationConflictException ex) {
            return "redirect:/?error=conflict";
        }
    }
}


