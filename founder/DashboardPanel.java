package gui.founder;

import gui.FounderDashboard;
import model.Event;
import model.User;
import service.EventService;
import service.ReportService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Founder home screen showing live metrics, reviewing admin status, quick navigation, and upcoming events.
 */
public class DashboardPanel extends JPanel {

    private final FounderDashboard parentDashboard;
    private final User currentUser;
    private final ReportService reportService = new ReportService();
    private final EventService eventService = new EventService();

    private JPanel statsRow;
    private JLabel welcomeLabel;
    private JPanel reviewDecisionCard;
    private JPanel upcomingEventsContainer;

    public DashboardPanel(FounderDashboard parentDashboard, User currentUser) {
        this.parentDashboard = parentDashboard;
        this.currentUser = currentUser != null ? currentUser : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIHelper.BG_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        // 1. Top Welcome Header
        String founderName = this.currentUser != null ? this.currentUser.getFullName() : "Founder";
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UIHelper.BG_COLOR);
        topHeader.setMaximumSize(new Dimension(2000, 55));
        topHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomeLabel = new JLabel("Welcome back, " + founderName + "!");
        welcomeLabel.setFont(UIHelper.FONT_TITLE);
        welcomeLabel.setForeground(UIHelper.TEXT_DARK);

        JLabel subtitle = new JLabel("NIET Center for Innovation & Incubation  •  Live Venture Overview & Admin Decisions");
        subtitle.setFont(UIHelper.FONT_SUBTITLE);
        subtitle.setForeground(UIHelper.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(UIHelper.BG_COLOR);
        titleBlock.add(welcomeLabel);
        titleBlock.add(Box.createRigidArea(new Dimension(0, 3)));
        titleBlock.add(subtitle);

        topHeader.add(titleBlock, BorderLayout.WEST);

        // 2. Stat Cards Row
        statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setBackground(UIHelper.BG_COLOR);
        statsRow.setMaximumSize(new Dimension(2000, 95));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Review Decision Card (Who is the Admin that accepted/rejected)
        reviewDecisionCard = new JPanel(new BorderLayout(15, 10));
        reviewDecisionCard.setBackground(Color.WHITE);
        reviewDecisionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        reviewDecisionCard.setMaximumSize(new Dimension(2000, 180));
        reviewDecisionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 4. Two-Column Lower Content (Quick Actions & Upcoming Events)
        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        lowerGrid.setBackground(UIHelper.BG_COLOR);
        lowerGrid.setMaximumSize(new Dimension(2000, 240));
        lowerGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        lowerGrid.add(buildQuickActionsCard());
        lowerGrid.add(buildUpcomingEventsCard());

        // Assemble with controlled spacing
        contentPanel.add(topHeader);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(statsRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(reviewDecisionCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(lowerGrid);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public DashboardPanel() {
        this(null, null);
    }

    public void refreshData() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        Map<String, String> stats = reportService.getFounderDashboardStats(founderId);

        // Populate Stat Cards
        statsRow.removeAll();
        statsRow.add(UIHelper.createStatCard("My Startup", stats.getOrDefault("startupName", "None")));
        statsRow.add(UIHelper.createStatCard("Venture Status", stats.getOrDefault("startupStatus", "Under Review")));
        statsRow.add(UIHelper.createStatCard("Incubation Cohort", stats.getOrDefault("incubationStatus", "Not Applied")));
        statsRow.add(UIHelper.createStatCard("Mentor Guidance", stats.getOrDefault("mentorStatus", "Not Requested")));
        statsRow.revalidate();
        statsRow.repaint();

        // Populate Review Decision Card (Who is the Admin that accepted/rejected)
        renderReviewDecisionCard(stats);

        // Populate Upcoming Events
        loadUpcomingEvents();
    }

    private void renderReviewDecisionCard(Map<String, String> stats) {
        reviewDecisionCard.removeAll();

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);

        JLabel cardTitle = new JLabel("Venture Review & Incubation Committee Decision");
        cardTitle.setFont(UIHelper.FONT_HEADING);
        cardTitle.setForeground(UIHelper.TEXT_DARK);

        String startupStatus = stats.getOrDefault("startupStatus", "Under Review");
        JLabel statusBadge = UIHelper.createStatusBadge(startupStatus);

        headerRow.add(cardTitle, BorderLayout.WEST);
        headerRow.add(statusBadge, BorderLayout.EAST);
        reviewDecisionCard.add(headerRow, BorderLayout.NORTH);

        // Center Content: Reviewer Admin Name & Remarks
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 8));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        // Line 1: Startup Name + Reviewed By Admin
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row1.setBackground(Color.WHITE);

        String startupName = stats.getOrDefault("startupName", "N/A");
        String category = stats.getOrDefault("startupCategory", "Technology");
        String stage = stats.getOrDefault("startupStage", "Ideation");
        String reviewerName = stats.getOrDefault("startupReviewedBy", "Pending Committee Review");

        JLabel ventureInfo = new JLabel("<html><b>Venture:</b> " + startupName + " (" + category + " • " + stage + ")&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Reviewed By Admin:</b> <font color='#1e3a8a'><b>" + reviewerName + "</b></font></html>");
        ventureInfo.setFont(UIHelper.FONT_LABEL);
        row1.add(ventureInfo);

