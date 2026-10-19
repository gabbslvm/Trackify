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

public class SignUpPanel extends JPanel {
    private final MainFrame frame;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JButton signUpButton;

    public SignUpPanel(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(TrackifyTheme.CANVAS);
        JPanel root = TrackifyTheme.surfacePanel(new GridBagLayout(), new Insets(30, 44, 30, 44));
        root.setPreferredSize(new Dimension(430, 550));
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
        TrackifyTheme.styleInput(confirmField);

        signUpButton = TrackifyTheme.button("Create Account", TrackifyTheme.PRIMARY, Color.WHITE);
        signUpButton.setPreferredSize(new Dimension(0, 44));
        signUpButton.addActionListener(e -> signUp());

        JButton backButton = TrackifyTheme.button("Already have an account? Log In", TrackifyTheme.SURFACE,
            TrackifyTheme.PRIMARY);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> backToLogin());

        gbc.gridy = 0;
        root.add(makeLabel("Trackify", 32, Font.BOLD, TrackifyTheme.PRIMARY), gbc);
        gbc.gridy = 1;
        root.add(makeLabel("Create a new account", 14, Font.PLAIN, TrackifyTheme.MUTED), gbc);
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
        gbc.insets = new Insets(10, 0, 2, 0);
        root.add(makeLabel("Confirm Password", 13, Font.BOLD, TrackifyTheme.INK), gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(2, 0, 6, 0);
        root.add(confirmField, gbc);
        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 6, 0);
        root.add(signUpButton, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(6, 0, 6, 0);
        root.add(backButton, gbc);

    }

    JButton getDefaultButton() {
        return signUpButton;
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        return TrackifyTheme.label(text, size, style, color);
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
        frame.showLogin();
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmField.setText("");
    }
}