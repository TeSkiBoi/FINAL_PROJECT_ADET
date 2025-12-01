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
        // Validate input
        if (projectName == null || projectName.trim().isEmpty()) {
            util.Logger.logError("ProjectModel", "Cannot add project with empty name", null);
            throw new SQLException("Project name is required");
        }
        if (proponent == null || proponent.trim().isEmpty()) {
            util.Logger.logError("ProjectModel", "Cannot add project with empty proponent", null);
            throw new SQLException("Proponent is required");
        }
        if (startDate == null) {
            util.Logger.logError("ProjectModel", "Cannot add project with null start date", null);
            throw new SQLException("Start date is required");
        }
        if (totalBudget < 0) {
            util.Logger.logError("ProjectModel", "Cannot add project with negative budget", null);
            throw new SQLException("Total budget cannot be negative");
        }
        if (budgetUtilized < 0 || budgetUtilized > totalBudget) {
            util.Logger.logError("ProjectModel", "Invalid budget utilized amount", null);
            throw new SQLException("Budget utilized must be between 0 and total budget");
        }
        if (progressPercentage < 0 || progressPercentage > 100) {
            util.Logger.logError("ProjectModel", "Invalid progress percentage", null);
            throw new SQLException("Progress percentage must be between 0 and 100");
        }
        
        String sql = "INSERT INTO barangay_projects (project_name, project_description, project_status, " +
                    "start_date, end_date, proponent, total_budget, budget_utilized, progress_percentage, remarks) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, projectName.trim());
            ps.setString(2, projectDescription != null ? projectDescription.trim() : "");
            ps.setString(3, projectStatus);
            ps.setDate(4, startDate);
            ps.setDate(5, endDate);
            ps.setString(6, proponent.trim());
            ps.setDouble(7, totalBudget);
            ps.setDouble(8, budgetUtilized);
            ps.setInt(9, progressPercentage);
            ps.setString(10, remarks != null ? remarks.trim() : "");
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                // Get generated project ID
                ResultSet generatedKeys = ps.getGeneratedKeys();
                String projectId = "";
                if (generatedKeys.next()) {
                    projectId = String.valueOf(generatedKeys.getInt(1));
                }
                util.Logger.logCRUDOperation("CREATE", "Project", projectId, 
                    String.format("Name: %s, Proponent: %s, Budget: %.2f", projectName, proponent, totalBudget));
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("ProjectModel", "Error adding project: " + projectName, e);
            throw e;
        }
    }
    
    /**
     * Update existing project
     */
    public static boolean updateProject(int projectId, String projectName, String projectDescription, 
                                        String projectStatus, Date startDate, Date endDate, 
                                        String proponent, double totalBudget, 
                                        int progressPercentage, String remarks) throws SQLException {
        // Validate input
        if (projectId <= 0) {
            util.Logger.logError("ProjectModel", "Invalid project ID: " + projectId, null);
            throw new SQLException("Invalid project ID");
        }
        if (projectName == null || projectName.trim().isEmpty()) {
            util.Logger.logError("ProjectModel", "Cannot update project with empty name", null);
            throw new SQLException("Project name is required");
        }
        if (proponent == null || proponent.trim().isEmpty()) {
            util.Logger.logError("ProjectModel", "Cannot update project with empty proponent", null);
            throw new SQLException("Proponent is required");
        }
        if (startDate == null) {
            util.Logger.logError("ProjectModel", "Cannot update project with null start date", null);
            throw new SQLException("Start date is required");
        }
        if (totalBudget < 0) {
            util.Logger.logError("ProjectModel", "Cannot update project with negative budget", null);
            throw new SQLException("Total budget cannot be negative");
        }
        if (progressPercentage < 0 || progressPercentage > 100) {
            util.Logger.logError("ProjectModel", "Invalid progress percentage", null);
            throw new SQLException("Progress percentage must be between 0 and 100");
        }
        
        String sql = "UPDATE barangay_projects SET project_name=?, project_description=?, project_status=?, " +
                    "start_date=?, end_date=?, proponent=?, total_budget=?, progress_percentage=?, remarks=? " +
                    "WHERE project_id=?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, projectName.trim());
            ps.setString(2, projectDescription != null ? projectDescription.trim() : "");
            ps.setString(3, projectStatus);
            ps.setDate(4, startDate);
            ps.setDate(5, endDate);
            ps.setString(6, proponent.trim());
            ps.setDouble(7, totalBudget);
            ps.setInt(8, progressPercentage);
            ps.setString(9, remarks != null ? remarks.trim() : "");
            ps.setInt(10, projectId);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("UPDATE", "Project", String.valueOf(projectId), 
                    String.format("Name: %s, Status: %s, Progress: %d%%", projectName, projectStatus, progressPercentage));
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("ProjectModel", "Error updating project: " + projectId, e);
            throw e;
        }
    }
    
    /**
     * Delete project
     */
    public static boolean deleteProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            util.Logger.logError("ProjectModel", "Invalid project ID for deletion: " + projectId, null);
            throw new SQLException("Invalid project ID");
        }
        
        String sql = "DELETE FROM barangay_projects WHERE project_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, projectId);
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("DELETE", "Project", String.valueOf(projectId), "Successfully deleted");
                return true;
            } else {
                util.Logger.logError("ProjectModel", "No project found with ID: " + projectId, null);
                return false;
            }
        } catch (SQLException e) {
            util.Logger.logError("ProjectModel", "Error deleting project: " + projectId, e);
            throw e;
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
