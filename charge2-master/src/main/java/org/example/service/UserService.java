package org.example.service;

import org.example.db.DatabaseManager;

import java.sql.*;

public class UserService {

    // 登入功能：成功回傳 user_id，失敗回傳 -1
    public static int login(String username, String password) {
        try {
            String sql = "SELECT id FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 註冊功能：成功回傳 true，失敗回傳 false
    public static boolean register(String name, String username, String password) {
        try {
            // 檢查使用者名稱是否已存在
            PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE username = ?"
            );
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // 帳號已存在
            }

            // 插入新使用者
            PreparedStatement insertStmt = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO users (name, username, password) VALUES (?, ?, ?)"
            );
            insertStmt.setString(1, name);
            insertStmt.setString(2, username);
            insertStmt.setString(3, password);
            insertStmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
