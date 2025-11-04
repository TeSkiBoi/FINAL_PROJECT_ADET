package db;
import java.sql.*;

public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventorydb";
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
            System.out.println("✅ Database Connected Successfully!");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Failed: " + e.getMessage());
            throw e;
        }
    }
}