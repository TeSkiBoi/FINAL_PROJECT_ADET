package model;

import db.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Get all households with head name and member count
     * Returns a list of maps containing household details
     */
    public static List<Map<String, Object>> getAllWithDetails() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT h.household_id, h.family_no, " +
                    "CONCAT(COALESCE(r.first_name, ''), ' ', COALESCE(r.middle_name, ''), ' ', COALESCE(r.last_name, '')) AS head_name, " +
                    "h.address, h.income, " +
                    "(SELECT COUNT(*) FROM residents r2 WHERE r2.household_id = h.household_id) as member_count " +
                    "FROM households h " +
                    "LEFT JOIN residents r ON h.household_id = r.household_id AND r.resident_id = " +
                    "(SELECT MIN(r3.resident_id) FROM residents r3 WHERE r3.household_id = h.household_id) " +
                    "ORDER BY h.household_id";
        
        try (Connection conn = DbConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> household = new HashMap<>();
                household.put("household_id", rs.getInt("household_id"));
                household.put("family_no", rs.getInt("family_no"));
                
                String headName = rs.getString("head_name");
                if (headName == null || headName.trim().isEmpty()) {
                    headName = "Not assigned yet";
                } else {
                    headName = headName.trim();
                }
                household.put("head_name", headName);
                household.put("address", rs.getString("address"));
                household.put("income", rs.getDouble("income"));
                household.put("member_count", rs.getInt("member_count"));
                
                list.add(household);
            }
        }
        return list;
    }

    /**
     * Get household by ID
     */
    public static HouseholdModel getById(int householdId) throws SQLException {
        String sql = "SELECT household_id, family_no, address, income FROM households WHERE household_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, householdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HouseholdModel h = new HouseholdModel();
                    h.setHouseholdId(rs.getInt("household_id"));
                    h.setFamilyNo(rs.getInt("family_no"));
                    h.setAddress(rs.getString("address"));
                    h.setIncome(rs.getDouble("income"));
                    return h;
                }
            }
        }
        return null;
    }

    /**
     * Get member count for a household
     */
    public static int getMemberCount(int householdId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM residents WHERE household_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, householdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    /**
     * Delete household and all its members
     */
    public static boolean deleteHouseholdAndMembers(int householdId) throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete all residents first
                PreparedStatement ps = conn.prepareStatement("DELETE FROM residents WHERE household_id = ?");
                ps.setInt(1, householdId);
                ps.executeUpdate();
                
                // Then delete household
                ps = conn.prepareStatement("DELETE FROM households WHERE household_id = ?");
                ps.setInt(1, householdId);
                ps.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
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