package gui.admin;

import model.Startup;
import service.StartupService;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin panel for reviewing, approving, or rejecting registered startups.
 */
public class AdminStartupPanel extends JPanel {

    private final StartupService startupService = new StartupService();
    private JTable table;
    private DefaultTableModel model;
    private List<Startup> startupList;

    public AdminStartupPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Startup Directory & Portfolio Management");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"ID", "Startup Name", "Founder Name", "Category", "Venture Stage", "Status", "Reviewed By Admin"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        UIHelper.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(850, 240));
        scrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(UIHelper.BG_COLOR);
        JButton viewBtn = UIHelper.createSecondaryButton("View Startup Profile");
        JButton approveBtn = UIHelper.createPrimaryButton("Approve Startup");
        JButton rejectBtn = UIHelper.createDangerButton("Reject Startup");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");

        buttonRow.add(viewBtn);
        buttonRow.add(approveBtn);
        buttonRow.add(rejectBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        viewBtn.addActionListener(e -> viewSelectedStartup());
        approveBtn.addActionListener(e -> updateSelectedStatus("Approved"));
        rejectBtn.addActionListener(e -> updateSelectedStatus("Rejected"));
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            startupList = startupService.getAllStartups();
            for (Startup s : startupList) {
                String reviewer = (s.getReviewedByAdminName() != null && !s.getReviewedByAdminName().isEmpty())
                        ? s.getReviewedByAdminName() : "Pending";
                model.addRow(new Object[]{
                        "S-" + String.format("%03d", s.getId()),
                        s.getName(),
                        s.getFounderName() != null ? s.getFounderName() : "Founder",
                        s.getCategory(),
                        s.getStage() != null ? s.getStage() : "Ideation",
                        s.getStatus(),
                        reviewer
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load startups: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedStartup() {
        int row = table.getSelectedRow();
        if (row == -1 || startupList == null || row >= startupList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a startup first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Startup s = startupList.get(row);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        String dateStr = s.getCreatedAt() != null ? sdf.format(s.getCreatedAt()) : "Recently";

        StringBuilder sb = new StringBuilder();
        sb.append("Startup Name: ").append(s.getName()).append("\n");
        sb.append("Founder: ").append(s.getFounderName() != null ? s.getFounderName() : "N/A").append("\n");
        sb.append("Category: ").append(s.getCategory()).append("\n");
        sb.append("Stage: ").append(s.getStage()).append("\n");
        sb.append("Status: ").append(s.getStatus()).append("\n");
        sb.append("Reviewed By Admin: ").append(s.getReviewedByAdminName() != null ? s.getReviewedByAdminName() : "Pending Committee Review").append("\n");
        sb.append("Admin Remarks: ").append(s.getAdminRemarks() != null ? s.getAdminRemarks() : "None recorded yet").append("\n");
        sb.append("Registration Date: ").append(dateStr).append("\n\n");
        sb.append("Venture Description:\n").append(s.getDescription()).append("\n");

        JTextArea area = new JTextArea(sb.toString(), 15, 42);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Startup Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSelectedStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1 || startupList == null || row >= startupList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a startup first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Startup s = startupList.get(row);
        model.User currentAdmin = utils.SessionManager.getCurrentUser();
        String adminName = currentAdmin != null ? currentAdmin.getFullName() : "Admin Officer";
        int adminId = currentAdmin != null ? currentAdmin.getId() : 1;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Decision: " + newStatus, true);
        dialog.setSize(500, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel info = new JLabel("<html><b>Startup:</b> " + s.getName() + " (" + s.getCategory() + ")<br>" +
                "<b>Reviewing Admin:</b> <font color='#1e3a8a'>" + adminName + "</font><br>" +
                "<b>New Decision:</b> <font color='" + ("Approved".equals(newStatus) ? "#16a34a" : "#dc2626") + "'>" + newStatus + "</font></html>");
        info.setFont(UIHelper.FONT_LABEL);
        panel.add(info, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(6, 6));
        centerPanel.setBackground(Color.WHITE);
        JLabel remarksLabel = new JLabel("Admin Review Remarks / Committee Notes *:");
        remarksLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        centerPanel.add(remarksLabel, BorderLayout.NORTH);

        JTextArea remarksArea = new JTextArea(4, 25);
        remarksArea.setFont(UIHelper.FONT_LABEL);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setText("Approved".equals(newStatus)
                ? "Approved by " + adminName + ": Strong market potential and innovative solution. Welcome to the incubation cohort."
                : "Reviewed by " + adminName + ": Application deferred. Focus on refining customer validation and reapply next cycle.");
        JScrollPane scroll = new JScrollPane(remarksArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        centerPanel.add(scroll, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton confirmBtn = "Approved".equals(newStatus) ? UIHelper.createPrimaryButton("Confirm Approval") : UIHelper.createDangerButton("Confirm Rejection");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> {
            String remarks = remarksArea.getText().trim();
            try {
                startupService.updateStartupStatus(s.getId(), newStatus, adminId, adminName, remarks);
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Startup '" + s.getName() + "' has been " + newStatus.toLowerCase() + " by " + adminName + ".",
                        "Decision Recorded", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Database update error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
