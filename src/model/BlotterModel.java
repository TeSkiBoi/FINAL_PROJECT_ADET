package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DbConnection;

/**
 * Model class for Blotter/Incident data operations
 * Handles all CRUD operations for blotter incidents
 */
public class BlotterModel {
    
    /**
     * Data class representing a blotter incident
     */
    public static class Incident {
        private int incidentId;
        private String caseNumber;
        private String incidentType;
        private Date incidentDate;
        private Time incidentTime;
        private String location;
        private String complainant;
        private String respondent;
        private String status;
        
        public Incident(int incidentId, String caseNumber, String incidentType, 
                       Date incidentDate, Time incidentTime, String location,
                       String complainant, String respondent, String status) {
            this.incidentId = incidentId;
            this.caseNumber = caseNumber;
            this.incidentType = incidentType;
            this.incidentDate = incidentDate;
            this.incidentTime = incidentTime;
            this.location = location;
            this.complainant = complainant;
            this.respondent = respondent;
            this.status = status;
        }
        
        // Getters
        public int getIncidentId() { return incidentId; }
        public String getCaseNumber() { return caseNumber; }
        public String getIncidentType() { return incidentType; }
        public Date getIncidentDate() { return incidentDate; }
        public Time getIncidentTime() { return incidentTime; }
        public String getLocation() { return location; }
        public String getComplainant() { return complainant; }
        public String getRespondent() { return respondent; }
        public String getStatus() { return status; }
    }
    
