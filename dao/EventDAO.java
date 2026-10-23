package dao;

import model.Event;
import model.EventRegistration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public Event createEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, event_date, venue, capacity, description, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getEventDate());
            ps.setString(3, event.getVenue());
            ps.setInt(4, event.getCapacity());
            ps.setString(5, event.getDescription());
            ps.setString(6, event.getStatus() != null ? event.getStatus() : "UPCOMING");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                event.setId(rs.getInt(1));
            }
            return event;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        String sql = "SELECT e.*, " +
                "(SELECT COUNT(*) FROM event_registrations r WHERE r.event_id = e.id) AS reg_count " +
                "FROM events e " +
                "ORDER BY e.id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Event> getUpcomingEvents() throws SQLException {
        String sql = "SELECT e.*, " +
                "(SELECT COUNT(*) FROM event_registrations r WHERE r.event_id = e.id) AS reg_count " +
                "FROM events e " +
                "WHERE e.status = 'UPCOMING' " +
                "ORDER BY e.id ASC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public boolean updateEvent(Event event) throws SQLException {
        String sql = "UPDATE events SET title = ?, event_date = ?, venue = ?, capacity = ?, description = ?, status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getEventDate());
            ps.setString(3, event.getVenue());
            ps.setInt(4, event.getCapacity());
            ps.setString(5, event.getDescription());
            ps.setString(6, event.getStatus());
            ps.setInt(7, event.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public boolean deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public boolean registerUser(int eventId, int userId) throws SQLException {
        String sql = "INSERT INTO event_registrations (event_id, user_id) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    public boolean isUserRegistered(int eventId, int userId) throws SQLException {
        String sql = "SELECT id FROM event_registrations WHERE event_id = ? AND user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public List<Integer> getRegisteredEventIdsForUser(int userId) throws SQLException {
        String sql = "SELECT event_id FROM event_registrations WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Integer> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    public int getRegistrationCount(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE event_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("event_date"),
                rs.getString("venue"),
                rs.getInt("capacity"),
                rs.getInt("reg_count"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
