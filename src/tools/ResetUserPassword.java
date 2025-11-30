package tools;

import db.DbConnection;
import crypto.PasswordHashing;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

/**
 * Simple command-line utility to set a new password for a user.
 * Usage: java -cp bin;path\to\mysql-connector-java.jar tools.ResetUserPassword <username> <newPassword>
 * This will generate a random salt, PBKDF2-hash the password and update `users` table's salt and hashed_password columns.
 */
public class ResetUserPassword {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ResetUserPassword <username> <newPassword>");
            System.exit(2);
        }
        String username = args[0];
        String newPassword = args[1];

        try (Connection conn = DbConnection.getConnection()) {
            // Ensure salt column exists
            DatabaseMetaData md = conn.getMetaData();
            boolean hasSalt = false;
            try (ResultSet cols = md.getColumns(null, null, "users", "salt")) {
                if (cols != null && cols.next()) hasSalt = true;
            }

            if (!hasSalt) {
                System.err.println("ERROR: 'users' table does not have 'salt' column. Add it first:");
                System.err.println("ALTER TABLE users ADD COLUMN salt VARCHAR(255) DEFAULT NULL;");
                System.exit(3);
            }

            String salt = PasswordHashing.generateSalt();
            String hash = PasswordHashing.hashPassword(newPassword, salt);

            // store PBKDF2 hash into `password_hash` column (schema uses password_hash)
            String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE username = ? OR email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hash);
                ps.setString(2, salt);
                ps.setString(3, username);
                ps.setString(4, username);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("Password updated for user '" + username + "'.");
                    System.out.println("New salt: " + salt);
                } else {
                    System.err.println("No user found with username/email: " + username);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}