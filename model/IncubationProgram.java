package model;

public class IncubationProgram {
    private int id;
    private String title;
    private String duration;
    private String description;
    private String eligibility;
    private String status; // "ACTIVE", "CLOSED"

    public IncubationProgram() {}

    public IncubationProgram(int id, String title, String duration, String description, String eligibility, String status) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.description = description;
        this.eligibility = eligibility;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return title + (duration != null ? " (" + duration + ")" : "");
    }
}
