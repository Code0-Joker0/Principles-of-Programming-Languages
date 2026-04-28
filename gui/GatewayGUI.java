package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GatewayGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfName, tfEndpoint;
    private JCheckBox chkActive;

    public GatewayGUI() {
        setTitle("Payment Gateways Management");
        setSize(650, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"ID", "Gateway Name", "API Endpoint", "Active"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> populateFields());

        tfName = new JTextField(); tfEndpoint = new JTextField(); chkActive = new JCheckBox("Active", true);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Gateway Name:")); form.add(tfName);
        form.add(new JLabel("API Endpoint:")); form.add(tfEndpoint);
        form.add(new JLabel("")); form.add(chkActive);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.addActionListener(e -> addGateway());
        btnUpdate.addActionListener(e -> updateGateway());
        btnDelete.addActionListener(e -> deleteGateway());
        btnRefresh.addActionListener(e -> loadGateways());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadGateways();
        setVisible(true);
    }

    private void loadGateways() {
        model.setRowCount(0);
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM gateways")) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)});
        } catch (SQLException e) { showError(e); }
    }

    private void populateFields() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfName.setText((String) model.getValueAt(row, 1));
        tfEndpoint.setText((String) model.getValueAt(row, 2));
        chkActive.setSelected((Boolean) model.getValueAt(row, 3));
    }

    private void addGateway() {
        String sql = "INSERT INTO gateways (gateway_name, api_endpoint, is_active) VALUES (?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfEndpoint.getText().trim());
            ps.setBoolean(3, chkActive.isSelected());
            ps.executeUpdate();
            loadGateways(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void updateGateway() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE gateways SET gateway_name=?, api_endpoint=?, is_active=? WHERE gateway_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfName.getText().trim());
            ps.setString(2, tfEndpoint.getText().trim());
            ps.setBoolean(3, chkActive.isSelected());
            ps.setInt(4, id);
            ps.executeUpdate();
            loadGateways(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void deleteGateway() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete gateway " + id + "?") != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement("DELETE FROM gateways WHERE gateway_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadGateways(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void clearFields() { tfName.setText(""); tfEndpoint.setText(""); chkActive.setSelected(true); }
    private void showError(SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
}
