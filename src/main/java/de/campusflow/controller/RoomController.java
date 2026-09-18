package de.campusflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    @GetMapping("/rooms")
    public String getRooms() {
        return "Raumliste";
    }
}