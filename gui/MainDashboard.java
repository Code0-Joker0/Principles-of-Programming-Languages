package gui;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        setTitle("Payment Gateway System - Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Payment Gateway System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 20, 60));

        JButton btnUsers       = new JButton("👤  Manage Users");
        JButton btnTransactions = new JButton("💳  Manage Transactions");
        JButton btnGateways    = new JButton("🔗  Manage Gateways");
        JButton btnLogs        = new JButton("📋  Payment Logs");
        JButton btnAudit       = new JButton("🔍  Audit Trail");

        btnUsers.addActionListener(e -> new UserGUI());
        btnTransactions.addActionListener(e -> new TransactionGUI());
        btnGateways.addActionListener(e -> new GatewayGUI());
        btnLogs.addActionListener(e -> new PaymentLogGUI());
        btnAudit.addActionListener(e -> new AuditTrailGUI());

        btnPanel.add(btnUsers);
        btnPanel.add(btnTransactions);
        btnPanel.add(btnGateways);
        btnPanel.add(btnLogs);

        JPanel south = new JPanel();
        south.add(btnAudit);

        add(btnPanel, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainDashboard::new);
    }
}
