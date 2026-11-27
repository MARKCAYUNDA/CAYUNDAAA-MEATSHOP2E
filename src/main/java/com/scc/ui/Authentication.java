package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.sc = sc;
    }

    public void start() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("      CAYUNDA'S MEAT SHOP SYSTEM");
            System.out.println("==========================================");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[0] Exit");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> handleRegister();
                case "2" -> handleLogin();
                case "0" -> {
                    System.out.println("Exiting system. Goodbye!");
                    return;
                }
                default -> System.out.println("-> Invalid choice. Please try again.");
            }
        }
    }

    private void handleRegister() {
        System.out.println("\n--- ACCOUNT REGISTRATION ---");
        System.out.print("Full Name: ");
        String fullName = sc.nextLine().trim();
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        System.out.println("Select Role: [1] ADMIN  [2] CASHIER  [3] INVENTORY_STAFF");
        System.out.print("Role choice: ");
        String roleChoice = sc.nextLine().trim();

        String role = switch (roleChoice) {
            case "1" -> "ADMIN";
            case "2" -> "CASHIER";
            case "3" -> "INVENTORY_STAFF";
            default -> null;
        };

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
            System.out.println("-> ERROR: All fields are required and role must be valid.");
            return;
        }

        User newUser = new User(0, fullName, username, password, role, "PENDING");
        if (userDao.registerUser(newUser)) {
            System.out.println("-> SUCCESS: Registered! Status is PENDING admin approval.");
            logDao.logAction(newUser.getUserId(), "USER_REGISTERED",
                    "User ID " + newUser.getUserId() + " (" + username + ") registered as " + role);
        } else {
            System.out.println("-> FAILED: Username already exists.");
        }
    }

    private void handleLogin() {
        System.out.println("\n--- SYSTEM LOGIN ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("-> ERROR: Invalid username or password.");
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {
            System.out.println("-> ACCESS DENIED: Account status is " + user.getStatus() + ".");
            return;
        }

        System.out.println("-> Welcome, " + user.getFullName() + " | ROLE: " + user.getRole());
        logDao.logAction(user.getUserId(), "USER_LOGIN",
                "User " + user.getUserId() + " (" + user.getUsername() + ") logged in");

        switch (user.getRole().toUpperCase()) {
            case "ADMIN" -> new AdminMenu(user, userDao, logDao, sc).display();
            case "CASHIER" -> new CashierMenu(user, logDao, sc).display();
            case "INVENTORY_STAFF" -> new InventoryStaffMenu(user, logDao, sc).display();
            default -> System.out.println("-> Unknown role: " + user.getRole());
        }
    }
}