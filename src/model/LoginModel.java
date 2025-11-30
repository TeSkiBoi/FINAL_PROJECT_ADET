package model;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import crypto.PasswordHashing;
import db.DbConnection;

public class LoginModel {

    private Connection connection;

    public LoginModel() {
        try {
            this.connection = DbConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to acquire DB connection:");
            e.printStackTrace();
            this.connection = null;
        }
    }

    public LoginModel(Connection connection) {
        this.connection = connection;
    }

    // Helper: get set of column names for users table in current catalog
    private Set<String> getUsersTableColumns() throws SQLException {
        Set<String> cols = new HashSet<>();
        String catalog = safeGetCatalog(connection);
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'users'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, catalog);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cols.add(rs.getString(1));
                }
            }
        }
        return cols;
    }

    // Helper: return first existing candidate column name or null
    private String firstExistingColumnName(Set<String> existingCols, String... candidates) {
        for (String c : candidates) {
            if (existingCols.contains(c)) return c;
        }
        return null;
    }

    public boolean login(String usernameOrEmail, String password) {
        if (connection == null) {
            System.err.println("[ERROR] No database connection.");
            return false;
        }

        try {
            Set<String> cols = getUsersTableColumns();

            // Prefer these names in order
            String hashedCol = firstExistingColumnName(cols, "hashed_password", "password_hash", "passwordHash", "password");
            String saltCol = firstExistingColumnName(cols, "salt", "password_salt", "passwordSalt");

            if (hashedCol == null || saltCol == null) {
                System.err.println("[ERROR] Required password columns not found in users table. Found: " + cols);
                return false;
            }

            String sql = "SELECT user_id, " + hashedCol + " AS hashed_password, " + saltCol + " AS salt, role_id, status, fullname, email, username FROM users WHERE username = ? OR email = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, usernameOrEmail);
                stmt.setString(2, usernameOrEmail);

                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    System.err.println("[ERROR] No user found for: " + usernameOrEmail);
                    return false;
                }

                // Extract values
                String userId = rs.getString("user_id");
                String storedHash = rs.getString("hashed_password");
                String storedSalt = rs.getString("salt");
                String fullname = rs.getString("fullname");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String status = rs.getString("status");
                String roleId = rs.getString("role_id");

                if (!"active".equalsIgnoreCase(status)) {
                    System.err.println("[ERROR] Account is inactive.");
                    return false;
                }

                if (storedSalt == null || storedSalt.isEmpty() || storedHash == null || storedHash.isEmpty()) {
                    System.err.println("[ERROR] Missing hashed password or salt in DB.");
                    return false;
                }

                boolean ok = PasswordHashing.verifyPassword(password, storedSalt, storedHash);

                if (ok) {
                    // (Optional session logic)
                    UserModel um = new UserModel();
                    um.logUserActivity(userId, "User logged in", getLocalIpAddress());

                    return true;
                } else {
                    System.err.println("[ERROR] Incorrect password.");
                    return false;
                }

            } catch (SQLSyntaxErrorException se) {
                System.err.println("[SQL ERROR] Syntax error during user lookup. Catalog: " + safeGetCatalog(connection));
                se.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("[SQL ERROR] During login:");
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("[PBKDF2 ERROR] Failed to verify password:");
            e.printStackTrace();
            return false;
        }
    }

    public String getRole(String usernameOrEmail) {
        String sql = "SELECT role_id FROM users WHERE username = ? OR email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role_id");
                return role;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not retrieve role:");
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasConnection() {
        return this.connection != null;
    }

    private String getLocalIpAddress() {
        return "127.0.0.1";
    }

    public boolean userNeedsPasswordReset(String usernameOrEmail) {
        if (connection == null) return false;

        try {
            Set<String> cols = getUsersTableColumns();
            String hashedCol = firstExistingColumnName(cols, "hashed_password", "password_hash", "passwordHash", "password");
            String saltCol = firstExistingColumnName(cols, "salt", "password_salt", "passwordSalt");

            if (hashedCol == null || saltCol == null) {
                return false;
            }

            String sql = "SELECT " + hashedCol + " AS hashed_password, " + saltCol + " AS salt FROM users WHERE username = ? OR email = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, usernameOrEmail);
                stmt.setString(2, usernameOrEmail);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String h = rs.getString("hashed_password");
                    String s = rs.getString("salt");

                    return (h == null || h.isEmpty() || s == null || s.isEmpty());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setUserPassword(String usernameOrEmail, String newPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (connection == null) return false;

        try {
            Set<String> cols = getUsersTableColumns();
            String hashedCol = firstExistingColumnName(cols, "hashed_password", "password_hash", "passwordHash", "password");
            String saltCol = firstExistingColumnName(cols, "salt", "password_salt", "passwordSalt");

            if (hashedCol == null || saltCol == null) {
                System.err.println("[ERROR] Required password columns not found for update. Columns found: " + cols);
                return false;
            }

            String sqlSelect = "SELECT user_id FROM users WHERE username = ? OR email = ?";

            try (PreparedStatement ps = connection.prepareStatement(sqlSelect)) {
                ps.setString(1, usernameOrEmail);
                ps.setString(2, usernameOrEmail);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String salt = PasswordHashing.generateSalt();
                    String hash = PasswordHashing.hashPassword(newPassword, salt);

                    // Build update to set whichever columns exist (set both if both exist)
                    StringBuilder updateSql = new StringBuilder("UPDATE users SET ");
                    boolean first = true;
                    if (cols.contains("hashed_password") || cols.contains("password_hash") || cols.contains("passwordHash") || cols.contains("password")) {
                        updateSql.append(hashedCol + " = ?");
                        first = false;
                    }
                    if (!first) updateSql.append(", ");
                    updateSql.append(saltCol + " = ? WHERE user_id = ?");

                    try (PreparedStatement pu = connection.prepareStatement(updateSql.toString())) {
                        pu.setString(1, hash);
                        pu.setString(2, salt);
                        pu.setString(3, userId);

                        boolean updated = pu.executeUpdate() > 0;

                        return updated;
                    }
                } else {
                    System.err.println("[ERROR] User not found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update password:");
            e.printStackTrace();
        }

        return false;
    }

    private String safeGetCatalog(Connection conn) {
        try {
            return conn.getCatalog();
        } catch (SQLException e) {
            return "<unknown>";
        }
    }
}