        // Line 2: Committee Feedback & Remarks Callout
        String remarks = stats.getOrDefault("startupRemarks", "Application is in the queue for committee screening and evaluation.");
        JPanel remarksCallout = new JPanel(new BorderLayout(8, 0));
        remarksCallout.setBackground(new Color(248, 250, 252));
        remarksCallout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel quoteLabel = new JLabel("<html><b>Committee Remarks:</b> <i>\"" + remarks + "\"</i></html>");
        quoteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quoteLabel.setForeground(new Color(51, 65, 85));

        remarksCallout.add(quoteLabel, BorderLayout.CENTER);

        body.add(row1);
        body.add(remarksCallout);
        reviewDecisionCard.add(body, BorderLayout.CENTER);

        // Footer: Link to full details
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);

        String incProg = stats.getOrDefault("incubationProgram", "None");
        String incReviewer = stats.getOrDefault("incubationReviewedBy", "Pending");
        JLabel incSub = new JLabel("<html>Cohort: <b>" + incProg + "</b>  •  Incubation Officer: <font color='#1e3a8a'>" + incReviewer + "</font></html>");
        incSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        incSub.setForeground(UIHelper.TEXT_MUTED);

        JButton viewProfileBtn = UIHelper.createSecondaryButton("View Full Venture Profile →");
        viewProfileBtn.addActionListener(e -> navigateTo(FounderDashboard.MY_STARTUP));

        footer.add(incSub, BorderLayout.WEST);
        footer.add(viewProfileBtn, BorderLayout.EAST);
        reviewDecisionCard.add(footer, BorderLayout.SOUTH);

        reviewDecisionCard.revalidate();
        reviewDecisionCard.repaint();
    }

    private JPanel buildQuickActionsCard() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel heading = UIHelper.createHeading("Quick Portal Navigation");
        wrapper.add(heading, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 12, 12));
        buttons.setBackground(Color.WHITE);

        JButton myStartupBtn = UIHelper.createPrimaryButton("My Startup");
        JButton incubationBtn = UIHelper.createPrimaryButton("Incubation");
        JButton mentorBtn = UIHelper.createPrimaryButton("Mentorship");
        JButton fundingBtn = UIHelper.createPrimaryButton("Funding");

        // Fixed compact button size so it never stretches vertically
        Dimension btnSize = new Dimension(140, 40);
        myStartupBtn.setPreferredSize(btnSize);
        incubationBtn.setPreferredSize(btnSize);
        mentorBtn.setPreferredSize(btnSize);
        fundingBtn.setPreferredSize(btnSize);

        buttons.add(myStartupBtn);
        buttons.add(incubationBtn);
        buttons.add(mentorBtn);
        buttons.add(fundingBtn);

        myStartupBtn.addActionListener(e -> navigateTo(FounderDashboard.MY_STARTUP));
        incubationBtn.addActionListener(e -> navigateTo(FounderDashboard.INCUBATION));
        mentorBtn.addActionListener(e -> navigateTo(FounderDashboard.MENTORSHIP));
        fundingBtn.addActionListener(e -> navigateTo(FounderDashboard.FUNDING));

        wrapper.add(buttons, BorderLayout.CENTER);
        return wrapper;
    }

    private void navigateTo(String cardKey) {
        if (parentDashboard != null) {
            parentDashboard.switchTab(cardKey);
        } else {
            JOptionPane.showMessageDialog(this, "Opening " + cardKey + " section.");
        }
    }

    private JPanel buildUpcomingEventsCard() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel heading = UIHelper.createHeading("Upcoming Innovation Events");
        JButton viewAllBtn = new JButton("View All →");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllBtn.setForeground(UIHelper.PRIMARY_COLOR);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> navigateTo(FounderDashboard.EVENTS));

        header.add(heading, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        upcomingEventsContainer = new JPanel();
        upcomingEventsContainer.setLayout(new BoxLayout(upcomingEventsContainer, BoxLayout.Y_AXIS));
        upcomingEventsContainer.setBackground(Color.WHITE);

        wrapper.add(upcomingEventsContainer, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadUpcomingEvents() {
        if (upcomingEventsContainer == null) return;
        upcomingEventsContainer.removeAll();

        try {
            List<Event> events = eventService.getUpcomingEvents();
            if (events.isEmpty()) {
                JLabel empty = new JLabel("No upcoming events scheduled right now.");
                empty.setFont(UIHelper.FONT_LABEL);
                empty.setForeground(UIHelper.TEXT_MUTED);
                upcomingEventsContainer.add(empty);
            } else {
                int limit = Math.min(events.size(), 2);
                for (int i = 0; i < limit; i++) {
                    Event ev = events.get(i);
                    JPanel item = new JPanel(new BorderLayout(5, 2));
                    item.setBackground(Color.WHITE);
                    item.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, (i < limit - 1 ? 1 : 0), 0, new Color(241, 245, 249)),
                            BorderFactory.createEmptyBorder(6, 0, 6, 0)));

                    JLabel title = new JLabel("• " + ev.getTitle());
                    title.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    title.setForeground(UIHelper.TEXT_DARK);

                    JLabel meta = new JLabel(ev.getEventDate() + " | " + ev.getVenue());
                    meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    meta.setForeground(UIHelper.TEXT_MUTED);

                    item.add(title, BorderLayout.NORTH);
                    item.add(meta, BorderLayout.SOUTH);
                    upcomingEventsContainer.add(item);
                }
            }
        } catch (Exception e) {
            JLabel err = new JLabel("Could not load events: " + e.getMessage());
            err.setFont(UIHelper.FONT_LABEL);
            upcomingEventsContainer.add(err);
        }

        upcomingEventsContainer.revalidate();
        upcomingEventsContainer.repaint();
    }
}
