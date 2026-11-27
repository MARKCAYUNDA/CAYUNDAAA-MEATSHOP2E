package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String createUsersTable = """
    CREATE TABLE IF NOT EXISTS users (
        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
        full_name TEXT NOT NULL,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        role TEXT NOT NULL,
        status TEXT NOT NULL
    );
    """;

        String createActivityLogsTable = """
    CREATE TABLE IF NOT EXISTS activity_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        action_type TEXT NOT NULL,
        description TEXT NOT NULL,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );
    """;

        // Default Admin account para maka-login dayon ka inig run
        String seedDefaultAdmin = """
            INSERT OR IGNORE INTO users (user_id, full_name, username, password, role, status)
            VALUES (1, 'Mark Cayunda', 'admin', 'admin123', 'ADMIN', 'APPROVED');
            """;

        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createActivityLogsTable);
            stmt.execute(seedDefaultAdmin);
            System.out.println("-> Database initialized (MeatsRecords.db).");
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }
}