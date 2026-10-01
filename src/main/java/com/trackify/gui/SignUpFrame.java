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

public class SignUpFrame extends JFrame {
    private static final Color BLUE = new Color(30, 90, 130);
    private static final Color BACKGROUND = new Color(246, 247, 249);
    private static final Color GRAY_TEXT = new Color(120, 126, 134);

    private final TransactionManager manager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    public SignUpFrame(TransactionManager manager) {
        this.manager = manager;

        setTitle("Trackify - Sign Up");
        setSize(420, 560);
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
        confirmField.setPreferredSize(new Dimension(0, 36));

        JButton signUpButton = new JButton("Create Account");
        signUpButton.setBackground(BLUE);
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        signUpButton.setOpaque(true);
        signUpButton.setBorderPainted(false);
        signUpButton.setFocusPainted(false);
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpButton.setPreferredSize(new Dimension(0, 42));
        signUpButton.addActionListener(e -> signUp());

        JButton backButton = new JButton("Already have an account? Log In");
        backButton.setForeground(BLUE);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> backToLogin());

        gbc.gridy = 0;
        root.add(makeLabel("Trackify", 32, Font.BOLD, BLUE), gbc);
        gbc.gridy = 1;
        root.add(makeLabel("Create a new account", 14, Font.PLAIN, GRAY_TEXT), gbc);
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
        gbc.insets = new Insets(10, 0, 2, 0);
        root.add(makeLabel("Confirm Password", 13, Font.BOLD, GRAY_TEXT), gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(confirmField, gbc);
        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 6, 0);
        root.add(signUpButton, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(6, 0, 6, 0);
        root.add(backButton, gbc);

        getRootPane().setDefaultButton(signUpButton);
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        return label;
    }

    private void signUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        UserDAO dao = new UserDAO();
        if (dao.usernameExists(username)) {
            showError("Username is already taken.");
            return;
        }
        if (!dao.registerUser(username, password)) {
            showError("Could not create the account. Please try again.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created! You can now log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        backToLogin();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Sign Up Error", JOptionPane.ERROR_MESSAGE);
    }

    private void backToLogin() {
        new LoginFrame(manager).setVisible(true);
        dispose();
    }
}