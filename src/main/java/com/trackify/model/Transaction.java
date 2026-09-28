package com.trackify.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private int userId;
    private String type; // "INCOME" or "EXPENSE"
    private String category;
    private double amount;
    private LocalDate date;
    private String description;

    public Transaction(int id, int userId, String type, String category,
            double amount, LocalDate date, String description) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type=" + type + ", category=" + category
                + ", amount=" + amount + ", date=" + date + "}";
    }
}