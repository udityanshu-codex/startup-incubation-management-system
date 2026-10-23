package model;

import java.sql.Timestamp;

public class IncubationApplication {
    private int id;
    private int startupId;
    private String startupName;
    private Integer programId;
    private String programTitle;
    private String pitchDeckUrl;
    private String objectives;
    private String status; // "Under Review", "Approved", "Rejected"
    private String adminFeedback;
    private Integer reviewedByAdminId;
    private String reviewedByAdminName;
    private Timestamp appliedDate;
    private Timestamp decidedDate;

    public IncubationApplication() {}

    public IncubationApplication(int id, int startupId, String startupName, Integer programId, String programTitle,
                                 String pitchDeckUrl, String objectives, String status, String adminFeedback,
                                 Timestamp appliedDate, Timestamp decidedDate) {
        this(id, startupId, startupName, programId, programTitle, pitchDeckUrl, objectives, status, adminFeedback,
                null, null, appliedDate, decidedDate);
    }

    public IncubationApplication(int id, int startupId, String startupName, Integer programId, String programTitle,
                                 String pitchDeckUrl, String objectives, String status, String adminFeedback,
                                 Integer reviewedByAdminId, String reviewedByAdminName,
                                 Timestamp appliedDate, Timestamp decidedDate) {
        this.id = id;
        this.startupId = startupId;
        this.startupName = startupName;
        this.programId = programId;
        this.programTitle = programTitle;
        this.pitchDeckUrl = pitchDeckUrl;
        this.objectives = objectives;
        this.status = status;
        this.adminFeedback = adminFeedback;
        this.reviewedByAdminId = reviewedByAdminId;
        this.reviewedByAdminName = reviewedByAdminName;
        this.appliedDate = appliedDate;
        this.decidedDate = decidedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStartupId() { return startupId; }
    public void setStartupId(int startupId) { this.startupId = startupId; }

    public String getStartupName() { return startupName; }
    public void setStartupName(String startupName) { this.startupName = startupName; }

    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }

    public String getProgramTitle() { return programTitle; }
    public void setProgramTitle(String programTitle) { this.programTitle = programTitle; }

    public String getPitchDeckUrl() { return pitchDeckUrl; }
    public void setPitchDeckUrl(String pitchDeckUrl) { this.pitchDeckUrl = pitchDeckUrl; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminFeedback() { return adminFeedback; }
    public void setAdminFeedback(String adminFeedback) { this.adminFeedback = adminFeedback; }

    public Integer getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(Integer reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }

    public String getReviewedByAdminName() { return reviewedByAdminName; }
    public void setReviewedByAdminName(String reviewedByAdminName) { this.reviewedByAdminName = reviewedByAdminName; }

    public Timestamp getAppliedDate() { return appliedDate; }
    public void setAppliedDate(Timestamp appliedDate) { this.appliedDate = appliedDate; }

    public Timestamp getDecidedDate() { return decidedDate; }
    public void setDecidedDate(Timestamp decidedDate) { this.decidedDate = decidedDate; }
}
