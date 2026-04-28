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

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 20, 60));

        JButton btnUsers        = new JButton("\uD83D\uDC64  Manage Users");
        JButton btnTransactions = new JButton("\uD83D\uDCB3  Manage Transactions");
        JButton btnGateways     = new JButton("\uD83D\uDD17  Manage Gateways");
        JButton btnLogs         = new JButton("\uD83D\uDCCB  Payment Logs");
        JButton btnAudit        = new JButton("\uD83D\uDD0D  Audit Trail");

        btnUsers.addActionListener(e -> new UserGUI());
        btnTransactions.addActionListener(e -> new TransactionGUI());
        btnGateways.addActionListener(e -> new GatewayGUI());
        btnLogs.addActionListener(e -> new PaymentLogGUI());
        btnAudit.addActionListener(e -> new AuditTrailGUI());

        btnPanel.add(btnUsers);
        btnPanel.add(btnTransactions);
        btnPanel.add(btnGateways);
        btnPanel.add(btnLogs);
        btnPanel.add(btnAudit);

        add(btnPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainDashboard::new);
    }
}
