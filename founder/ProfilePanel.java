package gui.founder;

import model.Startup;
import model.User;
import service.AuthService;
import service.StartupService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Profile panel for founders to view credentials, update personal details,
 * and securely change their password using BCrypt hashing.
 */
public class ProfilePanel extends JPanel {

    private final User currentUser;
    private final AuthService authService = new AuthService();
    private final StartupService startupService = new StartupService();

    private JLabel nameVal, emailVal, phoneVal, roleVal, startupVal;

    public ProfilePanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Founder Account & Profile Settings");
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

        nameVal    = addRow(card, gbc, "Full Name:");
        emailVal   = addRow(card, gbc, "Login Email:");
        phoneVal   = addRow(card, gbc, "Phone Number:");
        roleVal    = addRow(card, gbc, "System Role:");
        startupVal = addRow(card, gbc, "Associated Startup:");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(Color.WHITE);
        JButton editBtn = UIHelper.createPrimaryButton("Edit Contact Info");
        JButton passBtn = UIHelper.createSecondaryButton("Change Password");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        buttonRow.add(editBtn);
        buttonRow.add(passBtn);
        buttonRow.add(refreshBtn);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 5, 5, 5);
        card.add(buttonRow, gbc);

        editBtn.addActionListener(e -> openEditProfileDialog());
        passBtn.addActionListener(e -> openChangePasswordDialog());
        refreshBtn.addActionListener(e -> refreshData());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIHelper.BG_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        wrapper.add(card, BorderLayout.NORTH);

        add(wrapper, BorderLayout.CENTER);

        refreshData();
    }

    public ProfilePanel() {
        this(null);
    }

    public void refreshData() {
        User u = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser() : currentUser;
        if (u != null) {
            nameVal.setText(u.getFullName());
            emailVal.setText(u.getEmail());
            phoneVal.setText(u.getPhone() != null && !u.getPhone().isEmpty() ? u.getPhone() : "Not provided");
            roleVal.setText(u.getRole());

            try {
                Startup s = startupService.getStartupForFounder(u.getId());
                startupVal.setText(s != null ? s.getName() : "None registered");
            } catch (Exception e) {
                startupVal.setText("—");
            }
        }
    }

    private JLabel addRow(JPanel form, GridBagConstraints gbc, String label) {
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

    private void openEditProfileDialog() {
        User u = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser() : currentUser;
        if (u == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.setSize(440, 260);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel nameLbl = new JLabel("Full Name *:");
        nameLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        form.add(nameLbl, gbc);

        JTextField nameField = UIHelper.createInputField(20);
        nameField.setText(u.getFullName());
        gbc.gridy++;
        form.add(nameField, gbc);

        JLabel phoneLbl = new JLabel("Contact Phone Number:");
        phoneLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(phoneLbl, gbc);

        JTextField phoneField = UIHelper.createInputField(20);
        phoneField.setText(u.getPhone());
        gbc.gridy++;
        form.add(phoneField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Save");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be blank.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                authService.updateProfile(u.getId(), newName, phoneField.getText().trim());
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void openChangePasswordDialog() {
        User u = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser() : currentUser;
        if (u == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Account Password", true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel oldLbl = new JLabel("Current Password *:");
        oldLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        form.add(oldLbl, gbc);

        JPasswordField oldField = UIHelper.createPasswordField(20);
        gbc.gridy++;
        form.add(oldField, gbc);

        JLabel newLbl = new JLabel("New Password (min 6 chars) *:");
        newLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(newLbl, gbc);

        JPasswordField newField = UIHelper.createPasswordField(20);
        gbc.gridy++;
        form.add(newField, gbc);

        JLabel confLbl = new JLabel("Confirm New Password *:");
        confLbl.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(confLbl, gbc);

        JPasswordField confField = UIHelper.createPasswordField(20);
        gbc.gridy++;
        form.add(confField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton("Update Password");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String oldPass = new String(oldField.getPassword());
            String newPass = new String(newField.getPassword());
            String confPass = new String(confField.getPassword());

            try {
                authService.changePassword(u.getId(), oldPass, newPass, confPass);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Password changed successfully! Please use your new password next time.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Password Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }
}
