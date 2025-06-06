package org.example.model;// model/model.TransactionDAO.java

// model/TransactionDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertTransaction(Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (type, description, amount, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tx.getType());
            stmt.setString(2, tx.getDescription());
            stmt.setDouble(3, tx.getAmount());
            stmt.setString(4, tx.getDate());
            stmt.executeUpdate();
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("刪除記帳資料失敗，ID = " + id);
            e.printStackTrace();
            return false;
        }
    }


    public double getMonthlyTotal(String month, String type) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE date LIKE ? AND type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, month + "%");  // e.g., 2025-06%
            stmt.setString(2, type);         // 收入 or 支出
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.getDouble(1);      // 回傳總和
            }
        }
    }

    // 新增：查詢所有資料
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction tx = new Transaction(
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("date")
                );
                tx.setId(rs.getInt("id")); // 記得補上 id
                list.add(tx);
            }
        }

        return list;
    }
}