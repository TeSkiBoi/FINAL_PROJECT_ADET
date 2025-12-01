package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import db.DbConnection;

/**
 * Model for barangay_projects table operations
 */
public class ProjectModel {
    
    /**
     * Get all projects
     */
    public static List<Map<String, Object>> getAllProjects() throws SQLException {
        List<Map<String, Object>> projects = new ArrayList<>();
        String sql = "SELECT project_id, project_name, project_status, start_date, end_date, " +
                    "proponent, total_budget, progress_percentage FROM barangay_projects ORDER BY project_id";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> project = new HashMap<>();
                project.put("project_id", rs.getInt("project_id"));
                project.put("project_name", rs.getString("project_name"));
                project.put("project_status", rs.getString("project_status"));
                project.put("start_date", rs.getDate("start_date"));
                project.put("end_date", rs.getDate("end_date"));
                project.put("proponent", rs.getString("proponent"));
                project.put("total_budget", rs.getBigDecimal("total_budget"));
                project.put("progress_percentage", rs.getInt("progress_percentage"));
                projects.add(project);
            }
        }
        return projects;
    }
    
    /**
     * Search projects by keyword
     */
    public static List<Map<String, Object>> searchProjects(String keyword) throws SQLException {
        List<Map<String, Object>> projects = new ArrayList<>();
        String sql = "SELECT project_id, project_name, project_status, start_date, end_date, " +
                    "proponent, total_budget, progress_percentage FROM barangay_projects " +
                    "WHERE project_name LIKE ? OR proponent LIKE ? ORDER BY project_id";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String search = "%" + keyword + "%";
            ps.setString(1, search);
            ps.setString(2, search);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> project = new HashMap<>();
                    project.put("project_id", rs.getInt("project_id"));
                    project.put("project_name", rs.getString("project_name"));
                    project.put("project_status", rs.getString("project_status"));
                    project.put("start_date", rs.getDate("start_date"));
                    project.put("end_date", rs.getDate("end_date"));
                    project.put("proponent", rs.getString("proponent"));
                    project.put("total_budget", rs.getBigDecimal("total_budget"));
                    project.put("progress_percentage", rs.getInt("progress_percentage"));
                    projects.add(project);
                }
            }
        }
        return projects;
    }
    
    /**
     * Get project by ID
     */
    public static Map<String, Object> getProjectById(int projectId) throws SQLException {
        String sql = "SELECT * FROM barangay_projects WHERE project_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, projectId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> project = new HashMap<>();
                    project.put("project_id", rs.getInt("project_id"));
                    project.put("project_name", rs.getString("project_name"));
                    project.put("project_description", rs.getString("project_description"));
                    project.put("project_status", rs.getString("project_status"));
                    project.put("start_date", rs.getDate("start_date"));
                    project.put("end_date", rs.getDate("end_date"));
                    project.put("proponent", rs.getString("proponent"));
                    project.put("total_budget", rs.getBigDecimal("total_budget"));
                    project.put("budget_utilized", rs.getBigDecimal("budget_utilized"));
                    project.put("progress_percentage", rs.getInt("progress_percentage"));
                    project.put("remarks", rs.getString("remarks"));
                    return project;
                }
            }
        }
        return null;
    }
    
    /**
     * Add new project
     */
    public static boolean addProject(String projectName, String projectDescription, String projectStatus,
                                     Date startDate, Date endDate, String proponent, 
                                     double totalBudget, double budgetUtilized, 
                                     int progressPercentage, String remarks) throws SQLException {
        String sql = "INSERT INTO barangay_projects (project_name, project_description, project_status, " +
                    "start_date, end_date, proponent, total_budget, budget_utilized, progress_percentage, remarks) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, projectName);
            ps.setString(2, projectDescription);
            ps.setString(3, projectStatus);
            ps.setDate(4, startDate);
            ps.setDate(5, endDate);
            ps.setString(6, proponent);
            ps.setDouble(7, totalBudget);
            ps.setDouble(8, budgetUtilized);
            ps.setInt(9, progressPercentage);
            ps.setString(10, remarks);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Update existing project
     */
    public static boolean updateProject(int projectId, String projectName, String projectDescription, 
                                        String projectStatus, Date startDate, Date endDate, 
                                        String proponent, double totalBudget, 
                                        int progressPercentage, String remarks) throws SQLException {
        String sql = "UPDATE barangay_projects SET project_name=?, project_description=?, project_status=?, " +
                    "start_date=?, end_date=?, proponent=?, total_budget=?, progress_percentage=?, remarks=? " +
                    "WHERE project_id=?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, projectName);
            ps.setString(2, projectDescription);
            ps.setString(3, projectStatus);
            ps.setDate(4, startDate);
            ps.setDate(5, endDate);
            ps.setString(6, proponent);
            ps.setDouble(7, totalBudget);
            ps.setInt(8, progressPercentage);
            ps.setString(9, remarks);
            ps.setInt(10, projectId);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete project
     */
    public static boolean deleteProject(int projectId) throws SQLException {
        String sql = "DELETE FROM barangay_projects WHERE project_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, projectId);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Get project count
     */
    public static int getProjectCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM barangay_projects";
        
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
     * Get project count by category
     */
    public static Map<String, Integer> getProjectCountByCategory() throws SQLException {
        Map<String, Integer> categoryCounts = new HashMap<>();
        String sql = "SELECT project_category AS category, COUNT(*) as count " +
                    "FROM barangay_projects GROUP BY project_category";
        
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categoryCounts.put(rs.getString("category"), rs.getInt("count"));
            }
        }
        return categoryCounts;
    }
}
