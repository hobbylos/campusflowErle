package de.campusflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class RolevalidatorController {
    
    @GetMapping("/rolevalidator")
    public String getValidator() {
        return "RolevalidatorListe";
    }
}
