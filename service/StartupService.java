package service;

import dao.StartupDAO;
import model.Startup;

import java.sql.SQLException;
import java.util.List;

public class StartupService {

    private final StartupDAO startupDAO = new StartupDAO();

    public Startup getStartupForFounder(int founderId) throws SQLException {
        Startup startup = startupDAO.findByFounderId(founderId);
        if (startup == null) {
            // Auto-create a default placeholder startup if none exists for this founder
            startup = new Startup(founderId, "My New Startup", "Technology", "Add your startup description here.");
            startup = startupDAO.createStartup(startup);
        }
        return startup;
    }

    public boolean updateStartup(Startup startup) throws Exception {
        if (startup.getName() == null || startup.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Startup name cannot be empty.");
        }
        return startupDAO.updateStartup(startup);
    }

    public List<Startup> getAllStartups() throws SQLException {
        return startupDAO.getAllStartups();
    }

    public boolean updateStartupStatus(int startupId, String status) throws SQLException {
        return startupDAO.updateStatus(startupId, status);
    }

    public boolean updateStartupStatus(int startupId, String status, Integer adminId, String adminName, String remarks) throws SQLException {
        return startupDAO.updateStatus(startupId, status, adminId, adminName, remarks);
    }
}
