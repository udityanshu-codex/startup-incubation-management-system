package gui.founder;

import model.Startup;
import model.User;
import service.StartupService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Panel for managing the logged-in founder's own startup profile.
 * Strictly enforces ownership so founders only access their own venture.
 */
public class MyStartupPanel extends JPanel {

    private final User currentUser;
    private final StartupService startupService = new StartupService();
    private Startup currentStartup;

    private JLabel nameVal, founderVal, catVal, stageVal, descVal, dateVal, statusVal, reviewedByVal, remarksVal;
    private JButton editBtn;

    public MyStartupPanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("My Startup Profile & Review Status");
        add(heading, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        nameVal       = addDetailRow(card, gbc, "Startup Name:");
        founderVal    = addDetailRow(card, gbc, "Founder / Lead:");
        catVal        = addDetailRow(card, gbc, "Industry Category:");
        stageVal      = addDetailRow(card, gbc, "Venture Stage:");
        descVal       = addDetailRow(card, gbc, "Value Proposition / Bio:");
        dateVal       = addDetailRow(card, gbc, "Registered On:");
        statusVal     = addDetailRow(card, gbc, "Incubation Status:");
        reviewedByVal = addDetailRow(card, gbc, "Reviewed By Admin:");
        remarksVal    = addDetailRow(card, gbc, "Admin Committee Remarks:");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(Color.WHITE);
        editBtn = UIHelper.createPrimaryButton("Edit Startup Profile");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh Details");
        buttonRow.add(editBtn);
        buttonRow.add(refreshBtn);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 5, 5, 5);
        card.add(buttonRow, gbc);

        editBtn.addActionListener(e -> openEditDialog());
        refreshBtn.addActionListener(e -> refreshData());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIHelper.BG_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        wrapper.add(card, BorderLayout.NORTH);

        add(wrapper, BorderLayout.CENTER);

        refreshData();
    }

    public MyStartupPanel() {
        this(null);
    }

    public void refreshData() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        try {
            currentStartup = startupService.getStartupForFounder(founderId);
            if (currentStartup != null) {
                nameVal.setText(currentStartup.getName());
                founderVal.setText(currentStartup.getFounderName() != null ? currentStartup.getFounderName() : (currentUser != null ? currentUser.getFullName() : "Founder"));
                catVal.setText(currentStartup.getCategory());
                stageVal.setText(currentStartup.getStage() != null ? currentStartup.getStage() : "Ideation");
                descVal.setText("<html><body style='width: 450px;'>" + currentStartup.getDescription() + "</body></html>");

                String dateStr = currentStartup.getCreatedAt() != null ?
                        new SimpleDateFormat("dd MMM yyyy").format(currentStartup.getCreatedAt()) : "Recently";
                dateVal.setText(dateStr);

                statusVal.setText(currentStartup.getStatus());
                if ("Approved".equalsIgnoreCase(currentStartup.getStatus())) {
                    statusVal.setForeground(new Color(5, 150, 105));
                } else if ("Rejected".equalsIgnoreCase(currentStartup.getStatus())) {
                    statusVal.setForeground(new Color(220, 38, 38));
                } else {
                    statusVal.setForeground(new Color(217, 119, 6));
                }

                String reviewer = (currentStartup.getReviewedByAdminName() != null && !currentStartup.getReviewedByAdminName().isEmpty())
                        ? currentStartup.getReviewedByAdminName() : "Pending Committee Review";
                reviewedByVal.setText(reviewer);
                reviewedByVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
                reviewedByVal.setForeground(UIHelper.PRIMARY_COLOR);

                String remarks = (currentStartup.getAdminRemarks() != null && !currentStartup.getAdminRemarks().isEmpty())
                        ? currentStartup.getAdminRemarks() : "Application is in the queue for committee screening and evaluation.";
                remarksVal.setText("<html><body style='width: 450px; color: #334155;'>" + remarks + "</body></html>");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load startup details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel addDetailRow(JPanel form, GridBagConstraints gbc, String label) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIHelper.TEXT_DARK);
        gbc.gridx = 0;
        form.add(l, gbc);

        JLabel v = new JLabel("—");
        v.setFont(UIHelper.FONT_LABEL);
        v.setForeground(UIHelper.TEXT_DARK);
        gbc.gridx = 1;
        form.add(v, gbc);

        gbc.gridy++;
        return v;
    }

    private void openEditDialog() {
        if (currentStartup == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Startup Profile", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField nameField = UIHelper.createInputField(20);
        nameField.setText(currentStartup.getName());

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "Technology", "Healthcare & MedTech", "E-Commerce", "EdTech & Education",
                "AgriTech", "FinTech", "CleanTech & Sustainability", "Other"
        });
        categoryBox.setFont(UIHelper.FONT_LABEL);
        categoryBox.setSelectedItem(currentStartup.getCategory());

        JComboBox<String> stageBox = new JComboBox<>(new String[]{
                "Ideation", "Validation / Prototype", "Early Traction", "Scaling"
        });
        stageBox.setFont(UIHelper.FONT_LABEL);
        stageBox.setSelectedItem(currentStartup.getStage());

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(UIHelper.FONT_LABEL);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setText(currentStartup.getDescription());
        descArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        addDialogField(panel, gbc, "Startup Name:", nameField);
        addDialogField(panel, gbc, "Category:", categoryBox);
        addDialogField(panel, gbc, "Current Stage:", stageBox);
        addDialogField(panel, gbc, "Pitch / Value Proposition:", descScroll);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Save Changes");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(18, 5, 5, 5);
        panel.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Startup name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            currentStartup.setName(newName);
            currentStartup.setCategory((String) categoryBox.getSelectedItem());
            currentStartup.setStage((String) stageBox.getSelectedItem());
            currentStartup.setDescription(descArea.getText().trim());

            try {
                startupService.updateStartup(currentStartup);
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Startup details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving startup: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void addDialogField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        panel.add(l, gbc);
        gbc.gridy++;
        panel.add(comp, gbc);
    }
}
