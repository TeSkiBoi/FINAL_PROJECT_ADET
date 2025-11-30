package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.SessionManager;
import model.User;

/**
 * Centralized logging utility for the application.
 * Handles both user activity logs (user.log) and error logs (error.log).
 */
public class Logger {
    
    private static final String USER_LOG_FILE = "logs/user.log";
    private static final String ERROR_LOG_FILE = "logs/error.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        // Ensure logs directory exists
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }
    
    /**
     * Log user activity to user.log file
     * @param username The username performing the action
     * @param action The action being performed
     * @param details Additional details about the action
     */
    public static void logUserActivity(String username, String action, String details) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] USER: %s | ACTION: %s | DETAILS: %s%n", 
            timestamp, username, action, details);
        
        writeToFile(USER_LOG_FILE, logMessage);
        System.out.println(logMessage.trim()); // Also print to console
    }
    
    /**
     * Log user activity with current logged-in user
     * @param action The action being performed
     * @param details Additional details about the action
     */
    public static void logUserActivity(String action, String details) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        logUserActivity(username, action, details);
    }
    
    /**
     * Log error to error.log file
     * @param errorMessage The error message
     * @param exception The exception that occurred (can be null)
     */
    public static void logError(String errorMessage, Exception exception) {
        String timestamp = DATE_FORMAT.format(new Date());
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[%s] ERROR: %s%n", timestamp, errorMessage));
        
        if (exception != null) {
            logMessage.append(String.format("Exception: %s%n", exception.getClass().getName()));
            logMessage.append(String.format("Message: %s%n", exception.getMessage()));
            
            // Get stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            logMessage.append("Stack Trace:\n");
            logMessage.append(sw.toString());
            logMessage.append("\n");
        }
        
        writeToFile(ERROR_LOG_FILE, logMessage.toString());
        System.err.println(logMessage.toString()); // Also print to error console
    }
    
    /**
     * Log error with current user context
     * @param action The action being performed when error occurred
     * @param errorMessage The error message
     * @param exception The exception that occurred
     */
    public static void logError(String action, String errorMessage, Exception exception) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        
        String fullMessage = String.format("User: %s | Action: %s | Error: %s", 
            username, action, errorMessage);
        
        logError(fullMessage, exception);
    }
    
    /**
     * Log database error
     * @param query The SQL query that failed
     * @param exception The SQL exception
     */
    public static void logDatabaseError(String query, Exception exception) {
        String errorMessage = String.format("Database Error | Query: %s", query);
        logError(errorMessage, exception);
    }
    
    /**
     * Log authentication attempt
     * @param username The username attempting to login
     * @param success Whether the login was successful
     * @param ipAddress The IP address of the login attempt
     */
    public static void logAuthenticationAttempt(String username, boolean success, String ipAddress) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        String details = String.format("IP: %s", ipAddress);
        logUserActivity(username, action, details);
    }
    
    /**
     * Log CRUD operation
     * @param operation CREATE, READ, UPDATE, DELETE
     * @param entity The entity being operated on (e.g., "Household", "Resident")
     * @param recordId The ID of the record (can be null for READ operations)
     * @param details Additional details
     */
    public static void logCRUDOperation(String operation, String entity, String recordId, String details) {
        String action = String.format("%s_%s", operation, entity.toUpperCase());
        String fullDetails = (recordId != null) 
            ? String.format("ID: %s | %s", recordId, details)
            : details;
        logUserActivity(action, fullDetails);
    }
    
    /**
     * Write message to specified log file
     * @param filename The log file path
     * @param message The message to write
     */
    private static synchronized void writeToFile(String filename, String message) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + filename);
            e.printStackTrace();
        }
    }
    
    /**
     * Log info message (general information)
     * @param message The info message
     */
    public static void logInfo(String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] INFO: %s%n", timestamp, message);
        writeToFile(USER_LOG_FILE, logMessage);
        System.out.println(logMessage.trim());
    }
    
    /**
     * Log warning message
     * @param message The warning message
     */
    public static void logWarning(String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] WARNING: %s%n", timestamp, message);
        writeToFile(ERROR_LOG_FILE, logMessage);
        System.err.println(logMessage.trim());
    }
    
    /**
     * Clear log file contents (use with caution)
     * @param logType "user" or "error"
     */
    public static void clearLog(String logType) {
        String filename = logType.equalsIgnoreCase("user") ? USER_LOG_FILE : ERROR_LOG_FILE;
        try (FileWriter fw = new FileWriter(filename, false)) {
            fw.write(""); // Clear file
            logInfo(String.format("Log file cleared: %s", filename));
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + filename);
            e.printStackTrace();
        }
    }
}
