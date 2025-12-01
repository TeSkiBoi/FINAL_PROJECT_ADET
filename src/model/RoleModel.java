package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Role data operations
 * Handles all CRUD operations for roles
 */
public class RoleModel {
    
    /**
     * Data class representing a role
     */
    public static class Role {
        private String roleId;
        private String roleName;
        
        public Role(String roleId, String roleName) {
            this.roleId = roleId;
            this.roleName = roleName;

        }
        
        // Getters
        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
    }
    
    /**
     * Get all roles
     * @return List of Role objects
     */
    public static List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT role_id, role_name " +
                        "FROM roles ORDER BY role_id";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                roles.add(new Role(
                    rs.getString("role_id"),
                    rs.getString("role_name")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error loading roles", e);
            throw new RuntimeException("Failed to load roles: " + e.getMessage(), e);
        }
        
        return roles;
    }
    
    /**
     * Get role by ID
     * @param roleId The role ID
     * @return Role object or null if not found
     */
    public static Role getRoleById(String roleId) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM roles WHERE role_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Role(
                    rs.getString("role_id"),
                    rs.getString("role_name")
                );
            }
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error loading role by ID", e);
        }
        
        return null;
    }
    
    /**
     * Check if a role name already exists
     * @param roleName The role name to check
     * @return true if role name exists, false otherwise
     */
    public static boolean roleNameExists(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM roles WHERE role_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName.trim());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error checking role name existence", e);
        }
        
        return false;
    }
    
    /**
     * Add new role
     * @param roleName Role name
     * @return true if successful
     */
    public static boolean addRole(String roleName) {
        System.out.println("DEBUG [RoleModel.addRole()]: Starting - roleName=" + roleName);
        
        // Validate input
        if (roleName == null || roleName.trim().isEmpty()) {
            System.out.println("DEBUG [RoleModel.addRole()]: VALIDATION FAILED - Empty role name");
            util.Logger.logError("RoleModel", "Cannot add role with empty name", null);
            return false;
        }
        
        System.out.println("DEBUG [RoleModel.addRole()]: Checking for duplicate role name");
        
        // Check for duplicate role name
        if (roleNameExists(roleName)) {
            System.out.println("DEBUG [RoleModel.addRole()]: DUPLICATE FOUND - Role name exists: " + roleName);
            util.Logger.logError("RoleModel", "Role name already exists: " + roleName, null);
            return false;
        }
        
        System.out.println("DEBUG [RoleModel.addRole()]: No duplicate, proceeding with insert");
        
        try (Connection conn = DbConnection.getConnection()) {
            // Insert new role
            String sql = "INSERT INTO roles (role_name) VALUES (?)";
            System.out.println("DEBUG [RoleModel.addRole()]: SQL: " + sql);
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, roleName.trim());
            
            System.out.println("DEBUG [RoleModel.addRole()]: Executing insert...");
            int result = ps.executeUpdate();
            System.out.println("DEBUG [RoleModel.addRole()]: Rows affected: " + result);
            
            if (result > 0) {
                // Get the generated role_id
                ResultSet generatedKeys = ps.getGeneratedKeys();
                String roleId = "";
                if (generatedKeys.next()) {
                    roleId = String.valueOf(generatedKeys.getInt(1));
                    System.out.println("DEBUG [RoleModel.addRole()]: SUCCESS - Generated role_id: " + roleId);
                } else {
                    System.out.println("DEBUG [RoleModel.addRole()]: WARNING - Insert succeeded but no generated keys");
                }
                util.Logger.logCRUDOperation("CREATE", "Role", roleId, "Role name: " + roleName);
                return true;
            }
            
            System.out.println("DEBUG [RoleModel.addRole()]: FAILURE - No rows affected");
            System.out.println(sql + " " + result);
            return false;
        } catch (SQLException e) {
            System.out.println("DEBUG [RoleModel.addRole()]: EXCEPTION - SQLException");
            System.out.println("DEBUG [RoleModel.addRole()]: Message: " + e.getMessage());
            System.out.println("DEBUG [RoleModel.addRole()]: SQLState: " + e.getSQLState());
            System.out.println("DEBUG [RoleModel.addRole()]: ErrorCode: " + e.getErrorCode());
            util.Logger.logError("RoleModel", "Database error adding role: " + roleName, e);
            System.err.println("SQL Error adding role '" + roleName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update role
     * @param roleId Role ID
     * @param roleName Role name
     * @return true if successful
     */
    public static boolean updateRole(String roleId, String roleName) {
        // Validate input
        if (roleId == null || roleId.trim().isEmpty()) {
            util.Logger.logError("RoleModel", "Cannot update role with empty ID", null);
            return false;
        }
        
        if (roleName == null || roleName.trim().isEmpty()) {
            util.Logger.logError("RoleModel", "Cannot update role with empty name", null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            // Check if another role already has this name (excluding current role)
            String checkSql = "SELECT COUNT(*) FROM roles WHERE role_name = ? AND role_id != ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, roleName.trim());
            checkPs.setString(2, roleId);
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                util.Logger.logError("RoleModel", "Role name already exists: " + roleName, null);
                return false;
            }
            
            // Update role
            String sql = "UPDATE roles SET role_name=? WHERE role_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName.trim());
            ps.setString(2, roleId);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("UPDATE", "Role", roleId, "Role name: " + roleName);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Database error updating role: " + roleId, e);
            System.err.println("SQL Error updating role ID '" + roleId + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete role
     * @param roleId Role ID
     * @return true if successful
     */
    public static boolean deleteRole(String roleId) {
        // Prevent deleting system roles
        if ("1".equals(roleId) || "2".equals(roleId)) {
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM roles WHERE role_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleId);
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("DELETE", "Role", roleId, "");
            
            return result > 0;
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Database error deleting role: " + roleId, e);
            System.err.println("SQL Error deleting role ID '" + roleId + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
