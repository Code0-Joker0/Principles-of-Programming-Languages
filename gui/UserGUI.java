package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserGUI extends JPanel {

    private final MainDashboard dashboard;
    private JTable              table;
    private DefaultTableModel   model;
    private JTextField          tfSearch, tfName, tfEmail, tfPhone;
    private JLabel              lblCount;
    private JPanel              formPanel;
    private JButton             btnSave, btnCancel;
    private int                 editingId = -1;

    public UserGUI(MainDashboard dashboard) {
        this.dashboard = dashboard;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildForm(),    BorderLayout.EAST);

        loadUsers(null);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Users");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        lblCount = new JLabel("0 records");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title);
        left.add(Box.createHorizontalStrut(12));
        left.add(lblCount);

        tfSearch = Theme.styledField("Search by name or email...");
        tfSearch.setPreferredSize(new Dimension(260, 36));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadUsers(tfSearch.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadUsers(tfSearch.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadUsers(tfSearch.getText()); }
        });

        JButton btnAdd = Theme.primaryButton("+ Add User");
        btnAdd.addActionListener(e -> openForm(-1, "", "", ""));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch);
        right.add(btnAdd);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Created At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        // Click row → populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            openForm(
                (int)    model.getValueAt(row, 0),
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (String) model.getValueAt(row, 3)
            );
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.add(Theme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    // ── Slide-in form ─────────────────────────────────────────────────────────
    private JPanel buildForm() {
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Theme.BG_CARD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(24, 20, 24, 20)
        ));
        formPanel.setPreferredSize(new Dimension(280, 0));
        formPanel.setVisible(false);

        JLabel heading = new JLabel("User Details");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfName  = Theme.styledField("Full name");
        tfEmail = Theme.styledField("Email address");
        tfPhone = Theme.styledField("Phone number");

        for (JTextField f : new JTextField[]{tfName, tfEmail, tfPhone})
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        btnSave   = Theme.primaryButton("Save");
        btnCancel = Theme.ghostButton("Cancel");
        JButton btnDelete = Theme.dangerButton("Delete");

        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDelete.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave.addActionListener(e -> saveUser());
        btnCancel.addActionListener(e -> closeForm());
        btnDelete.addActionListener(e -> deleteUser());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Theme.BG_CARD);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(btnSave);
        btnRow.add(btnCancel);

        formPanel.add(heading);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(fieldLabel("Name"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(tfName);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(fieldLabel("Email"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(tfEmail);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(fieldLabel("Phone"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(tfPhone);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnRow);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnDelete);

        return formPanel;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Data operations ───────────────────────────────────────────────────────
    private void loadUsers(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM users" +
            (search != null && !search.isBlank()
                ? " WHERE name LIKE ? OR email LIKE ?"
                : "") + " ORDER BY user_id DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null && !search.isBlank()) {
                ps.setString(1, "%" + search + "%");
                ps.setString(2, "%" + search + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5)});
            lblCount.setText(model.getRowCount() + " record" + (model.getRowCount() != 1 ? "s" : ""));
        } catch (SQLException e) { showErr(e); }
    }

    private void openForm(int id, String name, String email, String phone) {
        editingId = id;
        tfName.setText(name);
        tfEmail.setText(email);
        tfPhone.setText(phone);
        btnSave.putClientProperty("text", id < 0 ? "Add" : "Update");
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

    private void saveUser() {
        String name  = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = DBConnection.getConnection()) {
            if (editingId < 0) {
                PreparedStatement ps = c.prepareStatement("INSERT INTO users (name,email,phone) VALUES (?,?,?)");
                ps.setString(1, name); ps.setString(2, email); ps.setString(3, phone);
                ps.executeUpdate();
                logAudit(c, "users", "INSERT", -1, "Added user: " + name);
            } else {
                PreparedStatement ps = c.prepareStatement("UPDATE users SET name=?,email=?,phone=? WHERE user_id=?");
                ps.setString(1, name); ps.setString(2, email); ps.setString(3, phone); ps.setInt(4, editingId);
                ps.executeUpdate();
                logAudit(c, "users", "UPDATE", editingId, "Updated user: " + name);
            }
        } catch (SQLException e) { showErr(e); return; }
        loadUsers(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    private void deleteUser() {
        if (editingId < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this user? All their transactions will also be removed.", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, editingId);
            ps.executeUpdate();
            logAudit(c, "users", "DELETE", editingId, "Deleted user ID " + editingId);
        } catch (SQLException e) { showErr(e); return; }
        loadUsers(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    private void logAudit(Connection c, String table, String op, int recordId, String notes) {
        try {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_trail (table_name,operation,record_id,notes) VALUES (?,?,?,?)");
            ps.setString(1, table); ps.setString(2, op);
            ps.setInt(3, recordId < 0 ? 0 : recordId); ps.setString(4, notes);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void showErr(SQLException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
