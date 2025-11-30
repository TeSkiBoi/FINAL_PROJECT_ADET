package util;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;

/**
 * Utility class for displaying user-friendly error messages
 * Converts technical errors into human-readable messages
 */
public class ErrorHandler {
    
    /**
     * Show user-friendly error message based on exception type
     */
    public static void showError(Component parent, String action, Exception e) {
        String userMessage = getUserFriendlyMessage(action, e);
        
        // Log the technical error
        Logger.logError(action, e.getMessage(), e);
        
        // Show user-friendly message
        JOptionPane.showMessageDialog(parent, 
            userMessage, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show simple error message
     */
    public static void showError(Component parent, String message) {
        Logger.logWarning("User error: " + message);
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show warning message
     */
    public static void showWarning(Component parent, String message) {
        Logger.logInfo("User warning: " + message);
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Warning", 
            JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show info message
     */
    public static void showInfo(Component parent, String message) {
        Logger.logInfo("User info: " + message);
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Information", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show success message
     */
    public static void showSuccess(Component parent, String message) {
        Logger.logInfo("Success: " + message);
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Convert technical exception to user-friendly message
     */
    private static String getUserFriendlyMessage(String action, Exception e) {
        if (e instanceof SQLException) {
            return handleSQLException(action, (SQLException) e);
        } else if (e instanceof NumberFormatException) {
            return "Please enter valid numbers in all numeric fields.\n" +
                   "Check that Family No, Age, and Income contain only digits.";
        } else if (e instanceof IllegalArgumentException) {
            return "Invalid input: " + e.getMessage() + "\n" +
                   "Please check your entries and try again.";
        } else if (e instanceof NullPointerException) {
            return "An unexpected error occurred while " + action + ".\n" +
                   "Some required information may be missing.\n" +
                   "Please try again or contact support if the problem persists.";
        }
        
        // Generic error message
        return "Unable to complete the operation: " + action + "\n" +
               "Reason: " + getSimplifiedMessage(e.getMessage()) + "\n\n" +
               "Please try again or contact support if the problem persists.";
    }
    
    /**
     * Handle SQL exceptions with user-friendly messages
     */
    private static String handleSQLException(String action, SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
        String message = e.getMessage().toLowerCase();
        
        // Connection errors
        if (message.contains("connection") || message.contains("communications link failure")) {
            return "Cannot connect to the database.\n" +
                   "Please check:\n" +
                   "• Is the database server running?\n" +
                   "• Is your network connection active?\n" +
                   "• Contact your system administrator if the problem persists.";
        }
        
        // Table doesn't exist
        if (message.contains("table") && (message.contains("doesn't exist") || message.contains("not found"))) {
            return "The database table is missing or not properly set up.\n" +
                   "Please contact your system administrator to set up the database.";
        }
        
        // Duplicate entry (unique constraint violation)
        if (sqlState != null && sqlState.startsWith("23") || message.contains("duplicate")) {
            return "This record already exists in the database.\n" +
                   "Please check for duplicate entries and try again.";
        }
        
        // Foreign key constraint violation
        if (message.contains("foreign key constraint") || message.contains("cannot delete")) {
            return "Cannot delete this record because it is being used by other records.\n" +
                   "Please remove related records first.";
        }
        
        // Access denied
        if (message.contains("access denied") || message.contains("permission")) {
            return "Access denied to the database.\n" +
                   "Please contact your system administrator to verify your permissions.";
        }
        
        // Column not found
        if (message.contains("unknown column") || message.contains("column") && message.contains("not found")) {
            return "The database structure may be outdated.\n" +
                   "Please contact your system administrator to update the database.";
        }
        
        // Data too long
        if (message.contains("data too long") || message.contains("too many characters")) {
            return "The information you entered is too long.\n" +
                   "Please shorten your text and try again.";
        }
        
        // Generic SQL error
        return "Database error while " + action + ".\n" +
               "This might be a temporary issue.\n" +
               "Please try again in a moment.\n\n" +
               "If the problem persists, contact support with error code: " + errorCode;
    }
    
    /**
     * Simplify technical error messages for users
     */
    private static String getSimplifiedMessage(String technicalMessage) {
        if (technicalMessage == null || technicalMessage.isEmpty()) {
            return "Unknown error";
        }
        
        // Remove package names and stack trace info
        technicalMessage = technicalMessage.replaceAll("java\\.\\w+\\.\\w+:", "");
        technicalMessage = technicalMessage.replaceAll("at .*\\(.*\\)", "");
        
        // Truncate if too long
        if (technicalMessage.length() > 150) {
            technicalMessage = technicalMessage.substring(0, 147) + "...";
        }
        
        return technicalMessage.trim();
    }
    
    /**
     * Show confirmation dialog with user-friendly message
     */
    public static boolean confirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, 
            message, 
            title, 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show validation error for required fields
     */
    public static void showValidationError(Component parent, String fieldName) {
        showError(parent, 
            "Please fill in the required field: " + fieldName + "\n" +
            "This field cannot be empty.");
    }
    
    /**
     * Show validation error for invalid format
     */
    public static void showFormatError(Component parent, String fieldName, String expectedFormat) {
        showError(parent, 
            "Invalid format in field: " + fieldName + "\n" +
            "Expected format: " + expectedFormat + "\n" +
            "Please correct and try again.");
    }
}
