package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class UserGUI extends JPanel {

    private final MainDashboard dashboard;
    private JTable            table;
    private DefaultTableModel model;
    private JTextField        tfSearch, tfName, tfEmail, tfPhone;
    private JLabel            lblCount;
    private JPanel            formPanel;
    private int               editingId = -1;

    public UserGUI(MainDashboard dashboard) {
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

        JLabel title = new JLabel("Users");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        lblCount = new JLabel("0 records");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title); left.add(Box.createHorizontalStrut(12)); left.add(lblCount);

        tfSearch = Theme.styledField("Search by name or email...");
        tfSearch.setPreferredSize(new Dimension(260, 36));
        tfSearch.getDocument().addDocumentListener(docListener(() -> load(tfSearch.getText())));

        JButton btnAdd = Theme.primaryButton("+ Add User");
        btnAdd.addActionListener(e -> openForm(-1, "", "", ""));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch); right.add(btnAdd);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Created At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0) openForm(
                (int)    model.getValueAt(row, 0),
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (String) model.getValueAt(row, 3));
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

        JLabel heading = new JLabel("User Details");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfName  = Theme.styledField("Full name");
        tfEmail = Theme.styledField("Email address");
        tfPhone = Theme.styledField("Phone number");
        for (JTextField f : new JTextField[]{tfName, tfEmail, tfPhone})
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
        formPanel.add(Theme.fieldLabel("Name"));  formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfName);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Email")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfEmail);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Phone")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(tfPhone);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnRow);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnDelete);
        return formPanel;
    }

    private void load(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM users" +
            (search != null && !search.isBlank() ? " WHERE name LIKE ? OR email LIKE ?" : "") +
            " ORDER BY user_id DESC";
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
        } catch (SQLException e) { err(e); }
    }

    private void openForm(int id, String name, String email, String phone) {
        editingId = id;
        tfName.setText(name); tfEmail.setText(email); tfPhone.setText(phone);
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
        String name = tfName.getText().trim(), email = tfEmail.getText().trim(), phone = tfPhone.getText().trim();
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = DBConnection.getConnection()) {
            if (editingId < 0) {
                PreparedStatement ps = c.prepareStatement("INSERT INTO users (name,email,phone) VALUES (?,?,?)");
                ps.setString(1, name); ps.setString(2, email); ps.setString(3, phone);
                ps.executeUpdate();
                audit(c, "users", "INSERT", 0, "Added user: " + name);
            } else {
                PreparedStatement ps = c.prepareStatement("UPDATE users SET name=?,email=?,phone=? WHERE user_id=?");
                ps.setString(1, name); ps.setString(2, email); ps.setString(3, phone); ps.setInt(4, editingId);
                ps.executeUpdate();
                audit(c, "users", "UPDATE", editingId, "Updated user: " + name);
            }
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    private void delete() {
        if (editingId < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete this user? Their transactions will also be removed.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, editingId); ps.executeUpdate();
            audit(c, "users", "DELETE", editingId, "Deleted user ID " + editingId);
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
        dashboard.refreshStats();
    }

    static void audit(Connection c, String tbl, String op, int rid, String notes) {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_trail (table_name,operation,record_id,notes) VALUES (?,?,?,?)")) {
            ps.setString(1, tbl); ps.setString(2, op); ps.setInt(3, rid); ps.setString(4, notes);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void err(SQLException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }

    static javax.swing.event.DocumentListener docListener(Runnable r) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        };
    }
}
