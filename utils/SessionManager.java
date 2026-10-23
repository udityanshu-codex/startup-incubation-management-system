package utils;

import model.User;

/**
 * Manages the active user session in memory.
 */
public class SessionManager {

    private static User currentUser = null;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static synchronized boolean isFounder() {
        return currentUser != null && currentUser.isFounder();
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
