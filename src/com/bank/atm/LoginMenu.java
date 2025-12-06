package com.bank.atm;

import java.util.Scanner;

public class LoginMenu {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== BANK MANAGEMENT SYSTEM =====");
            System.out.println("1. User Login (ATM)");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    ATM_Main.startATM();
                    break;
                case 2:
                    AdminPanel.startAdmin();
                    break;
                case 3:
                    System.out.println("Exiting... Thank you!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}
