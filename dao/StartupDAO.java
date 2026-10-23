package dao;

import model.Startup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StartupDAO {

    public Startup createStartup(Startup startup) throws SQLException {
        String sql = "INSERT INTO startups (founder_id, name, category, description, stage, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, startup.getFounderId());
            ps.setString(2, startup.getName());
            ps.setString(3, startup.getCategory());
            ps.setString(4, startup.getDescription());
            ps.setString(5, startup.getStage() != null ? startup.getStage() : "Ideation");
            ps.setString(6, startup.getStatus() != null ? startup.getStatus() : "Under Review");

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                startup.setId(rs.getInt(1));
            }
            return startup;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public Startup findByFounderId(int founderId) throws SQLException {
        String sql = "SELECT s.*, u.full_name AS founder_name " +
                "FROM startups s " +
                "JOIN users u ON s.founder_id = u.id " +
                "WHERE s.founder_id = ? " +
                "ORDER BY s.id DESC LIMIT 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, founderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapStartup(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public Startup findById(int id) throws SQLException {
        String sql = "SELECT s.*, u.full_name AS founder_name " +
                "FROM startups s " +
                "JOIN users u ON s.founder_id = u.id " +
                "WHERE s.id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapStartup(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Startup> getAllStartups() throws SQLException {
        String sql = "SELECT s.*, u.full_name AS founder_name " +
                "FROM startups s " +
                "JOIN users u ON s.founder_id = u.id " +
                "ORDER BY s.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Startup> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapStartup(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public boolean updateStartup(Startup startup) throws SQLException {
        String sql = "UPDATE startups SET name = ?, category = ?, description = ?, stage = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, startup.getName());
            ps.setString(2, startup.getCategory());
            ps.setString(3, startup.getDescription());
            ps.setString(4, startup.getStage());
            ps.setInt(5, startup.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public boolean updateStatus(int startupId, String status) throws SQLException {
        return updateStatus(startupId, status, null, null, null);
    }

    public boolean updateStatus(int startupId, String status, Integer adminId, String adminName, String remarks) throws SQLException {
        String sql = "UPDATE startups SET status = ?, reviewed_by_admin_id = ?, reviewed_by_admin_name = ?, admin_remarks = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            if (adminId != null) {
                ps.setInt(2, adminId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, adminName);
            ps.setString(4, remarks);
            ps.setInt(5, startupId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    private Startup mapStartup(ResultSet rs) throws SQLException {
        Integer adminId = null;
        try {
            int val = rs.getInt("reviewed_by_admin_id");
            if (!rs.wasNull()) adminId = val;
        } catch (Exception ignored) {}
        String adminName = null;
        try { adminName = rs.getString("reviewed_by_admin_name"); } catch (Exception ignored) {}
        String remarks = null;
        try { remarks = rs.getString("admin_remarks"); } catch (Exception ignored) {}

        return new Startup(
                rs.getInt("id"),
                rs.getInt("founder_id"),
                rs.getString("founder_name"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("stage"),
                rs.getString("status"),
                adminId,
                adminName,
                remarks,
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}
