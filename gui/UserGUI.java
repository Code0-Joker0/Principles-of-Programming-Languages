package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfName, tfEmail, tfPhone;

    public UserGUI() {
        setTitle("Users Management");
        setSize(700, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Created At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> populateFields());

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        tfName  = new JTextField(); tfEmail = new JTextField(); tfPhone = new JTextField();
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Phone:")); form.add(tfPhone);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnRefresh.addActionListener(e -> loadUsers());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadUsers();
        setVisible(true);
    }

    private void loadUsers() {
        model.setRowCount(0);
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5)});
        } catch (SQLException e) { showError(e); }
    }

    private void populateFields() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfName.setText((String) model.getValueAt(row, 1));
        tfEmail.setText((String) model.getValueAt(row, 2));
        tfPhone.setText((String) model.getValueAt(row, 3));
    }

    private void addUser() {
        String sql = "INSERT INTO users (name, email, phone) VALUES (?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfEmail.getText().trim());
            ps.setString(3, tfPhone.getText().trim());
            ps.executeUpdate();
            loadUsers(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void updateUser() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE users SET name=?, email=?, phone=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfEmail.getText().trim());
            ps.setString(3, tfPhone.getText().trim());
            ps.setInt(4, id);
            ps.executeUpdate();
            loadUsers(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete user " + id + "?") != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadUsers(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void clearFields() { tfName.setText(""); tfEmail.setText(""); tfPhone.setText(""); }
    private void showError(SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
}