    /**
     * Get all incidents
     * @return List of Incident objects
     */
    public static List<Incident> getAllIncidents() {
        List<Incident> incidents = new ArrayList<>();
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT incident_id, case_number, incident_type, incident_date, " +
                        "incident_time, incident_location, complainant_name, respondent_name, incident_status " +
                        "FROM blotter_incidents ORDER BY incident_date DESC";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                incidents.add(new Incident(
                    rs.getInt("incident_id"),
                    rs.getString("case_number"),
                    rs.getString("incident_type"),
                    rs.getDate("incident_date"),
                    rs.getTime("incident_time"),
                    rs.getString("incident_location"),
                    rs.getString("complainant_name"),
                    rs.getString("respondent_name"),
                    rs.getString("incident_status")
                ));
            }
        } catch (SQLException e) {
            util.Logger.logError("BlotterModel", "Error loading incidents", e);
            throw new RuntimeException("Failed to load incidents: " + e.getMessage(), e);
        }
        
        return incidents;
    }
    
    /**
     * Get incident by ID
     * @param incidentId The incident ID
     * @return Incident object or null if not found
     */
    public static Incident getIncidentById(int incidentId) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM blotter_incidents WHERE incident_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, incidentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Incident(
                    rs.getInt("incident_id"),
                    rs.getString("case_number"),
                    rs.getString("incident_type"),
                    rs.getDate("incident_date"),
                    rs.getTime("incident_time"),
                    rs.getString("incident_location"),
                    rs.getString("complainant_name"),
                    rs.getString("respondent_name"),
                    rs.getString("incident_status")
                );
            }
        } catch (SQLException e) {
            util.Logger.logError("BlotterModel", "Error loading incident by ID", e);
        }
        
        return null;
    }
    
    /**
     * Add new incident
     * @param caseNumber Case number
     * @param type Incident type
     * @param date Incident date
     * @param time Incident time
     * @param location Location
     * @param complainant Complainant name
     * @param respondent Respondent name
     * @param status Status
     * @return true if successful
     */
    public static boolean addIncident(String caseNumber, String type, Date date, Time time,
                                     String location, String complainant, String respondent, String status) {
        System.out.println("DEBUG [BlotterModel.addIncident()]: Starting addIncident - caseNumber=" + caseNumber);
        
        // Validate input
        if (caseNumber == null || caseNumber.trim().isEmpty()) {
            System.out.println("DEBUG [BlotterModel.addIncident()]: VALIDATION FAILED - Empty case number");
            util.Logger.logError("BlotterModel", "Cannot add incident with empty case number", null);
            return false;
        }
        if (date == null) {
            System.out.println("DEBUG [BlotterModel.addIncident()]: VALIDATION FAILED - Null date");
            util.Logger.logError("BlotterModel", "Cannot add incident with null date", null);
            return false;
        }
        if (time == null) {
            System.out.println("DEBUG [BlotterModel.addIncident()]: VALIDATION FAILED - Null time");
            util.Logger.logError("BlotterModel", "Cannot add incident with null time", null);
            return false;
        }
        
        System.out.println("DEBUG [BlotterModel.addIncident()]: Validation passed");
        
        try (Connection conn = DbConnection.getConnection()) {
            System.out.println("DEBUG [BlotterModel.addIncident()]: Checking for duplicate case number");
            
            // Check for duplicate case number
            String checkSql = "SELECT COUNT(*) FROM blotter_incidents WHERE case_number = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, caseNumber.trim());
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.println("DEBUG [BlotterModel.addIncident()]: DUPLICATE FOUND - Case number exists: " + caseNumber);
                util.Logger.logError("BlotterModel", "Case number already exists: " + caseNumber, null);
                return false;
            }
            
            System.out.println("DEBUG [BlotterModel.addIncident()]: No duplicate, proceeding with insert");
            
            String sql = "INSERT INTO blotter_incidents (case_number, incident_type, incident_date, " +
                        "incident_time, incident_location, complainant_name, respondent_name, " +
                        "incident_description, incident_status) VALUES (?,?,?,?,?,?,?,?,?)";
            System.out.println("DEBUG [BlotterModel.addIncident()]: SQL: " + sql);
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, caseNumber.trim());
            ps.setString(2, type);
            ps.setDate(3, date);
            ps.setTime(4, time);
            ps.setString(5, location != null ? location.trim() : "");
            ps.setString(6, complainant != null ? complainant.trim() : "");
            ps.setString(7, respondent != null ? respondent.trim() : "");
            ps.setString(8, "");
            ps.setString(9, status);
            
            System.out.println("DEBUG [BlotterModel.addIncident()]: Executing insert...");
            int result = ps.executeUpdate();
            System.out.println("DEBUG [BlotterModel.addIncident()]: Rows affected: " + result);
            
            if (result > 0) {
                // Get generated incident ID
                ResultSet generatedKeys = ps.getGeneratedKeys();
                String incidentId = "";
                if (generatedKeys.next()) {
                    incidentId = String.valueOf(generatedKeys.getInt(1));
                    System.out.println("DEBUG [BlotterModel.addIncident()]: SUCCESS - Generated incident_id: " + incidentId);
                } else {
                    System.out.println("DEBUG [BlotterModel.addIncident()]: WARNING - Insert succeeded but no generated keys");
                }
                util.Logger.logCRUDOperation("CREATE", "Incident", incidentId, 
                    "Case: " + caseNumber + ", Type: " + type + ", Location: " + location);
                return true;
            }
            
            System.out.println("DEBUG [BlotterModel.addIncident()]: FAILURE - No rows affected");
            return false;
        } catch (SQLException e) {
            System.out.println("DEBUG [BlotterModel.addIncident()]: EXCEPTION - SQLException");
            System.out.println("DEBUG [BlotterModel.addIncident()]: Message: " + e.getMessage());
            System.out.println("DEBUG [BlotterModel.addIncident()]: SQLState: " + e.getSQLState());
            System.out.println("DEBUG [BlotterModel.addIncident()]: ErrorCode: " + e.getErrorCode());
            util.Logger.logError("BlotterModel", "Database error adding incident: " + caseNumber, e);
            System.err.println("SQL Error adding blotter incident '" + caseNumber + "': " + e.getMessage());
            System.err.println("Details - Type: " + type + ", Location: " + location);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update incident
     * @param incidentId Incident ID
     * @param caseNumber Case number
     * @param type Incident type
     * @param date Incident date
     * @param time Incident time
     * @param location Location
     * @param complainant Complainant name
     * @param respondent Respondent name
     * @param status Status
     * @return true if successful
     */
    public static boolean updateIncident(int incidentId, String caseNumber, String type, Date date, 
                                        Time time, String location, String complainant, 
                                        String respondent, String status) {
        // Validate input
        if (caseNumber == null || caseNumber.trim().isEmpty()) {
            util.Logger.logError("BlotterModel", "Cannot update incident with empty case number", null);
            return false;
        }
        if (date == null || time == null) {
            util.Logger.logError("BlotterModel", "Cannot update incident with null date/time", null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            // Check if another incident has this case number (excluding current incident)
            String checkSql = "SELECT COUNT(*) FROM blotter_incidents WHERE case_number = ? AND incident_id != ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, caseNumber.trim());
            checkPs.setInt(2, incidentId);
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                util.Logger.logError("BlotterModel", "Case number already exists: " + caseNumber, null);
                return false;
            }
            
            String sql = "UPDATE blotter_incidents SET case_number=?, incident_type=?, incident_date=?, " +
                        "incident_time=?, incident_location=?, complainant_name=?, respondent_name=?, " +
                        "incident_status=? WHERE incident_id=?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, caseNumber.trim());
            ps.setString(2, type);
            ps.setDate(3, date);
            ps.setTime(4, time);
            ps.setString(5, location != null ? location.trim() : "");
            ps.setString(6, complainant != null ? complainant.trim() : "");
            ps.setString(7, respondent != null ? respondent.trim() : "");
            ps.setString(8, status);
            ps.setInt(9, incidentId);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("UPDATE", "Incident", String.valueOf(incidentId), 
                    "Case: " + caseNumber + ", Type: " + type);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            util.Logger.logError("BlotterModel", "Database error updating incident: " + incidentId, e);
            System.err.println("SQL Error updating incident ID '" + incidentId + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete incident
     * @param incidentId Incident ID
     * @return true if successful
     */
    public static boolean deleteIncident(int incidentId) {
        if (incidentId <= 0) {
            util.Logger.logError("BlotterModel", "Invalid incident ID for deletion: " + incidentId, null);
            return false;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM blotter_incidents WHERE incident_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, incidentId);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                util.Logger.logCRUDOperation("DELETE", "Incident", String.valueOf(incidentId), "Successfully deleted");
                return true;
            } else {
                util.Logger.logError("BlotterModel", "No incident found with ID: " + incidentId, null);
                return false;
            }
        } catch (SQLException e) {
            util.Logger.logError("BlotterModel", "Database error deleting incident: " + incidentId, e);
            System.err.println("SQL Error deleting incident ID '" + incidentId + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
