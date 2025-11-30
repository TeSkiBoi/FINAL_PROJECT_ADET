package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import db.DbConnection;
import util.DateTimeFormatter;
import theme.Theme;

public class ActivityLogPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnClear;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);
        
        // Panel title
        JLabel titleLabel = new JLabel("📋 Activity Log");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(30);
        
        btnRefresh = new JButton("🔄 Refresh");
        btnClear = new JButton("🗑 Clear Old Logs");
        
        styleButton(btnRefresh);
        styleButton(btnClear);
        
        topPanel.add(lblSearch);
        topPanel.add(txtSearch);
        topPanel.add(btnRefresh);
        topPanel.add(btnClear);
        
        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table with username column
        model = new DefaultTableModel(
            new String[]{"Log ID", "Username", "User ID", "Action", "Time", "IP Address"}, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Log ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // User ID
        table.getColumnModel().getColumn(3).setPreferredWidth(200);  // Action
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Time
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // IP
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Event handlers
        btnRefresh.addActionListener(e -> loadLogs());
        btnClear.addActionListener(e -> clearOldLogs());
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });
        
        loadLogs();
    }
    
    private void styleButton(JButton b) {
        b.setBackground(Theme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void search() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadLogs() {
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            // Join with users table to get username
            String sql = "SELECT ul.log_id, ul.user_id, u.username, ul.action, ul.log_time, ul.ip_address " +
                        "FROM user_logs ul " +
                        "LEFT JOIN users u ON ul.user_id = u.user_id " +
                        "ORDER BY ul.log_time DESC LIMIT 500";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String username = rs.getString("username");
                if (username == null || username.isEmpty()) {
                    username = "Unknown";
                }
                
                Timestamp logTime = rs.getTimestamp("log_time");
                String formattedTime = DateTimeFormatter.formatDateTime(logTime);
                
                model.addRow(new Object[]{
                    rs.getInt("log_id"),
                    username,
                    rs.getString("user_id"),
                    rs.getString("action"),
                    formattedTime,
                    rs.getString("ip_address")
                });
            }
            
            if (!hasData) {
                model.addRow(new Object[]{"", "No activity logs found", "", "User actions will appear here", "", ""});
            }
        } catch (SQLException e) {
            util.Logger.logError("Loading activity logs", "Failed to load activity logs from database", e);
            JOptionPane.showMessageDialog(this, 
                "Error loading logs: " + e.getMessage(), 
                "DB Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will delete logs older than 30 days. Continue?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbConnection.getConnection()) {
                String sql = "DELETE FROM user_logs WHERE log_time < DATE_SUB(NOW(), INTERVAL 30 DAY)";
                Statement st = conn.createStatement();
                int deleted = st.executeUpdate(sql);
                
                util.Logger.logUserActivity("CLEAR_LOGS", 
                    String.format("Deleted %d old log entries", deleted));
                
                JOptionPane.showMessageDialog(this,
                    String.format("Deleted %d old log entries", deleted),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadLogs();
            } catch (SQLException e) {
                util.Logger.logError("Clearing old logs", "Failed to clear old logs", e);
                JOptionPane.showMessageDialog(this,
                    "Error clearing logs: " + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
