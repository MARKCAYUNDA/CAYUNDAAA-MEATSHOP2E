package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("         Cayunda's Meat Shop");
            System.out.println("========================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid Choice, Try Again.");
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n    ===== LOG IN =====");

        String username = promptNonEmpty("Enter Username: ", "Username");
        String password = promptNonEmpty("Enter Password: ", "Password");

        User user = userDao.login(username.toLowerCase(), password);

        if (user == null) {
            System.out.println(" -> Invalid username or password.");
            return;
        }
        if ("PENDING".equalsIgnoreCase(user.status())) {
            System.out.println(" -> Account is pending. Waiting for approval.");
            return;
        }
        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println(" -> Account is " + user.status() + ". Contact the administrator.");
            return;
        }

        System.out.println("\n -> Welcome " + user.full_name() + " | ROLE: " + user.role());
        routeByRole(user);
    }

    private void routeByRole(User user) {
        switch (user.role()) {
            case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
            case "CASHIER" -> new CashierMenu(userDao, sc).display(user);
            case "INVENTORY STAFF" -> new InventoryStaffMenu(userDao, sc).display(user);
            default -> System.out.println(" -> Unknown role: " + user.role());
        }
    }

    private void handleRegister() {
        System.out.println("\n    ===== REGISTER =====");

        String fullName = promptNonEmpty("Enter your Fullname: ", "Fullname");

        String username;
        while (true) {
            System.out.print("Enter username (with @gmail.com): ");
            username = sc.nextLine().trim().toLowerCase();

            if (!username.endsWith("@gmail.com")
                    || username.length() <= "@gmail.com".length()
                    || username.contains(" ")) {
                System.out.println("Invalid! Enter a valid Gmail without spaces (example: lito@gmail.com).");
            } else if (userDao.usernameExists(username)) {
                System.out.println("Invalid! Username already taken.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = sc.nextLine().trim();

            if (password.length() < 5) {
                System.out.println("Invalid! Password must be at least 5 characters.");
                continue;
            }

            boolean confirmed = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.print("Confirm Password: ");
                if (sc.nextLine().trim().equals(password)) {
                    confirmed = true;
                    break;
                }
                System.out.println("\nPasswords do not match! Attempt " + attempt + " of 3.");
            }

            if (confirmed) break;
            System.out.println("Too many wrong attempts. Register again.");
            return;
        }

        String role = selectRole();

        if (userDao.registerUser(fullName, username, password, role)) {
            System.out.println("-> Successful registration! Waiting for approval.");
        } else {
            System.out.println("-> Registration failed. Please try again.");
        }
    }

    private String selectRole() {
        while (true) {
            System.out.println("\n-- Select Role --");
            System.out.println("[1] ADMIN");
            System.out.println("[2] CASHIER");
            System.out.println("[3] INVENTORY STAFF");
            System.out.print("Enter Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": return "ADMIN";
                case "2": return "CASHIER";
                case "3": return "INVENTORY STAFF";
                default: System.out.println("Invalid! Please select 1, 2, or 3 only.");
            }
        }
    }

    private String promptNonEmpty(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Invalid! " + fieldName + " cannot be empty.");
        }
    }
}