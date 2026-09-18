package de.campusflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingController {

    @GetMapping("/bookings")
    public String getBookings() {
        return "Buchungsliste";
    }
}