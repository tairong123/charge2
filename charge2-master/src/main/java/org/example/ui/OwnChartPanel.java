package org.example.ui;

import org.example.db.DatabaseManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;

public class OwnChartPanel extends JPanel {
    private final int userId;
    private final JComboBox<String> yearBox = new JComboBox<>();
    private final JComboBox<String> monthBox = new JComboBox<>();
    private final JLabel totalLabel = new JLabel("總金額統計", SwingConstants.CENTER);

    private final DefaultPieDataset incomeDataset = new DefaultPieDataset();
    private final DefaultPieDataset expenseDataset = new DefaultPieDataset();

    public OwnChartPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("年份:"));
        top.add(yearBox);
        top.add(new JLabel("月份:"));
        top.add(monthBox);
        add(top, BorderLayout.NORTH);

        yearBox.addItem("全部年份");
        monthBox.addItem("全部月份");
        for (int m = 1; m <= 12; m++) {
            monthBox.addItem(String.format("%02d", m));
        }
        refreshYearBox(userId);

        JPanel grid = new JPanel(new GridLayout(1, 2));
        grid.add(new ChartPanel(createPieChart("收入分析", incomeDataset)));
        grid.add(new ChartPanel(createPieChart("支出分析", expenseDataset)));
        add(grid, BorderLayout.CENTER);
        add(totalLabel, BorderLayout.SOUTH);

        ActionListener refresh = e -> reload();
        yearBox.addActionListener(refresh);
        monthBox.addActionListener(refresh);
        reload();
    }

    private JFreeChart createPieChart(String title, DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})")); // 含百分比
        return chart;
    }

    private void reload() {
        incomeDataset.clear();
        expenseDataset.clear();
        String year = (String) yearBox.getSelectedItem();
        String month = (String) monthBox.getSelectedItem();

        boolean hasYear = !"全部年份".equals(year);
        boolean hasMonth = !"全部月份".equals(month);

        String where = "user_id=?";
        if (hasYear) where += " AND strftime('%Y', date)=?";
        if (hasMonth) where += " AND strftime('%m', date)=?";

        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "SELECT type, category, SUM(amount) AS total FROM records WHERE " + where + " GROUP BY type, category";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            ps.setInt(idx++, userId);
            if (hasYear) ps.setString(idx++, year);
            if (hasMonth) ps.setString(idx++, month);
            ResultSet rs = ps.executeQuery();

            double totalIncome = 0, totalExpense = 0;
            while (rs.next()) {
                String type = rs.getString("type");
                String category = rs.getString("category");
                double amt = rs.getDouble("total");
                if ("收入".equals(type)) {
                    incomeDataset.setValue(category, amt);
                    totalIncome += amt;
                } else {
                    expenseDataset.setValue(category, amt);
                    totalExpense += amt;
                }
            }

            totalLabel.setText(String.format("總收入：%.2f 元     總支出：%.2f 元     淨收入：%.2f 元",
                    totalIncome, totalExpense, totalIncome - totalExpense));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshYearBox(int userId) {
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT strftime('%Y', date) y FROM records WHERE user_id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            java.util.List<String> years = new java.util.ArrayList<>();
            while (rs.next()) years.add(rs.getString("y"));
            if (years.isEmpty()) years.add("2025"); // fallback
            years.sort((a, b) -> b.compareTo(a));

            yearBox.removeAllItems();
            yearBox.addItem("全部年份");
            for (String y : years) yearBox.addItem(y);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
