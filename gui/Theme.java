package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class Theme {
    public static final Color BG_DARK      = new Color(15,  17,  26);
    public static final Color BG_CARD      = new Color(24,  27,  40);
    public static final Color BG_SIDEBAR   = new Color(18,  20,  32);
    public static final Color BG_INPUT     = new Color(30,  34,  52);
    public static final Color BG_TABLE_ROW = new Color(28,  32,  46);
    public static final Color BG_TABLE_ALT = new Color(22,  26,  38);
    public static final Color BG_HOVER     = new Color(40,  45,  65);
    public static final Color BG_SELECTED  = new Color(55,  90, 160);

    public static final Color ACCENT       = new Color(99, 149, 255);
    public static final Color ACCENT_HOVER = new Color(120, 170, 255);
    public static final Color SUCCESS      = new Color(52, 199, 120);
    public static final Color WARNING      = new Color(255, 184,  64);
    public static final Color DANGER       = new Color(255,  75,  75);
    public static final Color MUTED        = new Color(120, 130, 160);

    public static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    public static final Color TEXT_SECONDARY = new Color(150, 160, 195);
    public static final Color BORDER_COLOR   = new Color(40,  46,  70);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 12);

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }
    public static Border focusBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(MUTED);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_BODY);
        f.setBorder(inputBorder());
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(focusBorder()); }
            public void focusLost(FocusEvent e)   { f.setBorder(inputBorder()); }
        });
        return f;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? BG_SELECTED : BG_INPUT);
                setForeground(TEXT_PRIMARY);
                setFont(FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        return cb;
    }

    public static JButton primaryButton(String text) {
        return styledButton(text, ACCENT, ACCENT_HOVER, Color.WHITE);
    }
    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, new Color(255, 100, 100), Color.WHITE);
    }
    public static JButton ghostButton(String text) {
        return styledButton(text, BG_INPUT, BG_HOVER, TEXT_PRIMARY);
    }

    private static JButton styledButton(String text, Color base, Color hover, Color fg) {
        JButton b = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? hover : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(fg);
                g2.setFont(FONT_BODY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(120, 36));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_TABLE_ROW);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(BG_SELECTED);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_SIDEBAR);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(FONT_SMALL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? BG_SELECTED : (row % 2 == 0 ? BG_TABLE_ROW : BG_TABLE_ALT));
                setForeground(sel ? Color.WHITE : TEXT_PRIMARY);
                setFont(FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }

    public static JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(BG_TABLE_ROW);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return sp;
    }

    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(bg);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        l.setForeground(bg);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    public static Color statusColor(String status) {
        if (status == null) return MUTED;
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> SUCCESS;
            case "FAILED"  -> DANGER;
            case "PENDING" -> WARNING;
            default        -> MUTED;
        };
    }

    public static Color operationColor(String op) {
        if (op == null) return MUTED;
        return switch (op.toUpperCase()) {
            case "INSERT" -> SUCCESS;
            case "UPDATE" -> WARNING;
            case "DELETE" -> DANGER;
            default       -> MUTED;
        };
    }

    public static void applyGlobal() {
        UIManager.put("OptionPane.background",        BG_CARD);
        UIManager.put("Panel.background",             BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background",            BG_INPUT);
        UIManager.put("Button.foreground",            TEXT_PRIMARY);
        UIManager.put("ComboBox.background",          BG_INPUT);
        UIManager.put("ComboBox.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.background",         BG_INPUT);
        UIManager.put("TextField.foreground",         TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",    ACCENT);
        UIManager.put("Label.foreground",             TEXT_PRIMARY);
    }

    /** Shared field label used in all form panels */
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
