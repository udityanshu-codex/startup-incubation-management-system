import gui.LoginFrame;
import javax.swing.*;

// Entry point of the Startup Incubation & Innovation Management System.
// Initializes the native modern look-and-feel and opens the Login screen.
public class Main {
    public static void main(String[] args) {
        try {
            // Apply native system Look and Feel for clean, modern rendering
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Run the GUI on the Event Dispatch Thread (standard Swing practice)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
