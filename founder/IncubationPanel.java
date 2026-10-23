package gui.founder;

import model.IncubationApplication;
import model.IncubationProgram;
import model.Startup;
import model.User;
import service.IncubationService;
import service.StartupService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Incubation panel for founders to view programs, submit applications,
 * track application history, and read review decisions & feedback.
 */
public class IncubationPanel extends JPanel {

    private final User currentUser;
    private final IncubationService incubationService = new IncubationService();
    private final StartupService startupService = new StartupService();

    private JTable table;
    private DefaultTableModel model;
    private List<IncubationApplication> applicationList;

    public IncubationPanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Incubation Applications & Cohorts");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"App ID", "Startup", "Program Cohort", "Applied Date", "Decision Status", "Reviewed By Admin"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        UIHelper.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 220));
        scrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(UIHelper.BG_COLOR);
        JButton applyBtn = UIHelper.createPrimaryButton("Apply for Incubation Cohort");
        JButton viewBtn = UIHelper.createSecondaryButton("View Application & Feedback");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh List");
        buttonRow.add(applyBtn);
        buttonRow.add(viewBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        applyBtn.addActionListener(e -> openApplyDialog());
        viewBtn.addActionListener(e -> viewSelectedDetails());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public IncubationPanel() {
        this(null);
    }

    public void refreshData() {
        model.setRowCount(0);
        int founderId = currentUser != null ? currentUser.getId() : 2;
        try {
            applicationList = incubationService.getApplicationsForFounder(founderId);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            for (IncubationApplication a : applicationList) {
                String dateStr = a.getAppliedDate() != null ? sdf.format(a.getAppliedDate()) : "N/A";
                String reviewer = (a.getReviewedByAdminName() != null && !a.getReviewedByAdminName().isEmpty())
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
                    "Failed to load incubation applications: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openApplyDialog() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        Startup startup;
        try {
            startup = startupService.getStartupForFounder(founderId);
            if (startup == null) {
                JOptionPane.showMessageDialog(this,
                        "Please create your startup profile first under 'My Startup'.",
                        "Startup Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load startup: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<IncubationProgram> programs;
        try {
            programs = incubationService.getPrograms();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load incubation programs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply for Incubation", true);
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel startupLabel = new JLabel("Applying Startup: " + startup.getName());
        startupLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        form.add(startupLabel, gbc);

        JLabel progLabel = new JLabel("Select Incubation Program:");
        progLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(progLabel, gbc);

        JComboBox<IncubationProgram> programBox = new JComboBox<>(programs.toArray(new IncubationProgram[0]));
        programBox.setFont(UIHelper.FONT_LABEL);
        programBox.setPreferredSize(new Dimension(300, 34));
        gbc.gridy++;
        form.add(programBox, gbc);

        JLabel pitchLabel = new JLabel("Pitch Deck URL (Optional):");
        pitchLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(pitchLabel, gbc);

        JTextField pitchField = UIHelper.createInputField(20);
        gbc.gridy++;
        form.add(pitchField, gbc);

        JLabel objLabel = new JLabel("Key Milestones / Objectives *:");
        objLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(objLabel, gbc);

        JTextArea objArea = new JTextArea(4, 20);
        objArea.setFont(UIHelper.FONT_LABEL);
        objArea.setLineWrap(true);
        objArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(objArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        gbc.gridy++;
        form.add(scroll, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton submitBtn = UIHelper.createPrimaryButton("Submit Application");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(18, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String objectives = objArea.getText().trim();
            if (objectives.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please specify your milestones/objectives.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            IncubationProgram selectedProg = (IncubationProgram) programBox.getSelectedItem();
            Integer progId = selectedProg != null ? selectedProg.getId() : null;

            try {
                incubationService.applyForIncubation(startup.getId(), progId, pitchField.getText().trim(), objectives);
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Application submitted successfully! Our committee will review it soon.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to submit: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void viewSelectedDetails() {
        int row = table.getSelectedRow();
        if (row == -1 || applicationList == null || row >= applicationList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application to view details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IncubationApplication app = applicationList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Application ID: APP-").append(String.format("%03d", app.getId())).append("\n");
        sb.append("Startup: ").append(app.getStartupName()).append("\n");
        sb.append("Program: ").append(app.getProgramTitle() != null ? app.getProgramTitle() : "General Cohort").append("\n");
        sb.append("Status: ").append(app.getStatus()).append("\n");
        sb.append("Reviewed By Admin: ").append(app.getReviewedByAdminName() != null && !app.getReviewedByAdminName().isEmpty() ? app.getReviewedByAdminName() : "Pending Committee Review").append("\n");
        sb.append("Pitch Deck: ").append(app.getPitchDeckUrl() != null && !app.getPitchDeckUrl().isEmpty() ? app.getPitchDeckUrl() : "None").append("\n\n");
        sb.append("Submitted Objectives:\n").append(app.getObjectives()).append("\n\n");
        sb.append("Committee Review Feedback:\n");
        if (app.getAdminFeedback() != null && !app.getAdminFeedback().trim().isEmpty()) {
            sb.append(app.getAdminFeedback());
        } else {
            sb.append("Pending evaluation by the incubation steering committee.");
        }

        JTextArea area = new JTextArea(sb.toString(), 14, 40);
        area.setFont(UIHelper.FONT_LABEL);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Application Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
