package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final int userId;
    private final JTabbedPane tabbedPane;

    public MainFrame(int userId) {
        this.userId = userId;

        setTitle("記帳程式");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

        tabbedPane.add("記帳", new TodayPanel(userId));
        tabbedPane.add("歷史紀錄", new HistoryPanel(userId));
        tabbedPane.add("收支分析", new OwnChartPanel(userId));

        tabbedPane.addChangeListener(e -> refreshTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshTab() {
        int index = tabbedPane.getSelectedIndex();
        String title = tabbedPane.getTitleAt(index);
        switch (title) {
            case "記帳" -> tabbedPane.setComponentAt(index, new TodayPanel(userId));
            case "歷史紀錄" -> tabbedPane.setComponentAt(index, new HistoryPanel(userId));
            case "收支分析" -> tabbedPane.setComponentAt(index, new OwnChartPanel(userId));
        }
    }
}
