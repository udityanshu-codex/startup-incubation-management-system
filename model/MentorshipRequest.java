package model;

import java.sql.Timestamp;

public class MentorshipRequest {
    private int id;
    private int startupId;
    private String startupName;
    private Integer mentorId;
    private String mentorName;
    private String topic;
    private String notes;
    private String status; // "Pending", "Assigned", "Completed", "Rejected"
    private String adminRemarks;
    private Timestamp requestDate;

    public MentorshipRequest() {}

    public MentorshipRequest(int id, int startupId, String startupName, Integer mentorId, String mentorName,
                             String topic, String notes, String status, String adminRemarks, Timestamp requestDate) {
        this.id = id;
        this.startupId = startupId;
        this.startupName = startupName;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.topic = topic;
        this.notes = notes;
        this.status = status;
        this.adminRemarks = adminRemarks;
        this.requestDate = requestDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStartupId() { return startupId; }
    public void setStartupId(int startupId) { this.startupId = startupId; }

    public String getStartupName() { return startupName; }
    public void setStartupName(String startupName) { this.startupName = startupName; }

    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }

    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
}
