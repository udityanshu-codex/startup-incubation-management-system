package gui.admin;

import service.ReportService;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * Admin panel for viewing aggregate reports, executive statistics,
 * previewing incubation summaries, and exporting reports to disk.
 */
public class AdminReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private JPanel statsGrid;

    public AdminReportPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Incubation Audit & Executive Analytics Reports");
        add(heading, BorderLayout.NORTH);

        statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setBackground(UIHelper.BG_COLOR);
        statsGrid.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(UIHelper.BG_COLOR);
        JButton exportBtn = UIHelper.createPrimaryButton("Export Report to File");
        JButton previewBtn = UIHelper.createSecondaryButton("Preview Full Report");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh Analytics");

        buttonRow.add(exportBtn);
        buttonRow.add(previewBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.add(statsGrid, BorderLayout.NORTH);
        center.add(buttonRow, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        exportBtn.addActionListener(e -> exportReport());
        previewBtn.addActionListener(e -> previewReport());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        statsGrid.removeAll();
        Map<String, String> stats = reportService.getAdminDashboardStats();

        statsGrid.add(UIHelper.createStatCard("Total Registered Startups", stats.getOrDefault("totalStartups", "0")));
        statsGrid.add(UIHelper.createStatCard("Approved Incubation Startups", stats.getOrDefault("approvedStartups", "0")));
        statsGrid.add(UIHelper.createStatCard("Rejected Applications", stats.getOrDefault("rejectedStartups", "0")));
        statsGrid.add(UIHelper.createStatCard("Total Funding Capital Granted", stats.getOrDefault("totalFunding", "₹0")));
        statsGrid.add(UIHelper.createStatCard("Active Advisory Mentors", stats.getOrDefault("activeMentors", "0")));
        statsGrid.add(UIHelper.createStatCard("Innovation Events Scheduled", stats.getOrDefault("upcomingEvents", "0")));

        statsGrid.revalidate();
        statsGrid.repaint();
    }

    private void previewReport() {
        String report = reportService.generateFullReportText();
        JTextArea area = new JTextArea(report, 22, 60);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 480));
        JOptionPane.showMessageDialog(this, scroll, "Official Incubation Summary Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("incubation_report_" + System.currentTimeMillis() + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                reportService.exportReportToFile(target);
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + target.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Export error: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
