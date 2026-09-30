package com.trackify;

import com.trackify.dao.DBConnection;
import com.trackify.gui.LoginFrame;

import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Database connection: OK");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database. Start MySQL in XAMPP.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}