
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean registerUser(String fullName, String username,
                                String password, String role) {

        String sql = "INSERT INTO users(full_name, username, password, role, status) " +
                "VALUES(?, ?, ?, ?, 'PENDING')";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role.toUpperCase());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return true;
        }
    }

    public User login(String username, String password) {

        String sql = "SELECT user_id, full_name, username, password, role, status " +
                "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }

        return null;
    }

    public List<User> getAllUsers() {

        String sql = "SELECT user_id, full_name, username, password, role, status " +
                "FROM users ORDER BY user_id";

        return queryUsers(sql, null);
    }

    public List<User> getPendingUsers() {

        String sql = "SELECT user_id, full_name, username, password, role, status " +
                "FROM users WHERE status = ? ORDER BY user_id";

        return queryUsers(sql, "PENDING");
    }

    public boolean approveUser(int userId) {

        String sql = "UPDATE users SET status = 'APPROVED' " +
                "WHERE user_id = ? AND status = 'PENDING'";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // DELIVERABLE 7: UPDATE USER ROLE AND STATUS
    // ==========================================

    public boolean updateUserRoleAndStatus(
            int userId, String newRole, String newStatus) {

        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private List<User> queryUsers(String sql, String statusParam) {

        List<User> users = new ArrayList<>();

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (statusParam != null) {
                pstmt.setString(1, statusParam);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }

        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {

        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("status")
        );
    }
}
