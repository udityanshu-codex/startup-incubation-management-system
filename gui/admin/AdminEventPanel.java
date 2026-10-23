package gui.admin;

import model.Event;
import service.EventService;
import utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for organizing, publishing, editing, and deleting incubation events.
 */
public class AdminEventPanel extends JPanel {

    private final EventService eventService = new EventService();
    private JTable table;
    private DefaultTableModel model;
    private List<Event> eventList;

    public AdminEventPanel() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel heading = UIHelper.createHeading("Incubation Events & Competitions Management");
        add(heading, BorderLayout.NORTH);

        String[] columns = {"ID", "Event Title", "Date", "Venue", "Capacity", "Registrations", "Status"};
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
        JButton addBtn = UIHelper.createPrimaryButton("Publish New Event");
        JButton editBtn = UIHelper.createSecondaryButton("Edit Event");
        JButton deleteBtn = UIHelper.createDangerButton("Cancel / Delete Event");
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");

        buttonRow.add(addBtn);
        buttonRow.add(editBtn);
        buttonRow.add(deleteBtn);
        buttonRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UIHelper.BG_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> openEventFormDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1 || eventList == null || row >= eventList.size()) {
                JOptionPane.showMessageDialog(this, "Please select an event to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openEventFormDialog(eventList.get(row));
        });
        deleteBtn.addActionListener(e -> handleDeleteEvent());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            eventList = eventService.getAllEvents();
            for (Event ev : eventList) {
                model.addRow(new Object[]{
                        "EV-" + String.format("%03d", ev.getId()),
                        ev.getTitle(),
                        ev.getEventDate(),
                        ev.getVenue(),
                        ev.getCapacity(),
                        ev.getRegisteredCount(),
                        ev.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load events: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEventFormDialog(Event existingEvent) {
        boolean isEdit = existingEvent != null;
        String title = isEdit ? "Edit Incubation Event" : "Publish New Incubation Event";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
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

        JTextField titleField = UIHelper.createInputField(20);
        JTextField dateField = UIHelper.createInputField(20);
        JTextField venueField = UIHelper.createInputField(20);
        JTextField capField = UIHelper.createInputField(20);

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(UIHelper.FONT_LABEL);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(descArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        if (isEdit) {
            titleField.setText(existingEvent.getTitle());
            dateField.setText(existingEvent.getEventDate());
            venueField.setText(existingEvent.getVenue());
            capField.setText(String.valueOf(existingEvent.getCapacity()));
            descArea.setText(existingEvent.getDescription());
        } else {
            dateField.setText("15 Oct 2026");
            capField.setText("50");
        }

        addField(form, gbc, "Event Title *:", titleField);
        addField(form, gbc, "Event Date & Time *:", dateField);
        addField(form, gbc, "Venue / Room *:", venueField);
        addField(form, gbc, "Participant Capacity *:", capField);
        addField(form, gbc, "Description & Agenda:", scroll);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);
        JButton saveBtn = UIHelper.createPrimaryButton(isEdit ? "Update Event" : "Publish Event");
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        gbc.gridy++;
        gbc.insets = new Insets(16, 5, 5, 5);
        form.add(btnRow, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = dateField.getText().trim();
            String v = venueField.getText().trim();
            String cStr = capField.getText().trim();
            int cap;
            try {
                cap = Integer.parseInt(cStr);
                if (cap <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid positive capacity number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (isEdit) {
                    existingEvent.setTitle(t);
                    existingEvent.setEventDate(d);
                    existingEvent.setVenue(v);
                    existingEvent.setCapacity(cap);
                    existingEvent.setDescription(descArea.getText().trim());
                    eventService.updateEvent(existingEvent);
                    JOptionPane.showMessageDialog(this, "Event updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    eventService.createEvent(t, d, v, cap, descArea.getText().trim());
                    JOptionPane.showMessageDialog(this, "Event published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                refreshData();
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

    private void handleDeleteEvent() {
        int row = table.getSelectedRow();
        if (row == -1 || eventList == null || row >= eventList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Event ev = eventList.get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete event '" + ev.getTitle() + "'?\nThis will cancel all registered seats.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                eventService.deleteEvent(ev.getId());
                refreshData();
                JOptionPane.showMessageDialog(this, "Event deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
