package gui.admin;

import model.Mentor;
import model.MentorshipRequest;
import service.MentorService;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for managing mentors, onboarding new advisors,
 * and reviewing & assigning mentorship requests submitted by founders.
 */
public class AdminMentorPanel extends JPanel {

    private final MentorService mentorService = new MentorService();
    private JTable table;
    private DefaultTableModel model;
    private List<Mentor> mentorList;

    public AdminMentorPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Industry Mentors & Advisory Management");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"ID", "Mentor Name", "Domain Expertise", "Email Address", "Phone", "Status"};
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
        JButton addBtn = UIHelper.createPrimaryButton("Add New Mentor");
        JButton assignBtn = UIHelper.createPrimaryButton("Review & Assign Requests");
        JButton toggleBtn = UIHelper.createSecondaryButton("Toggle Status (Active/Inactive)");
        JButton viewBtn = UIHelper.createSecondaryButton("View Bio");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");

        buttonRow.add(addBtn);
        buttonRow.add(assignBtn);
        buttonRow.add(toggleBtn);
        buttonRow.add(viewBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> openAddMentorDialog());
        assignBtn.addActionListener(e -> openAssignRequestsDialog());
        toggleBtn.addActionListener(e -> toggleSelectedStatus());
        viewBtn.addActionListener(e -> viewSelectedBio());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            mentorList = mentorService.getAllMentors();
            for (Mentor m : mentorList) {
                model.addRow(new Object[]{
                        "M-" + String.format("%03d", m.getId()),
                        m.getName(),
                        m.getExpertise(),
                        m.getEmail(),
                        m.getPhone() != null && !m.getPhone().isEmpty() ? m.getPhone() : "—",
                        m.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load mentors: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddMentorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Onboard New Mentor", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField nameField = UIHelper.createInputField(20);
        JTextField expField = UIHelper.createInputField(20);
        JTextField emailField = UIHelper.createInputField(20);
        JTextField phoneField = UIHelper.createInputField(20);

        JTextArea bioArea = new JTextArea(3, 20);
        bioArea.setFont(UIHelper.FONT_LABEL);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(bioArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        addField(form, gbc, "Mentor Name *:", nameField);
        addField(form, gbc, "Domain Expertise *:", expField);
        addField(form, gbc, "Email Address *:", emailField);
        addField(form, gbc, "Phone Number:", phoneField);
        addField(form, gbc, "Professional Bio:", scroll);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Save Mentor");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                mentorService.addMentor(nameField.getText().trim(), expField.getText().trim(),
                        emailField.getText().trim(), phoneField.getText().trim(), bioArea.getText().trim());
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Mentor onboarded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
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

    private void toggleSelectedStatus() {
        int row = table.getSelectedRow();
        if (row == -1 || mentorList == null || row >= mentorList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a mentor to toggle status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mentor m = mentorList.get(row);
        try {
            mentorService.toggleMentorStatus(m.getId(), m.getStatus());
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error toggling status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedBio() {
        int row = table.getSelectedRow();
        if (row == -1 || mentorList == null || row >= mentorList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a mentor first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mentor m = mentorList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Mentor Name: ").append(m.getName()).append("\n");
        sb.append("Expertise: ").append(m.getExpertise()).append("\n");
        sb.append("Email: ").append(m.getEmail()).append("\n");
        sb.append("Phone: ").append(m.getPhone()).append("\n");
        sb.append("Status: ").append(m.getStatus()).append("\n\n");
        sb.append("Bio & Background:\n").append(m.getBio() != null && !m.getBio().isEmpty() ? m.getBio() : "No bio entered.");

        JTextArea area = new JTextArea(sb.toString(), 10, 35);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Mentor Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openAssignRequestsDialog() {
        try {
            List<MentorshipRequest> requests = mentorService.getAllRequests();
            List<Mentor> activeMentors = mentorService.getActiveMentors();

            if (requests.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No mentorship requests from founders found.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Mentorship Requests", true);
            dialog.setSize(750, 420);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            String[] cols = {"Req ID", "Startup", "Topic / Need", "Status", "Assigned To"};
            DefaultTableModel reqModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (MentorshipRequest r : requests) {
                reqModel.addRow(new Object[]{
                        "REQ-" + String.format("%03d", r.getId()),
                        r.getStartupName(),
                        r.getTopic(),
                        r.getStatus(),
                        r.getMentorName() != null ? r.getMentorName() : "Unassigned"
                });
            }
            JTable reqTable = new JTable(reqModel);
            UIHelper.styleTable(reqTable);
            panel.add(new JScrollPane(reqTable), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            bottom.setBackground(Color.WHITE);

            JLabel mentorLbl = new JLabel("Assign Selected To:");
            mentorLbl.setFont(UIHelper.FONT_LABEL_BOLD);
            bottom.add(mentorLbl);

            JComboBox<Mentor> mentorCombo = new JComboBox<>(activeMentors.toArray(new Mentor[0]));
            mentorCombo.setFont(UIHelper.FONT_LABEL);
            bottom.add(mentorCombo);

            JButton assignBtn = UIHelper.createPrimaryButton("Confirm Assignment");
            JButton closeBtn = UIHelper.createSecondaryButton("Close");
            bottom.add(assignBtn);
            bottom.add(closeBtn);
            panel.add(bottom, BorderLayout.SOUTH);

            closeBtn.addActionListener(e -> dialog.dispose());
            assignBtn.addActionListener(e -> {
                int sel = reqTable.getSelectedRow();
                if (sel == -1) {
                    JOptionPane.showMessageDialog(dialog, "Please select a mentorship request to assign.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                MentorshipRequest selectedReq = requests.get(sel);
                Mentor selectedMentor = (Mentor) mentorCombo.getSelectedItem();
                if (selectedMentor == null) return;

                String remarks = "Mentor " + selectedMentor.getName() + " assigned for advisory engagement.";
                try {
                    mentorService.assignMentor(selectedReq.getId(), selectedMentor.getId(), "Assigned", remarks);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Assigned " + selectedMentor.getName() + " to startup '" + selectedReq.getStartupName() + "'.",
                            "Assignment Completed", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.setContentPane(panel);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
