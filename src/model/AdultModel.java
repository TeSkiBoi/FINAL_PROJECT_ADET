package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Adult data operations
 * Handles all database queries related to adults (residents 18-59)
 */
public class AdultModel {
    
    /**
     * Data class representing an adult record
     */
    public static class Adult {
        private int residentId;
        private String name;
        private int age;
        private String gender;
        private String contactNo;
        private String email;
        
        public Adult(int residentId, String name, int age, String gender, String contactNo, String email) {
            this.residentId = residentId;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.contactNo = contactNo;
            this.email = email;
        }
        
        public int getResidentId() { return residentId; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getGender() { return gender; }
        public String getContactNo() { return contactNo; }
        public String getEmail() { return email; }
    }
    
    /**
     * Get all adults (residents aged 18-59)
     * @return List of Adult objects
     */
    public static List<Adult> getAllAdults() {
        List<Adult> adults = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT resident_id, CONCAT(first_name, ' ', last_name) as name, " +
                        "age, gender, contact_no, email " +
                        "FROM residents WHERE age >= 18 AND age < 60 " +
                        "ORDER BY last_name, first_name";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                adults.add(new Adult(
                    rs.getInt("resident_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact_no"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("AdultModel", "Error loading adults", e);
            throw new RuntimeException("Failed to load adults: " + e.getMessage(), e);
        }
        
        return adults;
    }
    
    /**
     * Get total count of adults
     * @return Number of adults in the system
     */
    public static int getAdultsCount() {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM residents WHERE age >= 18 AND age < 60";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            util.Logger.logError("AdultModel", "Error getting adults count", e);
        }
        
        return 0;
    }
}
