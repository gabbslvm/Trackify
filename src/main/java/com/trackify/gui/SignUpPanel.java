package com.trackify.gui;

import com.trackify.dao.UserDAO;
import com.trackify.manager.TransactionManager;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SignUpPanel extends JPanel {
    private final MainFrame frame;
    private final TransactionManager manager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JLabel messageLabel = MainFrame.label(" ", 12, Font.PLAIN, MainFrame.RED);
    private final JButton signUpButton = MainFrame.button("Create Account", MainFrame.BLUE, Color.WHITE);

    public SignUpPanel(MainFrame frame, TransactionManager manager) {
        this.frame = frame;
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(MainFrame.authBrand(
                "<html>Small habits.<br>Clearer finances.</html>",
                "<html>Track what comes in, what goes out,<br>and what stays with you.</html>"),
                BorderLayout.WEST);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(24, 35, 24, 35));
        JPanel form = MainFrame.transparent(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        form.add(MainFrame.label("YOUR NEXT GOOD HABIT", 10, Font.BOLD, MainFrame.MUTED), c);
        c.gridy = 1;
        c.insets = new Insets(10, 0, 4, 0);
        form.add(MainFrame.label("Make a fresh start.", 28, Font.BOLD, MainFrame.TEXT), c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 22, 0);
        form.add(MainFrame.label("Create your personal Trackify account.", 13, Font.PLAIN, MainFrame.MUTED), c);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        form.add(MainFrame.formRow("Username", usernameField), c);
        c.gridy = 4;
        form.add(MainFrame.formRow("Password", passwordField), c);
        c.gridy = 5;
        form.add(MainFrame.formRow("Confirm password", confirmField), c);
        c.gridy = 6;
        c.insets = new Insets(0, 0, 8, 0);
        form.add(messageLabel, c);
        c.gridy = 7;
        signUpButton.addActionListener(e -> signUp());
        form.add(signUpButton, c);

        JButton backButton = MainFrame.button("Already have an account? Log in", Color.WHITE, MainFrame.BLUE);
        backButton.addActionListener(e -> frame.showScreen("LOGIN"));
        c.gridy = 8;
        c.insets = new Insets(6, 0, 0, 0);
        form.add(backButton, c);

        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);
    }

    private void signUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }
        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        if (manager.isDemoMode()) {
            frame.showScreen("LOGIN");
            frame.showLoginMessage("Preview account created. Log in with any username and password.");
            return;
        }

        UserDAO dao = new UserDAO();
        if (dao.usernameExists(username)) {
            messageLabel.setText("Username is already taken.");
            return;
        }
        if (!dao.registerUser(username, password)) {
            messageLabel.setText("Could not create the account. Please try again.");
            return;
        }

        frame.showScreen("LOGIN");
        frame.showLoginMessage("Account created. You can now log in.");
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        messageLabel.setText(" ");
    }

    JButton getSignUpButton() {
        return signUpButton;
    }
}