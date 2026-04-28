package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/payment_gateway_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    // Set DB_PASS environment variable, or fall back to empty string
    private static final String PASS = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "";

    private DBConnection() {}

    // Returns a fresh connection per call — thread-safe, no shared state
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
}
