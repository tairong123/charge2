package org.example.ui;

import org.example.service.UserService;

import javax.swing.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("登入");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        add(panel);

        JLabel userLabel = new JLabel("帳號:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("密碼:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("登入");
        loginButton.setBounds(10, 80, 120, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("註冊");
        registerButton.setBounds(140, 80, 120, 25);
        panel.add(registerButton);

        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            int userId = UserService.login(username, password);

            if (userId != -1) {
                dispose(); // 關閉登入視窗
                new MainFrame(userId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "帳號或密碼錯誤");
            }
        });

        registerButton.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            Object[] fields = {"名字:", nameField, "帳號:", userField, "密碼:", passField};

            int option = JOptionPane.showConfirmDialog(this, fields, "註冊", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                boolean success = UserService.register(
                        nameField.getText(),
                        userField.getText(),
                        new String(passField.getPassword())
                );
                JOptionPane.showMessageDialog(this, success ? "註冊成功！" : "註冊失敗");
            }
        });
    }
}
