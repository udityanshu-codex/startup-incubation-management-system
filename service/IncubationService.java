package service;

import dao.IncubationDAO;
import model.IncubationApplication;
import model.IncubationProgram;

import java.sql.SQLException;
import java.util.List;

public class IncubationService {

    private final IncubationDAO incubationDAO = new IncubationDAO();

    public List<IncubationProgram> getPrograms() throws SQLException {
        return incubationDAO.getAllPrograms();
    }

    public IncubationApplication applyForIncubation(int startupId, Integer programId, String pitchUrl, String objectives) throws Exception {
        if (startupId <= 0) {
            throw new IllegalArgumentException("Valid startup ID is required to apply.");
        }
        if (objectives == null || objectives.trim().isEmpty()) {
            throw new IllegalArgumentException("Objectives / Milestones cannot be empty.");
        }

        IncubationApplication app = new IncubationApplication();
        app.setStartupId(startupId);
        app.setProgramId(programId);
        app.setPitchDeckUrl(pitchUrl != null ? pitchUrl.trim() : "");
        app.setObjectives(objectives.trim());
        app.setStatus("Under Review");

        return incubationDAO.createApplication(app);
    }

    public List<IncubationApplication> getApplicationsForFounder(int founderId) throws SQLException {
        return incubationDAO.getApplicationsByFounderId(founderId);
    }

    public List<IncubationApplication> getAllApplications() throws SQLException {
        return incubationDAO.getAllApplications();
    }

    public boolean decideApplication(int applicationId, String status, String feedback) throws SQLException {
        return incubationDAO.updateApplicationDecision(applicationId, status, feedback != null ? feedback.trim() : "");
    }

    public boolean decideApplication(int applicationId, String status, String feedback, Integer adminId, String adminName) throws SQLException {
        return incubationDAO.updateApplicationDecision(applicationId, status, feedback != null ? feedback.trim() : "", adminId, adminName);
    }
}
