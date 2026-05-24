package com.infotact.fleet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "wms";
        String password = "wms";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            System.out.println("Connected to PostgreSQL system. Attempting to create database 'fleet_db'...");
            stmt.execute("CREATE DATABASE fleet_db");
            System.out.println("SUCCESS: Database 'fleet_db' created successfully!");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("already exists")) {
                System.out.println("SUCCESS: Database 'fleet_db' already exists!");
            } else {
                System.err.println("ERROR creating database: " + msg);
            }
        }
    }
}
