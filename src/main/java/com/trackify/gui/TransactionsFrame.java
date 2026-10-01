package com.trackify.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.trackify.manager.TransactionManager;
import com.trackify.model.Transaction;

public class TransactionsFrame extends JFrame {
    private static final Color BLUE = new Color(30, 90, 130);
    private static final Color GREEN = new Color(20, 140, 90);
    private static final Color RED = new Color(205, 65, 65);
    private static final Color GRAY = new Color(120, 126, 134);

    private final TransactionManager manager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField nameField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[] { "INCOME", "EXPENSE" });
    private final JTextField amountField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JLabel incomeValue = new JLabel("PHP 0.00");
    private final JLabel expenseValue = new JLabel("PHP 0.00");
    private final JLabel balanceValue = new JLabel("PHP 0.00");
    private final JLabel topValue = new JLabel("N/A");
    private int selectedId = -1;

    public TransactionsFrame(TransactionManager manager) {
        this.manager = manager;

        setTitle("Trackify - Transactions");
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Transactions");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Transaction Form"));
        formPanel.setPreferredSize(new Dimension(480, 330));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(formPanel, gbc, 0, 0, "Name", nameField);
        addFormField(formPanel, gbc, 0, 1, "Type", typeCombo);
        addFormField(formPanel, gbc, 0, 2, "Amount", amountField);
        addFormField(formPanel, gbc, 0, 3, "Date (yyyy-MM-dd)", dateField);
        addFormField(formPanel, gbc, 0, 4, "Category", categoryField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = makeButton("Add", BLUE);
        JButton editButton = makeButton("Edit", BLUE);
        JButton deleteButton = makeButton("Delete", RED);
        JButton undoButton = makeButton("Undo", GRAY);
        JButton clearButton = makeButton("Clear", GRAY);

        addButton.addActionListener(e -> addTransaction());
        editButton.addActionListener(e -> updateTransaction());
        deleteButton.addActionListener(e -> deleteTransaction());
        undoButton.addActionListener(e -> undoTransaction());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        addSummaryRow(summaryPanel, 0, "Total Income:", incomeValue);
        addSummaryRow(summaryPanel, 1, "Total Expenses:", expenseValue);
        addSummaryRow(summaryPanel, 2, "Remaining Balance:", balanceValue);
        addSummaryRow(summaryPanel, 3, "Top Category:", topValue);
        incomeValue.setForeground(GREEN);
        expenseValue.setForeground(RED);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = 4;
        filler.weighty = 1.0;
        summaryPanel.add(new JPanel(), filler);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        searchPanel.add(new JLabel("Keyword: "), BorderLayout.WEST);
        searchField.setPreferredSize(new Dimension(0, 30));
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = makeButton("Search", BLUE);
        searchButton.addActionListener(e -> searchTransactions());
        searchPanel.add(searchButton, BorderLayout.EAST);

        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(summaryPanel, BorderLayout.CENTER);

        content.add(formPanel, BorderLayout.WEST);
        content.add(rightPanel, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[] { "ID", "Name", "Type", "Amount", "Category", "Date" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
                fillFormFromSelected(selectedId);
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createTitledBorder("Transaction Records")));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(content, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        refreshTable();
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int x, int y, String labelText,
            java.awt.Component component) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(labelText + ":"), gbc);

        component.setPreferredSize(new Dimension(0, 30));
        gbc.gridx = x + 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
        gbc.weightx = 0.0;
    }

    private void addSummaryRow(JPanel panel, int row, String labelText, JLabel valueLabel) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 12, 8, 12);
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.gridx = 0;
        c.weightx = 0;
        panel.add(label, c);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(valueLabel, c);
    }

    private JButton makeButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(78, 32));
        return button;
    }

    private void addTransaction() {
        try {
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = dateField.getText().trim();
            String category = categoryField.getText().trim();

            manager.addTransaction(name, type, amount, date, category);
            clearForm();
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTransaction() {
        if (selectedId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = dateField.getText().trim();
            String category = categoryField.getText().trim();

            manager.editTransaction(selectedId, name, type, amount, date, category);
            clearForm();
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTransaction() {
        if (selectedId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected transaction?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.deleteTransaction(selectedId);
            clearForm();
            refreshTable();
        }
    }

    private void undoTransaction() {
        if (manager.undoLastAction()) {
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "No recent action to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchTransactions() {
        List<Transaction> matches = manager.searchTransactions(searchField.getText());
        tableModel.setRowCount(0);
        for (Transaction t : matches) {
            tableModel.addRow(new Object[] { t.getId(), t.getDescription(), t.getType(), t.getAmount(), t.getCategory(),
                    t.getDate() });
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Transaction t : manager.getTransactions()) {
            tableModel.addRow(new Object[] { t.getId(), t.getDescription(), t.getType(), t.getAmount(), t.getCategory(),
                    t.getDate() });
        }
        refreshSummary();
    }

    private void refreshSummary() {
        incomeValue.setText("PHP " + String.format("%,.2f", manager.getTotalIncome()));
        expenseValue.setText("PHP " + String.format("%,.2f", manager.getTotalExpenses()));
        balanceValue.setText("PHP " + String.format("%,.2f", manager.getRemainingBalance()));
        topValue.setText(manager.getTopSpendingCategory());
    }

    private void fillFormFromSelected(int id) {
        for (Transaction t : manager.getTransactions()) {
            if (t.getId() == id) {
                nameField.setText(t.getDescription());
                typeCombo.setSelectedItem(t.getType());
                amountField.setText(String.valueOf(t.getAmount()));
                dateField.setText(t.getDate().toString());
                categoryField.setText(t.getCategory());
                return;
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        typeCombo.setSelectedIndex(0);
        amountField.setText("");
        dateField.setText("");
        categoryField.setText("");
        searchField.setText("");
        table.clearSelection();
        refreshTable();
    }
}