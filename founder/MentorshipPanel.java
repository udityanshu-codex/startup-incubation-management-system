package gui.founder;

import model.Mentor;
import model.MentorshipRequest;
import model.Startup;
import model.User;
import service.MentorService;
import service.StartupService;
import utils.SessionManager;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Mentorship panel for founders to explore available industry mentors,
 * request mentorship sessions, and track assignments.
 */
public class MentorshipPanel extends JPanel {

    private final User currentUser;
    private final MentorService mentorService = new MentorService();
    private final StartupService startupService = new StartupService();

    private JLabel mentorNameVal, expertiseVal, emailVal, phoneVal, statusVal, remarksVal;
    private JTable requestsTable;
    private DefaultTableModel requestsModel;
    private List<MentorshipRequest> requestList;

    public MentorshipPanel(User user) {
        this.currentUser = user != null ? user : SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Mentorship & Advisory");
        add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIHelper.BG_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // 1. Current Mentor Status Card
        content.add(buildCurrentMentorCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Mentorship Requests History Table
        content.add(buildRequestsTableCard());

        add(content, BorderLayout.CENTER);

        refreshData();
    }

    public MentorshipPanel() {
        this(null);
    }

    private JPanel buildCurrentMentorCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        mentorNameVal = addRow(card, gbc, "Assigned Mentor:");
        expertiseVal  = addRow(card, gbc, "Domain Expertise:");
        emailVal      = addRow(card, gbc, "Mentor Email:");
        phoneVal      = addRow(card, gbc, "Contact Phone:");
        statusVal     = addRow(card, gbc, "Advisory Status:");
        remarksVal    = addRow(card, gbc, "Admin Notes / Plan:");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setBackground(Color.WHITE);
        JButton requestBtn = UIHelper.createPrimaryButton("Request Mentorship Session");
        JButton viewDirBtn = UIHelper.createSecondaryButton("Browse All Mentors");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        buttonRow.add(requestBtn);
        buttonRow.add(viewDirBtn);
        buttonRow.add(refreshBtn);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 5, 5, 5);
        card.add(buttonRow, gbc);

        requestBtn.addActionListener(e -> openRequestDialog());
        viewDirBtn.addActionListener(e -> openMentorsDirectoryDialog());
        refreshBtn.addActionListener(e -> refreshData());

