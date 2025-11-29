package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentModel {
    private int residentId;
    private int householdId;
    private String firstName;
    private String middleName;
    private String lastName;
    private Date birthDate;
    private int age;
    private String gender;
    private String contactNo;
    private String email;

    public ResidentModel() {}

    public ResidentModel(int residentId, int householdId, String firstName, String middleName, String lastName, Date birthDate, int age, String gender, String contactNo, String email) {
        this.residentId = residentId;
        this.householdId = householdId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.age = age;
        this.gender = gender;
        this.contactNo = contactNo;
        this.email = email;
    }

    // CRUD methods

    public static List<ResidentModel> getAll() {
        List<ResidentModel> list = new ArrayList<>();
        String sql = "SELECT resident_id, household_id, first_name, middle_name, last_name, birth_date, age, gender, contact_no, email FROM residents ORDER BY last_name, first_name";
        try (Connection conn = DbConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ResidentModel r = new ResidentModel();
                r.setResidentId(rs.getInt("resident_id"));
                r.setHouseholdId(rs.getInt("household_id"));
                r.setFirstName(rs.getString("first_name"));
                r.setMiddleName(rs.getString("middle_name"));
                r.setLastName(rs.getString("last_name"));
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
        String sql = "INSERT INTO residents (household_id, first_name, middle_name, last_name, birth_date, age, gender, contact_no, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, this.householdId);
            ps.setString(2, this.firstName);
            ps.setString(3, this.middleName);
            ps.setString(4, this.lastName);
            ps.setDate(5, this.birthDate);
            ps.setInt(6, this.age);
            ps.setString(7, this.gender);
            ps.setString(8, this.contactNo);
            ps.setString(9, this.email);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.residentId = rs.getInt(1);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE residents SET household_id=?, first_name=?, middle_name=?, last_name=?, birth_date=?, age=?, gender=?, contact_no=?, email=? WHERE resident_id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.householdId);
            ps.setString(2, this.firstName);
            ps.setString(3, this.middleName);
            ps.setString(4, this.lastName);
            ps.setDate(5, this.birthDate);
            ps.setInt(6, this.age);
            ps.setString(7, this.gender);
            ps.setString(8, this.contactNo);
            ps.setString(9, this.email);
            ps.setInt(10, this.residentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM residents WHERE resident_id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.residentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== Getters and setters =====

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }
    public int getHouseholdId() { return householdId; }
    public void setHouseholdId(int householdId) { this.householdId = householdId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
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