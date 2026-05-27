package com.infotact.fleet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDb {
    public static void main(String[] args) {
        String url = System.getenv().getOrDefault(
                "MYSQL_ADMIN_URL",
                "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"
        );
        String user = System.getenv().getOrDefault("MYSQL_ADMIN_USER", "root");
        String password = System.getenv().getOrDefault("MYSQL_ADMIN_PASSWORD", "root");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            System.out.println("Connected to MySQL. Ensuring database 'fleet_db' exists...");
            stmt.execute("CREATE DATABASE IF NOT EXISTS fleet_db");
            System.out.println("SUCCESS: Database 'fleet_db' is ready.");
        } catch (Exception e) {
            System.err.println("ERROR creating database: " + e.getMessage());
        }
    }
}
