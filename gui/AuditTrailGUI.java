package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuditTrailGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfTable, tfRecordId, tfNotes;
    private JComboBox<String> cbOperation;

    public AuditTrailGUI() {
        setTitle("Audit Trail Management");
        setSize(750, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"Audit ID", "Table", "Operation", "Record ID", "Performed At", "Notes"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> populateFields());

        tfTable = new JTextField(); tfRecordId = new JTextField(); tfNotes = new JTextField();
        cbOperation = new JComboBox<>(new String[]{"INSERT", "UPDATE", "DELETE"});

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Table Name:")); form.add(tfTable);
        form.add(new JLabel("Operation:")); form.add(cbOperation);
        form.add(new JLabel("Record ID:")); form.add(tfRecordId);
        form.add(new JLabel("Notes:")); form.add(tfNotes);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.addActionListener(e -> addAudit());
        btnUpdate.addActionListener(e -> updateAudit());
        btnDelete.addActionListener(e -> deleteAudit());
        btnRefresh.addActionListener(e -> loadAudits());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadAudits();
        setVisible(true);
    }

    private void loadAudits() {
        model.setRowCount(0);
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM audit_trail")) {
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getTimestamp(5), rs.getString(6)});
        } catch (SQLException e) { showError(e); }
    }

    private void populateFields() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfTable.setText((String) model.getValueAt(row, 1));
        cbOperation.setSelectedItem(model.getValueAt(row, 2));
        tfRecordId.setText(String.valueOf(model.getValueAt(row, 3)));
        tfNotes.setText((String) model.getValueAt(row, 5));
    }

    private void addAudit() {
        String sql = "INSERT INTO audit_trail (table_name, operation, record_id, notes) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfTable.getText().trim());
            ps.setString(2, (String) cbOperation.getSelectedItem());
            ps.setInt(3, Integer.parseInt(tfRecordId.getText().trim()));
            ps.setString(4, tfNotes.getText().trim());
            ps.executeUpdate();
            loadAudits(); clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void updateAudit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE audit_trail SET table_name=?, operation=?, record_id=?, notes=? WHERE audit_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tfTable.getText().trim());
            ps.setString(2, (String) cbOperation.getSelectedItem());
            ps.setInt(3, Integer.parseInt(tfRecordId.getText().trim()));
            ps.setString(4, tfNotes.getText().trim());
            ps.setInt(5, id);
            ps.executeUpdate();
            loadAudits(); clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void deleteAudit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete audit record " + id + "?") != JOptionPane.YES_OPTION) return;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement("DELETE FROM audit_trail WHERE audit_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadAudits(); clearFields();
        } catch (SQLException e) { showError(e); }
    }

    private void clearFields() { tfTable.setText(""); tfRecordId.setText(""); tfNotes.setText(""); }
    private void showError(SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
}
