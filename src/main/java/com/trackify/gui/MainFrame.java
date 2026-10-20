package com.trackify.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.trackify.manager.TransactionManager;

public class MainFrame extends JFrame {
    // ===== Colors / font (shared by all panels) =====
    static final Color BACKGROUND = TrackifyTheme.CANVAS;
    static final Color TEXT = TrackifyTheme.INK;
    static final Color MUTED = TrackifyTheme.MUTED;
    static final Color BLUE = TrackifyTheme.PRIMARY;
    static final Color GREEN = TrackifyTheme.GREEN;
    static final Color RED = TrackifyTheme.RED;
    static final String UI_FONT = "SansSerif";

    // ===== Screen names =====
    private static final String LOGIN = "LOGIN";
    private static final String SIGNUP = "SIGNUP";
    private static final String DASHBOARD = "DASHBOARD";
    private static final String TRANSACTIONS = "TRANSACTIONS";

    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);

    private final LoginPanel loginPanel;
    private final SignUpPanel signUpPanel;
    private final DashboardPanel dashboardPanel;
    private final TransactionsPanel transactionsPanel;

    public MainFrame(TransactionManager manager) {
        setTitle("Trackify");
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this, manager);
        signUpPanel = new SignUpPanel(this, manager);
        dashboardPanel = new DashboardPanel(this, manager);
        transactionsPanel = new TransactionsPanel(this, manager);

        container.add(loginPanel, LOGIN);
        container.add(signUpPanel, SIGNUP);
        container.add(dashboardPanel, DASHBOARD);
        container.add(transactionsPanel, TRANSACTIONS);
        setContentPane(container);

        showScreen(LOGIN);
    }

    // ===== Navigation =====
    public void showScreen(String name) {
        switch (name) {
            case LOGIN -> {
                loginPanel.clearFields();
                getRootPane().setDefaultButton(loginPanel.getLoginButton());
            }
            case SIGNUP -> {
                signUpPanel.clearFields();
                getRootPane().setDefaultButton(signUpPanel.getSignUpButton());
            }
            case DASHBOARD -> {
                dashboardPanel.refresh();
                getRootPane().setDefaultButton(null);
            }
            case TRANSACTIONS -> {
                transactionsPanel.refresh();
                getRootPane().setDefaultButton(null);
            }
        }
        cards.show(container, name);
    }

    // Kept so TransactionsPanel (frame.showDashboard()) still works
    public void showLogin() { showScreen(LOGIN); }
    public void showSignUp() { showScreen(SIGNUP); }
    public void showDashboard() { showScreen(DASHBOARD); }
    public void showTransactions() { showScreen(TRANSACTIONS); }

    // Shows a message on the login screen (call AFTER showScreen("LOGIN"))
    public void showLoginMessage(String message) {
        loginPanel.showMessage(message, GREEN);
    }

    // ===== Shared UI helpers =====
    static JLabel label(String text, int size, int style, Color color) {
        return TrackifyTheme.label(text, size, style, color);
    }

    static JButton button(String text, Color background, Color foreground) {
        return TrackifyTheme.button(text, background, foreground);
    }

    static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    static String money(double amount) {
        return "PHP " + String.format("%,.2f", amount);
    }

    // Label on top, input below
    static JPanel formRow(String labelText, JComponent field) {
        TrackifyTheme.styleInput(field);
        field.setPreferredSize(new Dimension(0, 38));
        JPanel row = transparent(new BorderLayout(0, 6));
        row.add(label(labelText, 13, Font.BOLD, TEXT), BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // Colored left side of the Login / Sign Up screens
    static JPanel authBrand(String title, String subtitle) {
        JPanel side = new JPanel(new GridBagLayout());
        side.setBackground(BLUE);
        side.setPreferredSize(new Dimension(420, 0));

        JPanel inner = transparent(null);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(0, 40, 0, 40));

        JLabel brand = label("Trackify", 18, Font.BOLD, Color.WHITE);
        JLabel heading = label(title, 34, Font.BOLD, Color.WHITE);
        JLabel sub = label(subtitle, 14, Font.PLAIN, new Color(211, 236, 228));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(brand);
        inner.add(javax.swing.Box.createVerticalStrut(30));
        inner.add(heading);
        inner.add(javax.swing.Box.createVerticalStrut(14));
        inner.add(sub);

        side.add(inner, new GridBagConstraints());
        return side;
    }

    // Top bar of the Dashboard: brand on the left, Log Out on the right
    static JPanel navigation(MainFrame frame, TransactionManager manager) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(0, 0, 1, 0, TrackifyTheme.BORDER),
                new EmptyBorder(12, 32, 12, 32)));

        bar.add(label("Trackify", 20, Font.BOLD, BLUE), BorderLayout.WEST);

        JButton logout = button("Log Out", BACKGROUND, MUTED);
        logout.addActionListener(e -> frame.showScreen(LOGIN));
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // Panel with rounded corners (used for the dashboard cards)
    static class RoundedPanel extends JPanel {
        RoundedPanel(LayoutManager layout, Color background) {
            super(layout);
            setBackground(background);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}