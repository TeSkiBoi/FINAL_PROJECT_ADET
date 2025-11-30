package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Children data operations
 * Handles all database queries related to children (residents under 18)
 */
public class ChildrenModel {
    
    /**
     * Data class representing a child record
     */
    public static class Child {
        private int residentId;
        private String name;
        private int age;
        private String guardian;
        
        public Child(int residentId, String name, int age, String guardian) {
            this.residentId = residentId;
            this.name = name;
            this.age = age;
            this.guardian = guardian;
        }
        
        public int getResidentId() { return residentId; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getGuardian() { return guardian; }
    }
    
    /**
     * Get all children (residents under 18)
     * @return List of Child objects
     */
    public static List<Child> getAllChildren() {
        List<Child> children = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT r.resident_id, CONCAT(r.first_name, ' ', r.last_name) as name, r.age, " +
                        "(SELECT CONCAT(h.first_name, ' ', h.last_name) FROM residents h " +
                        "WHERE h.household_id = r.household_id AND h.age >= 18 LIMIT 1) as guardian " +
                        "FROM residents r WHERE r.age < 18 ORDER BY r.age";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                children.add(new Child(
                    rs.getInt("resident_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("guardian")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("ChildrenModel", "Error loading children", e);
            throw new RuntimeException("Failed to load children: " + e.getMessage(), e);
        }
        
        return children;
    }
    
    /**
     * Get total count of children
     * @return Number of children in the system
     */
    public static int getChildrenCount() {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM residents WHERE age < 18";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            util.Logger.logError("ChildrenModel", "Error getting children count", e);
        }
        
        return 0;
    }
}
