package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/payment_gateway_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root"; // change to your MySQL password

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found.", e);
            }
        }
        return connection;
    }
}
