package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error recording activity log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY timestamp DESC, id DESC";
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving activity logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error filtering activity logs: " + e.getMessage());
        }
        return logs;
    }

    public boolean clearHistoricalLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.out.println("Error clearing activity logs: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}