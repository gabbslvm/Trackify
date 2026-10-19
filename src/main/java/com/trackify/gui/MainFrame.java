package com.trackify.gui;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.trackify.manager.TransactionManager;

public class MainFrame extends JFrame {
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
        signUpPanel = new SignUpPanel(this);
        dashboardPanel = new DashboardPanel(this, manager);
        transactionsPanel = new TransactionsPanel(this, manager);

        container.add(loginPanel, LOGIN);
        container.add(signUpPanel, SIGNUP);
        container.add(dashboardPanel, DASHBOARD);
        container.add(transactionsPanel, TRANSACTIONS);
        setContentPane(container);

        showLogin();
    }

    public void showLogin() {
        loginPanel.clearFields();
        cards.show(container, LOGIN);
        getRootPane().setDefaultButton(loginPanel.getDefaultButton());
    }

    public void showSignUp() {
        signUpPanel.clearFields();
        cards.show(container, SIGNUP);
        getRootPane().setDefaultButton(signUpPanel.getDefaultButton());
    }

    public void showDashboard() {
        dashboardPanel.refresh();
        cards.show(container, DASHBOARD);
        getRootPane().setDefaultButton(null);
    }

    public void showTransactions() {
        transactionsPanel.refresh();
        cards.show(container, TRANSACTIONS);
        getRootPane().setDefaultButton(null);
    }
}