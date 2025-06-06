package org.example;// Main.java
import org.example.controller.AccountController;
import org.example.database.DBHelper;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DBHelper.connect()) {
            DBHelper.initDatabase(conn);
            AccountController controller = new AccountController(conn);
            controller.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}