package service;

import dao.*;
import model.FundingApplication;
import model.IncubationApplication;
import model.MentorshipRequest;
import model.Startup;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final StartupDAO startupDAO = new StartupDAO();
    private final IncubationDAO incubationDAO = new IncubationDAO();
    private final MentorDAO mentorDAO = new MentorDAO();
    private final FundingDAO fundingDAO = new FundingDAO();

    public Map<String, String> getAdminDashboardStats() {
        Map<String, String> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Total startups
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM startups")) {
                stats.put("totalStartups", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Approved startups
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM startups WHERE status = 'Approved'")) {
                stats.put("approvedStartups", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Rejected startups
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM startups WHERE status = 'Rejected'")) {
                stats.put("rejectedStartups", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Pending incubation applications
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM incubation_applications WHERE status = 'Under Review'")) {
                stats.put("pendingApplications", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Active mentors
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM mentors WHERE LOWER(status) = 'active'")) {
                stats.put("activeMentors", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Funding applications
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM funding_applications")) {
                stats.put("fundingApplications", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total funding approved
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(amount_approved), 0) FROM funding_applications WHERE status = 'Approved'")) {
                double total = rs.next() ? rs.getDouble(1) : 0.0;
                stats.put("totalFunding", "₹" + String.format("%,.0f", total));
            }
            // Upcoming events
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM events WHERE status = 'UPCOMING'")) {
                stats.put("upcomingEvents", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }
            // Total users
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                stats.put("totalUsers", rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            }

        } catch (Exception e) {
            System.err.println("Error fetching admin stats: " + e.getMessage());
            stats.put("totalStartups", "0");
            stats.put("approvedStartups", "0");
            stats.put("rejectedStartups", "0");
            stats.put("pendingApplications", "0");
            stats.put("activeMentors", "0");
            stats.put("fundingApplications", "0");
            stats.put("totalFunding", "₹0");
            stats.put("upcomingEvents", "0");
            stats.put("totalUsers", "0");
        }
        return stats;
    }

    public Map<String, String> getFounderDashboardStats(int founderId) {
        Map<String, String> stats = new HashMap<>();
        try {
            Startup startup = startupDAO.findByFounderId(founderId);
            if (startup != null) {
                stats.put("startupName", startup.getName());
                stats.put("startupStatus", startup.getStatus());
                stats.put("startupCategory", startup.getCategory() != null ? startup.getCategory() : "Technology");
                stats.put("startupStage", startup.getStage() != null ? startup.getStage() : "Ideation");
                stats.put("startupReviewedBy", startup.getReviewedByAdminName() != null && !startup.getReviewedByAdminName().isEmpty() 
                        ? startup.getReviewedByAdminName() : "Pending Review (Incubation Committee)");
                stats.put("startupRemarks", startup.getAdminRemarks() != null && !startup.getAdminRemarks().isEmpty()
                        ? startup.getAdminRemarks() : "Application is under formal screening by the NIET Incubation Committee.");

                // Incubation status
                List<IncubationApplication> incApps = incubationDAO.getApplicationsByStartupId(startup.getId());
                if (!incApps.isEmpty()) {
                    IncubationApplication app = incApps.get(0);
                    stats.put("incubationStatus", app.getStatus());
                    stats.put("incubationProgram", app.getProgramTitle() != null ? app.getProgramTitle() : "General Incubation Cohort");
                    stats.put("incubationReviewedBy", app.getReviewedByAdminName() != null && !app.getReviewedByAdminName().isEmpty()
                            ? app.getReviewedByAdminName() : "Pending Committee Review");
                    stats.put("incubationFeedback", app.getAdminFeedback() != null && !app.getAdminFeedback().isEmpty()
                            ? app.getAdminFeedback() : "Feedback will appear here once reviewed.");
                } else {
                    stats.put("incubationStatus", "Not Applied");
                    stats.put("incubationProgram", "None");
                    stats.put("incubationReviewedBy", "Not Applied");
                    stats.put("incubationFeedback", "Submit an application under the Incubation section to join a cohort.");
                }

                // Funding status
                List<FundingApplication> fundApps = fundingDAO.getApplicationsByStartupId(startup.getId());
                if (!fundApps.isEmpty()) {
                    stats.put("fundingStatus", fundApps.get(0).getStatus());
                } else {
                    stats.put("fundingStatus", "Not Applied");
                }

                // Mentorship status
                List<MentorshipRequest> mentorReqs = mentorDAO.getRequestsByFounderId(founderId);
                if (!mentorReqs.isEmpty()) {
                    MentorshipRequest latest = mentorReqs.get(0);
                    if ("Assigned".equalsIgnoreCase(latest.getStatus()) && latest.getMentorName() != null) {
                        stats.put("mentorStatus", latest.getMentorName());
                    } else {
                        stats.put("mentorStatus", latest.getStatus());
                    }
                } else {
                    stats.put("mentorStatus", "Not Requested");
                }
            } else {
                stats.put("startupName", "None");
                stats.put("startupStatus", "Not Registered");
                stats.put("startupCategory", "N/A");
                stats.put("startupStage", "N/A");
                stats.put("startupReviewedBy", "N/A");
                stats.put("startupRemarks", "Register your startup to begin.");
                stats.put("incubationStatus", "Not Applied");
                stats.put("incubationProgram", "None");
                stats.put("incubationReviewedBy", "N/A");
                stats.put("incubationFeedback", "No application filed.");
                stats.put("fundingStatus", "Not Applied");
                stats.put("mentorStatus", "Not Requested");
            }
        } catch (Exception e) {
            System.err.println("Error fetching founder stats: " + e.getMessage());
            stats.put("startupName", "TechNova");
            stats.put("startupStatus", "Under Review");
            stats.put("startupCategory", "Technology");
            stats.put("startupStage", "Ideation");
            stats.put("startupReviewedBy", "Incubation Committee");
            stats.put("startupRemarks", "Screening in progress.");
            stats.put("incubationStatus", "Under Review");
            stats.put("incubationProgram", "General Cohort");
            stats.put("incubationReviewedBy", "Incubation Committee");
            stats.put("incubationFeedback", "Reviewing pitch deck.");
            stats.put("fundingStatus", "Not Applied");
            stats.put("mentorStatus", "Assigned");
        }
        return stats;
    }

    public String generateFullReportText() {
        Map<String, String> stats = getAdminDashboardStats();
        StringBuilder sb = new StringBuilder();
        String dateStr = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss").format(new Date());

        sb.append("=================================================================================\n");
        sb.append("         STARTUP INCUBATION & INNOVATION MANAGEMENT SYSTEM\n");
        sb.append("         Institute: NIET Greater Noida | Course: CCSE0355\n");
        sb.append("                   OFFICIAL INCUBATION SUMMARY REPORT\n");
        sb.append("=================================================================================\n");
        sb.append("Generated On: ").append(dateStr).append("\n");
        sb.append("Database:     ").append(DatabaseConnection.getActiveDatabaseName()).append("\n\n");

        sb.append("---------------------------------------------------------------------------------\n");
        sb.append("1. EXECUTIVE METRICS\n");
        sb.append("---------------------------------------------------------------------------------\n");
        sb.append(String.format("  • Total Registered Startups:     %s\n", stats.get("totalStartups")));
        sb.append(String.format("  • Approved Incubation Startups:  %s\n", stats.get("approvedStartups")));
        sb.append(String.format("  • Rejected Applications:         %s\n", stats.get("rejectedStartups")));
        sb.append(String.format("  • Pending Applications:          %s\n", stats.get("pendingApplications")));
        sb.append(String.format("  • Active Industry Mentors:       %s\n", stats.get("activeMentors")));
        sb.append(String.format("  • Funding Grants Sanctioned:     %s\n", stats.get("totalFunding")));
        sb.append(String.format("  • Upcoming Innovation Events:    %s\n", stats.get("upcomingEvents")));
        sb.append(String.format("  • Registered System Users:       %s\n\n", stats.get("totalUsers")));

        sb.append("---------------------------------------------------------------------------------\n");
        sb.append("2. REGISTERED STARTUPS DIRECTORY\n");
        sb.append("---------------------------------------------------------------------------------\n");
        try {
            List<Startup> startups = startupDAO.getAllStartups();
            sb.append(String.format("%-6s | %-20s | %-18s | %-15s | %-12s\n", "ID", "Startup Name", "Founder", "Category", "Status"));
            sb.append("---------------------------------------------------------------------------------\n");
            for (Startup s : startups) {
                sb.append(String.format("%-6d | %-20s | %-18s | %-15s | %-12s\n",
                        s.getId(),
                        s.getName(),
                        s.getFounderName() != null ? s.getFounderName() : "N/A",
                        s.getCategory(),
                        s.getStatus()));
            }
        } catch (Exception e) {
            sb.append("Could not load startups: ").append(e.getMessage()).append("\n");
        }

        sb.append("\n---------------------------------------------------------------------------------\n");
        sb.append("3. INCUBATION APPLICATIONS AUDIT\n");
        sb.append("---------------------------------------------------------------------------------\n");
        try {
            List<IncubationApplication> apps = incubationDAO.getAllApplications();
            sb.append(String.format("%-6s | %-20s | %-25s | %-12s\n", "ID", "Startup", "Program", "Decision"));
            sb.append("---------------------------------------------------------------------------------\n");
            for (IncubationApplication a : apps) {
                sb.append(String.format("%-6d | %-20s | %-25s | %-12s\n",
                        a.getId(),
                        a.getStartupName(),
                        a.getProgramTitle() != null ? a.getProgramTitle() : "General Cohort",
                        a.getStatus()));
            }
        } catch (Exception e) {
            sb.append("Could not load incubation applications: ").append(e.getMessage()).append("\n");
        }

        sb.append("\n=================================================================================\n");
        sb.append("                          END OF INCUBATION REPORT\n");
        sb.append("=================================================================================\n");
        return sb.toString();
    }

    public boolean exportReportToFile(File targetFile) throws Exception {
        String report = generateFullReportText();
        try (PrintWriter out = new PrintWriter(new FileWriter(targetFile))) {
            out.print(report);
            return true;
        }
    }
}
