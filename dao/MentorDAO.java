package dao;

import model.Mentor;
import model.MentorshipRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MentorDAO {

    public Mentor createMentor(Mentor mentor) throws SQLException {
        String sql = "INSERT INTO mentors (name, expertise, email, phone, bio, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, mentor.getName());
            ps.setString(2, mentor.getExpertise());
            ps.setString(3, mentor.getEmail());
            ps.setString(4, mentor.getPhone());
            ps.setString(5, mentor.getBio());
            ps.setString(6, mentor.getStatus() != null ? mentor.getStatus() : "Active");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                mentor.setId(rs.getInt(1));
            }
            return mentor;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Mentor> getAllMentors() throws SQLException {
        String sql = "SELECT * FROM mentors ORDER BY id ASC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Mentor> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapMentor(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Mentor> getActiveMentors() throws SQLException {
        String sql = "SELECT * FROM mentors WHERE LOWER(status) = 'active' ORDER BY id ASC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Mentor> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapMentor(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public boolean updateMentorStatus(int mentorId, String status) throws SQLException {
        String sql = "UPDATE mentors SET status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, mentorId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public boolean deleteMentor(int mentorId) throws SQLException {
        String sql = "DELETE FROM mentors WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, mentorId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public MentorshipRequest createMentorshipRequest(MentorshipRequest req) throws SQLException {
        String sql = "INSERT INTO mentorship_requests (startup_id, mentor_id, topic, notes, status, admin_remarks) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, req.getStartupId());
            if (req.getMentorId() != null && req.getMentorId() > 0) {
                ps.setInt(2, req.getMentorId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, req.getTopic());
            ps.setString(4, req.getNotes());
            ps.setString(5, req.getStatus() != null ? req.getStatus() : "Pending");
            ps.setString(6, req.getAdminRemarks());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                req.setId(rs.getInt(1));
            }
            return req;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<MentorshipRequest> getRequestsByFounderId(int founderId) throws SQLException {
        String sql = "SELECT r.*, s.name AS startup_name, m.name AS mentor_name " +
                "FROM mentorship_requests r " +
                "JOIN startups s ON r.startup_id = s.id " +
                "LEFT JOIN mentors m ON r.mentor_id = m.id " +
                "WHERE s.founder_id = ? " +
                "ORDER BY r.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<MentorshipRequest> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, founderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<MentorshipRequest> getAllMentorshipRequests() throws SQLException {
        String sql = "SELECT r.*, s.name AS startup_name, m.name AS mentor_name " +
                "FROM mentorship_requests r " +
                "JOIN startups s ON r.startup_id = s.id " +
                "LEFT JOIN mentors m ON r.mentor_id = m.id " +
                "ORDER BY r.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<MentorshipRequest> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public boolean assignMentorToRequest(int requestId, int mentorId, String status, String remarks) throws SQLException {
        String sql = "UPDATE mentorship_requests SET mentor_id = ?, status = ?, admin_remarks = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, mentorId);
            ps.setString(2, status != null ? status : "Assigned");
            ps.setString(3, remarks);
            ps.setInt(4, requestId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    private Mentor mapMentor(ResultSet rs) throws SQLException {
        return new Mentor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("expertise"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("bio"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }

    private MentorshipRequest mapRequest(ResultSet rs) throws SQLException {
        return new MentorshipRequest(
                rs.getInt("id"),
                rs.getInt("startup_id"),
                rs.getString("startup_name"),
                rs.getInt("mentor_id") == 0 && rs.wasNull() ? null : rs.getInt("mentor_id"),
                rs.getString("mentor_name"),
                rs.getString("topic"),
                rs.getString("notes"),
                rs.getString("status"),
                rs.getString("admin_remarks"),
                rs.getTimestamp("request_date")
        );
    }
}
