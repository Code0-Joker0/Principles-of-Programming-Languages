package db;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static final String CONFIG_FILE = ".env";
    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) return connection;
        // Retry until the user either connects successfully or cancels
        while (true) {
            Properties cfg = loadOrPrompt();
            if (cfg == null) throw new SQLException("Database configuration cancelled.");
            String url = "jdbc:mysql://" + cfg.getProperty("host") + "/payment_gateway_db" +
                         "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try {
                connection = tryConnect(url, cfg.getProperty("user"), cfg.getProperty("pass"));
                return connection;
            } catch (SQLException e) {
                // Bad credentials or unreachable host — delete saved config and re-prompt
                new File(CONFIG_FILE).delete();
                JOptionPane.showMessageDialog(null,
                        "Connection failed: " + e.getMessage() + "\nPlease check your settings.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Connection tryConnect(String url, String user, String pass) throws SQLException {
        // Try new driver name first (Connector/J 8+), fall back to legacy (Connector/J 5.x)
        for (String driver : new String[]{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"}) {
            try {
                Class.forName(driver);
                return DriverManager.getConnection(url, user, pass);
            } catch (ClassNotFoundException ignored) {
            } catch (SQLException e) {
                throw e; // wrong credentials or server down — surface immediately
            }
        }
        throw new SQLException("MySQL JDBC driver not found in lib/.");
    }

    private static Properties loadOrPrompt() {
        Properties cfg = new Properties();
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                Properties raw = new Properties();
                raw.load(in);
                cfg.setProperty("host", raw.getProperty("DB_HOST", "localhost:3306"));
                cfg.setProperty("user", raw.getProperty("DB_USER", "root"));
                cfg.setProperty("pass", raw.getProperty("DB_PASS", ""));
                return cfg;
            } catch (IOException ignored) {}
        }
        // First run — show a simple dialog to collect connection details
        JTextField tfHost = new JTextField("localhost:3306");
        JTextField tfUser = new JTextField("root");
        JPasswordField tfPass = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Host:port:")); panel.add(tfHost);
        panel.add(new JLabel("Username:"));  panel.add(tfUser);
        panel.add(new JLabel("Password:"));  panel.add(tfPass);
        int result = JOptionPane.showConfirmDialog(null, panel,
                "Database Connection Setup", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return null;
        cfg.setProperty("host", tfHost.getText().trim());
        cfg.setProperty("user", tfUser.getText().trim());
        cfg.setProperty("pass", new String(tfPass.getPassword()));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            bw.write("DB_HOST=" + cfg.getProperty("host") + "\n");
            bw.write("DB_USER=" + cfg.getProperty("user") + "\n");
            bw.write("DB_PASS=" + cfg.getProperty("pass") + "\n");
        } catch (IOException ignored) {}
        return cfg;
    }
}
