package com.trackify.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.trackify.manager.TransactionManager;

public class DashboardFrame extends JFrame {

    private static final Color BLUE = new Color(30, 90, 130);
    private static final Color GREEN = new Color(20, 140, 90);
    private static final Color RED = new Color(205, 65, 65);
    private static final Color BACKGROUND = new Color(246, 247, 249);
    private static final Color GRAY_TEXT = new Color(120, 126, 134);
    private static final Color LIGHT_BLUE_TEXT = new Color(205, 225, 240);
    private final TransactionManager manager;
    private final JLabel incomeValue = new JLabel("PHP 0.00");
    private final JLabel expenseValue = new JLabel("PHP 0.00");
    private final JLabel balanceValue = new JLabel("PHP 0.00");
    private final JPanel balanceCard = new JPanel(new BorderLayout());

    public DashboardFrame(TransactionManager manager) {
        this.manager = manager;

        setTitle("Trackify - Dashboard");
        setSize(920, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(24, 40, 32, 40));
        setContentPane(root);

        // top side sa title, logout etc
        JLabel brand = makeLabel("Trackify", 18, Font.BOLD, BLUE);
        JButton logoutButton = makeButton("Log Out", new Color(232, 235, 240), GRAY_TEXT);
        logoutButton.addActionListener(e -> logout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(brand, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(makeLabel("Dashboard", 30, Font.BOLD, new Color(30, 34, 40)));
        heading.add(makeLabel("Here is your financial summary", 15, Font.PLAIN, GRAY_TEXT));

        JPanel top = new JPanel(new BorderLayout(0, 24));
        top.setOpaque(false);
        top.add(topBar, BorderLayout.NORTH);
        top.add(heading, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        // left side balance
        balanceValue.setFont(new Font("SansSerif", Font.BOLD, 40));
        balanceValue.setForeground(Color.WHITE);
        balanceCard.setBackground(BLUE);
        balanceCard.setBorder(new EmptyBorder(24, 28, 24, 28));
        balanceCard.add(makeLabel("Balance", 15, Font.PLAIN, LIGHT_BLUE_TEXT), BorderLayout.NORTH);
        balanceCard.add(balanceValue, BorderLayout.CENTER);
        balanceCard.add(makeLabel("Income minus expenses", 13, Font.PLAIN, LIGHT_BLUE_TEXT), BorderLayout.SOUTH);

        // right side total income at total expenses
        JPanel rightColumn = new JPanel(new GridLayout(2, 1, 20, 20));
        rightColumn.setOpaque(false);
        rightColumn.add(makeCard("Total income", incomeValue, GREEN));
        rightColumn.add(makeCard("Total expenses", expenseValue, RED));

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 20));
        cards.setOpaque(false);
        cards.add(balanceCard);
        cards.add(rightColumn);
        root.add(cards, BorderLayout.CENTER);

        // bottom part yung open transaction
        JButton transactionsButton = makeButton("Open Transactions", BLUE, Color.WHITE);
        transactionsButton.setPreferredSize(new Dimension(210, 46));
        transactionsButton.addActionListener(e -> openTransactions());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(transactionsButton);
        root.add(actions, BorderLayout.SOUTH);

        // Refresh
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshTotals();
            }
        });

        refreshTotals();
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        return label;
    }

    private JButton makeButton(String text, Color background, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(textColor);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // card design white
    private JPanel makeCard(String labelText, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 233, 238)),
                new EmptyBorder(20, 28, 20, 28)));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);

        card.add(makeLabel(labelText, 15, Font.PLAIN, GRAY_TEXT), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private void refreshTotals() {
        double balance = manager.getRemainingBalance();
        incomeValue.setText("PHP " + String.format("%,.2f", manager.getTotalIncome()));
        expenseValue.setText("PHP " + String.format("%,.2f", manager.getTotalExpenses()));
        balanceValue.setText("PHP " + String.format("%,.2f", balance));

        balanceCard.setBackground(balance < 0 ? RED : BLUE);
    }

    private void openTransactions() {
        new TransactionsFrame(manager).setVisible(true);
    }

    private void logout() {
        new LoginFrame(manager).setVisible(true);
        dispose();
    }
}