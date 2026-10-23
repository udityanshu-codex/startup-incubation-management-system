package model;

import java.sql.Timestamp;

public class Mentor {
    private int id;
    private String name;
    private String expertise;
    private String email;
    private String phone;
    private String bio;
    private String status; // "Active", "Inactive"
    private Timestamp createdAt;

    public Mentor() {}

    public Mentor(int id, String name, String expertise, String email, String phone, String bio, String status, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.expertise = expertise;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Mentor(String name, String expertise, String email, String phone, String bio) {
        this.name = name;
        this.expertise = expertise;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.status = "Active";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + expertise + ")";
    }
}
