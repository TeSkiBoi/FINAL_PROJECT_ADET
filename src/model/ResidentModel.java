package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentModel {
    private int residentId;
    private Integer householdId; // Changed to Integer to allow null
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private Date birthDate;
    private int age;
    private String gender;
    private String contactNo;
    private String email;

    public ResidentModel() {}

    public ResidentModel(int residentId, Integer householdId, String firstName, String middleName, String lastName, String suffix, Date birthDate, int age, String gender, String contactNo, String email) {
        this.residentId = residentId;
        this.householdId = householdId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
        this.birthDate = birthDate;
        this.age = age;
        this.gender = gender;
        this.contactNo = contactNo;
        this.email = email;
    }

    // CRUD methods

    public static List<ResidentModel> getAll() {
        List<ResidentModel> list = new ArrayList<>();
        String sql = "SELECT resident_id, household_id, first_name, middle_name, last_name, suffix, birth_date, age, gender, contact_no, email FROM residents ORDER BY last_name, first_name";
        try (Connection conn = DbConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ResidentModel r = new ResidentModel();
                r.setResidentId(rs.getInt("resident_id"));
                // Handle nullable household_id
                int hId = rs.getInt("household_id");
                r.setHouseholdId(rs.wasNull() ? null : hId);
                r.setFirstName(rs.getString("first_name"));
                r.setMiddleName(rs.getString("middle_name"));
                r.setLastName(rs.getString("last_name"));
                r.setSuffix(rs.getString("suffix"));
                r.setBirthDate(rs.getDate("birth_date"));
                r.setAge(rs.getInt("age"));
                r.setGender(rs.getString("gender"));
                r.setContactNo(rs.getString("contact_no"));
                r.setEmail(rs.getString("email"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create() {
        // Validate input
        if (this.firstName == null || this.firstName.trim().isEmpty()) {
            util.Logger.logError("ResidentModel", "Cannot create resident with empty first name", null);
            return false;
        }
        if (this.lastName == null || this.lastName.trim().isEmpty()) {
            util.Logger.logError("ResidentModel", "Cannot create resident with empty last name", null);
            return false;
        }
        if (this.birthDate == null) {
            util.Logger.logError("ResidentModel", "Cannot create resident with null birth date", null);
            return false;
        }
        
        // Auto-calculate age from birthdate
        calculateAndSetAge();
        
        String sql = "INSERT INTO residents (household_id, first_name, middle_name, last_name, suffix, birth_date, age, gender, contact_no, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Handle nullable household_id
            if (this.householdId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, this.householdId);
            }
            ps.setString(2, this.firstName.trim());
            ps.setString(3, this.middleName != null ? this.middleName.trim() : "");
            ps.setString(4, this.lastName.trim());
            ps.setString(5, this.suffix != null ? this.suffix.trim() : "");
            ps.setDate(6, this.birthDate);
            ps.setInt(7, this.age);
            ps.setString(8, this.gender);
            ps.setString(9, this.contactNo != null ? this.contactNo.trim() : "");
            ps.setString(10, this.email != null ? this.email.trim() : "");
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.residentId = rs.getInt(1);
                        util.Logger.logCRUDOperation("CREATE", "Resident", String.valueOf(this.residentId), 
                            String.format("%s %s, Age: %d", this.firstName, this.lastName, this.age));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            util.Logger.logError("ResidentModel", "Error creating resident", e);
        }
        return false;
    }

    public boolean update() {
        // Validate input
        if (this.residentId <= 0) {
            util.Logger.logError("ResidentModel", "Cannot update resident with invalid ID", null);
            return false;
        }
        if (this.firstName == null || this.firstName.trim().isEmpty()) {
            util.Logger.logError("ResidentModel", "Cannot update resident with empty first name", null);
            return false;
        }
        if (this.lastName == null || this.lastName.trim().isEmpty()) {
            util.Logger.logError("ResidentModel", "Cannot update resident with empty last name", null);
            return false;
        }
        if (this.birthDate == null) {
            util.Logger.logError("ResidentModel", "Cannot update resident with null birth date", null);
            return false;
        }
        
        // Auto-calculate age from birthdate
        calculateAndSetAge();
        
        String sql = "UPDATE residents SET household_id=?, first_name=?, middle_name=?, last_name=?, suffix=?, birth_date=?, age=?, gender=?, contact_no=?, email=? WHERE resident_id=?";
        try (Connection conn = DbConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Handle nullable household_id
            if (this.householdId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, this.householdId);
            }
            ps.setString(2, this.firstName.trim());
            ps.setString(3, this.middleName != null ? this.middleName.trim() : "");
            ps.setString(4, this.lastName.trim());
            ps.setString(5, this.suffix != null ? this.suffix.trim() : "");
            ps.setDate(6, this.birthDate);
            ps.setInt(7, this.age);
            ps.setString(8, this.gender);
            ps.setString(9, this.contactNo != null ? this.contactNo.trim() : "");
            ps.setString(10, this.email != null ? this.email.trim() : "");
            ps.setInt(11, this.residentId);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                util.Logger.logCRUDOperation("UPDATE", "Resident", String.valueOf(this.residentId), 
                    String.format("%s %s, Age: %d", this.firstName, this.lastName, this.age));
                return true;
            }
            return false;
        } catch (SQLException e) {
            util.Logger.logError("ResidentModel", "Error updating resident", e);
        }
        return false;
    }

    public boolean delete() {
        if (this.residentId <= 0) {
            util.Logger.logError("ResidentModel", "Cannot delete resident with invalid ID", null);
            return false;
        }
        
        String sql = "DELETE FROM residents WHERE resident_id=?";
        try (Connection conn = DbConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.residentId);
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("DELETE", "Resident", String.valueOf(this.residentId), 
                    String.format("%s %s", this.firstName, this.lastName));
                return true;
            } else {
                util.Logger.logError("ResidentModel", "No resident found with ID: " + this.residentId, null);
                return false;
            }
        } catch (SQLException e) {
            util.Logger.logError("ResidentModel", "Error deleting resident", e);
        }
        return false;
    }

    /**
     * Calculate age from birthdate to current date
     * @param birthDate The birthdate
     * @return Calculated age in years
     */
    public static int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        
        java.util.Calendar birthCal = java.util.Calendar.getInstance();
        birthCal.setTime(birthDate);
        
        java.util.Calendar today = java.util.Calendar.getInstance();
        
        int age = today.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR);
        
        // Check if birthday hasn't occurred yet this year
        if (today.get(java.util.Calendar.MONTH) < birthCal.get(java.util.Calendar.MONTH) ||
            (today.get(java.util.Calendar.MONTH) == birthCal.get(java.util.Calendar.MONTH) &&
             today.get(java.util.Calendar.DAY_OF_MONTH) < birthCal.get(java.util.Calendar.DAY_OF_MONTH))) {
            age--;
        }
        
        return age < 0 ? 0 : age;
    }
    
    /**
     * Calculate and set age based on current birthdate
     */
    public void calculateAndSetAge() {
        this.age = calculateAge(this.birthDate);
    }

    /**
     * Get count of children (age < 18)
     */
    public static int getChildrenCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM residents WHERE age < 18";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get count of adults (age >= 18 and < 60)
     */
    public static int getAdultsCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM residents WHERE age >= 18 AND age < 60";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get count of seniors (age >= 60)
     */
    public static int getSeniorsCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM residents WHERE age >= 60";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // ===== Getters and setters =====

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }
    public Integer getHouseholdId() { return householdId; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}