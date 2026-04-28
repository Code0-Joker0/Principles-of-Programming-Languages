package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class GatewayGUI extends JPanel {

    private final MainDashboard dashboard;
    private JTable            table;
    private DefaultTableModel model;
    private JTextField        tfSearch, tfName, tfEndpoint;
    private JCheckBox         chkActive;
    private JLabel            lblCount;
    private JPanel            formPanel;
    private int               editingId = -1;

    public GatewayGUI(MainDashboard dashboard) {
        this.dashboard = dashboard;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildForm(),   BorderLayout.EAST);
        load(null);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Payment Gateways");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        lblCount = new JLabel("0 gateways");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title); left.add(Box.createHorizontalStrut(12)); left.add(lblCount);

        tfSearch = Theme.styledField("Search gateways...");
        tfSearch.setPreferredSize(new Dimension(220, 36));
        tfSearch.getDocument().addDocumentListener(UserGUI.docListener(() -> load(tfSearch.getText())));

        JButton btnAdd = Theme.primaryButton("+ Add Gateway");
        btnAdd.addActionListener(e -> openForm(-1, "", "", true));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch); right.add(btnAdd);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new String[]{"ID", "Gateway Name", "API Endpoint", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                cell.setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                boolean active = Boolean.TRUE.equals(v);
                cell.add(Theme.badge(active ? "Active" : "Inactive", active ? Theme.SUCCESS : Theme.MUTED));
                return cell;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0) openForm(
                (int)     model.getValueAt(row, 0),
                (String)  model.getValueAt(row, 1),
                (String)  model.getValueAt(row, 2),
                (Boolean) model.getValueAt(row, 3));
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.add(Theme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildForm() {
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Theme.BG_CARD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(24, 20, 24, 20)));
        formPanel.setPreferredSize(new Dimension(280, 0));
        formPanel.setVisible(false);

        JLabel heading = new JLabel("Gateway Details");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfName     = Theme.styledField("e.g. Stripe");
        tfEndpoint = Theme.styledField("https://api.example.com");
        chkActive  = new JCheckBox("Mark as Active");
        chkActive.setBackground(Theme.BG_CARD);
        chkActive.setForeground(Theme.TEXT_PRIMARY);
        chkActive.setFont(Theme.FONT_BODY);
        chkActive.setSelected(true);
        chkActive.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JTextField f : new JTextField[]{tfName, tfEndpoint})
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton btnSave   = Theme.primaryButton("Save");
        JButton btnCancel = Theme.ghostButton("Cancel");
        JButton btnDelete = Theme.dangerButton("Delete");
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> closeForm());
        btnDelete.addActionListener(e -> delete());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Theme.BG_CARD);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(btnSave); btnRow.add(btnCancel);

        formPanel.add(heading);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(Theme.fieldLabel("Gateway Name")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfName);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("API Endpoint")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfEndpoint);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(chkActive);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnRow);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnDelete);
        return formPanel;
    }

    private void load(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM gateways" +
            (search != null && !search.isBlank() ? " WHERE gateway_name LIKE ? OR api_endpoint LIKE ?" : "") +
            " ORDER BY gateway_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null && !search.isBlank()) {
                ps.setString(1, "%" + search + "%");
                ps.setString(2, "%" + search + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)});
            lblCount.setText(model.getRowCount() + " gateway" + (model.getRowCount() != 1 ? "s" : ""));
        } catch (SQLException e) { err(e); }
    }

    private void openForm(int id, String name, String endpoint, boolean active) {
        editingId = id;
        tfName.setText(name); tfEndpoint.setText(endpoint); chkActive.setSelected(active);
        formPanel.setVisible(true);
        revalidate();
        tfName.requestFocus();
    }

    private void closeForm() {
        formPanel.setVisible(false);
        table.clearSelection();
        editingId = -1;
        revalidate();
    }

    private void save() {
        String name = tfName.getText().trim(), endpoint = tfEndpoint.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gateway name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = DBConnection.getConnection()) {
            if (editingId < 0) {
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO gateways (gateway_name,api_endpoint,is_active) VALUES (?,?,?)");
                ps.setString(1, name); ps.setString(2, endpoint); ps.setBoolean(3, chkActive.isSelected());
                ps.executeUpdate();
                UserGUI.audit(c, "gateways", "INSERT", 0, "Added gateway: " + name);
            } else {
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE gateways SET gateway_name=?,api_endpoint=?,is_active=? WHERE gateway_id=?");
                ps.setString(1, name); ps.setString(2, endpoint);
                ps.setBoolean(3, chkActive.isSelected()); ps.setInt(4, editingId);
                ps.executeUpdate();
                UserGUI.audit(c, "gateways", "UPDATE", editingId, "Updated gateway: " + name);
            }
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    private void delete() {
        if (editingId < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete gateway #" + editingId + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM gateways WHERE gateway_id=?")) {
            ps.setInt(1, editingId); ps.executeUpdate();
            UserGUI.audit(c, "gateways", "DELETE", editingId, "Deleted gateway #" + editingId);
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    private void err(SQLException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
}
