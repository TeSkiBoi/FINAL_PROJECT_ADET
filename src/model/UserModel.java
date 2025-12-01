package model;

import db.DbConnection;
import crypto.PasswordHashing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
        
        // Only select fields needed for session (NO password for security!)
        String sql = "SELECT user_id, username, fullname, email, role_id, status " +
                     "FROM users WHERE username = ? OR email = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    null,  // No password hash in session (security!)
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
        if (conn == null) {
            util.Logger.logError("Log user activity", "Database connection is null", null);
            return false;
        }
        
        String sql = "INSERT INTO user_logs (user_id, action, ip_address) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, ipAddress);
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                // Also log to user.log file
                try {
                    // Get username for better logging
                    String username = getUsernameById(userId);
                    util.Logger.logUserActivity(username != null ? username : userId, action, "IP: " + ipAddress);
                } catch (Exception e) {
                    // Don't fail the database insert if file logging fails
                    System.err.println("Failed to write to user.log: " + e.getMessage());
                }
            }
            
            return success;
        } catch (SQLException e) {
            util.Logger.logDatabaseError(sql, e);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get username by user ID
     */
    private String getUsernameById(String userId) {
        if (conn == null) return null;
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
    
    /**
     * Data class for User display (no sensitive fields)
     */
    public static class UserDisplay {
        private String userId;
        private String username;
        private String fullname;
        private String email;
        private String roleName;  // NOT role_id
        private String status;
        
        public UserDisplay(String userId, String username, String fullname, String email, String roleName, String status) {
            this.userId = userId;
            this.username = username;
            this.fullname = fullname;
            this.email = email;
            this.roleName = roleName;
            this.status = status;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullname() { return fullname; }
        public String getEmail() { return email; }
        public String getRoleName() { return roleName; }
        public String getStatus() { return status; }
    }
    
    /**
     * Get all users with role names (NO password/salt/hash)
     */
    public static List<UserDisplay> getAllUsers() {
        List<UserDisplay> users = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT u.user_id, u.username, u.fullname, u.email, r.role_name, u.status " +
                        "FROM users u " +
                        "LEFT JOIN roles r ON u.role_id = r.role_id " +
                        "ORDER BY u.user_id";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                users.add(new UserDisplay(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("role_name"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("UserModel", "Error loading users", e);
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }
        
        return users;
    }
    
    /**
     * Add new user with password hashing
     */
    public static boolean addUser(String username, String password, String fullname, String email, String roleName, String status) {
        System.out.println("DEBUG [UserModel.addUser()]: Starting addUser - username=" + username + ", roleName=" + roleName);
        
        try (Connection conn = DbConnection.getConnection()) {
            System.out.println("DEBUG [UserModel.addUser()]: Connection obtained");
            
            // Get role_id from role_name
            String roleId = null;
            PreparedStatement psRole = conn.prepareStatement("SELECT role_id FROM roles WHERE role_name = ?");
            psRole.setString(1, roleName);
            ResultSet rsRole = psRole.executeQuery();
            if (rsRole.next()) {
                roleId = rsRole.getString("role_id");
                System.out.println("DEBUG [UserModel.addUser()]: Found role_id: " + roleId + " for role: " + roleName);
            } else {
                System.out.println("DEBUG [UserModel.addUser()]: FAILURE - Role not found: " + roleName);
            }
            
            if (roleId == null) {
                System.out.println("DEBUG [UserModel.addUser()]: FAILURE - roleId is null, returning false");
                util.Logger.logError("UserModel", "Role not found: " + roleName, null);
                return false;
            }
            
            System.out.println("DEBUG [UserModel.addUser()]: Generating salt and hashing password...");
            // Generate salt and hash password
            String salt = PasswordHashing.generateSalt();
            String hashedPassword = PasswordHashing.hashPassword(password, salt);
            System.out.println("DEBUG [UserModel.addUser()]: Password hashed successfully");
            
            String sql = "INSERT INTO users (username, hashed_password, salt, fullname, email, role_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            System.out.println("DEBUG [UserModel.addUser()]: SQL: " + sql);
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, salt);
            ps.setString(4, fullname);
            ps.setString(5, email);
            ps.setString(6, roleId);
            ps.setString(7, status);
            
            System.out.println("DEBUG [UserModel.addUser()]: Executing insert...");
            int result = ps.executeUpdate();
            System.out.println("DEBUG [UserModel.addUser()]: Rows affected: " + result);
            
            if (result > 0) {
                System.out.println("DEBUG [UserModel.addUser()]: SUCCESS - User added");
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        String userId = generatedKeys.getString(1);
                        System.out.println("DEBUG [UserModel.addUser()]: Generated user_id: " + userId);
                    }
                }
                util.Logger.logCRUDOperation("CREATE", "User", username, 
                    "Role: " + roleName + ", Email: " + email);
                System.out.println("✓ SUCCESS: User added successfully! Username: " + username + ", Role: " + roleName);
                return true;
            } else {
                System.out.println("DEBUG [UserModel.addUser()]: FAILURE - No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("DEBUG [UserModel.addUser()]: EXCEPTION - SQLException");
            System.out.println("DEBUG [UserModel.addUser()]: Message: " + e.getMessage());
            System.out.println("DEBUG [UserModel.addUser()]: SQLState: " + e.getSQLState());
            System.out.println("DEBUG [UserModel.addUser()]: ErrorCode: " + e.getErrorCode());
            util.Logger.logError("UserModel", "Database error adding user: " + username, e);
            System.err.println("SQL Error adding user '" + username + "': " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            System.err.println("Details - Username: " + username + ", Role: " + roleName + ", Email: " + (email != null ? email : "N/A"));
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("DEBUG [UserModel.addUser()]: EXCEPTION - Password hashing error");
            System.out.println("DEBUG [UserModel.addUser()]: Message: " + e.getMessage());
            util.Logger.logError("UserModel", "Password hashing error for user: " + username, e);
            System.err.println("Password hashing error for user '" + username + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update user (password optional - only updates if provided)
     */
    public static boolean updateUser(String userId, String username, String password, String fullname, String email, String roleName, String status) {
        try (Connection conn = DbConnection.getConnection()) {
            // Get role_id from role_name
            String roleId = null;
            PreparedStatement psRole = conn.prepareStatement("SELECT role_id FROM roles WHERE role_name = ?");
            psRole.setString(1, roleName);
            ResultSet rsRole = psRole.executeQuery();
            if (rsRole.next()) {
                roleId = rsRole.getString("role_id");
            }
            
            if (roleId == null) {
                return false;
            }
            
            String sql;
            PreparedStatement ps;
            
            // Update password only if provided
            if (password != null && !password.trim().isEmpty()) {
                String salt = PasswordHashing.generateSalt();
                String hashedPassword = PasswordHashing.hashPassword(password, salt);
                
                sql = "UPDATE users SET username=?, hashed_password=?, salt=?, fullname=?, email=?, role_id=?, status=? WHERE user_id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, hashedPassword);
                ps.setString(3, salt);
                ps.setString(4, fullname);
                ps.setString(5, email);
                ps.setString(6, roleId);
                ps.setString(7, status);
                ps.setString(8, userId);
            } else {
                sql = "UPDATE users SET username=?, fullname=?, email=?, role_id=?, status=? WHERE user_id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, fullname);
                ps.setString(3, email);
                ps.setString(4, roleId);
                ps.setString(5, status);
                ps.setString(6, userId);
            }
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("UPDATE", "User", userId, username);
            
            if (result > 0) {
                System.out.println("✓ SUCCESS: User updated successfully! Username: " + username + ", ID: " + userId);
            }
            
            return result > 0;
        } catch (SQLException e) {
            util.Logger.logError("UserModel", "Database error updating user: " + userId, e);
            System.err.println("SQL Error updating user ID '" + userId + "': " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            util.Logger.logError("UserModel", "Password hashing error updating user: " + userId, e);
            System.err.println("Password hashing error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete user
     */
    public static boolean deleteUser(String userId) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("DELETE", "User", userId, "");
            
            return result > 0;
        } catch (SQLException e) {
            util.Logger.logError("UserModel", "Database error deleting user: " + userId, e);
            System.err.println("SQL Error deleting user ID '" + userId + "': " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all role names for dropdown
     */
    public static List<String> getAllRoleNames() {
        List<String> roleNames = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT role_name FROM roles ORDER BY role_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                roleNames.add(rs.getString("role_name"));
            }
        } catch (SQLException e) {
            util.Logger.logError("UserModel", "Error loading role names", e);
        }
        
        return roleNames;
    }
    
    /**
     * Get count of active users
     */
    public static int getActiveUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users WHERE status = 'Active'";
        
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}
