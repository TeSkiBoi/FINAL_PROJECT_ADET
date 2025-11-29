package model;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import crypto.PasswordHashing;
import db.DbConnection;

/**
 * Added a no-arg constructor that will obtain a Connection from DbConnection.
 * This keeps the existing constructor (Connection) intact for callers that
 * want to supply their own connection, and fixes IDE errors when callers
 * construct LoginModel without a Connection.
 */
public class LoginModel {

    private Connection connection;

    // No-arg constructor that acquires a connection from DbConnection
    public LoginModel() {
        try {
            this.connection = DbConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            this.connection = null;
        }
    }

    public LoginModel(Connection connection) {
        this.connection = connection;
    }

    /**
     * Authenticate user by username or email and password
     */
    public boolean login(String usernameOrEmail, String password) {
        String sql = "SELECT hashed_password, salt, role_id, status FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                String storedHash = rs.getString("hashed_password");
                String storedSalt = rs.getString("salt");

                if (!"Active".equalsIgnoreCase(status)) return false; // account inactive

                try {
                    // Only PBKDF2 verification
                    return PasswordHashing.verifyPassword(password, storedSalt, storedHash);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false; // user not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get role ID of a user
     */
    public String getRole(String usernameOrEmail) {
        String sql = "SELECT role_id FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper to check if a DB connection was successfully established
    public boolean hasConnection() {
        return this.connection != null;
    }
}