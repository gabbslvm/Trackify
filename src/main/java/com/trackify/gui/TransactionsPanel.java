package com.trackify.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.trackify.manager.TransactionManager;
import com.trackify.model.Transaction;

public class TransactionsPanel extends JPanel {
    private static final String OTHERS = "Others";
    private static final Color UNDO_HIGHLIGHT = new Color(255, 232, 140);
    private static final String[] INCOME_CATEGORIES = { "Salary", "Allowance", "Incentive", "Bonus",
            "Freelance", "Gift", OTHERS };
    private static final String[] EXPENSE_CATEGORIES = { "Food", "Beverage", "Transportation", "Entertainment",
            "Electricity", "Water", "Internet", "Rent", "Bills", "Shopping", "Health", "Education", OTHERS };

    private final MainFrame frame;
    private final TransactionManager manager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField nameField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[] { "INCOME", "EXPENSE" });
    private final JTextField amountField = new JTextField();
    private final DatePicker datePicker;
    private final JComboBox<String> categoryCombo = new JComboBox<>();
    private final JTextField otherCategoryField = new JTextField();
    private final JPanel categoryPanel = new JPanel(new BorderLayout(8, 0));
    private final JTextField searchField = new JTextField();
    private final JLabel incomeValue = new JLabel("PHP 0.00");
    private final JLabel expenseValue = new JLabel("PHP 0.00");
    private final JLabel balanceValue = new JLabel("PHP 0.00");
    private final JLabel topValue = new JLabel("N/A");
    private int selectedId = -1;
    private boolean ignoreSelection = false;

    public TransactionsPanel(MainFrame frame, TransactionManager manager) {
        this.frame = frame;
        this.manager = manager;

        setBackground(TrackifyTheme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 18, 20));
        setLayout(new BorderLayout(12, 12));

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TrackifyTheme.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Transactions");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JButton backButton = TrackifyTheme.button("Back to Dashboard", TrackifyTheme.SURFACE,
                TrackifyTheme.PRIMARY_DARK);
        backButton.addActionListener(e -> backToDashboard());
        header.add(backButton, BorderLayout.EAST);

        // ===== Date picker (calendar, mm/dd/yyyy) =====
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("MM/dd/yyyy");
        datePicker = new DatePicker(dateSettings);
        datePicker.setDate(LocalDate.now());

        // ===== Category (dropdown + "Others" text box) =====
        TrackifyTheme.styleInput(categoryCombo);
        TrackifyTheme.styleInput(otherCategoryField);
        otherCategoryField.setPreferredSize(new Dimension(150, 30));
        categoryPanel.setOpaque(false);
        categoryPanel.setPreferredSize(new Dimension(0, 30));
        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        categoryPanel.add(otherCategoryField, BorderLayout.EAST);
        categoryCombo.addActionListener(e -> updateOtherVisibility());
        typeCombo.addActionListener(e -> loadCategories());
        loadCategories();

        // ===== Form =====
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel formPanel = TrackifyTheme.sectionPanel("Transaction Form", new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(500, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        addFormField(formPanel, gbc, 0, 0, "Name", nameField);
        addFormField(formPanel, gbc, 0, 1, "Type", typeCombo);
        addFormField(formPanel, gbc, 0, 2, "Amount", amountField);
        addFormRow(formPanel, gbc, 0, 3, "Date (mm/dd/yyyy)", datePicker);
        addFormRow(formPanel, gbc, 0, 4, "Category", categoryPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = makeButton("Add", TrackifyTheme.PRIMARY);
        JButton editButton = makeButton("Edit", TrackifyTheme.PRIMARY);
        JButton deleteButton = makeButton("Delete", TrackifyTheme.RED);
        JButton undoButton = makeButton("Undo", TrackifyTheme.MUTED);
        JButton clearButton = makeButton("Clear", TrackifyTheme.MUTED);

        addButton.addActionListener(e -> addTransaction());
        editButton.addActionListener(e -> updateTransaction());
        deleteButton.addActionListener(e -> deleteTransaction());
        undoButton.addActionListener(e -> undoTransaction());
        clearButton.addActionListener(e -> confirmClear());

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

        // ===== Summary =====
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setOpaque(false);
        JPanel summaryPanel = TrackifyTheme.sectionPanel("Summary", new GridBagLayout());

        addSummaryRow(summaryPanel, 0, "Total Income:", incomeValue);
        addSummaryRow(summaryPanel, 1, "Total Expenses:", expenseValue);
        addSummaryRow(summaryPanel, 2, "Remaining Balance:", balanceValue);
        addSummaryRow(summaryPanel, 3, "Top Categories:", topValue);
        incomeValue.setForeground(TrackifyTheme.GREEN);
        expenseValue.setForeground(TrackifyTheme.RED);
        balanceValue.setForeground(TrackifyTheme.INK);
        topValue.setForeground(TrackifyTheme.PRIMARY);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = 4;
        filler.weighty = 1.0;
        summaryPanel.add(new JPanel(), filler);

        // ===== Search =====
        JPanel searchPanel = TrackifyTheme.sectionPanel("Search", new BorderLayout(8, 8));
        searchPanel.add(new JLabel("Keyword: "), BorderLayout.WEST);
        TrackifyTheme.styleInput(searchField);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = makeButton("Search", TrackifyTheme.PRIMARY);
        searchButton.addActionListener(e -> searchTransactions());
        searchPanel.add(searchButton, BorderLayout.EAST);

        JButton resetButton = makeButton("Show All", TrackifyTheme.MUTED);
        resetButton.setPreferredSize(new Dimension(106, 36));
        resetButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchActions.setOpaque(false);
        searchActions.add(resetButton);
        searchPanel.add(searchActions, BorderLayout.SOUTH);

        // ===== Sort =====
        JPanel sortPanel = TrackifyTheme.sectionPanel("Sort", new FlowLayout(FlowLayout.LEFT, 8, 0));
        JComboBox<String> sortField = new JComboBox<>(new String[] { "Date", "Amount" });
        JComboBox<String> sortOrder = new JComboBox<>(new String[] { "Ascending", "Descending" });
        TrackifyTheme.styleInput(sortField);
        TrackifyTheme.styleInput(sortOrder);
        JButton sortButton = makeButton("Apply", TrackifyTheme.PRIMARY);
        sortButton.addActionListener(e -> {
            String field = (String) sortField.getSelectedItem();
            boolean ascending = "Ascending".equals(sortOrder.getSelectedItem());
            fillTable(manager.getSortedTransactions(field, ascending));
        });
        sortPanel.add(sortField);
        sortPanel.add(sortOrder);
        sortPanel.add(sortButton);

        JPanel filters = new JPanel(new GridLayout(2, 1, 0, 8));
        filters.setOpaque(false);
        filters.add(searchPanel);
        filters.add(sortPanel);

        rightPanel.add(filters, BorderLayout.NORTH);
        rightPanel.add(summaryPanel, BorderLayout.CENTER);

        content.add(formPanel, BorderLayout.WEST);
        content.add(rightPanel, BorderLayout.CENTER);

        // ===== Table =====
        tableModel = new DefaultTableModel(new Object[] { "ID", "Name", "Type", "Amount", "Category", "Date" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(TrackifyTheme.BORDER);
        table.setSelectionBackground(TrackifyTheme.SELECTION);
        table.setSelectionForeground(TrackifyTheme.INK);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(TrackifyTheme.SURFACE);
        table.getTableHeader().setForeground(TrackifyTheme.MUTED);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (ignoreSelection)
                return;
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
                fillFormFromSelected(selectedId);
            }
        });

        JPanel tablePanel = TrackifyTheme.sectionPanel("Transaction Records", new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(12, 12));
        upper.setOpaque(false);
        upper.add(header, BorderLayout.NORTH);
        upper.add(content, BorderLayout.CENTER);
        add(upper, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        refreshTable();
    }

    // ===== Form helpers =====

    // Adds a label + component and styles the component like a text field
    private void addFormField(JPanel panel, GridBagConstraints gbc, int x, int y, String labelText,
            JComponent component) {
        TrackifyTheme.styleInput(component);
        component.setPreferredSize(new Dimension(0, 30));
        addFormRow(panel, gbc, x, y, labelText, component);
    }

    // Adds a label + component as-is (used for the date picker and category panel)
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int x, int y, String labelText,
            JComponent component) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(labelText + ":"), gbc);

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

        JLabel label = TrackifyTheme.label(labelText, 14, Font.PLAIN, TrackifyTheme.INK);
        c.gridx = 0;
        c.weightx = 0;
        panel.add(label, c);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(valueLabel, c);
    }

    private JButton makeButton(String text, Color background) {
        JButton button = TrackifyTheme.button(text, background, Color.WHITE);
        button.setPreferredSize(new Dimension(82, 34));
        return button;
    }

    // ===== Category helpers =====

    private String[] currentCategories() {
        return "INCOME".equals(typeCombo.getSelectedItem()) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    }

    // Reloads the category list when the type (INCOME / EXPENSE) changes
    private void loadCategories() {
        categoryCombo.setModel(new DefaultComboBoxModel<>(currentCategories()));
        updateOtherVisibility();
    }

    // Shows the text box only when "Others" is selected
    private void updateOtherVisibility() {
        boolean others = OTHERS.equals(categoryCombo.getSelectedItem());
        otherCategoryField.setVisible(others);
        if (!others)
            otherCategoryField.setText("");
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    // The category to save: the dropdown value, or the typed text if "Others"
    private String getCategory() {
        String selected = (String) categoryCombo.getSelectedItem();
        if (!OTHERS.equals(selected))
            return selected;

        String custom = otherCategoryField.getText().trim();
        if (custom.isEmpty())
            throw new IllegalArgumentException("Please enter your category.");
        for (String c : currentCategories()) {
            if (!OTHERS.equals(c) && c.equalsIgnoreCase(custom))
                return c;
        }
        return custom;
    }

    // Used when a row is selected: pick the category, or "Others" + its text
    private void setCategory(String value) {
        for (String c : currentCategories()) {
            if (!OTHERS.equals(c) && c.equals(value)) {
                categoryCombo.setSelectedItem(c);
                return;
            }
        }
        categoryCombo.setSelectedItem(OTHERS);
        otherCategoryField.setText(value);
    }

    // The picked date as yyyy-MM-dd (what TransactionManager expects)
    private String getDateText() {
        LocalDate date = datePicker.getDate();
        if (date == null)
            throw new IllegalArgumentException("Please select a valid date (mm/dd/yyyy).");
        return date.toString();
    }

    // ===== Actions =====

    private void addTransaction() {
        try {
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = getDateText();
            String category = getCategory();

            manager.addTransaction(name, type, amount, date, category);
            clearForm();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
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
            String date = getDateText();
            String category = getCategory();

            manager.editTransaction(selectedId, name, type, amount, date, category);
            clearForm();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Update Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
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
        }
    }

    private void undoTransaction() {
        String message = manager.describeLastAction();
        if (message == null) {
            JOptionPane.showMessageDialog(this, "No recent action to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // show all rows, then highlight the row that will be affected
        refreshTable();
        table.setSelectionBackground(UNDO_HIGHLIGHT);
        highlightRow(manager.getLastActionTransactionId());

        boolean confirmed = confirmAtTop(message, "Confirm Undo");

        table.setSelectionBackground(TrackifyTheme.SELECTION);
        if (confirmed) {
            manager.undoLastAction();
            clearForm();
        } else {
            ignoreSelection = true;
            table.clearSelection();
            ignoreSelection = false;
        }
    }

    private void confirmClear() {
        int confirm = JOptionPane.showConfirmDialog(this, "Clear all the fields?", "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION)
            clearForm();
    }

    // Selects a row by ID without filling the form
    private void highlightRow(int id) {
        if (id < 0)
            return;
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == id) {
                ignoreSelection = true;
                table.setRowSelectionInterval(row, row);
                ignoreSelection = false;
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return;
            }
        }
    }

    // Yes/No popup placed near the top so it does not cover the table rows
    private boolean confirmAtTop(String message, String title) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(this, title);
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null)
            dialog.setLocation(window.getX() + (window.getWidth() - dialog.getWidth()) / 2, window.getY() + 60);
        dialog.setVisible(true);
        dialog.dispose();
        return Integer.valueOf(JOptionPane.YES_OPTION).equals(pane.getValue());
    }

    private void searchTransactions() {
        List<Transaction> matches = manager.searchTransactions(searchField.getText());
        fillTable(matches);
        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching transactions.", "Search",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void fillTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[] { t.getId(), t.getDescription(), t.getType(), t.getAmount(),
                    t.getCategory(), t.getDate().format(TransactionManager.DATE_FORMAT) });
        }
    }

    private void refreshTable() {
        fillTable(manager.getTransactions());
        refreshSummary();
    }

    // Called by MainFrame every time this screen is shown
    public void refresh() {
        clearForm();
    }

    private void backToDashboard() {
        frame.showDashboard();
    }

    private void refreshSummary() {
        incomeValue.setText("PHP " + String.format("%,.2f", manager.getTotalIncome()));
        expenseValue.setText("PHP " + String.format("%,.2f", manager.getTotalExpenses()));
        balanceValue.setText("PHP " + String.format("%,.2f", manager.getRemainingBalance()));
        List<String> topCategories = manager.getTopCategories();
        topValue.setText(topCategories.isEmpty() ? "N/A" : String.join(", ", topCategories));
    }

    private void fillFormFromSelected(int id) {
        for (Transaction t : manager.getTransactions()) {
            if (t.getId() == id) {
                nameField.setText(t.getDescription());
                typeCombo.setSelectedItem(t.getType());
                amountField.setText(String.valueOf(t.getAmount()));
                datePicker.setDate(t.getDate());
                setCategory(t.getCategory());
                return;
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        typeCombo.setSelectedIndex(0);
        loadCategories();
        amountField.setText("");
        datePicker.setDate(LocalDate.now());
        searchField.setText("");
        table.clearSelection();
        refreshTable();
    }
}