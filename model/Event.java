package model;

import java.sql.Timestamp;

public class Event {
    private int id;
    private String title;
    private String eventDate;
    private String venue;
    private int capacity;
    private int registeredCount;
    private String description;
    private String status; // "UPCOMING", "COMPLETED", "CANCELLED"
    private Timestamp createdAt;

    public Event() {}

    public Event(int id, String title, String eventDate, String venue, int capacity,
                 int registeredCount, String description, String status, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Event(String title, String eventDate, String venue, int capacity, String description) {
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.capacity = capacity;
        this.description = description;
        this.status = "UPCOMING";
        this.registeredCount = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isFull() {
        return registeredCount >= capacity;
    }
}
