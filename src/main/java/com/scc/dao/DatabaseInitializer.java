package com.scc.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username  TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL,
                    role      TEXT NOT NULL,
                    status    TEXT NOT NULL DEFAULT 'PENDING'
                )
                """);
            seedDefaultAdmin(conn);
            System.out.println("-> Database initialized.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement ins = conn.createStatement()) {
                    ins.executeUpdate("INSERT INTO users(full_name, username, password, role, status) "
                            + "VALUES('Admin', 'admin@gmail.com', 'admin123', 'ADMIN', 'APPROVED')");
                }
                System.out.println("-> Default admin created (admin@gmail.com / admin123)");
            }
        }
    }
}