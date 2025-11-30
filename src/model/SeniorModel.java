package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Senior Citizen data operations
 * Handles all database queries related to senior citizens (residents 60+)
 */
public class SeniorModel {
    
    /**
     * Data class representing a senior citizen record
     */
    public static class Senior {
        private int residentId;
        private String name;
        private int age;
        private String gender;
        private String contactNo;
        
        public Senior(int residentId, String name, int age, String gender, String contactNo) {
            this.residentId = residentId;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.contactNo = contactNo;
        }
        
        public int getResidentId() { return residentId; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getGender() { return gender; }
        public String getContactNo() { return contactNo; }
    }
    
    /**
     * Get all senior citizens (residents aged 60+)
     * @return List of Senior objects
     */
    public static List<Senior> getAllSeniors() {
        List<Senior> seniors = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT resident_id, CONCAT(first_name, ' ', last_name) as name, " +
                        "age, gender, contact_no " +
                        "FROM residents WHERE age >= 60 " +
                        "ORDER BY age DESC";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                seniors.add(new Senior(
                    rs.getInt("resident_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact_no")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("SeniorModel", "Error loading seniors", e);
            throw new RuntimeException("Failed to load seniors: " + e.getMessage(), e);
        }
        
        return seniors;
    }
    
    /**
     * Get total count of senior citizens
     * @return Number of seniors in the system
     */
    public static int getSeniorsCount() {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM residents WHERE age >= 60";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            util.Logger.logError("SeniorModel", "Error getting seniors count", e);
        }
        
        return 0;
    }
}
