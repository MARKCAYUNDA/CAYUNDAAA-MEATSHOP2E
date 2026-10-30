package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class CashierMenu {
    private final UserDao userDao;
    private final Scanner sc;

    public CashierMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void display(User cashier) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=====================================");
            System.out.println("     CAYUNDA'S MEAT SHOP");
            System.out.println("     Cashier Menu");
            System.out.println("=====================================");
            System.out.println("[1] New Sale / Register Purchase");
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
                case "2":
                case "3":
                case "4":
                case "5":
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 5.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}