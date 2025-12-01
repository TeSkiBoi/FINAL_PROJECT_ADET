package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Barangay Officials data operations
 * Handles all CRUD operations for barangay officials
 */
public class OfficialModel {
    
    /**
     * Data class representing a barangay official
     */
    public static class Official {
        private int id;
        private String positionTitle;
        private String fullName;
        private String imagePath;
        private int displayOrder;
        private String isActive;
        
        public Official(int id, String positionTitle, String fullName, String imagePath, 
                       int displayOrder, String isActive) {
            this.id = id;
            this.positionTitle = positionTitle;
            this.fullName = fullName;
            this.imagePath = imagePath;
            this.displayOrder = displayOrder;
            this.isActive = isActive;
        }
        
        // Getters
        public int getId() { return id; }
        public String getPositionTitle() { return positionTitle; }
        public String getFullName() { return fullName; }
        public String getImagePath() { return imagePath; }
        public int getDisplayOrder() { return displayOrder; }
        public String getIsActive() { return isActive; }
    }
    
    /**
     * Get all officials
     * @return List of Official objects
     */
    public static List<Official> getAllOfficials() {
        List<Official> officials = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT id, position_title, full_name, image_path, display_order, is_active " +
                        "FROM barangay_officials ORDER BY display_order";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                officials.add(new Official(
                    rs.getInt("id"),
                    rs.getString("position_title"),
                    rs.getString("full_name"),
                    rs.getString("image_path"),
                    rs.getInt("display_order"),
                    rs.getString("is_active")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("OfficialModel", "Error loading officials", e);
            throw new RuntimeException("Failed to load officials: " + e.getMessage(), e);
        }
        
        return officials;
    }
    
    /**
     * Get official by ID
     * @param id The official ID
     * @return Official object or null if not found
     */
    public static Official getOfficialById(int id) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM barangay_officials WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Official(
                    rs.getInt("id"),
                    rs.getString("position_title"),
                    rs.getString("full_name"),
                    rs.getString("image_path"),
                    rs.getInt("display_order"),
                    rs.getString("is_active")
                );
            }
        } catch (SQLException e) {
            util.Logger.logError("OfficialModel", "Error loading official by ID", e);
        }
        
        return null;
    }
    
    /**
     * Add new official
     * @param positionTitle Position title
     * @param fullName Full name
     * @param imagePath Image path
     * @param displayOrder Display order
     * @param isActive Active status (Yes/No)
     * @return true if successful
     */
    public static boolean addOfficial(String positionTitle, String fullName, String imagePath, 
                                     int displayOrder, String isActive) {
        // Validate input
        if (positionTitle == null || positionTitle.trim().isEmpty()) {
            util.Logger.logError("OfficialModel", "Cannot add official with empty position title", null);
            return false;
        }
        if (displayOrder < 0) {
            util.Logger.logError("OfficialModel", "Cannot add official with negative display order", null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO barangay_officials (position_title, full_name, image_path, " +
                        "display_order, is_active) VALUES (?,?,?,?,?)";
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, positionTitle.trim());
            ps.setString(2, fullName != null ? fullName.trim() : "");
            ps.setString(3, imagePath != null ? imagePath.trim() : "");
            ps.setInt(4, displayOrder);
            ps.setString(5, isActive);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                // Get generated official ID
                ResultSet generatedKeys = ps.getGeneratedKeys();
                String officialId = "";
                if (generatedKeys.next()) {
                    officialId = String.valueOf(generatedKeys.getInt(1));
                }
                util.Logger.logCRUDOperation("CREATE", "Official", officialId, 
                    "Position: " + positionTitle + ", Name: " + fullName);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("OfficialModel", "Error adding official: " + positionTitle, e);
            return false;
        }
    }
    
    /**
     * Update official
     * @param id Official ID
     * @param positionTitle Position title
     * @param fullName Full name
     * @param imagePath Image path
     * @param displayOrder Display order
     * @param isActive Active status
     * @return true if successful
     */
    public static boolean updateOfficial(int id, String positionTitle, String fullName, 
                                        String imagePath, int displayOrder, String isActive) {
        // Validate input
        if (id <= 0) {
            util.Logger.logError("OfficialModel", "Invalid official ID: " + id, null);
            return false;
        }
        if (positionTitle == null || positionTitle.trim().isEmpty()) {
            util.Logger.logError("OfficialModel", "Cannot update official with empty position title", null);
            return false;
        }
        if (displayOrder < 0) {
            util.Logger.logError("OfficialModel", "Cannot update official with negative display order", null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE barangay_officials SET position_title=?, full_name=?, " +
                        "image_path=?, display_order=?, is_active=? WHERE id=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, positionTitle.trim());
            ps.setString(2, fullName != null ? fullName.trim() : "");
            ps.setString(3, imagePath != null ? imagePath.trim() : "");
            ps.setInt(4, displayOrder);
            ps.setString(5, isActive);
            ps.setInt(6, id);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("UPDATE", "Official", String.valueOf(id), 
                    fullName + " - " + positionTitle);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("OfficialModel", "Error updating official: " + id, e);
            return false;
        }
    }
    
    /**
     * Delete official
     * @param id Official ID
     * @return true if successful
     */
    public static boolean deleteOfficial(int id) {
        if (id <= 0) {
            util.Logger.logError("OfficialModel", "Invalid official ID for deletion: " + id, null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM barangay_officials WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("DELETE", "Official", String.valueOf(id), "Successfully deleted");
                return true;
            } else {
                util.Logger.logError("OfficialModel", "No official found with ID: " + id, null);
                return false;
            }
        } catch (SQLException e) {
            util.Logger.logError("OfficialModel", "Error deleting official: " + id, e);
            return false;
        }
    }
}
