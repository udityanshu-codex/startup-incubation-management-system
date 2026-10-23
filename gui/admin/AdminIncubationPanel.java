package gui.admin;

import model.IncubationApplication;
import service.IncubationService;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin panel for reviewing incubation applications, approving/rejecting them,
 * and saving structured feedback that is visible to founders.
 */
public class AdminIncubationPanel extends JPanel {

    private final IncubationService incubationService = new IncubationService();
    private JTable table;
    private DefaultTableModel model;
    private List<IncubationApplication> applicationList;

    public AdminIncubationPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Incubation Applications Review & Approvals");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"App ID", "Startup", "Program Cohort", "Applied Date", "Current Status", "Reviewed By Admin"};
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
        JButton viewBtn = UIHelper.createSecondaryButton("View Full Proposal");
        JButton approveBtn = UIHelper.createPrimaryButton("Approve Application");
        JButton rejectBtn = UIHelper.createDangerButton("Reject Application");
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

        viewBtn.addActionListener(e -> viewSelectedProposal());
        approveBtn.addActionListener(e -> openDecisionDialog("Approved"));
        rejectBtn.addActionListener(e -> openDecisionDialog("Rejected"));
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            applicationList = incubationService.getAllApplications();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            for (IncubationApplication a : applicationList) {
                String dateStr = a.getAppliedDate() != null ? sdf.format(a.getAppliedDate()) : "N/A";
                String reviewer = a.getReviewedByAdminName() != null && !a.getReviewedByAdminName().isEmpty()
                        ? a.getReviewedByAdminName() : "Pending";
                model.addRow(new Object[]{
                        "APP-" + String.format("%03d", a.getId()),
                        a.getStartupName(),
                        a.getProgramTitle() != null ? a.getProgramTitle() : "General Cohort",
                        dateStr,
                        a.getStatus(),
                        reviewer
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load applications: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedProposal() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IncubationApplication app = applicationList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Application: APP-").append(String.format("%03d", app.getId())).append("\n");
        sb.append("Startup: ").append(app.getStartupName()).append("\n");
        sb.append("Program: ").append(app.getProgramTitle() != null ? app.getProgramTitle() : "General Cohort").append("\n");
        sb.append("Status: ").append(app.getStatus()).append("\n");
        sb.append("Reviewed By Admin: ").append(app.getReviewedByAdminName() != null ? app.getReviewedByAdminName() : "Pending Committee Review").append("\n");
        sb.append("Pitch Deck Link: ").append(app.getPitchDeckUrl() != null && !app.getPitchDeckUrl().isEmpty() ? app.getPitchDeckUrl() : "None").append("\n\n");
        sb.append("Milestones / Objectives:\n").append(app.getObjectives()).append("\n\n");
        sb.append("Committee Evaluation Feedback:\n").append(app.getAdminFeedback() != null ? app.getAdminFeedback() : "None yet recorded.");

        JTextArea area = new JTextArea(sb.toString(), 15, 42);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Incubation Application Proposal", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openDecisionDialog(String decision) {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IncubationApplication app = applicationList.get(row);
        model.User currentAdmin = utils.SessionManager.getCurrentUser();
        String adminName = currentAdmin != null ? currentAdmin.getFullName() : "Admin Officer";
        int adminId = currentAdmin != null ? currentAdmin.getId() : 1;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Decision: " + decision, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel info = new JLabel("<html><b>Recording Decision for:</b> " + app.getStartupName() + "<br>" +
                "<b>Reviewing Admin:</b> <font color='#1e3a8a'>" + adminName + "</font><br>" +
                "<b>New Status:</b> <font color='" +
                ("Approved".equals(decision) ? "#16a34a" : "#dc2626") + "'>" + decision + "</font></html>");
        info.setFont(UIHelper.FONT_LABEL);
        panel.add(info, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(Color.WHITE);
        JLabel fbLabel = new JLabel("Committee Feedback & Instructions for Founder *:");
        fbLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        centerPanel.add(fbLabel, BorderLayout.NORTH);

        JTextArea fbArea = new JTextArea(5, 20);
        fbArea.setFont(UIHelper.FONT_LABEL);
        fbArea.setLineWrap(true);
        fbArea.setWrapStyleWord(true);
        fbArea.setText("Approved".equals(decision)
                ? "Accepted by " + adminName + ": Onboarding package and mentor allocation initiated."
                : "Deferred by " + adminName + ": Please refine MVP traction metrics and reapply in next quarterly cycle.");
        JScrollPane scroll = new JScrollPane(fbArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        centerPanel.add(scroll, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = "Approved".equals(decision) ? UIHelper.createPrimaryButton("Confirm Approval") : UIHelper.createDangerButton("Confirm Rejection");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String feedback = fbArea.getText().trim();
            try {
                incubationService.decideApplication(app.getId(), decision, feedback, adminId, adminName);
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Application marked as " + decision + " by " + adminName + ".", "Decision Recorded", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
