package com.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/meeting-scheduler";
    private static final String USER = "root";
    private static final String PASS = "student";

    static {
        try {
            // For MySQL Connector/J 8+ (and 9+), the class name is:
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // This should never happen if your JAR is on the classpath
            throw new RuntimeException("MySQL JDBC driver not found in classpath", e);
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    
 public static void main(String[] args) {
 
        
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to MySQL!");
            
            Statement stmt = conn.createStatement();
            
            ResultSet users = stmt.executeQuery("SELECT * FROM Users");
            System.out.println("\n Users:");
            while (users.next()) {
                String username = users.getString("username");
                String name = users.getString("email");
                String surname = users.getString("password_hash");
                System.out.printf("  - %s (%s %s)%n", username, name, surname);
            }
            
            conn.close();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }
    
}
