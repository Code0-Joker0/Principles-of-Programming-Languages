package TransactionPackage;

import db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

    public void add(int userId, int gatewayId, double amount, String status) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, gateway_id, amount, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, gatewayId);
            ps.setDouble(3, amount);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    public void update(int id, int userId, int gatewayId, double amount, String status) throws SQLException {
        String sql = "UPDATE transactions SET user_id=?, gateway_id=?, amount=?, status=? WHERE transaction_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, gatewayId);
            ps.setDouble(3, amount);
            ps.setString(4, status);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM transactions WHERE transaction_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getAll() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT t.transaction_id, u.name, g.gateway_name, t.amount, t.status, t.created_at " +
                     "FROM transactions t " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "JOIN gateways g ON t.gateway_id = g.gateway_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                                      rs.getDouble(4), rs.getString(5), rs.getTimestamp(6)});
        }
        return rows;
    }
}
