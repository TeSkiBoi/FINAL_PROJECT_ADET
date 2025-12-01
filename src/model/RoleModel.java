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
     * Add new role
     * @param roleName Role name
     * @return true if successful
     */
    public static boolean addRole(String roleName) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO roles (role_name) VALUES (?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            
            int result = ps.executeUpdate();
            util.Logger.logCRUDOperation("CREATE", "Role", roleName, "Role name: " + roleName);
            
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
     * @return true if successful
     */
    public static boolean updateRole(String roleId, String roleName) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE roles SET role_name=? WHERE role_id=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            ps.setString(2, roleId);
            
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
