package com.trackify.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.trackify.manager.TransactionManager;

public class MainFrame extends JFrame {
    private final TransactionManager manager;

    public MainFrame(TransactionManager manager) {
        this.manager = manager;

        setTitle("Trackify");
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showLogin();
    }

    private void show(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    public void showLogin() {
        // show(new LoginPanel(this, manager));
    }

    public void showSignUp() {
        // show(new SignUpPanel(this, manager));
    }

    public void showDashboard() {
        // show(new DashboardPanel(this, manager));
    }

    public void showTransactions() {
        // show(new TransactionsPanel(this, manager));
    }
}