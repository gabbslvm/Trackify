package com.trackify.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.trackify.manager.TransactionManager;

public class DashboardPanel extends JPanel {

    private final MainFrame frame;
    private final TransactionManager manager;
    private final JLabel incomeValue = new JLabel("PHP 0.00");
    private final JLabel expenseValue = new JLabel("PHP 0.00");
    private final JLabel balanceValue = new JLabel("PHP 0.00");
    private final JLabel topCategoryValue = new JLabel("N/A");
    private final JPanel balanceCard = new JPanel(new BorderLayout());
    private final DefaultTableModel monthlyModel = new DefaultTableModel(new Object[] { "Month", "Total Expenses" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DashboardPanel(MainFrame frame, TransactionManager manager) {
        this.frame = frame;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(TrackifyTheme.CANVAS);
        setBorder(new EmptyBorder(22, 34, 26, 34));

        JLabel brand = makeLabel("Trackify", 18, Font.BOLD, TrackifyTheme.PRIMARY);
        JButton logoutButton = TrackifyTheme.button("Log Out", TrackifyTheme.SURFACE, TrackifyTheme.MUTED);
        logoutButton.addActionListener(e -> logout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(brand, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(makeLabel("Dashboard", 30, Font.BOLD, TrackifyTheme.INK));
        heading.add(makeLabel("Here is your financial summary", 15, Font.PLAIN, TrackifyTheme.MUTED));

        JPanel top = new JPanel(new BorderLayout(0, 24));
        top.setOpaque(false);
        top.add(topBar, BorderLayout.NORTH);
        top.add(heading, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 14, 14));
        cards.setOpaque(false);
        balanceValue.setFont(new Font("SansSerif", Font.BOLD, 40));
        balanceValue.setForeground(Color.WHITE);
        balanceCard.setBackground(TrackifyTheme.PRIMARY);
        balanceCard.setBorder(new EmptyBorder(24, 28, 24, 28));
        balanceCard.add(makeLabel("Remaining Balance", 15, Font.PLAIN, new Color(211, 236, 228)), BorderLayout.NORTH);
        balanceCard.add(balanceValue, BorderLayout.CENTER);
        balanceCard.add(makeLabel("Income minus expenses", 13, Font.PLAIN, new Color(211, 236, 228)),
            BorderLayout.SOUTH);
        cards.add(balanceCard);
        cards.add(makeCard("Total Income", incomeValue, TrackifyTheme.GREEN));
        cards.add(makeCard("Total Expenses", expenseValue, TrackifyTheme.RED));
        cards.add(makeCard("Top Expense Categories", topCategoryValue, TrackifyTheme.PRIMARY));

        JTable monthlyTable = new JTable(monthlyModel);
        monthlyTable.setRowHeight(27);
        monthlyTable.setFillsViewportHeight(true);
        monthlyTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        monthlyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        monthlyTable.getTableHeader().setBackground(TrackifyTheme.SURFACE);
        monthlyTable.getTableHeader().setForeground(TrackifyTheme.MUTED);
        JScrollPane monthlyScroll = new JScrollPane(monthlyTable);
        monthlyScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel monthlyPanel = TrackifyTheme.surfacePanel(new BorderLayout(0, 10), new Insets(14, 18, 14, 18));
        monthlyPanel.add(makeLabel("Monthly Expenses", 17, Font.BOLD, TrackifyTheme.INK), BorderLayout.NORTH);
        monthlyPanel.add(monthlyScroll, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(cards, BorderLayout.NORTH);
        content.add(monthlyPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        JButton transactionsButton = TrackifyTheme.button("Open Transactions", TrackifyTheme.PRIMARY, Color.WHITE);
        transactionsButton.setPreferredSize(new Dimension(210, 46));
        transactionsButton.addActionListener(e -> openTransactions());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(transactionsButton);
        add(actions, BorderLayout.SOUTH);
    }

    private JLabel makeLabel(String text, int size, int style, Color color) {
        return TrackifyTheme.label(text, size, style, color);
    }

    private JPanel makeCard(String labelText, JLabel valueLabel, Color valueColor) {
        JPanel card = TrackifyTheme.surfacePanel(new BorderLayout(), new Insets(20, 28, 20, 28));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(valueColor);

        card.add(makeLabel(labelText, 15, Font.PLAIN, TrackifyTheme.MUTED), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private void logout() {
        frame.showLogin();
    }

    private void openTransactions() {
        frame.showTransactions();
    }

    public void refresh() {
        double balance = manager.getRemainingBalance();
        incomeValue.setText("PHP " + String.format("%,.2f", manager.getTotalIncome()));
        expenseValue.setText("PHP " + String.format("%,.2f", manager.getTotalExpenses()));
        balanceValue.setText("PHP " + String.format("%,.2f", balance));

        balanceCard.setBackground(balance < 0 ? TrackifyTheme.RED : TrackifyTheme.PRIMARY);
        java.util.List<String> topCategories = manager.getTopCategories();
        topCategoryValue.setText(topCategories.isEmpty() ? "N/A" : String.join(", ", topCategories));
        monthlyModel.setRowCount(0);
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy");
        for (Map.Entry<YearMonth, Double> entry : manager.getMonthlyExpenses().entrySet()) {
            monthlyModel.addRow(new Object[] { entry.getKey().format(monthFormat),
                    "PHP " + String.format("%,.2f", entry.getValue()) });
        }
    }
}