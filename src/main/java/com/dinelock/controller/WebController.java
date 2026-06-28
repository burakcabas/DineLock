package com.dinelock.controller;

import com.dinelock.entity.Reservation;
import com.dinelock.entity.RestaurantTable;
import com.dinelock.exception.ReservationConflictException;
import com.dinelock.repository.RestaurantTableRepository;
import com.dinelock.service.ReservationService;
import com.dinelock.service.RestaurantQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final RestaurantQueryService restaurantQueryService;
    private final ReservationService reservationService;
    private final RestaurantTableRepository restaurantTableRepository;

    @GetMapping("/")
    public String index() {
        return "landing";
    }

    @GetMapping("/restaurants")
    public String getRestaurantsByCuisine(@RequestParam(name = "cuisineType", defaultValue = "Italian") String cuisineType, Model model) {
        model.addAttribute("restaurants", restaurantQueryService.getTopRestaurantsByCuisine(cuisineType));
        model.addAttribute("selectedCuisine", cuisineType);
        return "dashboard";
    }

    @PostMapping("/reserve")
    public String reserveTable(@RequestParam Long tableId,
                               @RequestParam String customerName,
                               @RequestParam LocalDateTime startTime,
                               @RequestParam LocalDateTime endTime,
                               @RequestParam Integer numberOfGuests,
                               @RequestParam String selectedCuisine,
                               RedirectAttributes redirectAttributes) {
        try {
            RestaurantTable table = restaurantTableRepository.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException("Masa bulunamadı."));

            Reservation reservation = Reservation.builder()
                    .restaurantTable(table)
                    .customerName(customerName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .numberOfGuests(numberOfGuests)
                    .status(Reservation.ReservationStatus.valueOf("PENDING"))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            reservationService.createReservation(reservation);

            redirectAttributes.addAttribute("cuisineType", selectedCuisine);
            return "redirect:/restaurants?success=true";

        } catch (ReservationConflictException e) {
            // Tam eşzamanlılıktaki (Race Condition) kilitlenme hatalarını yakalar
            log.warn("Pessimistic Lock devreye girdi, çakışma önlendi: {}", e.getMessage());
            redirectAttributes.addAttribute("cuisineType", selectedCuisine);
            return "redirect:/restaurants?error=conflict";

        } catch (Exception e) {
            // Gecikmeli basımlarda ortaya çıkan standart doğrulama ve veritabanı hatalarını yakalar
            log.warn("Sıralı işlem veya doğrulama çakışması yakalandı: {}", e.getMessage());
            redirectAttributes.addAttribute("cuisineType", selectedCuisine);
            return "redirect:/restaurants?error=conflict";
        }
    }
}