package model;

import java.sql.*;
import db.DbConnection;

/**
 * Model for dashboard statistics and counts
 */
public class DashboardModel {
    
    /**
     * Get total resident count
     */
    public static int getResidentCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM residents";
        
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
     * Get total household count
     */
    public static int getHouseholdCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM households";
        
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
     * Get total blotter count
     */
    public static int getBlotterCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM blotter_incidents";
        
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
     * Get total official count
     */
    public static int getOfficialCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM barangay_officials";
        
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}
