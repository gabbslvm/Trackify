package com.trackify.manager;

import com.trackify.dao.BudgetDAO;
import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;
import com.trackify.util.SortUtil;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;

public class TransactionManager {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final double MAX_AMOUNT = 9_999_999_999.99;

    private final TransactionDAO dao = new TransactionDAO();
    private final BudgetDAO budgetDao = new BudgetDAO();
    private List<Transaction> transactions = new ArrayList<>();
    private final Stack<UndoAction> undoStack = new Stack<>();
    private int userId;
    private double monthlyBudget;

    public void setUserId(int userId) {
        List<Transaction> loaded = dao.getTransactionsByUser(userId);
        double budget = budgetDao.getMonthlyBudget(userId);

        this.userId = userId;
        this.transactions = loaded;
        this.monthlyBudget = budget;
        undoStack.clear();
    }

    private void reload() {
        transactions = dao.getTransactionsByUser(userId);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    private Transaction find(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    private Transaction build(int id, String name, String type, double amount, String date, String category) {
        if (name == null || name.isBlank() || category == null || category.isBlank())
            throw new IllegalArgumentException("Name and category are required.");
        if (name.length() > 255)
            throw new IllegalArgumentException("Name must be 255 characters or less.");
        if (category.length() > 50)
            throw new IllegalArgumentException("Category must be 50 characters or less.");
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type))
            throw new IllegalArgumentException("Type must be INCOME or EXPENSE.");
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (amount > MAX_AMOUNT)
            throw new IllegalArgumentException("Amount is too large.");
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format.");
        }
        return new Transaction(id, userId, type, category, amount, d, name);
    }

    public void addTransaction(String name, String type, double amount, String date, String category) {
        Transaction t = build(0, name, type, amount, date, category);
        if (!dao.addTransaction(t))
            throw new IllegalArgumentException("Failed to save transaction.");
        undoStack.push(new UndoAction.Add(t));
        reload();
    }

    public void editTransaction(int id, String name, String type, double amount, String date, String category) {
        Transaction old = find(id);
        if (old == null)
            throw new IllegalArgumentException("Transaction not found.");
        Transaction updated = build(id, name, type, amount, date, category);
        if (!dao.updateTransaction(updated))
            throw new IllegalArgumentException("Failed to update transaction.");
        undoStack.push(new UndoAction.Edit(old));
        reload();
    }

    public void deleteTransaction(int id) {
        Transaction old = find(id);
        if (old == null)
            throw new IllegalArgumentException("Transaction not found.");
        if (!dao.deleteTransaction(id, userId))
            throw new IllegalArgumentException("Failed to delete transaction.");
        undoStack.push(new UndoAction.Delete(old));
        reload();
    }

    public String describeLastAction() {
        if (undoStack.isEmpty())
            return null;
        return undoStack.peek().describe();
    }

    public int getLastActionTransactionId() {
        if (undoStack.isEmpty())
            return -1;
        return undoStack.peek().getTransactionId();
    }

    public boolean undoLastAction() {
        if (undoStack.isEmpty())
            return false;
        UndoAction a = undoStack.peek();
        a.undo(dao, userId);
        undoStack.pop();
        reload();
        return true;
    }

    public List<Transaction> searchTransactions(String keyword) {
        return filterTransactions(keyword, null, null, null);
    }

    public List<Transaction> filterTransactions(String keyword, String type, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to))
            throw new IllegalArgumentException("The From date cannot be after the To date.");

        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        boolean anyType = type == null || "ALL".equalsIgnoreCase(type);

        List<Transaction> result = new ArrayList<>();
        for (Transaction t : filterByDate(from, to)) {
            if (!anyType && !t.getType().equalsIgnoreCase(type))
                continue;
            if (!k.isEmpty() && !matchesKeyword(t, k))
                continue;
            result.add(t);
        }
        return result;
    }

    private List<Transaction> filterByDate(LocalDate from, LocalDate to) {
        if (from == null && to == null)
            return new ArrayList<>(transactions);

        TreeMap<LocalDate, List<Transaction>> byDate = new TreeMap<>();
        for (Transaction t : transactions) {
            byDate.computeIfAbsent(t.getDate(), d -> new ArrayList<>()).add(t);
        }

        NavigableMap<LocalDate, List<Transaction>> range;
        if (from != null && to != null)
            range = byDate.subMap(from, true, to, true);
        else if (from != null)
            range = byDate.tailMap(from, true);
        else
            range = byDate.headMap(to, true);

        List<Transaction> result = new ArrayList<>();
        for (List<Transaction> group : range.values()) {
            result.addAll(group);
        }
        return result;
    }

    private boolean matchesKeyword(Transaction t, String k) {
        return lower(t.getCategory()).contains(k)
                || lower(t.getDescription()).contains(k)
                || lower(t.getType()).contains(k);
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public List<Transaction> sortTransactions(List<Transaction> list, String field, boolean ascending) {
        List<Transaction> copy = new ArrayList<>(list);
        if ("amount".equalsIgnoreCase(field)) {
            SortUtil.mergeSort(copy, SortUtil.byAmount(ascending));
        } else {
            SortUtil.mergeSort(copy, SortUtil.byDate(ascending));
        }
        return copy;
    }

    public List<Transaction> getSortedTransactions(String field, boolean ascending) {
        return sortTransactions(transactions, field, ascending);
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

    private static String displayCategory(String c) {
        if (c == null || c.isEmpty())
            return "Uncategorized";
        return c.equals(c.toLowerCase()) ? Character.toUpperCase(c.charAt(0)) + c.substring(1) : c;
    }

    public HashMap<String, Double> getCategoryTotals() {
        HashMap<String, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                totals.merge(displayCategory(t.getCategory()), t.getAmount(), Double::sum);
        }
        return totals;
    }

    public TreeMap<YearMonth, Double> getMonthlyExpenses() {
        TreeMap<YearMonth, Double> monthly = new TreeMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                monthly.merge(YearMonth.from(t.getDate()), t.getAmount(), Double::sum);
        }
        return monthly;
    }

    public List<Map.Entry<String, Double>> getTopCategories(int n) {
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue()));
        pq.addAll(getCategoryTotals().entrySet());

        List<Map.Entry<String, Double>> top = new ArrayList<>();
        for (int i = 0; i < n && !pq.isEmpty(); i++) {
            top.add(pq.poll());
        }
        return top;
    }

    public List<String> getTopCategories() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Double> e : getTopCategories(3)) {
            names.add(e.getKey());
        }
        return names;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new IllegalArgumentException("Budget must be greater than 0.");
        if (amount > MAX_AMOUNT)
            throw new IllegalArgumentException("Budget is too large.");
        if (!budgetDao.saveMonthlyBudget(userId, amount))
            throw new IllegalArgumentException("Failed to save budget.");
        monthlyBudget = amount;
    }

    public double getCurrentMonthExpenses() {
        return getMonthlyExpenses().getOrDefault(YearMonth.now(), 0.0);
    }

    public int getBudgetUsedPercent() {
        if (monthlyBudget <= 0)
            return 0;
        return (int) Math.round(getCurrentMonthExpenses() / monthlyBudget * 100);
    }
}