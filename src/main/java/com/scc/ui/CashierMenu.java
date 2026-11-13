package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CashierMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    // In-memory store para sa session transactions ug sample meat products
    private final List<String> transactionHistory = new ArrayList<>();
    private String lastReceipt = null;

    private record MeatProduct(int id, String name, String category, double pricePerKg, double stockKg) {}
    private final List<MeatProduct> meatCatalog = new ArrayList<>();

    public CashierMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.sc = sc;
        initializeCatalog();
    }

    private void initializeCatalog() {
        meatCatalog.add(new MeatProduct(101, "Pork Belly (Liempo)", "Pork", 340.00, 50.0));
        meatCatalog.add(new MeatProduct(102, "Pork Chop", "Pork", 310.00, 40.0));
        meatCatalog.add(new MeatProduct(103, "Ground Pork (Giniling)", "Pork", 290.00, 35.0));
        meatCatalog.add(new MeatProduct(201, "Beef Brisket", "Beef", 420.00, 30.0));
        meatCatalog.add(new MeatProduct(202, "Beef Sirloin", "Beef", 460.00, 25.0));
        meatCatalog.add(new MeatProduct(301, "Whole Dressed Chicken", "Poultry", 210.00, 60.0));
        meatCatalog.add(new MeatProduct(302, "Chicken Breast Fillet", "Poultry", 240.00, 45.0));
    }

    public void display(User cashier) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=====================================");
            System.out.println("       CAYUNDA'S MEAT SHOP");
            System.out.println("          Cashier Menu");
            System.out.println("=====================================");
            System.out.println("[1] New Sale ");
            System.out.println("[2] View Product Information and Prices");
            System.out.println("[3] Search Product");
            System.out.println("[4] Print / Record Sales Receipt");
            System.out.println("[5] View Previous Transactions");
            System.out.println("[0] Logout");
            System.out.println("=====================================");
            System.out.print("Enter choice: ");

            String input = sc.nextLine().trim();

            switch (input) {
                case "1":
                    processNewSale(cashier);
                    break;
                case "2":
                    viewProductCatalog();
                    break;
                case "3":
                    searchProduct();
                    break;
                case "4":
                    printLastReceipt(cashier);
                    break;
                case "5":
                    viewPreviousTransactions();
                    break;
                case "0":
                    logDao.logAction(
                            cashier.user_id(),
                            "USER_LOGOUT",
                            "Cashier " + cashier.username() + " (ID: " + cashier.user_id() + ") logged out."
                    );
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 5.");
            }
        }
    }

    // ---------- [1] NEW SALE / REGISTER PURCHASE ----------
    private void processNewSale(User cashier) {
        System.out.println("\n=====================================");
        System.out.println("     NEW SALE / REGISTER PURCHASE");
        System.out.println("=====================================");

        viewProductCatalog();

        System.out.print("\nEnter Customer Name (or press Enter for Walk-in): ");
        String customerName = sc.nextLine().trim();
        if (customerName.isEmpty()) {
            customerName = "Walk-in Customer";
        }

        List<String> cartLines = new ArrayList<>();
        double grandTotal = 0.0;

        while (true) {
            int prodId = readIntInput("\nEnter Product ID to add (0 to finish/checkout): ");
            if (prodId == 0) {
                break;
            }

            MeatProduct selected = findProductById(prodId);
            if (selected == null) {
                System.out.println("Invalid Product ID. Please try again.");
                continue;
            }

            double kg = readDoubleInput("Enter weight in kg for " + selected.name() + ": ");
            if (kg <= 0) {
                System.out.println("Weight must be greater than 0.");
                continue;
            }
            if (kg > selected.stockKg()) {
                System.out.printf("Insufficient stock! Available: %.2f kg%n", selected.stockKg());
                continue;
            }

            double subtotal = selected.pricePerKg() * kg;
            grandTotal += subtotal;

            // Update stock in catalog
            int idx = meatCatalog.indexOf(selected);
            meatCatalog.set(idx, new MeatProduct(
                    selected.id(), selected.name(), selected.category(),
                    selected.pricePerKg(), selected.stockKg() - kg
            ));

            cartLines.add(String.format("%-24s %5.2f kg @ P%6.2f = P%8.2f",
                    selected.name(), kg, selected.pricePerKg(), subtotal));
            System.out.printf("-> Added %s (%.2f kg) - Subtotal: P%.2f%n", selected.name(), kg, subtotal);
        }

        if (cartLines.isEmpty()) {
            System.out.println("-> Sale cancelled (no items added).");
            return;
        }

        System.out.printf("\nTOTAL AMOUNT DUE: P%.2f%n", grandTotal);
        double cashReceived = 0.0;
        while (true) {
            cashReceived = readDoubleInput("Enter Cash Tendered (0 to cancel sale): P");
            if (cashReceived == 0) {
                System.out.println("-> Transaction cancelled.");
                return;
            }
            if (cashReceived < grandTotal) {
                System.out.println("Insufficient cash tendered! Please enter at least P" + String.format("%.2f", grandTotal));
            } else {
                break;
            }
        }

        double change = cashReceived - grandTotal;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String txId = "TX-" + (1001 + transactionHistory.size());

        // Build Official Receipt
        StringBuilder receiptBuilder = new StringBuilder();
        receiptBuilder.append("\n=============================================\n");
        receiptBuilder.append("           CAYUNDA'S MEAT SHOP\n");
        receiptBuilder.append("              OFFICIAL RECEIPT\n");
        receiptBuilder.append("=============================================\n");
        receiptBuilder.append("Transaction ID : ").append(txId).append("\n");
        receiptBuilder.append("Date & Time    : ").append(timestamp).append("\n");
        receiptBuilder.append("Cashier        : ").append(cashier.full_name()).append("\n");
        receiptBuilder.append("Customer       : ").append(customerName).append("\n");
        receiptBuilder.append("---------------------------------------------\n");
        for (String line : cartLines) {
            receiptBuilder.append(line).append("\n");
        }
        receiptBuilder.append("---------------------------------------------\n");
        receiptBuilder.append(String.format("TOTAL AMOUNT   : P%.2f%n", grandTotal));
        receiptBuilder.append(String.format("CASH TENDERED  : P%.2f%n", cashReceived));
        receiptBuilder.append(String.format("CHANGE         : P%.2f%n", change));
        receiptBuilder.append("=============================================\n");
        receiptBuilder.append("     Thank you for shopping with us!\n");
        receiptBuilder.append("=============================================\n");

        lastReceipt = receiptBuilder.toString();
        System.out.println(lastReceipt);

        // Save to session history
        String summary = String.format("%-8s | %-20s | %-18s | P%8.2f", txId, timestamp, truncate(customerName, 18), grandTotal);
        transactionHistory.add(summary);

        // DELIVERABLE 8: Automatic Activity Log Entry
        logDao.logAction(
                cashier.user_id(),
                "SALE_PROCESSED",
                "Processed sale " + txId + " for " + customerName + " - Total: P" + String.format("%.2f", grandTotal)
        );
    }

    // ---------- [2] VIEW PRODUCT INFORMATION AND PRICES ----------
    private void viewProductCatalog() {
        String format = "| %-5s | %-24s | %-10s | %-12s | %-10s |%n";
        String border = "+-------+--------------------------+------------+--------------+------------+";

        System.out.println("\n--- MEAT PRODUCT CATALOG & PRICES ---");
        System.out.println(border);
        System.out.printf(format, "ID", "Meat Cut / Product", "Category", "Price / kg", "Stock (kg)");
        System.out.println(border);

        for (MeatProduct p : meatCatalog) {
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
        System.out.print("\nEnter product name or category to search: ");
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

        for (MeatProduct p : meatCatalog) {
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
            System.out.println("| No matching meat products found.                                         |");
        }
        System.out.println(border);
    }

    // ---------- [4] PRINT / RECORD SALES RECEIPT ----------
    private void printLastReceipt(User cashier) {
        System.out.println("\n--- PRINT / RECORD SALES RECEIPT ---");
        if (lastReceipt == null) {
            System.out.println("-> No recent transaction in this session to print.");
            return;
        }

        System.out.println(lastReceipt);
        System.out.println("-> Receipt printed and recorded successfully.");

        logDao.logAction(
                cashier.user_id(),
                "RECEIPT_PRINTED",
                "Cashier " + cashier.username() + " reprinted the latest sales receipt."
        );
    }

    // ---------- [5] VIEW PREVIOUS TRANSACTIONS ----------
    private void viewPreviousTransactions() {
        System.out.println("\n=================================================================");
        System.out.println("                  PREVIOUS SALES TRANSACTIONS");
        System.out.println("=================================================================");
        System.out.printf("%-8s | %-20s | %-18s | %-10s%n", "TX ID", "Date & Time", "Customer", "Total");
        System.out.println("-----------------------------------------------------------------");

        if (transactionHistory.isEmpty()) {
            System.out.println("No sales transactions recorded yet in this session.");
        } else {
            for (String record : transactionHistory) {
                System.out.println(record);
            }
        }
        System.out.println("=================================================================");
    }

    // ---------- HELPER & INPUT VALIDATION METHODS ----------
    private MeatProduct findProductById(int id) {
        for (MeatProduct p : meatCatalog) {
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
                System.out.println("Invalid amount/weight! Please enter a valid number.");
            }
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}