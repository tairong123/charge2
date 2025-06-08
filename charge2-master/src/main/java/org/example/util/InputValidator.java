package org.example.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InputValidator {

    // 驗證金額為非負數字
    public static boolean isValidAmount(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        // 僅允許「整數」或「小數」，不能有符號、字母、空白等
        if (!text.matches("^\\d+(\\.\\d+)?$")) return false;

        try {
            double value = Double.parseDouble(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // 驗證日期格式 yyyy-MM-dd
    public static boolean isValidDate(String text) {
        return text.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // 驗證類別不為空
    public static boolean isValidCategory(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // 顯示錯誤訊息並還原欄位
    public static void showErrorAndCancelEdit(JTable table, String message, Object fallback, int row, int column) {
        JOptionPane.showMessageDialog(null, message);
        ((DefaultTableModel) table.getModel()).setValueAt(fallback, row, column);
    }
}
