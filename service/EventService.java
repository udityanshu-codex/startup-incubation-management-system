package service;

import dao.EventDAO;
import model.Event;

import java.sql.SQLException;
import java.util.List;

public class EventService {

    private final EventDAO eventDAO = new EventDAO();

    public List<Event> getAllEvents() throws SQLException {
        return eventDAO.getAllEvents();
    }

    public List<Event> getUpcomingEvents() throws SQLException {
        return eventDAO.getUpcomingEvents();
    }

    public Event createEvent(String title, String eventDate, String venue, int capacity, String description) throws Exception {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Event title is required.");
        if (eventDate == null || eventDate.trim().isEmpty()) throw new IllegalArgumentException("Event date is required.");
        if (venue == null || venue.trim().isEmpty()) throw new IllegalArgumentException("Venue is required.");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than zero.");

        Event event = new Event(title.trim(), eventDate.trim(), venue.trim(), capacity, description != null ? description.trim() : "");
        return eventDAO.createEvent(event);
    }

    public boolean updateEvent(Event event) throws Exception {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) throw new IllegalArgumentException("Event title is required.");
        if (event.getEventDate() == null || event.getEventDate().trim().isEmpty()) throw new IllegalArgumentException("Event date is required.");
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) throw new IllegalArgumentException("Venue is required.");
        return eventDAO.updateEvent(event);
    }

    public boolean deleteEvent(int eventId) throws SQLException {
        return eventDAO.deleteEvent(eventId);
    }

    public boolean registerUser(int eventId, int userId) throws Exception {
        if (eventDAO.isUserRegistered(eventId, userId)) {
            throw new Exception("You are already registered for this event.");
        }

        int count = eventDAO.getRegistrationCount(eventId);
        List<Event> all = eventDAO.getAllEvents();
        Event ev = null;
        for (Event e : all) {
            if (e.getId() == eventId) {
                ev = e;
                break;
            }
        }

        if (ev != null && count >= ev.getCapacity()) {
            throw new Exception("Registration closed: This event has reached its maximum capacity (" + ev.getCapacity() + " attendees).");
        }

        return eventDAO.registerUser(eventId, userId);
    }

    public List<Integer> getUserRegisteredEventIds(int userId) throws SQLException {
        return eventDAO.getRegisteredEventIdsForUser(userId);
    }

    public boolean isUserRegistered(int eventId, int userId) throws SQLException {
        return eventDAO.isUserRegistered(eventId, userId);
    }
}
