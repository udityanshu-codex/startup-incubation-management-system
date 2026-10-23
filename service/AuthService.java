package service;

import dao.StartupDAO;
import dao.UserDAO;
import model.Startup;
import model.User;
import utils.PasswordUtil;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final StartupDAO startupDAO = new StartupDAO();

    public User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            throw new Exception("Invalid email or password.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new Exception("Your account has been deactivated. Please contact the administrator.");
        }

        boolean match = PasswordUtil.checkPassword(password, user.getPasswordHash());
        if (!match) {
            throw new Exception("Invalid email or password.");
        }

        // Set the active session
        SessionManager.setCurrentUser(user);
        return user;
    }

    public User registerFounder(String name, String email, String password, String confirmPassword,
                                String phone, String startupName, String category) throws Exception {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Full name is required.");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email is required.");
        if (!email.contains("@") || !email.contains(".")) throw new IllegalArgumentException("Please enter a valid email address.");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (!password.equals(confirmPassword)) throw new IllegalArgumentException("Passwords do not match.");
        if (phone == null || phone.trim().isEmpty()) throw new IllegalArgumentException("Phone number is required.");
        if (startupName == null || startupName.trim().isEmpty()) throw new IllegalArgumentException("Startup name is required.");

        if (userDAO.emailExists(email.trim())) {
            throw new Exception("An account with this email (" + email + ") already exists. Please login instead.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User user = new User(name.trim(), email.trim().toLowerCase(), passwordHash, phone.trim(), "FOUNDER");
        user = userDAO.createUser(user);

        // Create the startup profile automatically for this founder
        Startup startup = new Startup(user.getId(), startupName.trim(), category != null ? category : "Technology", "New startup registered by " + name);
        startupDAO.createStartup(startup);

        return user;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword, String confirmNewPassword) throws Exception {
        if (oldPassword == null || oldPassword.isEmpty()) throw new IllegalArgumentException("Current password is required.");
        if (newPassword == null || newPassword.length() < 6) throw new IllegalArgumentException("New password must be at least 6 characters.");
        if (!newPassword.equals(confirmNewPassword)) throw new IllegalArgumentException("New passwords do not match.");

        User user = userDAO.findById(userId);
        if (user == null) throw new Exception("User not found.");

        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new Exception("Current password does not match our records.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }

    public boolean updateProfile(int userId, String fullName, String phone) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
        boolean updated = userDAO.updateProfile(userId, fullName.trim(), phone != null ? phone.trim() : "");
        if (updated) {
            User current = SessionManager.getCurrentUser();
            if (current != null && current.getId() == userId) {
                current.setFullName(fullName.trim());
                current.setPhone(phone != null ? phone.trim() : "");
            }
        }
        return updated;
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public boolean toggleUserStatus(int userId, String currentStatus) throws Exception {
        User u = userDAO.findById(userId);
        if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {
            throw new Exception("Administrator accounts cannot be deactivated.");
        }
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "DISABLED" : "ACTIVE";
        return userDAO.updateStatus(userId, newStatus);
    }
}
