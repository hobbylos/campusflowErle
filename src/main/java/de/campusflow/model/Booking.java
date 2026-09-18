package de.campusflow.model;

import java.time.LocalDateTime;

public class Booking {
    
    private Long id;
    private Long roomId;
    private String bookedBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getBookedBy() {
        return bookedBy;
    }
    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
