package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HouseholdModel {
    private int householdId;
    private int familyNo;
    private Integer householdHeadId; // Changed to household_head_id
    private String address;
    private double income;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public HouseholdModel() {}

    public HouseholdModel(int householdId, int familyNo, Integer householdHeadId, String address, double income) {
        this.householdId = householdId;
        this.familyNo = familyNo;
        this.householdHeadId = householdHeadId;
        this.address = address;
        this.income = income;
    }

    public static List<HouseholdModel> getAll() {
        List<HouseholdModel> list = new ArrayList<>();
        String sql = "SELECT household_id, family_no, household_head_id, address, income, created_at, updated_at FROM households ORDER BY family_no";
        try (Connection conn = DbConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                HouseholdModel h = new HouseholdModel();
                h.setHouseholdId(rs.getInt("household_id"));
                h.setFamilyNo(rs.getInt("family_no"));
                int headId = rs.getInt("household_head_id");
                h.setHouseholdHeadId(rs.wasNull() ? null : headId);
                h.setAddress(rs.getString("address"));
                h.setIncome(rs.getDouble("income"));
                h.setCreatedAt(rs.getTimestamp("created_at"));
                h.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create() {
        String sql = "INSERT INTO households (family_no, household_head_id, address, income) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, this.familyNo);
            if (this.householdHeadId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, this.householdHeadId);
            }
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
        String sql = "UPDATE households SET family_no=?, household_head_id=?, address=?, income=? WHERE household_id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.familyNo);
            if (this.householdHeadId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, this.householdHeadId);
            }
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
    public int getFamilyNo() { return familyNo; }
    public void setFamilyNo(int familyNo) { this.familyNo = familyNo; }
    public Integer getHouseholdHeadId() { return householdHeadId; }
    public void setHouseholdHeadId(Integer householdHeadId) { this.householdHeadId = householdHeadId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}