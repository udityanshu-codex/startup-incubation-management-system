package dao;

import model.FundingApplication;
import model.FundingOpportunity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FundingDAO {

    public List<FundingOpportunity> getAllOpportunities() throws SQLException {
        String sql = "SELECT * FROM funding_opportunities WHERE status = 'OPEN' ORDER BY id ASC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FundingOpportunity> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new FundingOpportunity(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("max_amount"),
                        rs.getString("deadline"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                ));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public FundingOpportunity createOpportunity(FundingOpportunity opp) throws SQLException {
        String sql = "INSERT INTO funding_opportunities (title, max_amount, deadline, description, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, opp.getTitle());
            ps.setDouble(2, opp.getMaxAmount());
            ps.setString(3, opp.getDeadline());
            ps.setString(4, opp.getDescription());
            ps.setString(5, opp.getStatus() != null ? opp.getStatus() : "OPEN");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                opp.setId(rs.getInt(1));
            }
            return opp;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public FundingApplication createApplication(FundingApplication app) throws SQLException {
        String sql = "INSERT INTO funding_applications (startup_id, opportunity_id, amount_requested, amount_approved, proposal, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, app.getStartupId());
            if (app.getOpportunityId() != null && app.getOpportunityId() > 0) {
                ps.setInt(2, app.getOpportunityId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, app.getAmountRequested());
            ps.setDouble(4, 0.0);
            ps.setString(5, app.getProposal());
            ps.setString(6, app.getStatus() != null ? app.getStatus() : "Under Review");
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

    public List<FundingApplication> getApplicationsByStartupId(int startupId) throws SQLException {
        String sql = "SELECT f.*, s.name AS startup_name, o.title AS opportunity_title " +
                "FROM funding_applications f " +
                "JOIN startups s ON f.startup_id = s.id " +
                "LEFT JOIN funding_opportunities o ON f.opportunity_id = o.id " +
                "WHERE f.startup_id = ? " +
                "ORDER BY f.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FundingApplication> list = new ArrayList<>();
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

    public List<FundingApplication> getApplicationsByFounderId(int founderId) throws SQLException {
        String sql = "SELECT f.*, s.name AS startup_name, o.title AS opportunity_title " +
                "FROM funding_applications f " +
                "JOIN startups s ON f.startup_id = s.id " +
                "LEFT JOIN funding_opportunities o ON f.opportunity_id = o.id " +
                "WHERE s.founder_id = ? " +
                "ORDER BY f.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FundingApplication> list = new ArrayList<>();
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

    public List<FundingApplication> getAllApplications() throws SQLException {
        String sql = "SELECT f.*, s.name AS startup_name, o.title AS opportunity_title " +
                "FROM funding_applications f " +
                "JOIN startups s ON f.startup_id = s.id " +
                "LEFT JOIN funding_opportunities o ON f.opportunity_id = o.id " +
                "ORDER BY f.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FundingApplication> list = new ArrayList<>();
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

    public boolean updateApplicationDecision(int applicationId, String status, double amountApproved, String feedback) throws SQLException {
        String sql = "UPDATE funding_applications SET status = ?, amount_approved = ?, admin_feedback = ?, decided_date = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setDouble(2, amountApproved);
            ps.setString(3, feedback);
            ps.setInt(4, applicationId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public double getTotalFundingApproved() throws SQLException {
        String sql = "SELECT SUM(amount_approved) FROM funding_applications WHERE status = 'Approved'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    private FundingApplication mapApplication(ResultSet rs) throws SQLException {
        return new FundingApplication(
                rs.getInt("id"),
                rs.getInt("startup_id"),
                rs.getString("startup_name"),
                rs.getInt("opportunity_id") == 0 && rs.wasNull() ? null : rs.getInt("opportunity_id"),
                rs.getString("opportunity_title"),
                rs.getDouble("amount_requested"),
                rs.getDouble("amount_approved"),
                rs.getString("proposal"),
                rs.getString("status"),
                rs.getString("admin_feedback"),
                rs.getTimestamp("applied_date"),
                rs.getTimestamp("decided_date")
        );
    }
}
