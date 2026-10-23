package utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Styling helper providing a clean, modern, and human-crafted aesthetic
 * for the Startup Incubation System.
 */
public class UIHelper {

    // Modern professional palette
    public static final Color PRIMARY_COLOR = new Color(30, 58, 138);     // Indigo Navy #1e3a8a
    public static final Color PRIMARY_HOVER = new Color(37, 99, 235);     // Royal Blue #2563eb
    public static final Color PRIMARY_DARK  = new Color(15, 23, 42);      // Deep Slate #0f172a
    public static final Color ACCENT_COLOR  = new Color(13, 148, 136);    // Teal #0d9488

    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);    // Emerald #10b981
    public static final Color WARNING_COLOR = new Color(245, 158, 11);    // Amber #f59e0b
    public static final Color DANGER_COLOR  = new Color(239, 68, 68);     // Rose/Red #ef4444

    public static final Color BG_COLOR      = new Color(248, 250, 252);   // Light Slate #f8fafc
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color SIDEBAR_COLOR = new Color(15, 23, 42);      // Slate 900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);      // Slate 800
    public static final Color SIDEBAR_ACTIVE= new Color(30, 58, 138);

    public static final Color TEXT_DARK     = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED    = new Color(100, 116, 139);   // Slate 500
    public static final Color TEXT_LIGHT    = Color.WHITE;
    public static final Color BORDER_COLOR  = new Color(226, 232, 240);   // Slate 200

    // Clean typography using system native fonts
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SIDEBAR  = new Font("Segoe UI", Font.PLAIN, 14);

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(FONT_BUTTON);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(FONT_BUTTON);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(239, 246, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(FONT_BUTTON);
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel("  " + (status != null ? status : "Pending") + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);

        String s = status != null ? status.trim().toLowerCase() : "";
        if (s.contains("approved") || s.contains("active") || s.contains("completed") || s.contains("sanctioned")) {
            badge.setBackground(new Color(220, 252, 231)); // light green
            badge.setForeground(new Color(22, 101, 52));   // dark green
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(134, 239, 172), 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        } else if (s.contains("rejected") || s.contains("inactive") || s.contains("deactivated")) {
            badge.setBackground(new Color(254, 226, 226)); // light red
            badge.setForeground(new Color(153, 27, 27));   // dark red
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        } else {
            badge.setBackground(new Color(254, 243, 199)); // light amber
            badge.setForeground(new Color(146, 64, 14));   // dark amber
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(253, 230, 138), 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        }
        return badge;
    }

    public static JLabel createHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static JTextField createInputField(int columns) {
        JTextField field = new JTextField(columns);
        styleInput(field);
        return field;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInput(field);
        return field;
    }

    public static void styleInput(JTextField field) {
        field.setFont(FONT_LABEL);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_LABEL);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(titleLabel);
        return card;
    }

    public static JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_SIDEBAR);
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(SIDEBAR_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 10));
        button.setMaximumSize(new Dimension(240, 44));
        button.setPreferredSize(new Dimension(240, 44));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_HOVER);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_COLOR);
                button.setForeground(new Color(226, 232, 240));
            }
        });
        return button;
    }

    /**
     * Styles a JTable with padded rows, clear header, and custom cell badge renderers.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_LABEL);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Default padded cell renderer
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                String str = value != null ? value.toString().trim() : "";

                // Badge styling for status columns
                if (str.equalsIgnoreCase("Approved") || str.equalsIgnoreCase("Active") || str.equalsIgnoreCase("OPEN")) {
                    c.setForeground(new Color(5, 150, 105));
                    c.setFont(FONT_LABEL_BOLD);
                } else if (str.equalsIgnoreCase("Under Review") || str.equalsIgnoreCase("Pending")) {
                    c.setForeground(new Color(217, 119, 6));
                    c.setFont(FONT_LABEL_BOLD);
                } else if (str.equalsIgnoreCase("Rejected") || str.equalsIgnoreCase("Inactive") || str.equalsIgnoreCase("CANCELLED") || str.equalsIgnoreCase("Disabled")) {
                    c.setForeground(new Color(220, 38, 38));
                    c.setFont(FONT_LABEL_BOLD);
                } else {
                    if (!isSelected) {
                        c.setForeground(TEXT_DARK);
                    }
                    c.setFont(FONT_LABEL);
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }
    }
}
