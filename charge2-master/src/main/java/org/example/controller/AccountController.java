package org.example.controller;// controller/controller.AccountController.java
import org.example.model.*;
import org.example.view.ConsoleUI;

import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class AccountController {
    private TransactionDAO dao;
    private ConsoleUI ui;

    public AccountController(Connection conn) {
        this.dao = new TransactionDAO(conn);
        this.ui = new ConsoleUI();
    }

    public void run() {
        while (true) {
            ui.showMessage("\n=== 記帳系統選單 ===");
            ui.showMessage("1. 新增記帳");
            ui.showMessage("2. 顯示所有記帳紀錄");
            ui.showMessage("3. 查詢某月份的收支總和");
            ui.showMessage("4. 顯示圖表分析");
            ui.showMessage("5. 刪除某筆記帳紀錄");
            ui.showMessage("0. 離開");
            String choice = ui.prompt("請輸入選項：");

            switch (choice) {
                case "1" -> addTransaction();
                case "2" -> showAllTransactions();
                case "3" -> showMonthlySummary();
                case "4" -> showChartAnalysis();
                case "5" -> deleteTransaction();
                case "0" -> {
                    ui.showMessage("再見！");
                    return;
                }
                default -> ui.showMessage("無效的選項，請重新輸入。");
            }
        }
    }

    private void addTransaction() {
        String type = ui.prompt("輸入類別（收入/支出）：");
        String description = ui.prompt("輸入描述：");
        double amount = Double.parseDouble(ui.prompt("輸入金額："));
        String date = ui.prompt("輸入日期（YYYY-MM-DD）：");

        Transaction tx = new Transaction(type, description, amount, date);
        try {
            dao.insertTransaction(tx);
            ui.showMessage("✅ 記帳成功！");
        } catch (Exception e) {
            ui.showMessage("❌ 記帳失敗：" + e.getMessage());
        }
    }

    private void showAllTransactions() {
        try {
            List<Transaction> list = dao.getAllTransactions();
            if (list.isEmpty()) {
                ui.showMessage("目前沒有任何記帳資料！");
            } else {
                ui.showMessage("📋 所有記帳紀錄：");
                for (Transaction t : list) {
                    ui.showMessage(t.getId() + " | " + t.getDate() + " | " + t.getType()
                            + " | " + t.getDescription() + " | $" + t.getAmount());
                }
            }
        } catch (Exception e) {
            ui.showMessage("讀取失敗：" + e.getMessage());
        }
    }

    private void showMonthlySummary() {
        String month = ui.prompt("請輸入要查詢的月份（格式：YYYY-MM）：");
        try {
            double income = dao.getMonthlyTotal(month, "收入");
            double expense = dao.getMonthlyTotal(month, "支出");

            ui.showMessage("🔎 " + month + " 收入總和：$" + income);
            ui.showMessage("🔎 " + month + " 支出總和：$" + expense);
        } catch (Exception e) {
            ui.showMessage("查詢失敗：" + e.getMessage());
        }
    }

    private void showChartAnalysis() {
        try {
            List<Transaction> list = dao.getAllTransactions();
            if (list.isEmpty()) {
                ui.showMessage("目前沒有任何記帳資料，無法產生圖表！");
                return;
            }

            // 📊 1. 收支比例 - Pie Chart
            double totalIncome = 0;
            double totalExpense = 0;
            for (Transaction t : list) {
                if (t.getType().equals("收入")) {
                    totalIncome += t.getAmount();
                } else if (t.getType().equals("支出")) {
                    totalExpense += t.getAmount();
                }
            }

            org.jfree.data.general.DefaultPieDataset pieDataset = new org.jfree.data.general.DefaultPieDataset();
            pieDataset.setValue("收入", totalIncome);
            pieDataset.setValue("支出", totalExpense);

            org.jfree.chart.JFreeChart pieChart = org.jfree.chart.ChartFactory.createPieChart(
                    "收入與支出比例", pieDataset, true, true, false);

            Font chineseFont = new Font("微軟正黑體", Font.PLAIN, 14);
            pieChart.getTitle().setFont(new Font("微軟正黑體", Font.BOLD, 18));
            pieChart.getLegend().setItemFont(chineseFont);
            org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) pieChart.getPlot();
            plot.setLabelFont(chineseFont);

            org.jfree.chart.ChartFrame pieFrame = new org.jfree.chart.ChartFrame("收支分析", pieChart);
            pieFrame.pack();
            pieFrame.setVisible(true);

            //  2. 每月支出 - Line Chart
            java.util.Map<String, Double> monthlyExpenseMap = new java.util.TreeMap<>();
            for (Transaction t : list) {
                System.out.println("Date: " + t.getDate());
                if (t.getType().equals("支出")) {
                    String month = t.getDate().substring(0, 7); // YYYY-MM
                    monthlyExpenseMap.put(month,
                            monthlyExpenseMap.getOrDefault(month, 0.0) + t.getAmount());
                }
            }

            org.jfree.data.category.DefaultCategoryDataset lineDataset = new org.jfree.data.category.DefaultCategoryDataset();
            for (String month : monthlyExpenseMap.keySet()) {
                lineDataset.addValue(monthlyExpenseMap.get(month), "支出", month);
            }

            org.jfree.chart.JFreeChart lineChart = org.jfree.chart.ChartFactory.createLineChart(
                    "每月支出趨勢", "月份", "金額", lineDataset);

            Font font = new Font("微軟正黑體", Font.PLAIN, 12);
            lineChart.getTitle().setFont(new Font("微軟正黑體", Font.BOLD, 18));
            lineChart.getCategoryPlot().getDomainAxis().setLabelFont(font);
            lineChart.getCategoryPlot().getRangeAxis().setLabelFont(font);
            lineChart.getLegend().setItemFont(font);
            lineChart.getCategoryPlot().getRenderer().setDefaultItemLabelFont(font); // 如果有標籤也一起設


            org.jfree.chart.ChartFrame lineFrame = new org.jfree.chart.ChartFrame("趨勢圖", lineChart);
            lineFrame.pack();
            lineFrame.setVisible(true);

        } catch (Exception e) {
            ui.showMessage("圖表產生失敗：" + e.getMessage());
        }
    }

    private void deleteTransaction() {
        try {
            List<Transaction> list = dao.getAllTransactions();
            if (list.isEmpty()) {
                ui.showMessage("目前沒有任何記帳資料！");
                return;
            }

            ui.showMessage("所有記帳紀錄：");
            for (Transaction t : list) {
                ui.showMessage(t.getId() + " | " + t.getDate() + " | " + t.getType()
                        + " | " + t.getDescription() + " | $" + t.getAmount());
            }

            int id = Integer.parseInt(ui.prompt("請輸入要刪除的記帳 ID："));
            boolean success = dao.deleteTransaction(id);
            if (success) {
                ui.showMessage("刪除成功！");
            } else {
                ui.showMessage("刪除失敗，請確認 ID 是否正確！");
            }
        } catch (Exception e) {
            ui.showMessage("刪除時發生錯誤：" + e.getMessage());
        }
    }



}