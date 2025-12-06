package com.bank.atm;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class AdminPanel {

    public static void startAdmin() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Admin Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();

        // Hardcoded admin credentials
        if (!"admin".equals(username) || !"admin123".equals(password)) {
            System.out.println("❌ Invalid Admin Credentials");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            while (true) {
                System.out.println("\n===== ADMIN PANEL =====");
                System.out.println("1. Create User");
                System.out.println("2. Create Bank Account for User");
                System.out.println("3. View All Users");
                System.out.println("4. View All Accounts");
                System.out.println("5. Logout");
                System.out.println("6. Delete Account");
                System.out.print("Choose: ");
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> createUser(conn, sc);
                    case 2 -> createAccount(conn, sc);
                    case 3 -> viewAllUsers(conn);
                    case 4 -> viewAllAccounts(conn);
                    case 5 -> {
                        System.out.println("Logging out...");
                        return;
                    }
                    case 6 -> deleteAccount(conn, sc);
                    default -> System.out.println("Invalid choice! Try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createUser(Connection conn, Scanner sc) {
        try {
            System.out.print("Enter Username: ");
            String uname = sc.nextLine().trim();
            System.out.print("Enter PIN: ");
            String pin = sc.nextLine().trim();

            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, uname);
            ps.setString(2, pin);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    long userId = keys.getLong(1);
                    System.out.println("✅ User created successfully! User ID = " + userId);
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Username already exists.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createAccount(Connection conn, Scanner sc) {
        try {
            System.out.print("Enter User ID: ");
            long userId = sc.nextLong();
            System.out.print("Enter Initial Balance: ");
            double balance = sc.nextDouble();
            sc.nextLine(); // consume newline

            // Verify user exists
            PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE id = ?");
            check.setLong(1, userId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ User ID not found.");
                return;
            }

            // Generate unique 10-digit account number
            long accountNumber;
            Random rnd = new Random();
            do {
                accountNumber = 1000000000L + (long)(rnd.nextDouble() * 9000000000L);
            } while (checkAccountExists(conn, accountNumber));

            // Insert account with generated accountNumber
            String sql = "INSERT INTO accounts (account_number, user_id, balance) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, accountNumber);
            ps.setLong(2, userId);
            ps.setDouble(3, balance);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Account created successfully! Generated Account Number: " + accountNumber);
            } else {
                System.out.println("❌ Account creation failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkAccountExists(Connection conn, long accountNumber) {
        try {
            String sql = "SELECT account_number FROM accounts WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if account already exists
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // assume exists if error
        }
    }

    private static void viewAllUsers(Connection conn) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users");
            System.out.println("\nID | Username | PIN");
            System.out.println("-------------------------");
            while (rs.next()) {
                System.out.printf("%d | %s | %s%n",
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void viewAllAccounts(Connection conn) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM accounts");
            System.out.println("\nAccount_No | User_ID | Balance");
            System.out.println("------------------------------");
            while (rs.next()) {
                System.out.printf("%d | %d | %.2f%n",
                        rs.getLong("account_number"),
                        rs.getLong("user_id"),
                        rs.getDouble("balance"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteAccount(Connection conn, Scanner sc) {
        try {
            System.out.print("Enter Account Number to delete: ");
            long accountNumber = sc.nextLong();
            sc.nextLine(); // consume newline

            // Check if account exists
            PreparedStatement check = conn.prepareStatement("SELECT account_number FROM accounts WHERE account_number = ?");
            check.setLong(1, accountNumber);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Account not found.");
                return;
            }

            // Delete related transactions first
            PreparedStatement deleteTxns = conn.prepareStatement("DELETE FROM transactions WHERE account_number = ?");
            deleteTxns.setLong(1, accountNumber);
            deleteTxns.executeUpdate();

            // Delete account
            PreparedStatement deleteAcc = conn.prepareStatement("DELETE FROM accounts WHERE account_number = ?");
            deleteAcc.setLong(1, accountNumber);
            int affected = deleteAcc.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Account deleted successfully!");
            } else {
                System.out.println("❌ Account deletion failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
