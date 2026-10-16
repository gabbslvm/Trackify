package com.trackify.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.trackify.dao.UserDAO;
import com.trackify.manager.TransactionManager;
import com.trackify.model.User;

public class LoginPanel extends JFrame {
    private static final Color BLUE = new Color(30, 90, 130);
    private static final Color BACKGROUND = new Color(246, 247, 249);
    private static final Color GRAY_TEXT = new Color(120, 126, 134);

    private final TransactionManager manager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginPanel(TransactionManager manager) {
        this.manager = manager;

        setTitle("Trackify - Login");
        setSize(420, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        usernameField.setPreferredSize(new Dimension(0, 36));
        passwordField.setPreferredSize(new Dimension(0, 36));

        JButton loginButton = new JButton("Log In");
        loginButton.setBackground(BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(0, 42));
        loginButton.addActionListener(e -> login());

        JButton signUpButton = new JButton("Don't have an account? Sign Up");
        signUpButton.setForeground(BLUE);
        signUpButton.setContentAreaFilled(false);
        signUpButton.setBorderPainted(false);
        signUpButton.setFocusPainted(false);
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpButton.addActionListener(e -> openSignUp());

        gbc.gridy = 0;
        root.add(makeLabel("Trackify", 32, Font.BOLD, BLUE), gbc);
        gbc.gridy = 1;
        root.add(makeLabel("Log in to your account", 14, Font.PLAIN, GRAY_TEXT), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(24, 0, 2, 0);
        root.add(makeLabel("Username", 13, Font.BOLD, GRAY_TEXT), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(usernameField, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 2, 0);
        root.add(makeLabel("Password", 13, Font.BOLD, GRAY_TEXT), gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(passwordField, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 6, 0);
        root.add(loginButton, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 0, 6, 0);
        root.add(signUpButton, gbc);

        getRootPane().setDefaultButton(loginButton);
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        return label;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = new UserDAO().validateLogin(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }

        manager.setUserId(user.getId());
        new DashboardPanel(manager).setVisible(true);
        dispose();
    }

    private void openSignUp() {
        new SignUpPanel(manager).setVisible(true);
        dispose();
    }
}