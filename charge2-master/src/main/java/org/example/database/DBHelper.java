package org.example.database;// database/database.DBHelper.java
import java.sql.*;

public class DBHelper {
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:accounting.db");
    }

    public static void initDatabase(Connection conn) throws SQLException {
        String sql = """
    CREATE TABLE IF NOT EXISTS transactions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type TEXT NOT NULL,
        description TEXT,
        amount REAL NOT NULL,
        date TEXT NOT NULL
);
    """;
        conn.createStatement().execute(sql);
    }
}