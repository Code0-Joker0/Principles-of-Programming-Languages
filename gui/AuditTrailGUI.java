package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AuditTrailGUI extends JPanel {

    private final MainDashboard dashboard;
    private JTable            table;
    private DefaultTableModel model;
    private JTextField        tfSearch;
    private JComboBox<String> cbOpFilter;
    private JLabel            lblCount;

    public AuditTrailGUI(MainDashboard dashboard) {
        this.dashboard = dashboard;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildInfo(),   BorderLayout.SOUTH);
        load(null, "ALL");
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Audit Trail");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        lblCount = new JLabel("0 records");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title); left.add(Box.createHorizontalStrut(12)); left.add(lblCount);

        tfSearch = Theme.styledField("Search by table or notes...");
        tfSearch.setPreferredSize(new Dimension(240, 36));
        tfSearch.getDocument().addDocumentListener(UserGUI.docListener(this::reload));

        cbOpFilter = Theme.styledCombo("ALL", "INSERT", "UPDATE", "DELETE");
        cbOpFilter.setPreferredSize(new Dimension(120, 36));
        cbOpFilter.addActionListener(e -> reload());

        JButton btnRefresh = Theme.ghostButton("Refresh");
        btnRefresh.addActionListener(e -> reload());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch); right.add(cbOpFilter); right.add(btnRefresh);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(
            new String[]{"Audit ID", "Table", "Operation", "Record ID", "Performed At", "Notes"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        // Operation badge
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                cell.setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                String op = v == null ? "" : v.toString();
                cell.add(Theme.badge(op, Theme.operationColor(op)));
                return cell;
            }
        });

        // Table name — monospace accent
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                setForeground(sel ? Color.WHITE : Theme.ACCENT);
                setFont(Theme.FONT_MONO);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        // Notes — muted
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                setForeground(sel ? Color.WHITE : Theme.TEXT_SECONDARY);
                setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.add(Theme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildInfo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        p.setBackground(Theme.BG_DARK);
        JLabel info = new JLabel("\uD83D\uDEC1  Audit records are generated automatically when users, transactions, or gateways are modified.");
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.MUTED);
        p.add(info);
        return p;
    }

    private void reload() {
        load(tfSearch.getText(), (String) cbOpFilter.getSelectedItem());
    }

    private void load(String search, String opFilter) {
        model.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_trail WHERE 1=1");
        if (search != null && !search.isBlank())
            sql.append(" AND (table_name LIKE ? OR notes LIKE ?)");
        if (opFilter != null && !opFilter.equals("ALL"))
            sql.append(" AND operation=?");
        sql.append(" ORDER BY audit_id DESC");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (search != null && !search.isBlank()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            if (opFilter != null && !opFilter.equals("ALL"))
                ps.setString(idx, opFilter);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getInt(4), rs.getTimestamp(5), rs.getString(6)});
            lblCount.setText(model.getRowCount() + " record" + (model.getRowCount() != 1 ? "s" : ""));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
