package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryStaffMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    private record MeatProduct(int id, String name, String category, double pricePerKg, double stockKg) {}
    private final List<MeatProduct> inventory = new ArrayList<>();

    public InventoryStaffMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.sc = sc;
        initializeInventory();
    }

    private void initializeInventory() {
        inventory.add(new MeatProduct(101, "Pork Belly (Liempo)", "Pork", 340.00, 12.0)); // Low stock sample (< 15kg)
        inventory.add(new MeatProduct(102, "Pork Chop", "Pork", 310.00, 40.0));
        inventory.add(new MeatProduct(103, "Ground Pork (Giniling)", "Pork", 290.00, 35.0));
        inventory.add(new MeatProduct(201, "Beef Brisket", "Beef", 420.00, 8.0));  // Low stock sample
        inventory.add(new MeatProduct(202, "Beef Sirloin", "Beef", 460.00, 25.0));
        inventory.add(new MeatProduct(301, "Whole Dressed Chicken", "Poultry", 210.00, 60.0));
        inventory.add(new MeatProduct(302, "Chicken Breast Fillet", "Poultry", 240.00, 45.0));
    }

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=====================================");
            System.out.println("       CAYUNDA'S MEAT SHOP");
            System.out.println("      Inventory Staff Menu");
            System.out.println("=====================================");
            System.out.println("[1] Add New Meat Product");
            System.out.println("[2] View Current Inventory");
            System.out.println("[3] Search Product");
            System.out.println("[4] Update Stock Quantity");
            System.out.println("[5] Record Received Stocks");
            System.out.println("[6] Monitor Low-Stock Products");
            System.out.println("[0] Logout");
            System.out.println("=====================================");
            System.out.print("Enter choice: ");

            String input = sc.nextLine().trim();

            switch (input) {
                case "1":
                    addNewProduct(staff);
                    break;
                case "2":
                    viewCurrentInventory();
                    break;
                case "3":
                    searchProduct();
                    break;
                case "4":
                    updateStockQuantity(staff);
                    break;
                case "5":
                    recordReceivedStocks(staff);
                    break;
                case "6":
                    monitorLowStock();
                    break;
                case "0":
                    logDao.logAction(
                            staff.user_id(),
                            "USER_LOGOUT",
                            "Inventory Staff " + staff.username() + " (ID: " + staff.user_id() + ") logged out."
                    );
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 6.");
            }
        }
    }

    // ---------- [1] ADD NEW MEAT PRODUCT ----------
    private void addNewProduct(User staff) {
        System.out.println("\n--- ADD NEW MEAT PRODUCT ---");

        int id = readIntInput("Enter New Product ID (e.g. 401): ");
        if (findProductById(id) != null) {
            System.out.println("Error: Product ID already exists!");
            return;
        }

        System.out.print("Enter Product Name (e.g., Pork Pata): ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Product name cannot be empty.");
            return;
        }

        System.out.print("Enter Category (e.g., Pork, Beef, Poultry): ");
        String category = sc.nextLine().trim();

        double price = readDoubleInput("Enter Price per kg (P): ");
        double initialStock = readDoubleInput("Enter Initial Stock Quantity (kg): ");

        inventory.add(new MeatProduct(id, name, category, price, initialStock));
        System.out.println("-> SUCCESS: New product '" + name + "' added to inventory.");

        // DELIVERABLE 8 LOGGING
        logDao.logAction(
                staff.user_id(),
                "PRODUCT_ADDED",
                "Staff added new product ID " + id + " (" + name + ") with " + initialStock + "kg initial stock."
        );
    }

    // ---------- [2] VIEW CURRENT INVENTORY ----------
    private void viewCurrentInventory() {
        String format = "| %-5s | %-24s | %-10s | %-12s | %-10s |%n";
        String border = "+-------+--------------------------+------------+--------------+------------+";

        System.out.println("\n--- CURRENT MEAT INVENTORY ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Meat Cut / Product", "Category", "Price / kg", "Stock (kg)");
        System.out.println(border);

        for (MeatProduct p : inventory) {
            System.out.printf(format,
                    p.id(),
                    truncate(p.name(), 24),
                    p.category(),
                    String.format("P%.2f", p.pricePerKg()),
                    String.format("%.2f", p.stockKg())
            );
        }
        System.out.println(border);
    }

    // ---------- [3] SEARCH PRODUCT ----------
    private void searchProduct() {
        System.out.print("\nEnter keyword to search (name or category): ");
        String keyword = sc.nextLine().trim().toLowerCase();

        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        String format = "| %-5s | %-24s | %-10s | %-12s | %-10s |%n";
        String border = "+-------+--------------------------+------------+--------------+------------+";
        boolean found = false;

        System.out.println("\n--- SEARCH RESULTS FOR: \"" + keyword + "\" ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Meat Cut / Product", "Category", "Price / kg", "Stock (kg)");
        System.out.println(border);

        for (MeatProduct p : inventory) {
            if (p.name().toLowerCase().contains(keyword) || p.category().toLowerCase().contains(keyword)) {
                System.out.printf(format,
                        p.id(),
                        truncate(p.name(), 24),
                        p.category(),
                        String.format("P%.2f", p.pricePerKg()),
                        String.format("%.2f", p.stockKg())
                );
                found = true;
            }
        }

        if (!found) {
            System.out.println("| No matching products found in inventory.                                 |");
        }
        System.out.println(border);
    }

    // ---------- [4] UPDATE STOCK QUANTITY ----------
    private void updateStockQuantity(User staff) {
        System.out.println("\n--- UPDATE STOCK QUANTITY ---");
        viewCurrentInventory();

        int id = readIntInput("Enter Product ID to update (0 to cancel): ");
        if (id == 0) return;

        MeatProduct target = findProductById(id);
        if (target == null) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.println("Selected Product: " + target.name() + " | Current Stock: " + target.stockKg() + " kg");
        System.out.println("[1] Add Stock");
        System.out.println("[2] Deduct / Adjust Stock");
        int choice = readIntInput("Choose action: ");

        double qty = readDoubleInput("Enter quantity in kg: ");
        if (qty < 0) {
            System.out.println("Quantity cannot be negative.");
            return;
        }

        double newStock = target.stockKg();
        if (choice == 1) {
            newStock += qty;
        } else if (choice == 2) {
            newStock -= qty;
            if (newStock < 0) newStock = 0;
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        int index = inventory.indexOf(target);
        inventory.set(index, new MeatProduct(target.id(), target.name(), target.category(), target.pricePerKg(), newStock));

        System.out.printf("-> SUCCESS: Stock for %s updated to %.2f kg.%n", target.name(), newStock);

        // DELIVERABLE 8 LOGGING
        logDao.logAction(
                staff.user_id(),
                "STOCK_UPDATED",
                "Staff updated stock for Product ID " + id + " (" + target.name() + "). New stock: " + newStock + "kg"
        );
    }

    // ---------- [5] RECORD RECEIVED STOCKS (FROM SUPPLIER) ----------
    private void recordReceivedStocks(User staff) {
        System.out.println("\n--- RECORD RECEIVED STOCKS FROM SUPPLIER ---");
        viewCurrentInventory();

        int id = readIntInput("Enter Product ID delivered by Supplier (0 to cancel): ");
        if (id == 0) return;

        MeatProduct target = findProductById(id);
        if (target == null) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.print("Enter Supplier Name/Batch Reference: ");
        String supplier = sc.nextLine().trim();

        double deliveredQty = readDoubleInput("Enter delivered quantity (kg): ");
        if (deliveredQty <= 0) {
            System.out.println("Delivered quantity must be greater than zero.");
            return;
        }

        double newStock = target.stockKg() + deliveredQty;
        int index = inventory.indexOf(target);
        inventory.set(index, new MeatProduct(target.id(), target.name(), target.category(), target.pricePerKg(), newStock));

        System.out.printf("-> SUCCESS: Received %.2f kg of %s from %s. Total stock: %.2f kg.%n",
                deliveredQty, target.name(), supplier.isEmpty() ? "Supplier" : supplier, newStock);

        // DELIVERABLE 8 LOGGING
        logDao.logAction(
                staff.user_id(),
                "STOCK_RECEIVED",
                "Received " + deliveredQty + "kg of " + target.name() + " from supplier [" + supplier + "]."
        );
    }

    // ---------- [6] MONITOR LOW-STOCK PRODUCTS ----------
    private void monitorLowStock() {
        double threshold = 15.0; // Limit kon ang stock kay ubos sa 15kg
        String format = "| %-5s | %-24s | %-10s | %-10s |%n";
        String border = "+-------+--------------------------+------------+------------+";

        System.out.println("\n--- LOW-STOCK MONITOR (Threshold < " + threshold + " kg) ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Meat Cut / Product", "Category", "Stock (kg)");
        System.out.println(border);

        boolean lowStockFound = false;
        for (MeatProduct p : inventory) {
            if (p.stockKg() < threshold) {
                System.out.printf(format,
                        p.id(),
                        truncate(p.name(), 24),
                        p.category(),
                        String.format("%.2f kg", p.stockKg())
                );
                lowStockFound = true;
            }
        }

        if (!lowStockFound) {
            System.out.println("| All meat products have sufficient stock levels (> " + threshold + "kg).   |");
        }
        System.out.println(border);
    }

    // ---------- HELPER & INPUT METHODS ----------
    private MeatProduct findProductById(int id) {
        for (MeatProduct p : inventory) {
            if (p.id() == id) {
                return p;
            }
        }
        return null;
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter numbers only.");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount! Please enter a valid number.");
            }
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}