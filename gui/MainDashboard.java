package gui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MainDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JLabel     lblUsers, lblTransactions, lblRevenue, lblGateways;

    // Sidebar nav buttons kept as fields so we can highlight the active one
    private JButton[] navButtons;
    private static final String[] NAV_LABELS = {
        "  \uD83D\uDCCA  Overview",
        "  \uD83D\uDC64  Users",
        "  \uD83D\uDCB3  Transactions",
        "  \uD83D\uDD17  Gateways",
        "  \uD83D\uDCCB  Payment Logs",
        "  \uD83D\uDD0D  Audit Trail"
    };
    private static final String[] CARD_KEYS = {
        "overview", "users", "transactions", "gateways", "logs", "audit"
    };

    public MainDashboard() {
        Theme.applyGlobal();
        setTitle("PayFlow — Payment Gateway System");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(),  BorderLayout.WEST);
        add(buildContent(),  BorderLayout.CENTER);

        showCard("overview");
        setVisible(true);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Logo area
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        logo.setBackground(Theme.BG_SIDEBAR);
        logo.setMaximumSize(new Dimension(210, 70));
        logo.setMinimumSize(new Dimension(210, 70));
        logo.setPreferredSize(new Dimension(210, 70));
        JLabel logoLabel = new JLabel("\uD83D\uDCB8 PayFlow");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoLabel.setForeground(Theme.ACCENT);
        logo.add(logoLabel);
        sidebar.add(logo);

        sidebar.add(sectionLabel("NAVIGATION"));

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = navButton(NAV_LABELS[i]);
            navButtons[i].addActionListener(e -> {
                showCard(CARD_KEYS[idx]);
                setActiveNav(idx);
                if (CARD_KEYS[idx].equals("overview")) refreshStats();
            });
            sidebar.add(navButtons[i]);
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("  v1.0  •  DBMS Project", SwingConstants.LEFT);
        version.setFont(Theme.FONT_SMALL);
        version.setForeground(Theme.MUTED);
        version.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 0));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(version);

        return sidebar;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Theme.MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(18, 0, 6, 0));
        l.setMaximumSize(new Dimension(210, 30));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                if (active) {
                    g2.setColor(new Color(99, 149, 255, 30));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    g2.setColor(Theme.ACCENT);
                    g2.fillRoundRect(0, 8, 3, getHeight()-16, 3, 3);
                } else if (hovered) {
                    g2.setColor(new Color(255,255,255, 10));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                g2.setColor(active ? Theme.ACCENT : (hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY));
                g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
                g2.drawString(getText(), 18, getHeight()/2 + 5);
                g2.dispose();
            }
        };
        b.setMaximumSize(new Dimension(210, 42));
        b.setMinimumSize(new Dimension(210, 42));
        b.setPreferredSize(new Dimension(210, 42));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setActiveNav(int idx) {
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].putClientProperty("active", i == idx);
            navButtons[i].repaint();
        }
    }

    // ── Content area with CardLayout ──────────────────────────────────────────
    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_DARK);

        contentPanel.add(buildOverview(),          "overview");
        contentPanel.add(new UserGUI(this),        "users");
        contentPanel.add(new TransactionGUI(this), "transactions");
        contentPanel.add(new GatewayGUI(this),     "gateways");
        contentPanel.add(new PaymentLogGUI(this),  "logs");
        contentPanel.add(new AuditTrailGUI(this),  "audit");

        return contentPanel;
    }

    public void showCard(String key) {
        cardLayout.show(contentPanel, key);
    }

    // ── Overview panel ────────────────────────────────────────────────────────
    private JPanel buildOverview() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // Header
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        p.add(title, BorderLayout.NORTH);

        // Stats grid
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setBackground(Theme.BG_DARK);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblUsers        = statValue("0");
        lblTransactions = statValue("0");
        lblRevenue      = statValue("$0");
        lblGateways     = statValue("0");

        stats.add(statCard("\uD83D\uDC64  Total Users",        lblUsers,        Theme.ACCENT));
        stats.add(statCard("\uD83D\uDCB3  Transactions",       lblTransactions, Theme.SUCCESS));
        stats.add(statCard("\uD83D\uDCB0  Total Revenue",      lblRevenue,      Theme.WARNING));
        stats.add(statCard("\uD83D\uDD17  Active Gateways",    lblGateways,     new Color(180, 100, 255)));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG_DARK);
        center.add(stats);
        center.add(Box.createVerticalStrut(28));
        center.add(buildQuickActions());

        p.add(center, BorderLayout.CENTER);
        refreshStats();
        return p;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);

        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel statValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private JPanel buildQuickActions() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);

        JLabel heading = new JLabel("Quick Actions");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_SECONDARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        p.add(heading, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btns.setBackground(Theme.BG_DARK);

        JButton b1 = Theme.primaryButton("+ Add User");
        JButton b2 = Theme.primaryButton("+ New Transaction");
        JButton b3 = Theme.ghostButton("View Logs");
        JButton b4 = Theme.ghostButton("Audit Trail");

        b1.addActionListener(e -> { showCard("users");        setActiveNav(1); });
        b2.addActionListener(e -> { showCard("transactions"); setActiveNav(2); });
        b3.addActionListener(e -> { showCard("logs");         setActiveNav(4); });
        b4.addActionListener(e -> { showCard("audit");        setActiveNav(5); });

        btns.add(b1); btns.add(b2); btns.add(b3); btns.add(b4);
        p.add(btns, BorderLayout.CENTER);
        return p;
    }

    public void refreshStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override protected int[] doInBackground() throws Exception {
                int[] data = new int[4];
                try (Connection c = DBConnection.getConnection()) {
                    data[0] = c.createStatement().executeQuery("SELECT COUNT(*) FROM users").getInt(1);
                    data[1] = c.createStatement().executeQuery("SELECT COUNT(*) FROM transactions").getInt(1);
                    ResultSet rs = c.createStatement().executeQuery("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE status='SUCCESS'");
                    rs.next(); data[2] = rs.getInt(1);
                    data[3] = c.createStatement().executeQuery("SELECT COUNT(*) FROM gateways WHERE is_active=1").getInt(1);
                }
                return data;
            }
            @Override protected void done() {
                try {
                    int[] d = get();
                    lblUsers.setText(String.valueOf(d[0]));
                    lblTransactions.setText(String.valueOf(d[1]));
                    lblRevenue.setText("$" + d[2]);
                    lblGateways.setText(String.valueOf(d[3]));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainDashboard::new);
    }
}
