package com.bank.atm;

import java.sql.*;
import java.util.Scanner;

public class ATM_Main {

    private static Connection conn = DBConnection.getConnection();

    public static void startATM() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Account Number: ");
        long accountNumber = sc.nextLong();
        sc.nextLine(); // consume newline

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine();

        if (!login(accountNumber, pin)) {
            System.out.println("❌ Invalid Account Number or PIN");
            return;
        }

        while (true) {
            System.out.println("\n===== ATM MENU =====");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. View Transactions");
            System.out.println("5. Logout");
            System.out.println("6. Delete Account");

            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> checkBalance(accountNumber);
                case 2 -> depositMoney(accountNumber, sc);
                case 3 -> withdrawMoney(accountNumber, sc);
                case 4 -> viewTransactions(accountNumber);
                case 5 ->
                {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static boolean login(long accountNumber, String pin) {
        try {
            String sql = "SELECT * FROM users u JOIN accounts a ON u.id = a.user_id " +
                         "WHERE a.account_number = ? AND u.password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, accountNumber);
            ps.setString(2, pin);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void checkBalance(long accountNumber) {
        try {
            String sql = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, accountNumber);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("💰 Your Balance: " + rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void depositMoney(long accountNumber, Scanner sc) {
        try {
            System.out.print("Enter amount to deposit: ");
            double amount = sc.nextDouble();
            sc.nextLine();

            String update = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.setDouble(1, amount);
            ps.setLong(2, accountNumber);
            ps.executeUpdate();

            String insertTxn = "INSERT INTO transactions (account_number, type, amount) VALUES (?, 'Deposit', ?)";
            PreparedStatement txn = conn.prepareStatement(insertTxn);
            txn.setLong(1, accountNumber);
            txn.setDouble(2, amount);
            txn.executeUpdate();

            System.out.println("✅ Deposited: " + amount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void withdrawMoney(long accountNumber, Scanner sc) {
        try {
            System.out.print("Enter amount to withdraw: ");
            double amount = sc.nextDouble();
            sc.nextLine();

            // Check balance
            String check = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setLong(1, accountNumber);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (balance < amount) {
                    System.out.println("❌ Insufficient balance!");
                    return;
                }
            }

            String update = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.setDouble(1, amount);
            ps.setLong(2, accountNumber);
            ps.executeUpdate();

            String insertTxn = "INSERT INTO transactions (account_number, type, amount) VALUES (?, 'Withdraw', ?)";
            PreparedStatement txn = conn.prepareStatement(insertTxn);
            txn.setLong(1, accountNumber);
            txn.setDouble(2, amount);
            txn.executeUpdate();

            System.out.println("✅ Withdrawn: " + amount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewTransactions(long accountNumber) {
        try {
            String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, accountNumber);

            ResultSet rs = ps.executeQuery();
            System.out.println("\nDate | Type | Amount");
            System.out.println("----------------------");
            while (rs.next()) {
                System.out.printf("%s | %s | %.2f%n",
                        rs.getTimestamp("date"),
                        rs.getString("type"),
                        rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
