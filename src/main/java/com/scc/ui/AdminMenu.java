package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    // Session-based catalog para sa Product Management
    private record Product(int id, String name, String category, double pricePerKg, double stockKg) {}
    private final List<Product> productCatalog = new ArrayList<>();

    // Session-based catalog para sa Supplier Management
    private record Supplier(int id, String name, String contactPerson, String contactNumber, String meatSupplyType) {}
    private final List<Supplier> supplierCatalog = new ArrayList<>();

    // Session-based catalog para sa Sales Management
    private record SaleTransaction(String txId, String date, String customerName, double totalAmount) {}
    private final List<SaleTransaction> salesHistory = new ArrayList<>();

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.sc = sc;
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        productCatalog.add(new Product(101, "Pork Belly (Liempo)", "Pork", 340.00, 50.0));
        productCatalog.add(new Product(102, "Pork Chop", "Pork", 310.00, 40.0));
        productCatalog.add(new Product(201, "Beef Brisket", "Beef", 420.00, 30.0));
        productCatalog.add(new Product(301, "Whole Dressed Chicken", "Poultry", 210.00, 60.0));

        supplierCatalog.add(new Supplier(1, "Prime Quality Meats Corp.", "Juan Dela Cruz", "0917-123-4567", "Pork & Beef"));
        supplierCatalog.add(new Supplier(2, "FreshFarm Poultry Supply", "Maria Santos", "0922-987-6543", "Poultry (Chicken)"));

        salesHistory.add(new SaleTransaction("TX-1001", "2026-11-13 08:30", "Walk-in Customer", 1250.00));
        salesHistory.add(new SaleTransaction("TX-1002", "2026-11-13 09:15", "Resto Bar Buyer", 3400.00));
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
                    productManagementMenu(admin);
                    break;

                case 3:
                    supplierManagementMenu(admin);
                    break;

                case 4:
                    salesManagementMenu(admin);
                    break;

                case 5:
                    reportsMenu(admin);
                    break;

                case 6:
                    auditLogsMenu();
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
                    approveUserAccount(admin);
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

    // ---------- PRODUCT MANAGEMENT MENU ----------

    private void productManagementMenu(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println("         PRODUCT MANAGEMENT");
            System.out.println("=====================================");
            System.out.println("[1] View Meat Inventory");
            System.out.println("[2] Add New Meat Product");
            System.out.println("[3] Update Meat Stock / Price");
            System.out.println("[0] Back");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");
            switch (choice) {
                case 1:
                    printProductTable();
                    break;
                case 2:
                    addNewProduct(admin);
                    break;
                case 3:
                    updateProductStockOrPrice(admin);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 0 to 3.");
            }
        }
    }

    private void printProductTable() {
        String format = "| %-5s | %-24s | %-10s | %-12s | %-10s |%n";
        String border = "+-------+--------------------------+------------+--------------+------------+";

        System.out.println("\n--- MEAT PRODUCT INVENTORY ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Meat Cut / Product", "Category", "Price / kg", "Stock (kg)");
        System.out.println(border);

        if (productCatalog.isEmpty()) {
            System.out.println("| No products found in inventory.                                          |");
        } else {
            for (Product p : productCatalog) {
                System.out.printf(format,
                        p.id(),
                        truncate(p.name(), 24),
                        p.category(),
                        String.format("P%.2f", p.pricePerKg()),
                        String.format("%.2f", p.stockKg())
                );
            }
        }
        System.out.println(border);
    }

    private void addNewProduct(User admin) {
        System.out.println("\n--- ADD NEW MEAT PRODUCT ---");
        int id = readIntInput("Enter Product ID (e.g., 402): ");

        for (Product p : productCatalog) {
            if (p.id() == id) {
                System.out.println("Error: Product ID already exists.");
                return;
            }
        }

        System.out.print("Enter Product Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Product name cannot be empty.");
            return;
        }

        System.out.print("Enter Category (Pork, Beef, Poultry): ");
        String category = sc.nextLine().trim();

        double price = readDoubleInput("Enter Price per kg (P): ");
        double stock = readDoubleInput("Enter Initial Stock (kg): ");

        productCatalog.add(new Product(id, name, category, price, stock));
        System.out.println("-> SUCCESS: New product '" + name + "' added successfully.");

        logDao.logAction(
                admin.user_id(),
                "PRODUCT_ADDED",
                "Admin " + admin.user_id() + " added new product ID " + id + " (" + name + ")"
        );
    }

    private void updateProductStockOrPrice(User admin) {
        System.out.println("\n--- UPDATE MEAT STOCK OR PRICE ---");
        printProductTable();

        int id = readIntInput("Enter Product ID to update (0 to cancel): ");
        if (id == 0) return;

        Product target = null;
        for (Product p : productCatalog) {
            if (p.id() == id) {
                target = p;
                break;
            }
        }

        if (target == null) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.println("Selected: " + target.name() + " | Price: P" + target.pricePerKg() + " | Stock: " + target.stockKg() + "kg");
        System.out.println("[1] Update Price per kg");
        System.out.println("[2] Update Stock Quantity");
        int option = readIntInput("Choose option: ");

        if (option == 1) {
            double newPrice = readDoubleInput("Enter new price per kg (P): ");
            int idx = productCatalog.indexOf(target);
            productCatalog.set(idx, new Product(target.id(), target.name(), target.category(), newPrice, target.stockKg()));
            System.out.println("-> SUCCESS: Price updated to P" + newPrice);

            logDao.logAction(
                    admin.user_id(),
                    "PRODUCT_UPDATED",
                    "Admin " + admin.user_id() + " updated price for product ID " + id + " to P" + newPrice
            );
        } else if (option == 2) {
            double newStock = readDoubleInput("Enter new stock quantity (kg): ");
            int idx = productCatalog.indexOf(target);
            productCatalog.set(idx, new Product(target.id(), target.name(), target.category(), target.pricePerKg(), newStock));
            System.out.println("-> SUCCESS: Stock updated to " + newStock + "kg");

            logDao.logAction(
                    admin.user_id(),
                    "STOCK_UPDATED",
                    "Admin " + admin.user_id() + " updated stock for product ID " + id + " to " + newStock + "kg"
            );
        } else {
            System.out.println("Invalid option.");
        }
    }

    // ---------- SUPPLIER MANAGEMENT MENU ----------

    private void supplierManagementMenu(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println("         SUPPLIER MANAGEMENT");
            System.out.println("=====================================");
            System.out.println("[1] View All Suppliers");
            System.out.println("[2] Register New Supplier");
            System.out.println("[0] Back");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");
            switch (choice) {
                case 1:
                    printSupplierTable();
                    break;
                case 2:
                    registerNewSupplier(admin);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 0 to 2.");
            }
        }
    }

    private void printSupplierTable() {
        String format = "| %-5s | %-26s | %-18s | %-15s | %-18s |%n";
        String border = "+-------+----------------------------+--------------------+-----------------+--------------------+";

        System.out.println("\n--- REGISTERED MEAT SUPPLIERS ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Supplier Company Name", "Contact Person", "Contact No.", "Supply Type");
        System.out.println(border);

        if (supplierCatalog.isEmpty()) {
            System.out.println("| No registered suppliers found.                                                                |");
        } else {
            for (Supplier s : supplierCatalog) {
                System.out.printf(format,
                        s.id(),
                        truncate(s.name(), 26),
                        truncate(s.contactPerson(), 18),
                        s.contactNumber(),
                        truncate(s.meatSupplyType(), 18)
                );
            }
        }
        System.out.println(border);
    }

    private void registerNewSupplier(User admin) {
        System.out.println("\n--- REGISTER NEW MEAT SUPPLIER ---");
        int id = readIntInput("Enter Supplier ID (e.g., 3): ");

        for (Supplier s : supplierCatalog) {
            if (s.id() == id) {
                System.out.println("Error: Supplier ID already exists.");
                return;
            }
        }

        System.out.print("Enter Supplier Company Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Supplier name cannot be empty.");
            return;
        }

        System.out.print("Enter Contact Person: ");
        String contactPerson = sc.nextLine().trim();

        System.out.print("Enter Contact Number: ");
        String contactNumber = sc.nextLine().trim();

        System.out.print("Enter Meat Supply Type (e.g., Pork, Beef, Mixed): ");
        String supplyType = sc.nextLine().trim();

        supplierCatalog.add(new Supplier(id, name, contactPerson, contactNumber, supplyType));
        System.out.println("-> SUCCESS: Supplier '" + name + "' registered successfully.");

        logDao.logAction(
                admin.user_id(),
                "SUPPLIER_REGISTERED",
                "Admin " + admin.user_id() + " registered new supplier ID " + id + " (" + name + ")"
        );
    }

    // ---------- SALES MANAGEMENT MENU (Fully Implemented) ----------

    private void salesManagementMenu(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println("          SALES MANAGEMENT");
            System.out.println("=====================================");
            System.out.println("[1] View Daily Sales Transactions");
            System.out.println("[2] Process New Meat Sale Order");
            System.out.println("[0] Back");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");
            switch (choice) {
                case 1:
                    printSalesTable();
                    break;
                case 2:
                    processNewAdminSale(admin);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 0 to 2.");
            }
        }
    }

    private void printSalesTable() {
        String format = "| %-10s | %-18s | %-22s | %-12s |%n";
        String border = "+------------+--------------------+------------------------+--------------+";

        System.out.println("\n--- DAILY SALES TRANSACTIONS ---");
        System.out.println(border);
        System.out.printf(format, "TX ID", "Date & Time", "Customer Name", "Total Amount");
        System.out.println(border);

        double totalSalesSum = 0.0;
        if (salesHistory.isEmpty()) {
            System.out.println("| No sales transactions found.                                         |");
        } else {
            for (SaleTransaction tx : salesHistory) {
                System.out.printf(format,
                        tx.txId(),
                        tx.date(),
                        truncate(tx.customerName(), 22),
                        String.format("P%.2f", tx.totalAmount())
                );
                totalSalesSum += tx.totalAmount();
            }
        }
        System.out.println(border);
        System.out.printf("Total Accumulated Sales: P%.2f%n", totalSalesSum);
    }

    private void processNewAdminSale(User admin) {
        System.out.println("\n--- PROCESS NEW MEAT SALE ORDER ---");
        printProductTable();

        int prodId = readIntInput("Enter Product ID to sell (0 to cancel): ");
        if (prodId == 0) return;

        Product targetProduct = null;
        for (Product p : productCatalog) {
            if (p.id() == prodId) {
                targetProduct = p;
                break;
            }
        }

        if (targetProduct == null) {
            System.out.println("Product ID not found.");
            return;
        }

        double kg = readDoubleInput("Enter weight in kg for " + targetProduct.name() + ": ");
        if (kg <= 0 || kg > targetProduct.stockKg()) {
            System.out.println("Invalid weight or insufficient stock.");
            return;
        }

        System.out.print("Enter Customer Name: ");
        String customer = sc.nextLine().trim();
        if (customer.isEmpty()) customer = "Walk-in Customer";

        double totalAmount = targetProduct.pricePerKg() * kg;
        String txId = "TX-" + (1000 + salesHistory.size() + 1);
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Deduct stock
        int pIndex = productCatalog.indexOf(targetProduct);
        productCatalog.set(pIndex, new Product(targetProduct.id(), targetProduct.name(), targetProduct.category(), targetProduct.pricePerKg(), targetProduct.stockKg() - kg));

        // Record sale
        salesHistory.add(new SaleTransaction(txId, timestamp, customer, totalAmount));

        System.out.println("\n=====================================");
        System.out.println("          SALE SUCCESSFUL!           ");
        System.out.println("=====================================");
        System.out.println("Transaction ID : " + txId);
        System.out.println("Customer       : " + customer);
        System.out.println("Item Sold      : " + targetProduct.name() + " (" + kg + " kg)");
        System.out.println("Total Amount   : P" + String.format("%.2f", totalAmount));
        System.out.println("=====================================");

        logDao.logAction(
                admin.user_id(),
                "SALE_PROCESSED",
                "Admin processed sale " + txId + " for " + customer + " - Total: P" + String.format("%.2f", totalAmount)
        );
    }

    // ---------- REPORTS MENU (Fully Implemented) ----------

    private void reportsMenu(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println("               REPORTS");
            System.out.println("=====================================");
            System.out.println("[1] Inventory Status Report");
            System.out.println("[2] Sales Summary Report");
            System.out.println("[0] Back");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");
            switch (choice) {
                case 1:
                    generateInventoryReport(admin);
                    break;
                case 2:
                    generateSalesReport(admin);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 0 to 2.");
            }
        }
    }

    private void generateInventoryReport(User admin) {
        System.out.println("\n==================================================");
        System.out.println("          CAYUNDA'S MEAT SHOP - INVENTORY REPORT");
        System.out.println("==================================================");
        printProductTable();
        System.out.println("Report Status: Generated successfully.");
        System.out.println("==================================================");

        logDao.logAction(
                admin.user_id(),
                "REPORT_GENERATED",
                "Admin " + admin.user_id() + " generated Inventory Status Report."
        );
    }

    private void generateSalesReport(User admin) {
        System.out.println("\n==================================================");
        System.out.println("          CAYUNDA'S MEAT SHOP - SALES SUMMARY");
        System.out.println("==================================================");
        printSalesTable();
        System.out.println("Report Status: Generated successfully.");
        System.out.println("==================================================");

        logDao.logAction(
                admin.user_id(),
                "REPORT_GENERATED",
                "Admin " + admin.user_id() + " generated Sales Summary Report."
        );
    }

    // ---------- APPROVE USER ACCOUNT ----------

    private void approveUserAccount(User admin) {

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

                logDao.logAction(
                        admin.user_id(),
                        "USER_APPROVED",
                        "Admin " + admin.user_id() + " approved account for User ID " + targetId
                );

            } else {

                System.out.println("-> Approval failed. Please try again.");
            }

            return;
        }
    }

    // ==========================================
    // DELIVERABLE 7 & 8: UPDATE USER ROLE AND STATUS
    // ==========================================

    private void updateUserAccount(User admin) {

        System.out.println("\n=====================================");
        System.out.println("       UPDATE USER ROLE AND STATUS");
        System.out.println("=====================================");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printUserTable(users);

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

            String newRole = readValidRole();

            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();

            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            System.out.println("\n--- UPDATE CONFIRMATION ---");
            System.out.println("User: " + target.full_name());
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);

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

                logDao.logAction(
                        admin.user_id(),
                        "USER_UPDATED",
                        "Admin " + admin.user_id() + " changed role of User " + targetUserId + " to " + newRole
                );

            } else {

                System.out.println(
                        "Update failed. Please check your database connection."
                );
            }

            return;
        }
    }

    // ==========================================
    // DELIVERABLE 8: ADMIN AUDIT LOG INTERFACE
    // ==========================================

    private void auditLogsMenu() {
        while (true) {
            System.out.println("\n=====================================");
            System.out.println("        SYSTEM AUDIT LOGS MENU");
            System.out.println("=====================================");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[0] Back to Admin Menu");
            System.out.println("=====================================");

            int choice = readIntInput("Enter choice: ");

            if (choice == 1) {
                printLogsTable(logDao.getAllLogs());
            } else if (choice == 2) {
                int targetUserId = readIntInput("Enter User ID to filter: ");
                printLogsTable(logDao.getLogsByUserId(targetUserId));
            } else if (choice == 0) {
                break;
            } else {
                System.out.println("Invalid option. Enter 0 to 2.");
            }
        }
    }

    // ---------- LOGS TABLE FORMATTER (Deliverable 8 Requirement) ----------

    private void printLogsTable(List<ActivityLog> logs) {

        String format = "| %-5s | %-8s | %-18s | %-35s | %-20s |%n";
        String border = "+-------+----------+--------------------+-------------------------------------+----------------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println(border);

        if (logs.isEmpty()) {
            System.out.println("| No activity logs found.                                                                              |");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf(
                        format,
                        log.id(),
                        log.userId(),
                        truncate(log.actionType(), 18),
                        truncate(log.description(), 35),
                        log.timestamp() != null ? log.timestamp() : "N/A"
                );
            }
        }

        System.out.println(border);
        System.out.println("Total Logs: " + logs.size());
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

    // ---------- INPUT VALIDATION: DOUBLE ----------

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid amount.");
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
}