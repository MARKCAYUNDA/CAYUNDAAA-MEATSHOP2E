package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.model.User;

import java.util.*;

public class CashierMenu {

    private static class ProductItem {
        int id;
        String name;
        String category;
        double pricePerKg;
        double stockKg;

        ProductItem(int id, String name, String category, double pricePerKg, double stockKg) {
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

    private static final List<ProductItem> products = new ArrayList<>(List.of(
            new ProductItem(101, "Pork Belly (Liempo)", "Pork", 340.0, 50.0),
            new ProductItem(102, "Beef Brisket", "Beef", 420.0, 35.0),
            new ProductItem(103, "Whole Chicken", "Poultry", 195.0, 40.0)
    ));

    private final List<String> transactionHistory = new ArrayList<>();
    private String lastReceipt = null;

    public CashierMenu(User currentUser, ActivityLogDao logDao, Scanner sc) {
        this.currentUser = currentUser;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void display() {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("             CASHIER DASHBOARD");
            System.out.println("==========================================");
            System.out.println("[1] New Sale Transaction");
            System.out.println("[2] View Available Meat Products");
            System.out.println("[3] Search Product");
            System.out.println("[4] Print Last Receipt");
            System.out.println("[5] View Previous Transactions");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> processNewSale();
                case "2" -> viewProducts();
                case "3" -> searchProduct();
                case "4" -> printLastReceipt();
                case "5" -> viewPreviousTransactions();
                case "0" -> {
                    logDao.logAction(currentUser.getUserId(), "USER_LOGOUT",
                            "Cashier " + currentUser.getUserId() + " logged out");
                    return;
                }
                default -> System.out.println("-> Invalid choice.");
            }
        }
    }

    private void processNewSale() {
        System.out.print("\nEnter Customer Name: ");
        String customer = sc.nextLine().trim();
        if (customer.isEmpty()) customer = "Walk-in Customer";

        List<String> itemsBought = new ArrayList<>();
        double grandTotal = 0.0;

        while (true) {
            viewProducts();
            System.out.print("Enter Product ID to add (0 to checkout): ");
            int pid = readInt();
            if (pid == 0) break;

            ProductItem item = findById(pid);
            if (item == null) {
                System.out.println("-> Product not found.");
                continue;
            }
            System.out.print("Enter weight in kg: ");
            double kg = readDouble();
            if (kg <= 0 || kg > item.stockKg) {
                System.out.printf("-> Insufficient stock! Available: %.2fkg%n", item.stockKg);
                continue;
            }

            double subtotal = kg * item.pricePerKg;
            item.stockKg -= kg;
            grandTotal += subtotal;
            itemsBought.add(String.format("%s (%.2fkg @ P%.2f) = P%.2f",
                    item.name, kg, item.pricePerKg, subtotal));
        }

        if (itemsBought.isEmpty()) {
            System.out.println("-> Cart is empty.");
            return;
        }

        System.out.printf("%nGRAND TOTAL: P%.2f%n", grandTotal);
        double cash;
        while (true) {
            System.out.print("Enter Cash Tendered: P");
            cash = readDouble();
            if (cash >= grandTotal) break;
            System.out.println("-> Insufficient cash.");
        }

        double change = cash - grandTotal;
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("       CAYUNDA'S MEAT SHOP - RECEIPT\n");
        sb.append("==========================================\n");
        sb.append("Cashier : ").append(currentUser.getFullName()).append("\n");
        sb.append("Customer: ").append(customer).append("\n");
        for (String b : itemsBought) sb.append("  ").append(b).append("\n");
        sb.append(String.format("TOTAL: P%.2f | CASH: P%.2f | CHANGE: P%.2f%n", grandTotal, cash, change));
        sb.append("==========================================");

        lastReceipt = sb.toString();
        System.out.println("\n" + lastReceipt);
        transactionHistory.add(customer + " | Total: P" + String.format("%.2f", grandTotal));

        logDao.logAction(currentUser.getUserId(), "SALE_PROCESSED",
                "Sale for " + customer + " = P" + String.format("%.2f", grandTotal));
    }

    private void viewProducts() {
        System.out.printf("%-6s %-22s %-12s %-12s %-10s%n",
                "ID", "Product Name", "Category", "Price/kg", "Stock(kg)");
        for (ProductItem p : products) {
            System.out.printf("%-6d %-22s %-12s P%-11.2f %-10.2f%n",
                    p.id, p.name, p.category, p.pricePerKg, p.stockKg);
        }
    }

    private void searchProduct() {
        System.out.print("Enter keyword: ");
        String kw = sc.nextLine().trim().toLowerCase();
        for (ProductItem p : products) {
            if (p.name.toLowerCase().contains(kw) || p.category.toLowerCase().contains(kw)) {
                System.out.printf("#%d | %s (%s) - P%.2f/kg | Stock: %.2fkg%n",
                        p.id, p.name, p.category, p.pricePerKg, p.stockKg);
            }
        }
    }

    private void printLastReceipt() {
        if (lastReceipt == null) {
            System.out.println("-> No receipt available yet.");
            return;
        }
        System.out.println("\n" + lastReceipt);
        logDao.logAction(currentUser.getUserId(), "RECEIPT_PRINTED",
                "Cashier " + currentUser.getUserId() + " reprinted last receipt");
    }

    private void viewPreviousTransactions() {
        if (transactionHistory.isEmpty()) {
            System.out.println("(No transactions yet)");
            return;
        }
        for (int i = 0; i < transactionHistory.size(); i++) {
            System.out.println((i + 1) + ". " + transactionHistory.get(i));
        }
    }

    private ProductItem findById(int id) {
        for (ProductItem p : products) if (p.id == id) return p;
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