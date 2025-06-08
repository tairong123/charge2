package org.example.db;

import java.sql.*;

public class DatabaseManager {
    private static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:accounting.db");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, name TEXT, username TEXT, password TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS records(id INTEGER PRIMARY KEY, user_id INTEGER, type TEXT, category TEXT, amount REAL, date TEXT, note TEXT)");

            try {
                stmt.execute("ALTER TABLE records ADD COLUMN type TEXT DEFAULT '支出'");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
