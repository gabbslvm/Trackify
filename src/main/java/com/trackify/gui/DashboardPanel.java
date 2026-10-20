package com.trackify.gui;

import com.trackify.manager.TransactionManager;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {
    private final TransactionManager manager;
    private final JLabel incomeValue = MainFrame.label("PHP 0.00", 28, Font.BOLD, MainFrame.GREEN);
    private final JLabel expenseValue = MainFrame.label("PHP 0.00", 28, Font.BOLD, MainFrame.RED);
    private final JLabel balanceValue = MainFrame.label("PHP 0.00", 54, Font.BOLD, Color.WHITE);
    private final JLabel topCategory = MainFrame.label("N/A", 30, Font.BOLD, MainFrame.TEXT);
    private final JLabel topAmount = MainFrame.label("No expenses yet", 13, Font.PLAIN, MainFrame.MUTED);
    private final MainFrame.RoundedPanel balanceCard = new MainFrame.RoundedPanel(new BorderLayout(0, 16), MainFrame.BLUE);
    private final DefaultTableModel monthlyModel = new DefaultTableModel(new Object[] { "Month", "Total expenses" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DashboardPanel(MainFrame frame, TransactionManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(MainFrame.BACKGROUND);
        add(MainFrame.navigation(frame, manager), BorderLayout.NORTH);

        JPanel content = MainFrame.transparent(new BorderLayout(0, 20));
        content.setBorder(new EmptyBorder(25, 32, 20, 32));
        JPanel heading = MainFrame.transparent(new BorderLayout(20, 0));
        JPanel titles = MainFrame.transparent(new GridLayout(3, 1, 0, 3));
        titles.add(MainFrame.label("DASHBOARD", 10, Font.BOLD, MainFrame.MUTED));
        titles.add(MainFrame.label("Here is your financial summary", 32, Font.BOLD, MainFrame.TEXT));
        titles.add(MainFrame.label("A little clarity for your everyday spending.", 13, Font.PLAIN, MainFrame.MUTED));
        heading.add(titles, BorderLayout.CENTER);

        JButton openButton = MainFrame.button("Open Transactions", MainFrame.BLUE, Color.WHITE);
        openButton.addActionListener(e -> frame.showScreen("TRANSACTIONS"));
        JPanel action = MainFrame.transparent(new GridBagLayout());
        action.add(openButton);
        heading.add(action, BorderLayout.EAST);
        content.add(heading, BorderLayout.NORTH);

        JPanel body = MainFrame.transparent(new GridBagLayout());
        JPanel totals = MainFrame.transparent(new GridBagLayout());
        balanceCard.setBorder(new EmptyBorder(26, 30, 24, 30));
        balanceCard.add(MainFrame.label("Remaining balance", 13, Font.PLAIN, new Color(230, 240, 250)), BorderLayout.NORTH);
        balanceCard.add(balanceValue, BorderLayout.CENTER);
        balanceCard.add(MainFrame.label("Total income minus total expenses", 12, Font.PLAIN, new Color(230, 240, 250)), BorderLayout.SOUTH);
        GridBagConstraints totalsConstraints = new GridBagConstraints();
        totalsConstraints.gridy = 0;
        totalsConstraints.fill = GridBagConstraints.BOTH;
        totalsConstraints.weighty = 1;
        totalsConstraints.weightx = 1.65;
        totalsConstraints.insets = new Insets(0, 0, 0, 9);
        totals.add(balanceCard, totalsConstraints);

        JPanel right = MainFrame.transparent(new GridLayout(2, 1, 0, 14));
        right.add(totalCard("Total income", incomeValue));
        right.add(totalCard("Total expenses", expenseValue));
        totalsConstraints.gridx = 1;
        totalsConstraints.weightx = 1;
        totalsConstraints.insets = new Insets(0, 9, 0, 0);
        totals.add(right, totalsConstraints);
        GridBagConstraints bodyConstraints = new GridBagConstraints();
        bodyConstraints.gridx = 0;
        bodyConstraints.gridy = 0;
        bodyConstraints.weightx = 1;
        bodyConstraints.weighty = 1.4;
        bodyConstraints.fill = GridBagConstraints.BOTH;
        body.add(totals, bodyConstraints);

        JPanel details = MainFrame.transparent(new GridLayout(1, 2, 18, 0));
        MainFrame.RoundedPanel top = new MainFrame.RoundedPanel(new BorderLayout(0, 12), Color.WHITE);
        top.setBorder(new EmptyBorder(18, 20, 18, 20));
        top.add(MainFrame.label("Top spending category", 14, Font.BOLD, MainFrame.TEXT), BorderLayout.NORTH);
        JPanel topValues = MainFrame.transparent(new GridLayout(2, 1, 0, 5));
        topValues.add(topCategory);
        topValues.add(topAmount);
        top.add(topValues, BorderLayout.CENTER);
        details.add(top);
        details.add(monthlyCard());
        bodyConstraints.gridy = 1;
        bodyConstraints.weighty = 1;
        bodyConstraints.insets = new Insets(18, 0, 0, 0);
        body.add(details, bodyConstraints);
        content.add(body, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel totalCard(String title, JLabel value) {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(new BorderLayout(0, 4), Color.WHITE);
        card.setBorder(new EmptyBorder(10, 23, 10, 23));
        card.add(MainFrame.label(title, 12, Font.PLAIN, MainFrame.MUTED), BorderLayout.NORTH);
        value.setFont(new Font(MainFrame.UI_FONT, Font.BOLD, 28));
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel monthlyCard() {
        MainFrame.RoundedPanel card = new MainFrame.RoundedPanel(new BorderLayout(0, 14), Color.WHITE);
        card.setBorder(new EmptyBorder(20, 23, 15, 23));
        card.add(MainFrame.label("Monthly expenses", 14, Font.BOLD, MainFrame.TEXT), BorderLayout.NORTH);

        JTable table = new JTable(monthlyModel);
        table.setFont(new Font(MainFrame.UI_FONT, Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(MainFrame.TEXT);
        table.getTableHeader().setFont(new Font(MainFrame.UI_FONT, Font.BOLD, 12));
        table.getTableHeader().setBackground(MainFrame.BACKGROUND);
        table.getTableHeader().setForeground(MainFrame.MUTED);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            {
                setFont(new Font(MainFrame.UI_FONT, Font.PLAIN, 13));
                setForeground(MainFrame.TEXT);
                setBackground(Color.WHITE);
            }
        });
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer();
        moneyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(moneyRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        double balance = manager.getRemainingBalance();
        incomeValue.setText(MainFrame.money(manager.getTotalIncome()));
        expenseValue.setText(MainFrame.money(manager.getTotalExpenses()));
        balanceValue.setText(MainFrame.money(balance));
        balanceCard.setBackground(balance < 0 ? MainFrame.RED : MainFrame.BLUE);
        balanceCard.repaint();

        List<Map.Entry<String, Double>> categories = manager.getTopCategories(1);
        if (categories.isEmpty()) {
            topCategory.setText("N/A");
            topAmount.setText("No expenses yet");
        } else {
            Map.Entry<String, Double> top = categories.get(0);
            topCategory.setText(top.getKey());
            topAmount.setText(MainFrame.money(top.getValue()) + " in expenses");
        }

        monthlyModel.setRowCount(0);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        for (Map.Entry<YearMonth, Double> month : manager.getMonthlyExpenses().entrySet()) {
            monthlyModel.addRow(new Object[] { month.getKey().format(format), MainFrame.money(month.getValue()) });
        }
        if (monthlyModel.getRowCount() == 0) {
            monthlyModel.addRow(new Object[] { "No expenses yet", MainFrame.money(0) });
        }
    }
}