package org.example.ui;

import org.example.component.RecordDialog;
import org.example.db.DatabaseManager;
import org.example.util.InputValidator;
import org.example.component.ButtonRenderer;
import org.example.component.DeleteButtonEditor;


import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class TodayPanel extends JPanel {
    private final int userId;
    private final JComboBox<String> yearBox = new JComboBox<>();
    private final JComboBox<String> monthBox = new JComboBox<>();
    private final JComboBox<String> dayBox = new JComboBox<>();
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel incomeLabel = new JLabel();
    private final JLabel expenseLabel = new JLabel();
    private final JLabel netLabel = new JLabel();

    public TodayPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // 上方選單區
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("新增記帳");
        topPanel.add(addButton);

        topPanel.add(new JLabel("年份:"));
        topPanel.add(yearBox);
        topPanel.add(new JLabel("月份:"));
        topPanel.add(monthBox);
        topPanel.add(new JLabel("日期:"));
        topPanel.add(dayBox);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"ID", "類別", "金額", "日期", "備註", "類型", "刪除"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setMinWidth(0); // 隱藏 ID 欄
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumn("刪除").setCellRenderer(new ButtonRenderer());
        table.getColumn("刪除").setCellEditor(new DeleteButtonEditor(
                id -> {
                    try {
                        PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("DELETE FROM records WHERE id=?");
                        stmt.setInt(1, id);
                        stmt.executeUpdate();
                        refreshYearBoxOnly(yearBox, userId); // 年份即時更新
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                },
                () -> reload() // 執行完 reload，確保 UI 與 row index 正確同步
        ));

        table.getColumn("類型").setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"支出", "收入"})));

        model.addTableModelListener(e -> handleTableEdit(e));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 下方統計區
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.add(incomeLabel);
        statsPanel.add(expenseLabel);
        statsPanel.add(netLabel);
        add(statsPanel, BorderLayout.SOUTH);

        // 年月日設定與初始載入
        initDateSelectors();
        addButton.addActionListener(e -> {
            new RecordDialog(userId, yearBox).showDialog();
            refreshYearBoxOnly(yearBox, userId);
            reload();
        });

        yearBox.addActionListener(e -> { updateDayBox(); reload(); });
        monthBox.addActionListener(e -> { updateDayBox(); reload(); });
        dayBox.addActionListener(e -> reload());

        reload();
    }

    private void initDateSelectors() {
        // 年份
        refreshYearBoxOnly(yearBox, userId);

        // 月份
        for (int m = 1; m <= 12; m++) monthBox.addItem(String.format("%02d", m));

        // 預設為今天
        LocalDate today = LocalDate.now();
        yearBox.setSelectedItem(String.valueOf(today.getYear()));
        monthBox.setSelectedItem(String.format("%02d", today.getMonthValue()));
        updateDayBox();
        dayBox.setSelectedItem(String.format("%02d", today.getDayOfMonth()));
    }

    private void updateDayBox() {
        dayBox.removeAllItems();
        int y = Integer.parseInt((String) yearBox.getSelectedItem());
        int m = Integer.parseInt((String) monthBox.getSelectedItem());
        int max = YearMonth.of(y, m).lengthOfMonth();
        for (int d = 1; d <= max; d++) {
            dayBox.addItem(String.format("%02d", d));
        }
    }

    private void reload() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        table.clearSelection();

        String date = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
        loadRecordsByDate(date);
        updateStatsByDate(date);
    }

    private void loadRecordsByDate(String date) {
        model.setRowCount(0);
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM records WHERE user_id=? AND date=? ORDER BY id DESC");
            stmt.setInt(1, userId);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();
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

    private void updateStatsByDate(String date) {
        double income = 0, expense = 0;
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT amount, type FROM records WHERE user_id=? AND date=?");
            stmt.setInt(1, userId);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double amt = rs.getDouble("amount");
                if ("收入".equals(rs.getString("type"))) income += amt;
                else expense += amt;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        incomeLabel.setText("總收入：" + income + "   ");
        expenseLabel.setText("總支出：" + expense + "   ");
        netLabel.setText("淨收入：" + (income - expense));
    }

    private void handleTableEdit(TableModelEvent e) {
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

        try {
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

    private void refreshYearBoxOnly(JComboBox<String> box, int userId) {
        ActionListener[] listeners = box.getActionListeners();
        for (ActionListener al : listeners) box.removeActionListener(al);

        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                    "SELECT DISTINCT strftime('%Y',date) y FROM records WHERE user_id=?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            java.util.List<String> years = new java.util.ArrayList<>();
            while (rs.next()) {
                years.add(rs.getString("y"));
            }

            if (years.isEmpty()) {
                years.add(String.valueOf(LocalDate.now().getYear()));
            }

            years.sort((a, b) -> b.compareTo(a));
            box.removeAllItems();
            for (String y : years) box.addItem(y);

            box.setSelectedItem(String.valueOf(LocalDate.now().getYear()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (ActionListener al : listeners) box.addActionListener(al);
    }

}
