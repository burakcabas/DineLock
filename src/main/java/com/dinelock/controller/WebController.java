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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final RestaurantQueryService restaurantQueryService;
    private final ReservationService reservationService;
    private final RestaurantTableRepository restaurantTableRepository;

    // 1. AŞAMA: Giriş (Landing) Sayfası
    @GetMapping("/")
    public String index() {
        return "landing"; // landing.html dosyasını çağıracak
    }

    // 2. AŞAMA: Mutfak Seçimine Göre Restoranları Listeleme
    @GetMapping("/restaurants")
    public String getRestaurantsByCuisine(@RequestParam(name = "cuisineType", defaultValue = "Italian") String cuisineType, Model model) {
        // Seçilen mutfağa göre restoranları getir ve modele ekle
        model.addAttribute("restaurants", restaurantQueryService.getTopRestaurantsByCuisine(cuisineType));
        model.addAttribute("selectedCuisine", cuisineType);
        return "dashboard"; // dashboard.html dosyasını çağıracak
    }

    // 3. AŞAMA: Rezervasyon İşlemi (Race Condition Test Noktası)
    @PostMapping("/reserve")
    public String reserveTable(@RequestParam Long tableId,
                               @RequestParam String customerName,
                               @RequestParam LocalDateTime startTime,
                               @RequestParam LocalDateTime endTime,
                               @RequestParam Integer numberOfGuests,
                               @RequestParam String selectedCuisine, // Hangi mutfaktan geldiğini hatırlamak için
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
            // Başarılı olursa aynı mutfağa başarı mesajıyla dön
            redirectAttributes.addAttribute("cuisineType", selectedCuisine);
            return "redirect:/restaurants?success=true";

        } catch (ReservationConflictException e) {
            // Çakışma olursa aynı mutfağa hata mesajıyla dön
            redirectAttributes.addAttribute("cuisineType", selectedCuisine);
            return "redirect:/restaurants?error=conflict";
        }
    }
}