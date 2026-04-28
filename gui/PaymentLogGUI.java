package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PaymentLogGUI extends JPanel {

    private final MainDashboard dashboard;
    private JTable            table;
    private DefaultTableModel model;
    private JTextField        tfSearch;
    private JComboBox<String> cbTransaction;
    private JTextArea         taMessage;
    private JLabel            lblCount;
    private JPanel            formPanel;
    private int               editingId = -1;

    public PaymentLogGUI(MainDashboard dashboard) {
        this.dashboard = dashboard;
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildForm(),   BorderLayout.EAST);
        loadDropdown();
        load(null);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Payment Logs");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        lblCount = new JLabel("0 logs");
        lblCount.setFont(Theme.FONT_SMALL);
        lblCount.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_DARK);
        left.add(title); left.add(Box.createHorizontalStrut(12)); left.add(lblCount);

        tfSearch = Theme.styledField("Search log messages...");
        tfSearch.setPreferredSize(new Dimension(220, 36));
        tfSearch.getDocument().addDocumentListener(UserGUI.docListener(() -> load(tfSearch.getText())));

        JButton btnAdd     = Theme.primaryButton("+ Add Log");
        JButton btnRefresh = Theme.ghostButton("Refresh");
        btnAdd.addActionListener(e -> openForm(-1));
        btnRefresh.addActionListener(e -> { loadDropdown(); load(tfSearch.getText()); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Theme.BG_DARK);
        right.add(tfSearch); right.add(btnRefresh); right.add(btnAdd);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new String[]{"Log ID", "Txn ID", "Message", "Logged At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.BG_SELECTED : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                setForeground(sel ? Color.WHITE : Theme.TEXT_SECONDARY);
                setFont(Theme.FONT_MONO);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
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

        JLabel heading = new JLabel("Log Entry");
        heading.setFont(Theme.FONT_HEADING);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbTransaction = Theme.styledCombo();
        cbTransaction.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        taMessage = new JTextArea(5, 20);
        taMessage.setBackground(Theme.BG_INPUT);
        taMessage.setForeground(Theme.TEXT_PRIMARY);
        taMessage.setCaretColor(Theme.ACCENT);
        taMessage.setFont(Theme.FONT_BODY);
        taMessage.setLineWrap(true);
        taMessage.setWrapStyleWord(true);
        taMessage.setBorder(Theme.inputBorder());

        JScrollPane msgScroll = new JScrollPane(taMessage);
        msgScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        msgScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        formPanel.add(Theme.fieldLabel("Transaction")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(cbTransaction);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.fieldLabel("Log Message")); formPanel.add(Box.createVerticalStrut(4)); formPanel.add(msgScroll);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnRow);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(btnDelete);
        return formPanel;
    }

    private void loadDropdown() {
        cbTransaction.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             ResultSet rs = c.createStatement().executeQuery(
                 "SELECT transaction_id, amount, status FROM transactions ORDER BY transaction_id DESC")) {
            while (rs.next())
                cbTransaction.addItem(rs.getInt(1) + " \u2014 $" + String.format("%.2f", rs.getDouble(2)) + " [" + rs.getString(3) + "]");
        } catch (SQLException e) { err(e); }
    }

    private void load(String search) {
        model.setRowCount(0);
        String sql = "SELECT * FROM payment_logs" +
            (search != null && !search.isBlank() ? " WHERE log_message LIKE ?" : "") +
            " ORDER BY log_id DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null && !search.isBlank())
                ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getTimestamp(4)});
            lblCount.setText(model.getRowCount() + " log" + (model.getRowCount() != 1 ? "s" : ""));
        } catch (SQLException e) { err(e); }
    }

    private void openForm(int id) {
        editingId = id;
        if (id > 0) {
            int row = table.getSelectedRow();
            if (row >= 0) taMessage.setText((String) model.getValueAt(row, 2));
        } else {
            taMessage.setText("");
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

    private int selectedTxnId() {
        Object sel = cbTransaction.getSelectedItem();
        if (sel == null) throw new IllegalStateException("No transaction selected.");
        return Integer.parseInt(((String) sel).split(" \u2014 ")[0].trim());
    }

    private void save() {
        if (cbTransaction.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No transactions available.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String msg = taMessage.getText().trim();
        if (msg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Log message cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection c = DBConnection.getConnection()) {
            if (editingId < 0) {
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO payment_logs (transaction_id,log_message) VALUES (?,?)");
                ps.setInt(1, selectedTxnId()); ps.setString(2, msg);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE payment_logs SET transaction_id=?,log_message=? WHERE log_id=?");
                ps.setInt(1, selectedTxnId()); ps.setString(2, msg); ps.setInt(3, editingId);
                ps.executeUpdate();
            }
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
    }

    private void delete() {
        if (editingId < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete log #" + editingId + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM payment_logs WHERE log_id=?")) {
            ps.setInt(1, editingId); ps.executeUpdate();
        } catch (SQLException e) { err(e); return; }
        load(tfSearch.getText());
        closeForm();
    }

    private void err(SQLException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
}
