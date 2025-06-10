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

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("記帳系統");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("登出");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "確定要登出？", "登出確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        topPanel.add(logoutPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH); // ➕ 加到主視窗上方

        // 下方分頁內容
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
