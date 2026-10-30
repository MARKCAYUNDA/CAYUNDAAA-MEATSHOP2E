
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner sc;

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    // ---------- MAIN ADMIN MENU ----------

    public void display(User admin) {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("=====================================");
            System.out.println("       CAYUNDA'S MEAT SHOP");
            System.out.println("       Administrator Menu");
            System.out.println("=====================================");
            System.out.println("[1] User Management");
            System.out.println("[2] Product Management");
            System.out.println("[3] Supplier Management");
            System.out.println("[4] Sales Management");
            System.out.println("[5] Reports");
            System.out.println("[6] Activity Logs");
            System.out.println("[0] Logout");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1:
                    userManagementMenu(admin);
                    break;

                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    comingSoon();
                    break;

                case 0:
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Enter 0 to 6.");
            }
        }
    }

    // ---------- USER MANAGEMENT MENU ----------

    private void userManagementMenu(User admin) {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("=====================================");
            System.out.println("          USER MANAGEMENT");
            System.out.println("=====================================");
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Update User Role and Status");
            System.out.println("[0] Back");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1:
                    System.out.println("\n--- ALL SYSTEM USERS ---");
                    printUserTable(userDao.getAllUsers());
                    break;

                case 2:
                    System.out.println("\n--- PENDING REGISTRATIONS ---");
                    printUserTable(userDao.getPendingUsers());
                    break;

                case 3:
                    approveUserAccount();
                    break;

                case 4:
                    updateUserAccount(admin);
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Enter 0 to 4.");
            }
        }
    }

    // ---------- APPROVE USER ACCOUNT ----------

    private void approveUserAccount() {

        List<User> pending = userDao.getPendingUsers();

        System.out.println("\n--- APPROVE USER ACCOUNT ---");

        if (pending.isEmpty()) {
            System.out.println("-> No pending accounts to approve.");
            return;
        }

        printUserTable(pending);

        while (true) {

            int targetId = readIntInput(
                    "Enter Target User ID (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("-> Approval cancelled.");
                return;
            }

            User target = findById(pending, targetId);

            if (target == null) {
                System.out.println("Invalid! User ID not found.");
                continue;
            }

            if (userDao.approveUser(targetId)) {

                System.out.println("-> SUCCESS: " +
                        target.full_name() +
                        " is now APPROVED.");

            } else {

                System.out.println("-> Approval failed. Please try again.");
            }

            return;
        }
    }

    // ==========================================
    // DELIVERABLE 7: UPDATE USER ROLE AND STATUS
    // ==========================================

    private void updateUserAccount(User admin) {

        System.out.println("\n=====================================");
        System.out.println("       UPDATE USER ROLE AND STATUS");
        System.out.println("=====================================");

        // Display all users
        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printUserTable(users);

        // Ask for target user ID
        while (true) {

            int targetUserId = readIntInput(
                    "Enter Target User ID (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            // SELF-EDIT RESTRICTION
            if (targetUserId == admin.user_id()) {

                System.out.println(
                        "WARNING: Modifying the currently active admin account is restricted."
                );

                return;
            }

            // Find target user
            User target = findById(users, targetUserId);

            if (target == null) {
                System.out.println("Invalid User ID. Please try again.");
                continue;
            }

            System.out.println("\nSelected User:");
            System.out.println("Name: " + target.full_name());
            System.out.println("Username: " + target.username());
            System.out.println("Current Role: " + target.role());
            System.out.println("Current Status: " + target.status());

            // Read new role
            String newRole = readValidRole();

            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            // Read new status
            String newStatus = readValidStatus();

            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            // Confirmation
            System.out.println("\n--- UPDATE CONFIRMATION ---");
            System.out.println("User: " + target.full_name());
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);

            // Save to database
            boolean updated = userDao.updateUserRoleAndStatus(
                    targetUserId, newRole, newStatus
            );

            if (updated) {

                System.out.println("\n=====================================");
                System.out.println("       USER UPDATE SUCCESSFUL!");
                System.out.println("=====================================");
                System.out.println("User ID: " + targetUserId);
                System.out.println("New Role: " + newRole);
                System.out.println("New Status: " + newStatus);
                System.out.println("Database successfully updated.");

            } else {

                System.out.println(
                        "Update failed. Please check your database connection."
                );
            }

            return;
        }
    }

    // ---------- INPUT VALIDATION: INTEGER ----------

    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = sc.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input! Please enter numbers only."
                );
            }
        }
    }

    // ---------- INPUT VALIDATION: ROLE ----------

    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");
            System.out.println("[1] INVENTORY STAFF");
            System.out.println("[2] CASHIER");
            System.out.println("[3] ADMIN");
            System.out.println("[0] Cancel");

            System.out.print("Enter New Role: ");

            String role = sc.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }

            if (role.equals("INVENTORY STAFF") ||
                    role.equals("CASHIER") ||
                    role.equals("ADMIN")) {

                return role;
            }

            System.out.println(
                    "Invalid role! Enter INVENTORY STAFF, CASHIER, or ADMIN."
            );
        }
    }

    // ---------- INPUT VALIDATION: STATUS ----------

    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Status:");
            System.out.println("[1] APPROVED");
            System.out.println("[2] PENDING");
            System.out.println("[0] Cancel");

            System.out.print("Enter New Status: ");

            String status = sc.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }

            if (status.equals("APPROVED") ||
                    status.equals("PENDING")) {

                return status;
            }

            System.out.println(
                    "Invalid status! Enter APPROVED or PENDING."
            );
        }
    }

    // ---------- FIND USER BY ID ----------

    private User findById(List<User> users, int id) {

        for (User u : users) {

            if (u.user_id() == id) {
                return u;
            }
        }

        return null;
    }

    // ---------- USER TABLE ----------

    private void printUserTable(List<User> users) {

        String format =
                "| %-5s | %-15s | %-25s | %-15s | %-10s |%n";

        String border =
                "+-------+-----------------+---------------------------+-----------------+------------+";

        System.out.println(border);

        System.out.printf(
                format, "ID", "Full Name", "Username", "Role", "Status"
        );

        System.out.println(border);

        if (users.isEmpty()) {

            System.out.println("| No records found.");

        } else {

            for (User u : users) {

                System.out.printf(
                        format,
                        u.user_id(),
                        truncate(u.full_name(), 15),
                        truncate(u.username(), 25),
                        truncate(u.role(), 15),
                        u.status()
                );
            }
        }

        System.out.println(border);
        System.out.println("Total: " + users.size());
    }

    // ---------- TEXT TRUNCATION ----------

    private String truncate(String text, int max) {

        if (text == null) {
            return "";
        }

        return text.length() <= max
                ? text
                : text.substring(0, max - 3) + "...";
    }

    // ---------- COMING SOON ----------

    private void comingSoon() {

        System.out.println(">> Coming soon.");
    }
}
