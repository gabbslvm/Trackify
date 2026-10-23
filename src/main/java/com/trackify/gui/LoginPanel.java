package com.trackify.gui;

import com.trackify.dao.DataAccessException;
import com.trackify.dao.UserDAO;
import com.trackify.manager.TransactionManager;
import com.trackify.model.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginPanel extends JPanel {
    private final MainFrame frame;
    private final TransactionManager manager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel messageLabel = MainFrame.label(" ", 12, Font.PLAIN, MainFrame.RED);
    private final JButton loginButton = MainFrame.button("Log In", MainFrame.BLUE, Color.WHITE);

    public LoginPanel(MainFrame frame, TransactionManager manager) {
        this.frame = frame;
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(MainFrame.authBrand(
                "<html>A clearer view<br>of your money.</html>",
                "<html>Keep your income, expenses, and<br>everyday decisions in one place.</html>"),
                BorderLayout.WEST);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(30, 35, 30, 35));
        JPanel form = MainFrame.transparent(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        form.add(MainFrame.label("LET'S PICK UP WHERE YOU LEFT OFF", 10, Font.BOLD, MainFrame.MUTED), c);
        c.gridy = 1;
        c.insets = new Insets(10, 0, 4, 0);
        form.add(MainFrame.label("Welcome back.", 28, Font.BOLD, MainFrame.TEXT), c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 28, 0);
        form.add(MainFrame.label("Log in to see your financial overview.", 13, Font.PLAIN, MainFrame.MUTED), c);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 18, 0);
        form.add(MainFrame.formRow("Username", usernameField), c);
        c.gridy = 4;
        form.add(MainFrame.formRow("Password", passwordField), c);
        c.gridy = 5;
        c.insets = new Insets(0, 0, 10, 0);
        form.add(messageLabel, c);
        c.gridy = 6;
        loginButton.addActionListener(e -> login());
        form.add(loginButton, c);

        JButton signUpButton = MainFrame.button("New to Trackify? Create an account", Color.WHITE, MainFrame.BLUE);
        signUpButton.addActionListener(e -> frame.showScreen("SIGNUP"));
        c.gridy = 7;
        c.insets = new Insets(8, 0, 0, 0);
        form.add(signUpButton, c);

        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter your username and password.", MainFrame.RED);
            return;
        }

        try {
            User user = new UserDAO().validateLogin(username, password);
            if (user == null) {
                showMessage("Invalid username or password.", MainFrame.RED);
                passwordField.setText("");
                return;
            }

            manager.setUserId(user.getId());
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            showMessage("Cannot reach the database. Start MySQL in XAMPP.", MainFrame.RED);
            return;
        }

        frame.showScreen("DASHBOARD");
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        showMessage(" ", MainFrame.RED);
    }

    public void showMessage(String message, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText(message);
    }

    JButton getLoginButton() {
        return loginButton;
    }
}