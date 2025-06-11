package org.example.ui;

import org.example.component.ButtonRenderer;
import org.example.db.DatabaseManager;
import org.example.util.InputValidator;
import org.example.component.DeleteButtonEditor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;

public class HistoryPanel extends JPanel {//歷史記帳紀錄
    private final int userId;
    private final JComboBox<String> yearBox = new JComboBox<>();
    private final JComboBox<String> monthBox = new JComboBox<>();
    private final DefaultTableModel model;
    private final JTable table;

    public HistoryPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // 上方篩選欄
        JPanel top = new JPanel();
        top.add(new JLabel("年份:"));
        top.add(yearBox);
        top.add(new JLabel("月份:"));
        top.add(monthBox);
        add(top, BorderLayout.NORTH);

        // 表格欄位
        String[] columns = {"ID", "類別", "金額", "日期", "備註", "類型", "刪除"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumn("刪除").setCellRenderer(new ButtonRenderer());
        table.getColumn("刪除").setCellEditor(new DeleteButtonEditor(//刪除記帳
                id -> {
                    try {
                        PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("DELETE FROM records WHERE id=?");
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                        refreshYearBox(userId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },
                this::reload
        ));


        table.getColumn("類型").setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"支出", "收入"})));

        model.addTableModelListener(this::handleTableEdit);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 初始化年份與月份選單
        yearBox.addItem("全部年份");
        monthBox.addItem("全部月份");
        for (int m = 1; m <= 12; m++) {
            monthBox.addItem(String.format("%02d", m));
        }

        refreshYearBox(userId);
        ActionListener refresh = e -> reload();
        yearBox.addActionListener(refresh);
        monthBox.addActionListener(refresh);

        reload();
    }

    private void reload() {//刷新畫面
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        table.clearSelection();

        model.setRowCount(0);
        String year = (String) yearBox.getSelectedItem();
        String month = (String) monthBox.getSelectedItem();

        try {
            Connection conn = DatabaseManager.getConnection();
            StringBuilder sql = new StringBuilder("SELECT * FROM records WHERE user_id=?");
            if (!"全部年份".equals(year)) {
                sql.append(" AND strftime('%Y', date)=?");
            }
            if (!"全部月份".equals(month)) {
                sql.append(" AND strftime('%m', date)=?");
            }
            sql.append(" ORDER BY date DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            ps.setInt(idx++, userId);
            if (!"全部年份".equals(year)) ps.setString(idx++, year);
            if (!"全部月份".equals(month)) ps.setString(idx++, month);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("note"),
                        rs.getString("type"),
                        "刪除"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleTableEdit(TableModelEvent e) {//編輯記帳控制
        if (e.getType() != TableModelEvent.UPDATE) return;

        int row = e.getFirstRow();
        int col = e.getColumn();
        if (row < 0 || col < 0) return;

        int id = (int) model.getValueAt(row, 0);
        String field = switch (col) {
            case 1 -> "category";
            case 2 -> "amount";
            case 3 -> "date";
            case 4 -> "note";
            case 5 -> "type";
            default -> null;
        };
        if (field == null) return;

        try {//防呆
            String valueStr = model.getValueAt(row, col).toString();
            if (field.equals("amount") && !InputValidator.isValidAmount(valueStr)) {
                InputValidator.showErrorAndCancelEdit(table, "金額需為數字(非負數)！", 0.0, row, col);
                return;
            }
            if (field.equals("date") && !InputValidator.isValidDate(valueStr)) {
                InputValidator.showErrorAndCancelEdit(table, "日期格式錯誤，請使用 yyyy-MM-dd！", LocalDate.now().toString(), row, col);
                return;
            }
            if (field.equals("category") && !InputValidator.isValidCategory(valueStr)) {
                InputValidator.showErrorAndCancelEdit(table, "類別不能為空！", "未分類", row, col);
                return;
            }

            String sql = "UPDATE records SET " + field + " = ? WHERE id = ?";
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setObject(1, model.getValueAt(row, col));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshYearBox(int userId) {//即時更新年份選單
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT strftime('%Y',date) y FROM records WHERE user_id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            java.util.List<String> years = new java.util.ArrayList<>();
            while (rs.next()) years.add(rs.getString("y"));
            if (years.isEmpty()) years.add(String.valueOf(LocalDate.now().getYear()));
            years.sort((a, b) -> b.compareTo(a));

            yearBox.removeAllItems();
            yearBox.addItem("全部年份");
            for (String y : years) yearBox.addItem(y);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
