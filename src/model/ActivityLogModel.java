package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Activity Log data operations
 * Handles all operations for user activity logs
 */
public class ActivityLogModel {
    
    /**
     * Data class representing an activity log entry
     */
    public static class ActivityLog {
        private int logId;
        private String username;
        private String userId;
        private String action;
        private Timestamp logTime;
        private String ipAddress;
        
        public ActivityLog(int logId, String username, String userId, String action, 
                          Timestamp logTime, String ipAddress) {
            this.logId = logId;
            this.username = username;
            this.userId = userId;
            this.action = action;
            this.logTime = logTime;
            this.ipAddress = ipAddress;
        }
        
        // Getters
        public int getLogId() { return logId; }
        public String getUsername() { return username; }
        public String getUserId() { return userId; }
        public String getAction() { return action; }
        public Timestamp getLogTime() { return logTime; }
        public String getIpAddress() { return ipAddress; }
    }
    
    /**
     * Get all activity logs (limited to recent 500)
     * @return List of ActivityLog objects
     */
    public static List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT ul.log_id, ul.user_id, u.username, ul.action, ul.log_time, ul.ip_address " +
                        "FROM user_logs ul " +
                        "LEFT JOIN users u ON ul.user_id = u.user_id " +
                        "ORDER BY ul.log_time DESC LIMIT 500";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                String username = rs.getString("username");
                if (username == null || username.isEmpty()) {
                    username = "Unknown";
                }
                
                logs.add(new ActivityLog(
                    rs.getInt("log_id"),
                    username,
                    rs.getString("user_id"),
                    rs.getString("action"),
                    rs.getTimestamp("log_time"),
                    rs.getString("ip_address")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("ActivityLogModel", "Error loading activity logs", e);
            throw new RuntimeException("Failed to load activity logs: " + e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * Clear old logs (older than 30 days)
     * @return Number of logs deleted
     */
    public static int clearOldLogs() {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM user_logs WHERE log_time < DATE_SUB(NOW(), INTERVAL 30 DAY)";
            Statement st = conn.createStatement();
            int deleted = st.executeUpdate(sql);
            
            util.Logger.logUserActivity("CLEAR_LOGS", 
                String.format("Deleted %d old log entries", deleted));
            
            return deleted;
        } catch (SQLException e) {
            util.Logger.logError("ActivityLogModel", "Error clearing old logs", e);
            return 0;
        }
    }
    
    /**
     * Get logs by user
     * @param userId User ID to filter by
     * @return List of ActivityLog objects for specified user
     */
    public static List<ActivityLog> getLogsByUser(String userId) {
        List<ActivityLog> logs = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT ul.log_id, ul.user_id, u.username, ul.action, ul.log_time, ul.ip_address " +
                        "FROM user_logs ul " +
                        "LEFT JOIN users u ON ul.user_id = u.user_id " +
                        "WHERE ul.user_id = ? " +
                        "ORDER BY ul.log_time DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString("username");
                if (username == null || username.isEmpty()) {
                    username = "Unknown";
                }
                
                logs.add(new ActivityLog(
                    rs.getInt("log_id"),
                    username,
                    rs.getString("user_id"),
                    rs.getString("action"),
                    rs.getTimestamp("log_time"),
                    rs.getString("ip_address")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("ActivityLogModel", "Error loading logs by user", e);
        }
        
        return logs;
    }
}
