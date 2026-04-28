package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbUser, cbGateway, cbStatus;
    private JTextField tfAmount;

    public TransactionGUI() {
        setTitle("Transactions Management");
        setSize(800, 480);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"ID", "User", "Gateway", "Amount", "Status", "Created At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> populateFields());

        cbUser    = new JComboBox<>();
        cbGateway = new JComboBox<>();
        cbStatus  = new JComboBox<>(new String[]{"PENDING", "SUCCESS", "FAILED"});
        tfAmount  = new JTextField();

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("User:")); form.add(cbUser);
        form.add(new JLabel("Gateway:")); form.add(cbGateway);
        form.add(new JLabel("Amount:")); form.add(tfAmount);
        form.add(new JLabel("Status:")); form.add(cbStatus);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.addActionListener(e -> addTransaction());
        btnUpdate.addActionListener(e -> updateTransaction());
        btnDelete.addActionListener(e -> deleteTransaction());
        btnRefresh.addActionListener(e -> { loadDropdowns(); loadTransactions(); });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadDropdowns();
        loadTransactions();
        setVisible(true);
    }

    private void loadDropdowns() {
        cbUser.removeAllItems(); cbGateway.removeAllItems();
        try (Statement st = DBConnection.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT user_id, name FROM users");
            while (rs.next()) cbUser.addItem(rs.getInt(1) + " - " + rs.getString(2));
            rs = st.executeQuery("SELECT gateway_id, gateway_name FROM gateways");
            while (rs.next()) cbGateway.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (SQLException e) { showError(e); }
    }

    private void loadTransactions() {
        model.setRowCount(0);
        String sql = "SELECT t.transaction_id, u.name, g.gateway_name, t.amount, t.status, t.created_at " +
                     "FROM transactions t JOIN users u ON t.user_id=u.user_id JOIN gateways g ON t.gateway_id=g.gateway_id";
        try (Statement st = DBConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5), rs.getTimestamp(6)});
        } catch (SQLException e) { showError(e); }
    }

    private void populateFields() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfAmount.setText(String.valueOf(model.getValueAt(row, 3)));
        cbStatus.setSelectedItem(model.getValueAt(row, 4));
    }

    private int selectedId(JComboBox<String> cb) {
        return Integer.parseInt(((String) cb.getSelectedItem()).split(" - ")[0]);
    }

    private void addTransaction() {
        String sql = "INSERT INTO transactions (user_id, gateway_id, amount, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, selectedId(cbUser));
            ps.setInt(2, selectedId(cbGateway));
            ps.setDouble(3, Double.parseDouble(tfAmount.getText().trim()));
            ps.setString(4, (String) cbStatus.getSelectedItem());
            ps.executeUpdate();
            loadTransactions();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void updateTransaction() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE transactions SET user_id=?, gateway_id=?, amount=?, status=? WHERE transaction_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, selectedId(cbUser));
            ps.setInt(2, selectedId(cbGateway));
            ps.setDouble(3, Double.parseDouble(tfAmount.getText().trim()));
            ps.setString(4, (String) cbStatus.getSelectedItem());
            ps.setInt(5, id);
            ps.executeUpdate();
            loadTransactions();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void deleteTransaction() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete transaction " + id + "?") != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement("DELETE FROM transactions WHERE transaction_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadTransactions();
        } catch (SQLException e) { showError(e); }
    }

    private void showError(SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
}
