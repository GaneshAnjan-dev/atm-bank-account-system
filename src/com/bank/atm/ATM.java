package com.bank.atm;

import java.sql.*;
import java.util.Scanner;

public class ATM {
    private Connection conn;
    private int accountNumber;

    public ATM() throws Exception {
        conn = DBConnection.getConnection();
    }

    // User login
    public boolean login(int accountNumber, String pin) throws Exception {
        this.accountNumber = accountNumber;
        String sql = "SELECT * FROM accounts WHERE account_number=? AND user_id=(SELECT id FROM users WHERE password=?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, accountNumber);
        ps.setString(2, pin);

        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // Check balance
    public void checkBalance() throws Exception {
        String sql = "SELECT balance FROM accounts WHERE account_number=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, accountNumber);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println("Your Balance: ₹" + rs.getDouble("balance"));
        }
    }

    // Deposit
    public void deposit(double amount) throws Exception {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, amount);
        ps.setInt(2, accountNumber);
        ps.executeUpdate();
        System.out.println("₹" + amount + " deposited successfully.");
    }

    // Withdraw
    public void withdraw(double amount) throws Exception {
        String sql = "SELECT balance FROM accounts WHERE account_number=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, accountNumber);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            double balance = rs.getDouble("balance");
            if (balance >= amount) {
                sql = "UPDATE accounts SET balance = balance - ? WHERE account_number=?";
                ps = conn.prepareStatement(sql);
                ps.setDouble(1, amount);
                ps.setInt(2, accountNumber);
                ps.executeUpdate();
                System.out.println("₹" + amount + " withdrawn successfully.");
            } else {
                System.out.println("❌ Insufficient Balance!");
            }
        }
    }
}
