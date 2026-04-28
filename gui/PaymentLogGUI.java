package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PaymentLogGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbTransaction;
    private JTextArea taMessage;

    public PaymentLogGUI() {
        setTitle("Payment Logs Management");
        setSize(700, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"Log ID", "Transaction ID", "Message", "Logged At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> populateFields());

        cbTransaction = new JComboBox<>();
        taMessage = new JTextArea(3, 20);
        taMessage.setLineWrap(true);

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Transaction:")); form.add(cbTransaction);
        form.add(new JLabel("Log Message:")); form.add(new JScrollPane(taMessage));

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.addActionListener(e -> addLog());
        btnUpdate.addActionListener(e -> updateLog());
        btnDelete.addActionListener(e -> deleteLog());
        btnRefresh.addActionListener(e -> { loadDropdown(); loadLogs(); });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadDropdown();
        loadLogs();
        setVisible(true);
    }

    private void loadDropdown() {
        cbTransaction.removeAllItems();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT transaction_id, amount, status FROM transactions")) {
            while (rs.next())
                cbTransaction.addItem(rs.getInt(1) + " - $" + rs.getDouble(2) + " [" + rs.getString(3) + "]");
        } catch (SQLException e) { showError(e); }
    }

    private void loadLogs() {
        model.setRowCount(0);
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM payment_logs")) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getTimestamp(4)});
        } catch (SQLException e) { showError(e); }
    }

    private void populateFields() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        taMessage.setText((String) model.getValueAt(row, 2));
    }

    private int selectedTxnId() {
        return Integer.parseInt(((String) cbTransaction.getSelectedItem()).split(" - ")[0]);
    }

    private void addLog() {
        String sql = "INSERT INTO payment_logs (transaction_id, log_message) VALUES (?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, selectedTxnId());
            ps.setString(2, taMessage.getText().trim());
            ps.executeUpdate();
            loadLogs(); taMessage.setText("");
        } catch (SQLException e) { showError(e); }
    }

    private void updateLog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE payment_logs SET transaction_id=?, log_message=? WHERE log_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, selectedTxnId());
            ps.setString(2, taMessage.getText().trim());
            ps.setInt(3, id);
            ps.executeUpdate();
            loadLogs(); taMessage.setText("");
        } catch (SQLException e) { showError(e); }
    }

    private void deleteLog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete log " + id + "?") != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement("DELETE FROM payment_logs WHERE log_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadLogs();
        } catch (SQLException e) { showError(e); }
    }

    private void showError(SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
}
