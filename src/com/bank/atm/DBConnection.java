package com.bank.atm;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // Replace "atm_system" with the database you created in MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/atm_system?serverTimezone=UTC";
    private static final String USER = "root";        // your MySQL username
    private static final String PASSWORD = "12345";        // your MySQL password (empty if none)

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
