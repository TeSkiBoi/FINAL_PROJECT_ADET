package db;
import java.sql.*;

public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/barangay_biga_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // default for XAMPP

    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("❌ MySQL JDBC Driver not found!");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // Only log on first connection or after failures (to avoid log spam)
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Failed: " + e.getMessage());
            // Log database connection errors
            try {
                util.Logger.logError("Database Connection", 
                    String.format("Failed to connect to %s", URL), e);
            } catch (Exception logEx) {
                // Logging failed, but we still need to throw the original exception
                System.err.println("Failed to log database error: " + logEx.getMessage());
            }
            throw e;
        }
    }
}