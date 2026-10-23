package gui.founder;

import model.Event;
import model.User;
import service.EventService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Events panel for founders to explore incubation events, workshops,
 * register with real-time capacity checks, and avoid duplicate bookings.
 */
public class EventsPanel extends JPanel {

    private final User currentUser;
    private final EventService eventService = new EventService();

    private JPanel listPanel;

    public EventsPanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIHelper.BG_COLOR);

        JLabel heading = UIHelper.createHeading("Innovation Events, Hackathons & Workshops");
        topBar.add(heading, BorderLayout.WEST);

        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh Events");
        refreshBtn.addActionListener(e -> refreshData());
        topBar.add(refreshBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UIHelper.BG_COLOR);
        listPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public EventsPanel() {
        this(null);
    }

    public void refreshData() {
        listPanel.removeAll();
        int userId = currentUser != null ? currentUser.getId() : 2;

        try {
            List<Event> events = eventService.getAllEvents();
            List<Integer> registeredIds = eventService.getUserRegisteredEventIds(userId);

            if (events.isEmpty()) {
                JPanel emptyCard = new JPanel();
                emptyCard.setBackground(Color.WHITE);
                emptyCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIHelper.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)));
                emptyCard.add(new JLabel("No innovation events are currently listed. Check back soon!"));
                listPanel.add(emptyCard);
            } else {
                for (Event ev : events) {
                    boolean isRegistered = registeredIds.contains(ev.getId());
                    listPanel.add(createEventCard(ev, isRegistered, userId));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 14)));
                }
            }
        } catch (Exception e) {
            JLabel err = new JLabel("Failed to load events: " + e.getMessage());
            err.setFont(UIHelper.FONT_LABEL);
            listPanel.add(err);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createEventCard(Event event, boolean isRegistered, int userId) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 22, 16, 22)));
        card.setMaximumSize(new Dimension(850, 100));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(event.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIHelper.TEXT_DARK);

        int remaining = Math.max(0, event.getCapacity() - event.getRegisteredCount());
        String infoText = String.format("Date: %s   |   Venue: %s   |   Seats: %d / %d (%d left)",
                event.getEventDate(), event.getVenue(), event.getRegisteredCount(), event.getCapacity(), remaining);
        JLabel infoLabel = new JLabel(infoText);
        infoLabel.setFont(UIHelper.FONT_LABEL);
        infoLabel.setForeground(UIHelper.TEXT_MUTED);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        textPanel.add(infoLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(Color.WHITE);

        JButton viewBtn = UIHelper.createSecondaryButton("Details");
        buttonPanel.add(viewBtn);

        JButton registerBtn;
        if (isRegistered) {
            registerBtn = new JButton("Registered ✓");
            registerBtn.setFont(UIHelper.FONT_BUTTON);
            registerBtn.setBackground(new Color(240, 253, 244));
            registerBtn.setForeground(new Color(22, 163, 74));
            registerBtn.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208), 1));
            registerBtn.setEnabled(false);
        } else if (event.isFull()) {
            registerBtn = new JButton("Housefull");
            registerBtn.setFont(UIHelper.FONT_BUTTON);
            registerBtn.setBackground(new Color(254, 242, 242));
            registerBtn.setForeground(new Color(220, 38, 38));
            registerBtn.setEnabled(false);
        } else {
            registerBtn = UIHelper.createPrimaryButton("Register");
            registerBtn.addActionListener(e -> handleRegister(event.getId(), event.getTitle(), userId));
        }
        buttonPanel.add(registerBtn);

        viewBtn.addActionListener(e -> {
            String desc = event.getDescription() != null && !event.getDescription().isEmpty()
                    ? event.getDescription()
                    : "No extra description provided.";
            JOptionPane.showMessageDialog(this,
                    "Event: " + event.getTitle() + "\nDate: " + event.getEventDate() +
                            "\nVenue: " + event.getVenue() + "\nCapacity: " + event.getCapacity() +
                            "\nAttendees Registered: " + event.getRegisteredCount() +
                            "\n\nDescription:\n" + desc,
                    "Event Details", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);

        return card;
    }

    private void handleRegister(int eventId, String title, int userId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm registration for '" + title + "'?",
                "Confirm Registration", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            eventService.registerUser(eventId, userId);
            refreshData();
            JOptionPane.showMessageDialog(this,
                    "Successfully registered for " + title + "!",
                    "Registration Confirmed", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Failed", JOptionPane.WARNING_MESSAGE);
        }
    }
}
