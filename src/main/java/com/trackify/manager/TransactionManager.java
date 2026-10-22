package com.trackify.manager;

import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;
import com.trackify.util.SortUtil;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeMap;

public class TransactionManager {
    private final TransactionDAO dao = new TransactionDAO();
    private List<Transaction> transactions = new ArrayList<>();
    private final Stack<UndoAction> undoStack = new Stack<>();
    private int userId;


    public void setUserId(int userId) {
        this.userId = userId;
        undoStack.clear();
        reload();
    }

    public void clear() {
        userId = 0;
        transactions = new ArrayList<>();
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
        if (name.isEmpty() || category.isEmpty())
            throw new IllegalArgumentException("Name and category are required.");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
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
            return;
        if (dao.deleteTransaction(id, userId)) {
            undoStack.push(new UndoAction.Delete(old));
            reload();
        }
    }

    public boolean undoLastAction() {
        if (undoStack.isEmpty())
            return false;
        UndoAction a = undoStack.pop();
        a.undo(dao, userId); 
        reload();
        return true;
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

    public List<Transaction> sortTransactions(String field, boolean ascending) {
        List<Transaction> copy = new ArrayList<>(transactions);
        if ("amount".equalsIgnoreCase(field)) {
            SortUtil.mergeSort(copy, SortUtil.byAmount(ascending));
        } else {
            SortUtil.mergeSort(copy, SortUtil.byDate(ascending));
        }
        return copy;
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

    public HashMap<String, Double> getCategoryTotals() {
        HashMap<String, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                totals.merge(t.getCategory(), t.getAmount(), Double::sum);
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
}