package com.trackify;

import com.trackify.dao.DBConnection;
import com.trackify.dao.UserDAO;
import com.trackify.model.User;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Database connection: OK");
        } catch (Exception e) {
            System.out.println("Database connection: FAILED");
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();
        User user = null;

        while (user == null) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            user = userDAO.validateLogin(username, password);

            if (user == null) {
                System.out.println("Login failed. Try again.\n");
            }
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.getUsername() + " (User ID: " + user.getId() + ")");

        scanner.close();
    }
}