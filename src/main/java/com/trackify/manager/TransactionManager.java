package com.trackify.manager;

import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;
import com.trackify.util.SortUtil;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TransactionManager {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final TransactionDAO dao = new TransactionDAO();
    private List<Transaction> transactions = new ArrayList<>();
    private final Stack<Action> undoStack = new Stack<>();
    private int userId;

    private record Action(String kind, Transaction data) {
    }

    public void setUserId(int userId) {
        this.userId = userId;
        undoStack.clear();
        reload();
    }

    private void reload() {
        transactions = dao.getTransactionsByUser(userId);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private Transaction find(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    private Transaction build(int id, String name, String type, double amount, String date, String category) {
        if (name.isEmpty() || category.isEmpty())
            throw new IllegalArgumentException("Name and category are required.");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Please enter a valid date.");
        }
        return new Transaction(id, userId, type, category, amount, d, name);
    }

    public void addTransaction(String name, String type, double amount, String date, String category) {
        Transaction t = build(0, name, type, amount, date, category);
        if (!dao.addTransaction(t))
            throw new IllegalArgumentException("Failed to save transaction.");
        undoStack.push(new Action("ADD", t));
        reload();
    }

    public void editTransaction(int id, String name, String type, double amount, String date, String category) {
        Transaction old = find(id);
        if (old == null)
            throw new IllegalArgumentException("Transaction not found.");
        Transaction updated = build(id, name, type, amount, date, category);
        if (!dao.updateTransaction(updated))
            throw new IllegalArgumentException("Failed to update transaction.");
        undoStack.push(new Action("EDIT", old));
        reload();
    }

    public void deleteTransaction(int id) {
        Transaction old = find(id);
        if (old == null)
            return;
        if (dao.deleteTransaction(id, userId)) {
            undoStack.push(new Action("DELETE", old));
            reload();
        }
    }

    public boolean undoLastAction() {
        if (undoStack.isEmpty())
            return false;
        Action a = undoStack.pop();
        switch (a.kind()) {
            case "ADD" -> dao.deleteTransaction(a.data().getId(), userId);
            case "EDIT" -> dao.updateTransaction(a.data());
            case "DELETE" -> dao.restoreTransaction(a.data());
        }
        reload();
        return true;
    }

    // ===== Undo preview (used by the GUI before asking the user to confirm) =====

    private String describe(Transaction t) {
        return t.getDescription() + " | " + t.getType() + " | PHP " + String.format("%,.2f", t.getAmount())
                + " | " + t.getCategory() + " | " + t.getDate().format(DATE_FORMAT);
    }

    // Text shown in the confirm popup, or null if there is nothing to undo
    public String describeLastAction() {
        if (undoStack.isEmpty())
            return null;
        Action a = undoStack.peek();
        Transaction saved = a.data();

        if (a.kind().equals("ADD"))
            return "Undo the last ADD?\n\n" + describe(saved) + "\n\nThis transaction will be removed.";

        if (a.kind().equals("EDIT")) {
            Transaction current = find(saved.getId());
            return "Undo the last EDIT?\n\nCurrent:\n" + (current == null ? "(not found)" : describe(current))
                    + "\n\nIt will go back to:\n" + describe(saved);
        }

        return "Undo the last DELETE?\n\n" + describe(saved) + "\n\nThis transaction will be restored.";
    }

    // ID of the row to highlight in the table, or -1 if it is not in the table (DELETE)
    public int getLastActionTransactionId() {
        if (undoStack.isEmpty())
            return -1;
        Action a = undoStack.peek();
        return a.kind().equals("DELETE") ? -1 : a.data().getId();
    }

    public List<Transaction> searchTransactions(String keyword) {
        List<Transaction> result = new ArrayList<>();
        String k = keyword.trim().toLowerCase();
        for (Transaction t : transactions) {
            if (t.getCategory().toLowerCase().contains(k)
                    || t.getDescription().toLowerCase().contains(k)) {
                result.add(t);
            }
        }
        return result;
    }

    private double total(String type) {
        double sum = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals(type))
                sum += t.getAmount();
        }
        return sum;
    }

    public double getTotalIncome() {
        return total("INCOME");
    }

    public double getTotalExpenses() {
        return total("EXPENSE");
    }

    public double getRemainingBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

        // Demo mode is not used in the final app (database is always required)
    public boolean isDemoMode() {
        return false;
    }

    // Top N expense categories with their totals (used by the Dashboard)
    public List<Map.Entry<String, Double>> getTopCategories(int limit) {
        Map<String, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                totals.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue()));
        pq.addAll(totals.entrySet());

        List<Map.Entry<String, Double>> top = new ArrayList<>();
        for (int i = 0; i < limit && !pq.isEmpty(); i++)
            top.add(pq.poll());
        return top;
    }

    // Top 3 category names (used by the Transactions screen summary)
    public List<String> getTopCategories() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Double> e : getTopCategories(3))
            names.add(e.getKey());
        return names;
    }

    public Map<YearMonth, Double> getMonthlyExpenses() {
        Map<YearMonth, Double> monthly = new TreeMap<>(Comparator.reverseOrder());
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                monthly.merge(YearMonth.from(t.getDate()), t.getAmount(), Double::sum);
        }
        return monthly;
    }

    public List<Transaction> getSortedTransactions(String field, boolean ascending) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        Comparator<Transaction> cmp = field.equals("Amount")
                ? SortUtil.byAmount(ascending)
                : SortUtil.byDate(ascending);
        SortUtil.mergeSort(sorted, cmp);
        return sorted;
    }
}