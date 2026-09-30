package com.trackify.dao;

import com.trackify.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, type, category, amount, transaction_date, description)"
                + "VALUES(?, ?, ?, ?, ? , ?)";

        try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategory());
            ps.setDouble(4, t.getAmount());
            ps.setDate(5, Date.valueOf(t.getDate()));
            ps.setString(6, t.getDescription());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        t.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, user_id, type, category, amount, date, description "
                + "FROM transactions WHERE user_id = ? ORDER BY date DESC, id DESC";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET type = ?, category = ?, amount = ?, date = ?, description = ? "
                + "WHERE id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getType());
            ps.setString(2, t.getCategory());
            ps.setDouble(3, t.getAmount());
            ps.setDate(4, Date.valueOf(t.getDate()));
            ps.setString(5, t.getDescription());
            ps.setInt(6, t.getId());
            ps.setInt(7, t.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTransaction(int id, int userId) {
    String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";

    try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    return false;
}
}
