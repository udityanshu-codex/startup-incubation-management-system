package gui.admin;

import model.FundingApplication;
import service.FundingService;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Admin panel for reviewing funding applications, approving/rejecting capital grants,
 * and publishing new funding opportunities.
 */
public class AdminFundingPanel extends JPanel {

    private final FundingService fundingService = new FundingService();
    private JTable table;
    private DefaultTableModel model;
    private List<FundingApplication> applicationList;

    public AdminFundingPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Startup Funding Grants & Capital Decisions");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"App ID", "Startup", "Funding Scheme", "Requested (₹)", "Approved (₹)", "Applied Date", "Status"};
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
        JButton viewBtn = UIHelper.createSecondaryButton("View Proposal");
        JButton approveBtn = UIHelper.createPrimaryButton("Approve & Sanction Grant");
        JButton rejectBtn = UIHelper.createDangerButton("Reject Application");
        JButton newOppBtn = UIHelper.createPrimaryButton("+ Create Grant Scheme");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");

        buttonRow.add(viewBtn);
        buttonRow.add(approveBtn);
        buttonRow.add(rejectBtn);
        buttonRow.add(newOppBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        viewBtn.addActionListener(e -> viewSelectedProposal());
        approveBtn.addActionListener(e -> openApprovalDialog());
        rejectBtn.addActionListener(e -> openRejectionDialog());
        newOppBtn.addActionListener(e -> openCreateOpportunityDialog());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            applicationList = fundingService.getAllApplications();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            for (FundingApplication f : applicationList) {
                String dateStr = f.getAppliedDate() != null ? sdf.format(f.getAppliedDate()) : "N/A";
                String reqAmt = String.format("%,.0f", f.getAmountRequested());
                String appAmt = f.getAmountApproved() > 0 ? String.format("%,.0f", f.getAmountApproved()) : "—";

                model.addRow(new Object[]{
                        "FUN-" + String.format("%03d", f.getId()),
                        f.getStartupName(),
                        f.getOpportunityTitle() != null ? f.getOpportunityTitle() : "Seed Grant",
                        reqAmt,
                        appAmt,
                        dateStr,
                        f.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load funding records: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedProposal() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application to view.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FundingApplication app = applicationList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Application: FUN-").append(String.format("%03d", app.getId())).append("\n");
        sb.append("Startup: ").append(app.getStartupName()).append("\n");
        sb.append("Scheme: ").append(app.getOpportunityTitle() != null ? app.getOpportunityTitle() : "Seed Grant").append("\n");
        sb.append("Requested: ₹").append(String.format("%,.0f", app.getAmountRequested())).append("\n");
        sb.append("Approved: ₹").append(String.format("%,.0f", app.getAmountApproved())).append("\n");
        sb.append("Status: ").append(app.getStatus()).append("\n\n");
        sb.append("Proposal & Milestone Utilization Plan:\n").append(app.getProposal()).append("\n\n");
        sb.append("Committee Evaluation Feedback:\n");
        if (app.getAdminFeedback() != null && !app.getAdminFeedback().isEmpty()) {
            sb.append(app.getAdminFeedback());
        } else {
            sb.append("None yet recorded.");
        }

        JTextArea area = new JTextArea(sb.toString(), 14, 40);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Funding Proposal Review", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openApprovalDialog() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FundingApplication app = applicationList.get(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Approve Funding Grant", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLbl = new JLabel("<html><b>Grant for:</b> " + app.getStartupName() +
                "<br>Requested: ₹" + String.format("%,.0f", app.getAmountRequested()) + "</html>");
        titleLbl.setFont(UIHelper.FONT_LABEL);
        form.add(titleLbl, gbc);

        JLabel amtLbl = new JLabel("Sanctioned Amount (INR) *:");
        amtLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(amtLbl, gbc);

        JTextField amtField = UIHelper.createInputField(20);
        amtField.setText(String.format("%.0f", app.getAmountRequested()));
        gbc.gridy++;
        form.add(amtField, gbc);

        JLabel fbLbl = new JLabel("Grant Letter / Disbursement Terms *:");
        fbLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(fbLbl, gbc);

        JTextArea fbArea = new JTextArea(4, 20);
        fbArea.setFont(UIHelper.FONT_LABEL);
        fbArea.setLineWrap(true);
        fbArea.setWrapStyleWord(true);
        fbArea.setText("Approved by Investment Committee. 50% upfront tranches, remaining 50% upon Prototype validation.");
        JScrollPane scroll = new JScrollPane(fbArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        gbc.gridy++;
        form.add(scroll, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Sanction Grant");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            double approvedAmt;
            try {
                approvedAmt = Double.parseDouble(amtField.getText().trim().replace(",", ""));
                if (approvedAmt <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid positive numerical amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                fundingService.decideApplication(app.getId(), "Approved", approvedAmt, fbArea.getText().trim());
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Grant sanctioned successfully!", "Grant Approved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void openRejectionDialog() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FundingApplication app = applicationList.get(row);
        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for rejecting funding proposal of '" + app.getStartupName() + "':",
                "Rejection Reason", JOptionPane.PLAIN_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                fundingService.decideApplication(app.getId(), "Rejected", 0.0, reason.trim());
                refreshData();
                JOptionPane.showMessageDialog(this, "Funding application rejected.", "Application Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCreateOpportunityDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Grant Scheme", true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField titleField = UIHelper.createInputField(20);
        JTextField amtField = UIHelper.createInputField(20);
        JTextField deadlineField = UIHelper.createInputField(20);
        deadlineField.setText("30 Nov 2026");

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(UIHelper.FONT_LABEL);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        addField(form, gbc, "Grant Scheme Title *:", titleField);
        addField(form, gbc, "Maximum Grant Amount (INR) *:", amtField);
        addField(form, gbc, "Application Deadline *:", deadlineField);
        addField(form, gbc, "Eligibility / Description:", scroll);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Publish Grant");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String aStr = amtField.getText().trim().replace(",", "");
            double a;
            try {
                a = Double.parseDouble(aStr);
                if (a <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                fundingService.createOpportunity(t, a, deadlineField.getText().trim(), descArea.getText().trim());
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "New funding opportunity published!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void addField(JPanel form, GridBagConstraints gbc, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(l, gbc);
        gbc.gridy++;
        form.add(comp, gbc);
    }
}
