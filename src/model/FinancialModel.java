package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import db.DbConnection;

/**
 * Model for financial_transactions table operations
 */
public class FinancialModel {
    
    /**
     * Get all financial transactions
     */
    public static List<Map<String, Object>> getAllTransactions() throws SQLException {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id, transaction_date, transaction_type, category, " +
                    "amount, description, payment_method, payee_payer, reference_number " +
                    "FROM financial_transactions ORDER BY transaction_date DESC";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("transaction_id", rs.getInt("transaction_id"));
                transaction.put("transaction_date", rs.getDate("transaction_date"));
                transaction.put("transaction_type", rs.getString("transaction_type"));
                transaction.put("category", rs.getString("category"));
                transaction.put("amount", rs.getBigDecimal("amount"));
                transaction.put("description", rs.getString("description"));
                transaction.put("payment_method", rs.getString("payment_method"));
                transaction.put("payee_payer", rs.getString("payee_payer"));
                transaction.put("reference_number", rs.getString("reference_number"));
                transactions.add(transaction);
            }
        }
        return transactions;
    }
    
    /**
     * Search transactions by keyword and type filter
     */
    public static List<Map<String, Object>> searchTransactions(String keyword, String typeFilter) throws SQLException {
        List<Map<String, Object>> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT transaction_id, transaction_date, transaction_type, category, " +
            "amount, description, payment_method, payee_payer, reference_number FROM financial_transactions WHERE 1=1"
        );
        
        List<String> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (category LIKE ? OR description LIKE ? OR reference_number LIKE ? OR payee_payer LIKE ?)");
            String search = "%" + keyword + "%";
            params.add(search);
            params.add(search);
            params.add(search);
            params.add(search);
        }
        
        if (typeFilter != null && !typeFilter.equals("All")) {
            sql.append(" AND transaction_type = ?");
            params.add(typeFilter);
        }
        
        sql.append(" ORDER BY transaction_date DESC");
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("transaction_id", rs.getInt("transaction_id"));
                    transaction.put("transaction_date", rs.getDate("transaction_date"));
                    transaction.put("transaction_type", rs.getString("transaction_type"));
                    transaction.put("category", rs.getString("category"));
                    transaction.put("amount", rs.getBigDecimal("amount"));
                    transaction.put("description", rs.getString("description"));
                    transaction.put("payment_method", rs.getString("payment_method"));
                    transaction.put("payee_payer", rs.getString("payee_payer"));
                    transaction.put("reference_number", rs.getString("reference_number"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
    
    /**
     * Get transaction by ID
     */
    public static Map<String, Object> getTransactionById(int transactionId) throws SQLException {
        String sql = "SELECT * FROM financial_transactions WHERE transaction_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, transactionId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("transaction_id", rs.getInt("transaction_id"));
                    transaction.put("transaction_date", rs.getDate("transaction_date"));
                    transaction.put("transaction_type", rs.getString("transaction_type"));
                    transaction.put("category", rs.getString("category"));
                    transaction.put("amount", rs.getBigDecimal("amount"));
                    transaction.put("description", rs.getString("description"));
                    transaction.put("payment_method", rs.getString("payment_method"));
                    transaction.put("payee_payer", rs.getString("payee_payer"));
                    transaction.put("reference_number", rs.getString("reference_number"));
                    return transaction;
                }
            }
        }
        return null;
    }
    
    /**
     * Add new transaction
     */
    public static boolean addTransaction(Date transactionDate, String transactionType, String category,
                                         double amount, String description, String paymentMethod,
                                         String payeePayer, String referenceNumber) throws SQLException {
        String sql = "INSERT INTO financial_transactions (transaction_date, transaction_type, " +
                    "category, amount, description, payment_method, payee_payer, reference_number) " +
                    "VALUES (?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, transactionDate);
            ps.setString(2, transactionType);
            ps.setString(3, category);
            ps.setDouble(4, amount);
            ps.setString(5, description);
            ps.setString(6, paymentMethod);
            ps.setString(7, payeePayer);
            ps.setString(8, referenceNumber);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Update existing transaction
     */
    public static boolean updateTransaction(int transactionId, Date transactionDate, String transactionType,
                                           String category, double amount, String description, 
                                           String paymentMethod, String payeePayer, String referenceNumber) throws SQLException {
        String sql = "UPDATE financial_transactions SET transaction_date=?, transaction_type=?, " +
                    "category=?, amount=?, description=?, payment_method=?, payee_payer=?, reference_number=? " +
                    "WHERE transaction_id=?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, transactionDate);
            ps.setString(2, transactionType);
            ps.setString(3, category);
            ps.setDouble(4, amount);
            ps.setString(5, description);
            ps.setString(6, paymentMethod);
            ps.setString(7, payeePayer);
            ps.setString(8, referenceNumber);
            ps.setInt(9, transactionId);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete transaction
     */
    public static boolean deleteTransaction(int transactionId) throws SQLException {
        String sql = "DELETE FROM financial_transactions WHERE transaction_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        }
    }
}
