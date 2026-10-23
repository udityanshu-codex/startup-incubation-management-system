package gui.founder;

import model.FundingApplication;
import model.FundingOpportunity;
import model.Startup;
import model.User;
import service.FundingService;
import service.StartupService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Funding panel for founders to explore seed capital & innovation grants,
 * submit funding proposals, and view grant decisions and committee feedback.
 */
public class FundingPanel extends JPanel {

    private final User currentUser;
    private final FundingService fundingService = new FundingService();
    private final StartupService startupService = new StartupService();

    private JTable table;
    private DefaultTableModel model;
    private List<FundingApplication> applicationList;

    public FundingPanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Funding & Grant Applications");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"Application ID", "Funding Scheme", "Requested", "Approved", "Applied Date", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        UIHelper.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 220));
        scrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(UIHelper.BG_COLOR);
        JButton applyBtn = UIHelper.createPrimaryButton("Apply for Funding Grant");
        JButton statusBtn = UIHelper.createSecondaryButton("View Status & Feedback");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh List");
        buttonRow.add(applyBtn);
        buttonRow.add(statusBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        applyBtn.addActionListener(e -> openApplyDialog());
        statusBtn.addActionListener(e -> viewSelectedStatus());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public FundingPanel() {
        this(null);
    }

    public void refreshData() {
        model.setRowCount(0);
        int founderId = currentUser != null ? currentUser.getId() : 2;
        try {
            applicationList = fundingService.getApplicationsForFounder(founderId);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            for (FundingApplication f : applicationList) {
                String dateStr = f.getAppliedDate() != null ? sdf.format(f.getAppliedDate()) : "N/A";
                String reqAmt = "₹" + String.format("%,.0f", f.getAmountRequested());
                String appAmt = f.getAmountApproved() > 0 ? "₹" + String.format("%,.0f", f.getAmountApproved()) : "—";

                model.addRow(new Object[]{
                        "FUN-" + String.format("%03d", f.getId()),
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

    private void openApplyDialog() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        Startup startup;
        try {
            startup = startupService.getStartupForFounder(founderId);
            if (startup == null) {
                JOptionPane.showMessageDialog(this, "Please register your startup details under 'My Startup' first.", "Startup Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load startup: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<FundingOpportunity> opportunities;
        try {
            opportunities = fundingService.getFundingOpportunities();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load funding schemes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply for Funding Grant", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel startupLabel = new JLabel("Applying Venture: " + startup.getName());
        startupLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        form.add(startupLabel, gbc);

        JLabel oppLabel = new JLabel("Select Grant / Funding Opportunity:");
        oppLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(oppLabel, gbc);

        JComboBox<FundingOpportunity> oppBox = new JComboBox<>(opportunities.toArray(new FundingOpportunity[0]));
        oppBox.setFont(UIHelper.FONT_LABEL);
        oppBox.setPreferredSize(new Dimension(300, 34));
        gbc.gridy++;
        form.add(oppBox, gbc);

        JLabel amtLabel = new JLabel("Amount Requested (INR) *:");
        amtLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(amtLabel, gbc);

        JTextField amtField = UIHelper.createInputField(20);
        gbc.gridy++;
        form.add(amtField, gbc);

        JLabel propLabel = new JLabel("Fund Utilization Plan & Milestone Proposal *:");
        propLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(propLabel, gbc);

        JTextArea propArea = new JTextArea(4, 20);
        propArea.setFont(UIHelper.FONT_LABEL);
        propArea.setLineWrap(true);
        propArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(propArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        gbc.gridy++;
        form.add(scroll, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton submitBtn = UIHelper.createPrimaryButton("Submit Proposal");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(18, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String amtStr = amtField.getText().trim().replace(",", "");
            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid positive numerical amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String proposal = propArea.getText().trim();
            if (proposal.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your milestone and fund utilization plan.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            FundingOpportunity selectedOpp = (FundingOpportunity) oppBox.getSelectedItem();
            Integer oppId = selectedOpp != null ? selectedOpp.getId() : null;

            try {
                fundingService.applyForFunding(startup.getId(), oppId, amount, proposal);
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Funding grant proposal submitted successfully!", "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Submission error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void viewSelectedStatus() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a funding application from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FundingApplication app = applicationList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Application Reference: FUN-").append(String.format("%03d", app.getId())).append("\n");
        sb.append("Funding Scheme: ").append(app.getOpportunityTitle() != null ? app.getOpportunityTitle() : "Seed Grant").append("\n");
        sb.append("Requested Amount: ₹").append(String.format("%,.0f", app.getAmountRequested())).append("\n");
        sb.append("Approved Amount: ₹").append(String.format("%,.0f", app.getAmountApproved())).append("\n");
        sb.append("Current Status: ").append(app.getStatus()).append("\n\n");
        sb.append("Fund Utilization Proposal:\n").append(app.getProposal()).append("\n\n");
        sb.append("Investment Committee Feedback:\n");
        if (app.getAdminFeedback() != null && !app.getAdminFeedback().trim().isEmpty()) {
            sb.append(app.getAdminFeedback());
        } else {
            sb.append("Under technical and financial evaluation by the review panel.");
        }

        JTextArea area = new JTextArea(sb.toString(), 14, 40);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Funding Decision & Review", JOptionPane.INFORMATION_MESSAGE);
    }
}
