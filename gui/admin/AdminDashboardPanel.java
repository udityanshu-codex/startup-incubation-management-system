package gui.admin;

import service.ReportService;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Admin overview dashboard displaying live counts and metrics directly from the database.
 */
public class AdminDashboardPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private JPanel statsGrid;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel welcome = new JLabel("Incubation Operations & Administration Dashboard");
        welcome.setFont(UIHelper.FONT_TITLE);
        welcome.setForeground(UIHelper.TEXT_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.BG_COLOR);
        top.add(welcome, BorderLayout.WEST);

        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh Stats");
        refreshBtn.addActionListener(e -> refreshData());
        top.add(refreshBtn, BorderLayout.EAST);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setBackground(UIHelper.BG_COLOR);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.add(statsGrid, BorderLayout.NORTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        statsGrid.removeAll();
        Map<String, String> stats = reportService.getAdminDashboardStats();

        statsGrid.add(UIHelper.createStatCard("Total Registered Startups", stats.getOrDefault("totalStartups", "0")));
        statsGrid.add(UIHelper.createStatCard("Pending Incubation Reviews", stats.getOrDefault("pendingApplications", "0")));
        statsGrid.add(UIHelper.createStatCard("Approved Incubation Cohorts", stats.getOrDefault("approvedStartups", "0")));
        statsGrid.add(UIHelper.createStatCard("Active Industry Mentors", stats.getOrDefault("activeMentors", "0")));
        statsGrid.add(UIHelper.createStatCard("Funding Grant Applications", stats.getOrDefault("fundingApplications", "0")));
        statsGrid.add(UIHelper.createStatCard("Upcoming Events Scheduled", stats.getOrDefault("upcomingEvents", "0")));

        statsGrid.revalidate();
        statsGrid.repaint();
    }
}
