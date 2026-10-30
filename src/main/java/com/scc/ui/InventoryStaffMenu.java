package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class InventoryStaffMenu {
    private final UserDao userDao;
    private final Scanner sc;

    public InventoryStaffMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=====================================");
            System.out.println("     CAYUNDA'S MEAT SHOP");
            System.out.println("     Inventory Staff Menu");
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
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 6.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}