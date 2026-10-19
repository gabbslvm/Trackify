package com.trackify.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.trackify.dao.UserDAO;
import com.trackify.manager.TransactionManager;
import com.trackify.model.User;

public class LoginPanel extends JPanel {
    private final MainFrame frame;
    private final TransactionManager manager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton;

    public LoginPanel(MainFrame frame, TransactionManager manager) {
        this.frame = frame;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(TrackifyTheme.CANVAS);
        JPanel root = TrackifyTheme.surfacePanel(new GridBagLayout(), new Insets(36, 44, 36, 44));
        root.setPreferredSize(new Dimension(430, 470));
        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        centered.add(root);
        add(centered, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        TrackifyTheme.styleInput(usernameField);
        TrackifyTheme.styleInput(passwordField);

        loginButton = TrackifyTheme.button("Log In", TrackifyTheme.PRIMARY, Color.WHITE);
        loginButton.setPreferredSize(new Dimension(0, 44));
        loginButton.addActionListener(e -> login());

        JButton signUpButton = TrackifyTheme.button("Don't have an account? Sign Up", TrackifyTheme.SURFACE,
            TrackifyTheme.PRIMARY);
        signUpButton.setContentAreaFilled(false);
        signUpButton.setBorderPainted(false);
        signUpButton.setFocusPainted(false);
        signUpButton.addActionListener(e -> openSignUp());

        gbc.gridy = 0;
        root.add(makeLabel("Trackify", 32, Font.BOLD, TrackifyTheme.PRIMARY), gbc);
        gbc.gridy = 1;
        root.add(makeLabel("Log in to your account", 14, Font.PLAIN, TrackifyTheme.MUTED), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(24, 0, 2, 0);
        root.add(makeLabel("Username", 13, Font.BOLD, TrackifyTheme.INK), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(usernameField, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 2, 0);
        root.add(makeLabel("Password", 13, Font.BOLD, TrackifyTheme.INK), gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(passwordField, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 6, 0);
        root.add(loginButton, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 0, 6, 0);
        root.add(signUpButton, gbc);

    }

    JButton getDefaultButton() {
        return loginButton;
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        return TrackifyTheme.label(text, size, style, color);
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
        frame.showDashboard();
    }

    private void openSignUp() {
        frame.showSignUp();
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}