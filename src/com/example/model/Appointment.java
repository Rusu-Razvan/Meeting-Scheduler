package com.example.model;

import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int creatorId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    // + getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
