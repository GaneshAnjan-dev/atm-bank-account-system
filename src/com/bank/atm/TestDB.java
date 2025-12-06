package com.bank.atm;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✅ Database connected successfully!");
        } else {
            System.out.println("❌ Connection failed!");
        }
    }
}
