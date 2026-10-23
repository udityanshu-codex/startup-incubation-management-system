package model;

import java.sql.Timestamp;

public class FundingOpportunity {
    private int id;
    private String title;
    private double maxAmount;
    private String deadline;
    private String description;
    private String status; // "OPEN", "CLOSED"
    private Timestamp createdAt;

    public FundingOpportunity() {}

    public FundingOpportunity(int id, String title, double maxAmount, String deadline, String description, String status, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.maxAmount = maxAmount;
        this.deadline = deadline;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public FundingOpportunity(String title, double maxAmount, String deadline, String description) {
        this.title = title;
        this.maxAmount = maxAmount;
        this.deadline = deadline;
        this.description = description;
        this.status = "OPEN";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(double maxAmount) { this.maxAmount = maxAmount; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return title + " (Up to ₹" + String.format("%,.0f", maxAmount) + ")";
    }
}
