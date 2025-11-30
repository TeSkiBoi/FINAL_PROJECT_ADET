package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserModel {
    private Connection conn;

    public UserModel() {
        try {
            this.conn = DbConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            this.conn = null;
        }
    }

    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        if (conn == null) return null;
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("hashed_password"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("role_id"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean logUserActivity(String userId, String action, String ipAddress) {
        if (conn == null) return false;
        String sql = "INSERT INTO user_logs (user_id, action, ip_address) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, ipAddress);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getRoleName(String roleId) {
        if (conn == null) return null;
        String sql = "SELECT role_name FROM roles WHERE role_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("role_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String[]> getRecentLogs(int limit) {
        List<String[]> out = new ArrayList<>();
        if (conn == null) return out;
        String sql = "SELECT log_id, user_id, action, log_time, ip_address FROM user_logs ORDER BY log_time DESC LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                out.add(new String[]{
                    String.valueOf(rs.getInt("log_id")),
                    rs.getString("user_id"),
                    rs.getString("action"),
                    rs.getString("log_time"),
                    rs.getString("ip_address")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}