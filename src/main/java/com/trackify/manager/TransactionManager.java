package com.trackify.manager;

import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TransactionManager {
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
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format.");
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
            case "DELETE" -> dao.addTransaction(a.data());
        }
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

    public String getTopSpendingCategory() {
        Map<String, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE"))
                totals.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue()));
        pq.addAll(totals.entrySet());
        return pq.isEmpty() ? "N/A" : pq.peek().getKey();
    }
}