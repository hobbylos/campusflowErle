package de.campusflow.model;

import java.time.LocalDateTime;

public class Booking {
    
    private Long id;
    private Long roomId;
    private String bookedBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
