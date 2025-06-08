package org.example.component;

import org.example.db.DatabaseManager;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class RecordDialog {
    private final int userId;
    private final JComboBox<String> yearBox;
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"支出", "收入"});
    private final JTextField categoryField = new JTextField();
    private final JTextField amountField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextField noteField = new JTextField();

    public RecordDialog(int userId, JComboBox<String> yearBox) {
        this.userId = userId;
        this.yearBox = yearBox;
    }

    public void showDialog() {
        Object[] fields = {
                "類型:", typeBox,
                "類別:", categoryField,
                "金額:", amountField,
                "日期 (yyyy-MM-dd):", dateField,
                "備註:", noteField
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "新增記帳", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (!validateInputs()) return;
            insertRecord();
        }
    }

    private boolean validateInputs() {
        String amountText = amountField.getText();
        try {
            double amount = Double.parseDouble(amountText);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "金額必須為非負數字！");
            return false;
        }

        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "類別不能為空！");
            return false;
        }

        String date = dateField.getText().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(null, "日期格式錯誤！請使用 yyyy-MM-dd");
            return false;
        }

        return true;
    }

    private void insertRecord() {
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO records(user_id, type, category, amount, date, note) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setString(2, (String) typeBox.getSelectedItem());
            ps.setString(3, categoryField.getText().trim());
            ps.setDouble(4, Double.parseDouble(amountField.getText()));
            ps.setString(5, dateField.getText().trim());
            ps.setString(6, noteField.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "新增成功！");
            if (yearBox != null) yearBox.setSelectedItem(dateField.getText().substring(0, 4)); // 回到該年

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "新增失敗，請確認資料格式！");
        }
    }
}