        return card;
    }

    private JPanel buildRequestsTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel subHeading = new JLabel("My Mentorship Requests");
        subHeading.setFont(UIHelper.FONT_HEADING);
        subHeading.setForeground(UIHelper.TEXT_DARK);
        card.add(subHeading, BorderLayout.NORTH);

        String[] cols = {"Req ID", "Topic", "Mentor Assigned", "Status", "Committee Remarks"};
        requestsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        requestsTable = new JTable(requestsModel);
        UIHelper.styleTable(requestsTable);

        JScrollPane scroll = new JScrollPane(requestsTable);
        scroll.setPreferredSize(new Dimension(800, 160));
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(scroll, BorderLayout.CENTER);

        card.add(wrapper, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        try {
            requestList = mentorService.getFounderRequests(founderId);
            requestsModel.setRowCount(0);

            MentorshipRequest activeAssigned = null;

            for (MentorshipRequest r : requestList) {
                if ("Assigned".equalsIgnoreCase(r.getStatus()) && activeAssigned == null) {
                    activeAssigned = r;
                }
                requestsModel.addRow(new Object[]{
                        "REQ-" + String.format("%03d", r.getId()),
                        r.getTopic(),
                        r.getMentorName() != null ? r.getMentorName() : "To be assigned",
                        r.getStatus(),
                        r.getAdminRemarks() != null ? r.getAdminRemarks() : "Pending review"
                });
            }

            if (activeAssigned != null) {
                mentorNameVal.setText(activeAssigned.getMentorName());
                expertiseVal.setText(activeAssigned.getTopic());
                emailVal.setText("mentor@example.com");
                phoneVal.setText("+91 98111 22233");
                statusVal.setText("Active Advisory Assignment");
                statusVal.setForeground(new Color(5, 150, 105));
                remarksVal.setText(activeAssigned.getAdminRemarks() != null ? activeAssigned.getAdminRemarks() : "Session assigned");
            } else if (!requestList.isEmpty()) {
                MentorshipRequest latest = requestList.get(0);
                mentorNameVal.setText(latest.getMentorName() != null ? latest.getMentorName() : "Pending Assignment");
                expertiseVal.setText(latest.getTopic());
                emailVal.setText("—");
                phoneVal.setText("—");
                statusVal.setText(latest.getStatus());
                statusVal.setForeground(new Color(217, 119, 6));
                remarksVal.setText(latest.getAdminRemarks() != null ? latest.getAdminRemarks() : "Under review");
            } else {
                mentorNameVal.setText("None Assigned Yet");
                expertiseVal.setText("—");
                emailVal.setText("—");
                phoneVal.setText("—");
                statusVal.setText("No Active Requests");
                statusVal.setForeground(UIHelper.TEXT_MUTED);
                remarksVal.setText("Click 'Request Mentorship Session' to get paired with an expert.");
            }
        } catch (Exception e) {
            System.err.println("Error refreshing mentorship: " + e.getMessage());
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

    private void openRequestDialog() {
        int founderId = currentUser != null ? currentUser.getId() : 2;
        Startup startup;
        try {
            startup = startupService.getStartupForFounder(founderId);
            if (startup == null) {
                JOptionPane.showMessageDialog(this, "Please set up your startup profile first under 'My Startup'.", "Startup Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load startup: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Mentor> mentors;
        try {
            mentors = mentorService.getActiveMentors();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load mentors: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Request Mentorship Session", true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel startupLabel = new JLabel("Startup: " + startup.getName());
        startupLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        form.add(startupLabel, gbc);

        JLabel topicLabel = new JLabel("Mentorship Area / Topic *:");
        topicLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(topicLabel, gbc);

        JTextField topicField = UIHelper.createInputField(20);
        gbc.gridy++;
        form.add(topicField, gbc);

        JLabel mentorChoiceLabel = new JLabel("Preferred Mentor (Optional):");
        mentorChoiceLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(mentorChoiceLabel, gbc);

        JComboBox<Object> mentorBox = new JComboBox<>();
        mentorBox.addItem("Any Qualified Mentor (Committee Assignment)");
        for (Mentor m : mentors) {
            mentorBox.addItem(m);
        }
        mentorBox.setFont(UIHelper.FONT_LABEL);
        mentorBox.setPreferredSize(new Dimension(300, 34));
        gbc.gridy++;
        form.add(mentorBox, gbc);

        JLabel notesLabel = new JLabel("Specific Questions / Context for Mentor:");
        notesLabel.setFont(UIHelper.FONT_LABEL_BOLD);
        gbc.gridy++;
        form.add(notesLabel, gbc);

        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setFont(UIHelper.FONT_LABEL);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(notesArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        gbc.gridy++;
        form.add(scroll, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton submitBtn = UIHelper.createPrimaryButton("Submit Request");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(18, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String topic = topicField.getText().trim();
            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a topic or area for mentorship.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer mentorId = null;
            Object selected = mentorBox.getSelectedItem();
            if (selected instanceof Mentor) {
                mentorId = ((Mentor) selected).getId();
            }

            try {
                mentorService.requestMentorship(startup.getId(), mentorId, topic, notesArea.getText().trim());
                dialog.dispose();
                refreshData();
                JOptionPane.showMessageDialog(this, "Mentorship request submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void openMentorsDirectoryDialog() {
        try {
            List<Mentor> mentors = mentorService.getActiveMentors();
            String[] cols = {"Name", "Expertise", "Email", "Phone", "Bio"};
            DefaultTableModel dm = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Mentor m : mentors) {
                dm.addRow(new Object[]{m.getName(), m.getExpertise(), m.getEmail(), m.getPhone(), m.getBio()});
            }
            JTable t = new JTable(dm);
            UIHelper.styleTable(t);
            JScrollPane sp = new JScrollPane(t);
            sp.setPreferredSize(new Dimension(750, 300));
            JOptionPane.showMessageDialog(this, sp, "Active Mentors Directory", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load mentors: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
