package com.trackify.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetDAO {
    public double getMonthlyBudget(int userId) {
        String sql = "SELECT monthly_limit FROM budgets WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("monthly_limit");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean saveMonthlyBudget(int userId, double limit) {
        String sql = "INSERT INTO budgets (user_id, monthly_limit) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE monthly_limit = VALUES(monthly_limit)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDouble(2, limit);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the budget.", e);
        }
    }
}