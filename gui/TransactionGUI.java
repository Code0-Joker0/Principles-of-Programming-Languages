package gui;

import db.DBConnection;
import TransactionPackage.TransactionManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class TransactionGUI extends JPanel {

    private final MainDashboard   dashboard;
    private JTable                table;
    private DefaultTableModel     model;
    private JTextField            tfSearch, tfAmount;
    private JComboBox<String>     cbFilterStatus, cbUser, cbGateway, cbStatus;
    private JLabel                lblCount;
    private JPanel                formPanel;
    private int                   editingId = -1;
    private final TransactionManager manager = new TransactionManager();

    public TransactionGUI(MainDashboard dashboard) {
        this.dashboard = dashboard;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildForm(),   BorderLayout.EAST);
        loadDropdowns();
        reload();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Transactions");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        lblCount = new JLabel("0 records");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title); left.add(Box.createHorizontalStrut(12)); left.add(lblCount);

        tfSearch = Theme.styledField("Search by user or gateway...");
        tfSearch.setPreferredSize(new Dimension(220, 36));
        tfSearch.getDocument().addDocumentListener(UserGUI.docListener(this::reload));

        cbFilterStatus = Theme.styledCombo("ALL", "PENDING", "SUCCESS", "FAILED");
        cbFilterStatus.setPreferredSize(new Dimension(120, 36));
        cbFilterStatus.addActionListener(e -> reload());

        JButton btnAdd = Theme.primaryButton("+ New");
        btnAdd.addActionListener(e -> openForm(-1));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch); right.add(cbFilterStatus); right.add(btnAdd);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(
            new String[]{"ID", "User", "Gateway", "Amount", "Status", "Created At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                cell.setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                String s = v == null ? "" : v.toString();
                cell.add(Theme.badge(s, Theme.statusColor(s)));
                return cell;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0) openForm((int) model.getValueAt(row, 0));
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
        formPanel.setPreferredSize(new Dimension(290, 0));
        formPanel.setVisible(false);

        JLabel heading = new JLabel("Transaction");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbUser    = Theme.styledCombo();
        cbGateway = Theme.styledCombo();
        cbStatus  = Theme.styledCombo("PENDING", "SUCCESS", "FAILED");
        tfAmount  = Theme.styledField("0.00");
        for (JComponent c : new JComponent[]{cbUser, cbGateway, cbStatus, tfAmount})
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

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
        formPanel.add(Theme.fieldLabel("User"));       formPanel.add(Box.createVerticalStrut(4)); formPanel.add(cbUser);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Gateway"));    formPanel.add(Box.createVerticalStrut(4)); formPanel.add(cbGateway);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Amount ($)")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfAmount);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Status"));     formPanel.add(Box.createVerticalStrut(4)); formPanel.add(cbStatus);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnRow);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnDelete);
        return formPanel;
    }

    private void loadDropdowns() {
        cbUser.removeAllItems(); cbGateway.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT user_id, name FROM users ORDER BY name");
            while (rs.next()) cbUser.addItem(rs.getInt(1) + " \u2014 " + rs.getString(2));
            rs = st.executeQuery("SELECT gateway_id, gateway_name FROM gateways WHERE is_active=1");
            while (rs.next()) cbGateway.addItem(rs.getInt(1) + " \u2014 " + rs.getString(2));
        } catch (SQLException e) { err(e); }
    }

    private void reload() {
        model.setRowCount(0);
        String search = tfSearch != null ? tfSearch.getText() : null;
        String statusFilter = cbFilterStatus != null ? (String) cbFilterStatus.getSelectedItem() : "ALL";
        StringBuilder sql = new StringBuilder(
            "SELECT t.transaction_id, u.name, g.gateway_name, t.amount, t.status, t.created_at " +
            "FROM transactions t JOIN users u ON t.user_id=u.user_id JOIN gateways g ON t.gateway_id=g.gateway_id WHERE 1=1");
        if (search != null && !search.isBlank())
            sql.append(" AND (u.name LIKE ? OR g.gateway_name LIKE ?)");
        if (statusFilter != null && !statusFilter.equals("ALL"))
            sql.append(" AND t.status=?");
        sql.append(" ORDER BY t.transaction_id DESC");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (search != null && !search.isBlank()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            if (statusFilter != null && !statusFilter.equals("ALL"))
                ps.setString(idx, statusFilter);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                    String.format("$%.2f", rs.getDouble(4)), rs.getString(5), rs.getTimestamp(6)});
            if (lblCount != null)
                lblCount.setText(model.getRowCount() + " record" + (model.getRowCount() != 1 ? "s" : ""));
        } catch (SQLException e) { err(e); }
    }

    private void openForm(int id) {
        loadDropdowns();
        editingId = id;
        if (id > 0) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tfAmount.setText(model.getValueAt(row, 3).toString().replace("$", ""));
                cbStatus.setSelectedItem(model.getValueAt(row, 4));
            }
        } else {
            tfAmount.setText("");
            cbStatus.setSelectedIndex(0);
        }
        formPanel.setVisible(true);
        revalidate();
    }

    private void closeForm() {
        formPanel.setVisible(false);
        table.clearSelection();
        editingId = -1;
        revalidate();
    }

    private int selectedId(JComboBox<String> cb) {
        Object sel = cb.getSelectedItem();
        if (sel == null) throw new IllegalStateException("No item selected.");
        return Integer.parseInt(((String) sel).split(" \u2014 ")[0].trim());
    }

    private void save() {
        if (cbUser.getItemCount() == 0 || cbGateway.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Add users and gateways first.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double amount;
        try { amount = Double.parseDouble(tfAmount.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String status = (String) cbStatus.getSelectedItem();
        try (Connection c = DBConnection.getConnection()) {
            if (editingId < 0) {
                manager.add(selectedId(cbUser), selectedId(cbGateway), amount, status);
                UserGUI.audit(c, "transactions", "INSERT", 0, "New transaction $" + amount);
            } else {
                manager.update(editingId, selectedId(cbUser), selectedId(cbGateway), amount, status);
                UserGUI.audit(c, "transactions", "UPDATE", editingId, "Updated txn #" + editingId + " \u2192 " + status);
            }
        } catch (SQLException e) { err(e); return; }
        reload(); closeForm(); dashboard.refreshStats();
    }

    private void delete() {
        if (editingId < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete transaction #" + editingId + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection()) {
            manager.delete(editingId);
            UserGUI.audit(c, "transactions", "DELETE", editingId, "Deleted txn #" + editingId);
        } catch (SQLException e) { err(e); return; }
        reload(); closeForm(); dashboard.refreshStats();
    }

    private void err(SQLException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
}
