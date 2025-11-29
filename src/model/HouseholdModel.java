package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HouseholdModel {
    private int householdId;
    private String householdNo;
    private int familyNo;
    private String fullName;
    private String address;
    private double income;

    public HouseholdModel() {}

    public HouseholdModel(int householdId, String householdNo, int familyNo, String fullName, String address, double income) {
        this.householdId = householdId;
        this.householdNo = householdNo;
        this.familyNo = familyNo;
        this.fullName = fullName;
        this.address = address;
        this.income = income;
    }

    public static List<HouseholdModel> getAll() {
        List<HouseholdModel> list = new ArrayList<>();
        String sql = "SELECT household_id, household_no, family_no, full_name, address, income FROM households ORDER BY family_no";
        try (Connection conn = DbConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                HouseholdModel h = new HouseholdModel();
                h.setHouseholdId(rs.getInt("household_id"));
                h.setHouseholdNo(rs.getString("household_no"));
                h.setFamilyNo(rs.getInt("family_no"));
                h.setFullName(rs.getString("full_name"));
                h.setAddress(rs.getString("address"));
                h.setIncome(rs.getDouble("income"));
                list.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create() {
        String sql = "INSERT INTO households (family_no, full_name, address, income) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, this.familyNo);
            ps.setString(2, this.fullName);
            ps.setString(3, this.address);
            ps.setDouble(4, this.income);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.householdId = rs.getInt(1);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE households SET family_no=?, full_name=?, address=?, income=? WHERE household_id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.familyNo);
            ps.setString(2, this.fullName);
            ps.setString(3, this.address);
            ps.setDouble(4, this.income);
            ps.setInt(5, this.householdId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM households WHERE household_id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.householdId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Getters and Setters
    public int getHouseholdId() { return householdId; }
    public void setHouseholdId(int householdId) { this.householdId = householdId; }
    public String getHouseholdNo() { return householdNo; }
    public void setHouseholdNo(String householdNo) { this.householdNo = householdNo; }
    public int getFamilyNo() { return familyNo; }
    public void setFamilyNo(int familyNo) { this.familyNo = familyNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }
}
