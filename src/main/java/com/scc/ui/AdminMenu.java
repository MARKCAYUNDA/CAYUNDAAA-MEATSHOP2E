package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.*;

public class AdminMenu {

    private static class MeatProduct {
        int id;
        String name;
        String category;
        double pricePerKg;
        double stockKg;

        MeatProduct(int id, String name, String category, double pricePerKg, double stockKg) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.pricePerKg = pricePerKg;
            this.stockKg = stockKg;
        }
    }

    private static class Supplier {
        int id;
        String name;
        String contact;
        String supplyType;

        Supplier(int id, String name, String contact, String supplyType) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.supplyType = supplyType;
        }
    }

    private static class SaleRecord {
        String customer;
        String summary;
        double total;

        SaleRecord(String customer, String summary, double total) {
            this.customer = customer;
            this.summary = summary;
            this.total = total;
        }
    }

    private final User currentUser;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    private static final List<MeatProduct> catalog = new ArrayList<>(List.of(
            new MeatProduct(101, "Pork Belly (Liempo)", "Pork", 340.0, 50.0),
            new MeatProduct(102, "Beef Brisket", "Beef", 420.0, 35.0),
            new MeatProduct(103, "Whole Chicken", "Poultry", 195.0, 40.0)
    ));

    private static final List<Supplier> suppliers = new ArrayList<>(List.of(
            new Supplier(1, "Visayas Prime Livestock", "0917-123-4567", "Pork & Beef")
    ));

    private static final List<SaleRecord> salesRecords = new ArrayList<>();

    public AdminMenu(User currentUser, UserDao userDao, ActivityLogDao logDao, Scanner sc) {
        this.currentUser = currentUser;
        this.userDao = userDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void display() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("           CAYUNDA'S MEAT SHOP");
            System.out.println("            Administrator Menu");
            System.out.println("==========================================");
            System.out.println("[1] User Management");
            System.out.println("[2] Product Management");
            System.out.println("[3] Supplier Management");
            System.out.println("[4] Sales Management");
            System.out.println("[5] Reports");
            System.out.println("[6] View System Audit Logs");
            System.out.println("[0] Logout");
            System.out.println("==========================================");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> userManagementMenu();
                case "2" -> productManagementMenu();
                case "3" -> supplierManagementMenu();
                case "4" -> salesManagementMenu();
                case "5" -> reportsMenu();
                case "6" -> auditLogsMenu();
                case "0" -> {
                    logDao.logAction(currentUser.getUserId(), "USER_LOGOUT",
                            "Admin " + currentUser.getUserId() + " logged out");
                    return;
                }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void userManagementMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("              USER MANAGEMENT");
            System.out.println("==========================================");
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Update User Role and Status");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> printUsers(userDao.getAllUsers(), "ALL SYSTEM USERS");
                case "2" -> printUsers(userDao.getPendingUsers(), "PENDING USER REGISTRATIONS");
                case "3" -> {
                    printUsers(userDao.getPendingUsers(), "PENDING USER REGISTRATIONS");
                    System.out.print("Enter User ID to approve (0 to cancel): ");
                    int id = readInt();
                    if (id == 0) break;
                    User target = userDao.getUserById(id);
                    if (target == null) {
                        System.out.println("-> User not found.");
                    } else if (userDao.approveUser(id)) {
                        System.out.println("-> SUCCESS: Approved account for User ID " + id);
                        logDao.logAction(currentUser.getUserId(), "USER_APPROVED",
                                "Admin " + currentUser.getUserId() + " approved account for User ID " + id);
                    }
                }
                case "4" -> {
                    printUsers(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    System.out.print("Enter User ID to update (0 to cancel): ");
                    int id = readInt();
                    if (id == 0) break;
                    if (id == currentUser.getUserId()) {
                        System.out.println("-> WARNING: You cannot modify your own active admin account.");
                        break;
                    }
                    User target = userDao.getUserById(id);
                    if (target == null) {
                        System.out.println("-> User not found.");
                        break;
                    }
                    System.out.println("Select Role: [1] ADMIN  [2] CASHIER  [3] INVENTORY_STAFF");
                    String rCh = sc.nextLine().trim();
                    String newRole = switch (rCh) {
                        case "1" -> "ADMIN";
                        case "2" -> "CASHIER";
                        case "3" -> "INVENTORY_STAFF";
                        default -> null;
                    };
                    System.out.println("Select Status: [1] APPROVED  [2] PENDING  [3] SUSPENDED");
                    String sCh = sc.nextLine().trim();
                    String newStatus = switch (sCh) {
                        case "1" -> "APPROVED";
                        case "2" -> "PENDING";
                        case "3" -> "SUSPENDED";
                        default -> null;
                    };
                    if (newRole == null || newStatus == null) {
                        System.out.println("-> Invalid role or status.");
                        break;
                    }
                    if (userDao.updateUserRoleAndStatus(id, newRole, newStatus)) {
                        System.out.println("-> SUCCESS: Updated User ID " + id + " to " + newRole + " (" + newStatus + ")");
                        logDao.logAction(currentUser.getUserId(), "USER_UPDATED",
                                "Admin " + currentUser.getUserId() + " changed role of User " + id + " to " + newRole);
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void printUsers(List<User> users, String title) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("(No records found)");
            return;
        }
        System.out.printf("%-5s %-20s %-15s %-16s %-10s%n", "ID", "Full Name", "Username", "Role", "Status");
        for (User u : users) {
            System.out.printf("%-5d %-20s %-15s %-16s %-10s%n",
                    u.getUserId(), u.getFullName(), u.getUsername(), u.getRole(), u.getStatus());
        }
    }

    private void productManagementMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("            PRODUCT MANAGEMENT");
            System.out.println("==========================================");
            System.out.println("[1] View Meat Inventory");
            System.out.println("[2] Add New Meat Product");
            System.out.println("[3] Update Meat Stock / Price");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> printCatalog();
                case "2" -> {
                    System.out.print("Enter Product ID (e.g., 104): ");
                    int id = readInt();
                    if (findProduct(id) != null) {
                        System.out.println("-> Product ID already exists.");
                        break;
                    }
                    System.out.print("Enter Product Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Enter Category (Pork, Beef, Poultry): ");
                    String cat = sc.nextLine().trim();
                    System.out.print("Enter Price per kg (P): ");
                    double price = readDouble();
                    System.out.print("Enter Initial Stock (kg): ");
                    double stock = readDouble();

                    catalog.add(new MeatProduct(id, name, cat, price, stock));
                    System.out.println("-> SUCCESS: Added '" + name + "'.");
                    logDao.logAction(currentUser.getUserId(), "PRODUCT_ADDED",
                            "Added meat product #" + id + " (" + name + ")");
                }
                case "3" -> {
                    printCatalog();
                    System.out.print("Enter Product ID to update (0 to cancel): ");
                    int id = readInt();
                    if (id == 0) break;
                    MeatProduct p = findProduct(id);
                    if (p == null) {
                        System.out.println("-> Product not found.");
                        break;
                    }
                    System.out.println("[1] Update Price per kg   [2] Update Stock Quantity");
                    String opt = sc.nextLine().trim();
                    if ("1".equals(opt)) {
                        System.out.print("Enter new price per kg (P): ");
                        p.pricePerKg = readDouble();
                        System.out.printf("-> SUCCESS: Price updated to P%.2f%n", p.pricePerKg);
                        logDao.logAction(currentUser.getUserId(), "STOCK_UPDATED",
                                "Updated price of " + p.name + " to P" + p.pricePerKg);
                    } else if ("2".equals(opt)) {
                        System.out.print("Enter new stock quantity (kg): ");
                        p.stockKg = readDouble();
                        System.out.printf("-> SUCCESS: Stock updated to %.2fkg%n", p.stockKg);
                        logDao.logAction(currentUser.getUserId(), "STOCK_UPDATED",
                                "Updated stock of " + p.name + " to " + p.stockKg + "kg");
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void supplierManagementMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("           SUPPLIER MANAGEMENT");
            System.out.println("==========================================");
            System.out.println("[1] View Registered Suppliers");
            System.out.println("[2] Register New Supplier");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    System.out.printf("%-5s %-25s %-18s %-15s%n", "ID", "Supplier Name", "Contact", "Meat Supply");
                    for (Supplier s : suppliers) {
                        System.out.printf("%-5d %-25s %-18s %-15s%n", s.id, s.name, s.contact, s.supplyType);
                    }
                }
                case "2" -> {
                    System.out.print("Enter Supplier ID: ");
                    int id = readInt();
                    System.out.print("Enter Supplier / Farm Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Enter Contact Number: ");
                    String contact = sc.nextLine().trim();
                    System.out.print("Enter Meat Supply Type: ");
                    String type = sc.nextLine().trim();

                    suppliers.add(new Supplier(id, name, contact, type));
                    System.out.println("-> SUCCESS: Registered supplier '" + name + "'.");
                    logDao.logAction(currentUser.getUserId(), "SUPPLIER_REGISTERED",
                            "Registered supplier #" + id + " (" + name + ")");
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void salesManagementMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("            SALES MANAGEMENT");
            System.out.println("==========================================");
            System.out.println("[1] View Daily Sales Records");
            System.out.println("[2] Process Direct Sale");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    double grandTotal = 0;
                    for (int i = 0; i < salesRecords.size(); i++) {
                        SaleRecord sr = salesRecords.get(i);
                        grandTotal += sr.total;
                        System.out.printf("#%d | Customer: %s | %s | P%.2f%n",
                                i + 1, sr.customer, sr.summary, sr.total);
                    }
                    System.out.printf("TOTAL REVENUE: P%.2f%n", grandTotal);
                }
                case "2" -> {
                    printCatalog();
                    System.out.print("Enter Customer Name: ");
                    String cust = sc.nextLine().trim();
                    System.out.print("Enter Product ID: ");
                    int pid = readInt();
                    MeatProduct p = findProduct(pid);
                    if (p == null) {
                        System.out.println("-> Product not found.");
                        break;
                    }
                    System.out.print("Enter Weight (kg): ");
                    double kg = readDouble();
                    if (kg <= 0 || kg > p.stockKg) {
                        System.out.println("-> Invalid or insufficient stock.");
                        break;
                    }
                    double total = kg * p.pricePerKg;
                    p.stockKg -= kg;
                    String summary = String.format("%.2fkg %s", kg, p.name);
                    salesRecords.add(new SaleRecord(cust, summary, total));
                    System.out.printf("-> SALE COMPLETED! Total: P%.2f%n", total);
                    logDao.logAction(currentUser.getUserId(), "SALE_PROCESSED",
                            "Processed sale (" + summary + ") = P" + String.format("%.2f", total));
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void reportsMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("               REPORTS MENU");
            System.out.println("==========================================");
            System.out.println("[1] Inventory Valuation Report");
            System.out.println("[2] Sales Summary Report");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    printCatalog();
                    double totalVal = 0;
                    for (MeatProduct p : catalog) totalVal += p.pricePerKg * p.stockKg;
                    System.out.printf("TOTAL INVENTORY VALUE: P%.2f%n", totalVal);
                    logDao.logAction(currentUser.getUserId(), "REPORT_GENERATED",
                            "Generated Inventory Valuation Report");
                }
                case "2" -> {
                    double totalSales = 0;
                    for (SaleRecord sr : salesRecords) totalSales += sr.total;
                    System.out.printf("Transactions: %d | Total Sales: P%.2f%n", salesRecords.size(), totalSales);
                    logDao.logAction(currentUser.getUserId(), "REPORT_GENERATED",
                            "Generated Sales Summary Report");
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void auditLogsMenu() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("          SYSTEM AUDIT LOGS MENU");
            System.out.println("==========================================");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Clear Historical Logs");
            System.out.println("[0] Back to Admin Menu");
            System.out.println("==========================================");
            System.out.print("Enter choice: ");

            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> printLogsTable(logDao.getAllLogs());
                case "2" -> {
                    System.out.print("Enter User ID to filter: ");
                    int uid = readInt();
                    printLogsTable(logDao.getLogsByUserId(uid));
                }
                case "3" -> {
                    System.out.print("Are you sure you want to clear all historical logs? (Y/N): ");
                    String confirm = sc.nextLine().trim();
                    if ("Y".equalsIgnoreCase(confirm)) {
                        if (logDao.clearHistoricalLogs()) {
                            System.out.println("-> SUCCESS: Historical logs cleared.");
                            logDao.logAction(currentUser.getUserId(), "LOGS_CLEARED",
                                    "Admin " + currentUser.getUserId() + " cleared historical audit logs");
                        }
                    } else {
                        System.out.println("-> Operation cancelled.");
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void printLogsTable(List<ActivityLog> logs) {
        String border = "+-------+----------+--------------------+-------------------------------------+----------------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-8s | %-18s | %-35s | %-20s |%n",
                "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println(border);
        if (logs.isEmpty()) {
            System.out.printf("| %-98s |%n", "No activity logs found.");
        } else {
            for (ActivityLog l : logs) {
                String desc = l.description().length() > 35
                        ? l.description().substring(0, 32) + "..."
                        : l.description();
                System.out.printf("| %-5d | %-8d | %-18s | %-35s | %-20s |%n",
                        l.id(), l.userId(), l.actionType(), desc, l.timestamp());
            }
        }
        System.out.println(border);
        System.out.println("Total Logs: " + logs.size());
    }

    private void printCatalog() {
        System.out.println("\n--- MEAT PRODUCT INVENTORY ---");
        System.out.printf("%-6s %-22s %-12s %-12s %-10s%n",
                "ID", "Product Name", "Category", "Price/kg", "Stock(kg)");
        for (MeatProduct p : catalog) {
            System.out.printf("%-6d %-22s %-12s P%-11.2f %-10.2f%n",
                    p.id, p.name, p.category, p.pricePerKg, p.stockKg);
        }
    }

    private MeatProduct findProduct(int id) {
        for (MeatProduct p : catalog) {
            if (p.id == id) return p;
        }
        return null;
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }

    private double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}