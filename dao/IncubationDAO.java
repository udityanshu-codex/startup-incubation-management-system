package dao;

import model.IncubationApplication;
import model.IncubationProgram;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncubationDAO {

    public List<IncubationProgram> getAllPrograms() throws SQLException {
        String sql = "SELECT * FROM incubation_programs WHERE status = 'ACTIVE' ORDER BY id ASC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<IncubationProgram> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new IncubationProgram(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("duration"),
                        rs.getString("description"),
                        rs.getString("eligibility"),
                        rs.getString("status")
                ));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public IncubationProgram createProgram(IncubationProgram program) throws SQLException {
        String sql = "INSERT INTO incubation_programs (title, duration, description, eligibility, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, program.getTitle());
            ps.setString(2, program.getDuration());
            ps.setString(3, program.getDescription());
            ps.setString(4, program.getEligibility());
            ps.setString(5, program.getStatus() != null ? program.getStatus() : "ACTIVE");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                program.setId(rs.getInt(1));
            }
            return program;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public IncubationApplication createApplication(IncubationApplication app) throws SQLException {
        String sql = "INSERT INTO incubation_applications (startup_id, program_id, pitch_deck_url, objectives, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, app.getStartupId());
            if (app.getProgramId() != null && app.getProgramId() > 0) {
                ps.setInt(2, app.getProgramId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, app.getPitchDeckUrl());
            ps.setString(4, app.getObjectives());
            ps.setString(5, app.getStatus() != null ? app.getStatus() : "Under Review");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                app.setId(rs.getInt(1));
            }
            return app;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<IncubationApplication> getApplicationsByStartupId(int startupId) throws SQLException {
        String sql = "SELECT a.*, s.name AS startup_name, p.title AS program_title " +
                "FROM incubation_applications a " +
                "JOIN startups s ON a.startup_id = s.id " +
                "LEFT JOIN incubation_programs p ON a.program_id = p.id " +
                "WHERE a.startup_id = ? " +
                "ORDER BY a.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<IncubationApplication> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, startupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapApplication(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<IncubationApplication> getApplicationsByFounderId(int founderId) throws SQLException {
        String sql = "SELECT a.*, s.name AS startup_name, p.title AS program_title " +
                "FROM incubation_applications a " +
                "JOIN startups s ON a.startup_id = s.id " +
                "LEFT JOIN incubation_programs p ON a.program_id = p.id " +
                "WHERE s.founder_id = ? " +
                "ORDER BY a.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<IncubationApplication> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, founderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapApplication(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<IncubationApplication> getAllApplications() throws SQLException {
        String sql = "SELECT a.*, s.name AS startup_name, p.title AS program_title " +
                "FROM incubation_applications a " +
                "JOIN startups s ON a.startup_id = s.id " +
                "LEFT JOIN incubation_programs p ON a.program_id = p.id " +
                "ORDER BY a.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<IncubationApplication> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapApplication(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public boolean updateApplicationDecision(int applicationId, String status, String feedback) throws SQLException {
        return updateApplicationDecision(applicationId, status, feedback, null, null);
    }

    public boolean updateApplicationDecision(int applicationId, String status, String feedback, Integer adminId, String adminName) throws SQLException {
        String sql = "UPDATE incubation_applications SET status = ?, admin_feedback = ?, reviewed_by_admin_id = ?, reviewed_by_admin_name = ?, decided_date = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, feedback);
            if (adminId != null) {
                ps.setInt(3, adminId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, adminName);
            ps.setInt(5, applicationId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    private IncubationApplication mapApplication(ResultSet rs) throws SQLException {
        Integer adminId = null;
        try {
            int val = rs.getInt("reviewed_by_admin_id");
            if (!rs.wasNull()) adminId = val;
        } catch (Exception ignored) {}
        String adminName = null;
        try { adminName = rs.getString("reviewed_by_admin_name"); } catch (Exception ignored) {}

        return new IncubationApplication(
                rs.getInt("id"),
                rs.getInt("startup_id"),
                rs.getString("startup_name"),
                rs.getInt("program_id") == 0 && rs.wasNull() ? null : rs.getInt("program_id"),
                rs.getString("program_title"),
                rs.getString("pitch_deck_url"),
                rs.getString("objectives"),
                rs.getString("status"),
                rs.getString("admin_feedback"),
                adminId,
                adminName,
                rs.getTimestamp("applied_date"),
                rs.getTimestamp("decided_date")
        );
    }
}
