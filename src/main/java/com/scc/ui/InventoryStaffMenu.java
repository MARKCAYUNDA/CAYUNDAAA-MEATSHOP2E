package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.model.User;

import java.util.*;

public class InventoryStaffMenu {

    private static class StockItem {
        int id;
        String name;
        String category;
        double pricePerKg;
        double stockKg;

        StockItem(int id, String name, String category, double pricePerKg, double stockKg) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.pricePerKg = pricePerKg;
            this.stockKg = stockKg;
        }
    }

    private final User currentUser;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    private static final List<StockItem> inventory = new ArrayList<>(List.of(
            new StockItem(101, "Pork Belly (Liempo)", "Pork", 340.0, 50.0),
            new StockItem(102, "Beef Brisket", "Beef", 420.0, 12.0),
            new StockItem(103, "Whole Chicken", "Poultry", 195.0, 9.5)
    ));

    public InventoryStaffMenu(User currentUser, ActivityLogDao logDao, Scanner sc) {
        this.currentUser = currentUser;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void display() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("         INVENTORY STAFF DASHBOARD");
            System.out.println("==========================================");
            System.out.println("[1] Add New Product");
            System.out.println("[2] View Inventory");
            System.out.println("[3] Search Product");
            System.out.println("[4] Update Stock Quantity");
            System.out.println("[5] Record Received Stocks");
            System.out.println("[6] Monitor Low-Stock (< 15.0 kg)");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> addNewProduct();
                case "2" -> viewInventory();
                case "3" -> searchProduct();
                case "4" -> updateStockQuantity();
                case "5" -> recordReceivedStocks();
                case "6" -> monitorLowStock();
                case "0" -> {
                    logDao.logAction(currentUser.getUserId(), "USER_LOGOUT",
                            "Inventory Staff " + currentUser.getUserId() + " logged out");
                    return;
                }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void addNewProduct() {
        System.out.print("Enter Product ID: ");
        int id = readInt();
        if (findItem(id) != null) {
            System.out.println("-> Product ID already exists.");
            return;
        }
        System.out.print("Enter Meat Cut Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter Category: ");
        String cat = sc.nextLine().trim();
        System.out.print("Enter Price per kg (P): ");
        double price = readDouble();
        System.out.print("Enter Initial Stock (kg): ");
        double stock = readDouble();

        inventory.add(new StockItem(id, name, cat, price, stock));
        System.out.println("-> SUCCESS: Added '" + name + "'.");
        logDao.logAction(currentUser.getUserId(), "PRODUCT_ADDED",
                "Added product #" + id + " (" + name + ")");
    }

    private void viewInventory() {
        System.out.printf("%-6s %-22s %-12s %-12s %-10s%n",
                "ID", "Product Name", "Category", "Price/kg", "Stock(kg)");
        for (StockItem item : inventory) {
            System.out.printf("%-6d %-22s %-12s P%-11.2f %-10.2f%n",
                    item.id, item.name, item.category, item.pricePerKg, item.stockKg);
        }
    }

    private void searchProduct() {
        System.out.print("Enter keyword: ");
        String kw = sc.nextLine().trim().toLowerCase();
        for (StockItem item : inventory) {
            if (item.name.toLowerCase().contains(kw) || item.category.toLowerCase().contains(kw)) {
                System.out.printf("#%d | %s (%s) | Stock: %.2fkg | P%.2f/kg%n",
                        item.id, item.name, item.category, item.stockKg, item.pricePerKg);
            }
        }
    }

    private void updateStockQuantity() {
        viewInventory();
        System.out.print("Enter Product ID: ");
        int id = readInt();
        StockItem item = findItem(id);
        if (item == null) {
            System.out.println("-> Product not found.");
            return;
        }
        System.out.println("[1] Add Stock (kg)   [2] Deduct Stock (kg)");
        String opt = sc.nextLine().trim();
        System.out.print("Enter weight (kg): ");
        double kg = readDouble();
        if ("1".equals(opt) && kg > 0) {
            item.stockKg += kg;
        } else if ("2".equals(opt) && kg > 0 && kg <= item.stockKg) {
            item.stockKg -= kg;
        } else {
            System.out.println("-> Invalid operation.");
            return;
        }
        System.out.printf("-> SUCCESS: New stock for %s is %.2fkg%n", item.name, item.stockKg);
        logDao.logAction(currentUser.getUserId(), "STOCK_UPDATED",
                "Updated stock of " + item.name + " to " + item.stockKg + "kg");
    }

    private void recordReceivedStocks() {
        viewInventory();
        System.out.print("Enter Product ID: ");
        int id = readInt();
        StockItem item = findItem(id);
        if (item == null) {
            System.out.println("-> Product not found.");
            return;
        }
        System.out.print("Enter Supplier Name: ");
        String supplier = sc.nextLine().trim();
        System.out.print("Enter Delivered Weight (kg): ");
        double deliveredKg = readDouble();
        if (deliveredKg <= 0) return;

        item.stockKg += deliveredKg;
        System.out.printf("-> RECEIVED %.2fkg of %s from %s.%n", deliveredKg, item.name, supplier);
        logDao.logAction(currentUser.getUserId(), "STOCK_RECEIVED",
                "Received " + deliveredKg + "kg of " + item.name + " from " + supplier);
    }

    private void monitorLowStock() {
        System.out.println("\n--- LOW-STOCK ALERTS (< 15.0 kg) ---");
        for (StockItem item : inventory) {
            if (item.stockKg < 15.0) {
                System.out.printf("ALERT: #%d %s -> Only %.2fkg left!%n",
                        item.id, item.name, item.stockKg);
            }
        }
    }

    private StockItem findItem(int id) {
        for (StockItem item : inventory) if (item.id == id) return item;
        return null;
    }

    private int readInt() {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("Enter valid integer: "); }
        }
    }

    private double readDouble() {
        while (true) {
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("Enter valid number: "); }
        }
    }
}