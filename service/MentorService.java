package service;

import dao.MentorDAO;
import model.Mentor;
import model.MentorshipRequest;

import java.sql.SQLException;
import java.util.List;

public class MentorService {

    private final MentorDAO mentorDAO = new MentorDAO();

    public List<Mentor> getActiveMentors() throws SQLException {
        return mentorDAO.getActiveMentors();
    }

    public List<Mentor> getAllMentors() throws SQLException {
        return mentorDAO.getAllMentors();
    }

    public Mentor addMentor(String name, String expertise, String email, String phone, String bio) throws Exception {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Mentor name is required.");
        if (expertise == null || expertise.trim().isEmpty()) throw new IllegalArgumentException("Expertise domain is required.");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email address is required.");

        Mentor mentor = new Mentor(name.trim(), expertise.trim(), email.trim(), phone != null ? phone.trim() : "", bio != null ? bio.trim() : "");
        return mentorDAO.createMentor(mentor);
    }

    public boolean toggleMentorStatus(int mentorId, String currentStatus) throws SQLException {
        String newStatus = "Active".equalsIgnoreCase(currentStatus) ? "Inactive" : "Active";
        return mentorDAO.updateMentorStatus(mentorId, newStatus);
    }

    public boolean deleteMentor(int mentorId) throws SQLException {
        return mentorDAO.deleteMentor(mentorId);
    }

    public MentorshipRequest requestMentorship(int startupId, Integer mentorId, String topic, String notes) throws Exception {
        if (startupId <= 0) throw new IllegalArgumentException("Valid startup ID required.");
        if (topic == null || topic.trim().isEmpty()) throw new IllegalArgumentException("Mentorship topic/area is required.");

        MentorshipRequest req = new MentorshipRequest();
        req.setStartupId(startupId);
        req.setMentorId(mentorId);
        req.setTopic(topic.trim());
        req.setNotes(notes != null ? notes.trim() : "");
        req.setStatus("Pending");
        req.setAdminRemarks("Awaiting mentor assignment by incubation committee.");

        return mentorDAO.createMentorshipRequest(req);
    }

    public List<MentorshipRequest> getFounderRequests(int founderId) throws SQLException {
        return mentorDAO.getRequestsByFounderId(founderId);
    }

    public List<MentorshipRequest> getAllRequests() throws SQLException {
        return mentorDAO.getAllMentorshipRequests();
    }

    public boolean assignMentor(int requestId, int mentorId, String status, String remarks) throws SQLException {
        return mentorDAO.assignMentorToRequest(requestId, mentorId, status, remarks != null ? remarks.trim() : "");
    }
}
