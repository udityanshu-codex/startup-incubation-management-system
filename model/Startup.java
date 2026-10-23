package model;

import java.sql.Timestamp;

public class Startup {
    private int id;
    private int founderId;
    private String founderName;
    private String name;
    private String category;
    private String description;
    private String stage; // "Ideation", "Validation", "Early Traction", "Scaling"
    private String status; // "Under Review", "Approved", "Rejected"
    private Integer reviewedByAdminId;
    private String reviewedByAdminName;
    private String adminRemarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Startup() {}

    public Startup(int id, int founderId, String founderName, String name, String category,
                   String description, String stage, String status, Timestamp createdAt, Timestamp updatedAt) {
        this(id, founderId, founderName, name, category, description, stage, status, null, null, null, createdAt, updatedAt);
    }

    public Startup(int id, int founderId, String founderName, String name, String category,
                   String description, String stage, String status, Integer reviewedByAdminId,
                   String reviewedByAdminName, String adminRemarks, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.founderId = founderId;
        this.founderName = founderName;
        this.name = name;
        this.category = category;
        this.description = description;
        this.stage = stage;
        this.status = status;
        this.reviewedByAdminId = reviewedByAdminId;
        this.reviewedByAdminName = reviewedByAdminName;
        this.adminRemarks = adminRemarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Startup(int founderId, String name, String category, String description) {
        this.founderId = founderId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.stage = "Ideation";
        this.status = "Under Review";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFounderId() { return founderId; }
    public void setFounderId(int founderId) { this.founderId = founderId; }

    public String getFounderName() { return founderName; }
    public void setFounderName(String founderName) { this.founderName = founderName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(Integer reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }

    public String getReviewedByAdminName() { return reviewedByAdminName; }
    public void setReviewedByAdminName(String reviewedByAdminName) { this.reviewedByAdminName = reviewedByAdminName; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
