package org.example;

import org.example.db.DatabaseManager;
import org.example.ui.LoginFrame;

import javax.swing.*;

public class AccountingApp {
    public static void main(String[] args) {
        // 啟動資料庫
        DatabaseManager.connect();

        // 啟動畫面（登入視窗）
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
