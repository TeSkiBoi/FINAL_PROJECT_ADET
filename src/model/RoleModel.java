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
        private String description;
        private String permissions;
        
        public Role(String roleId, String roleName, String description, String permissions) {
            this.roleId = roleId;
            this.roleName = roleName;
            this.description = description;
            this.permissions = permissions;
        }
        
        // Getters
        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
        public String getDescription() { return description; }
        public String getPermissions() { return permissions; }
    }
    
    /**
     * Get all roles
     * @return List of Role objects
     */
    public static List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT role_id, role_name, description, permissions " +
                        "FROM roles ORDER BY role_id";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                roles.add(new Role(
                    rs.getString("role_id"),
                    rs.getString("role_name"),
                    rs.getString("description"),
                    rs.getString("permissions")
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
                    rs.getString("role_name"),
                    rs.getString("description"),
                    rs.getString("permissions")
                );
            }
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error loading role by ID", e);
        }
        
        return null;
    }
    
    /**
     * Add new role
     * @param roleName Role name
     * @param description Description
     * @param permissions Permissions
     * @return true if successful
     */
    public static boolean addRole(String roleName, String description, String permissions) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO roles (role_name, description, permissions) VALUES (?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            ps.setString(2, description);
            ps.setString(3, permissions);
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("CREATE", "Role", roleName, 
                "Description: " + description);
            
            return result > 0;
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error adding role", e);
            return false;
        }
    }
    
    /**
     * Update role
     * @param roleId Role ID
     * @param roleName Role name
     * @param description Description
     * @param permissions Permissions
     * @return true if successful
     */
    public static boolean updateRole(String roleId, String roleName, String description, String permissions) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE roles SET role_name=?, description=?, permissions=? WHERE role_id=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            ps.setString(2, description);
            ps.setString(3, permissions);
            ps.setString(4, roleId);
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("UPDATE", "Role", roleId, roleName);
            
            return result > 0;
        } catch (SQLException e) {
            util.Logger.logError("RoleModel", "Error updating role", e);
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
            util.Logger.logError("RoleModel", "Error deleting role", e);
            return false;
        }
    }
}
