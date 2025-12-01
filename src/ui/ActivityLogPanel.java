package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import model.ActivityLogModel;
import util.DateTimeFormatter;
import theme.Theme;

public class ActivityLogPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefresh, btnClear;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Activity Log"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        
        styleButton(btnRefresh);
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);
        
        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        btnClear = new JButton("🗑 Clear Old Logs");
        
        styleButton(btnClear);
        
        actionPanel.add(btnClear);
        
        // Top Panel (contains search and actions)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"Log ID", "Username", "User ID", "Action", "Time", "IP Address"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Log ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // User ID
        table.getColumnModel().getColumn(3).setPreferredWidth(200);  // Action
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Time
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // IP
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Activity Logs List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadLogs());
        btnClear.addActionListener(e -> clearOldLogs());

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
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
        tableModel.setRowCount(0);
        try {
            List<ActivityLogModel.ActivityLog> logs = ActivityLogModel.getAllLogs();

            if (logs.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No activity logs found", "", "User actions will appear here", "", ""});
            } else {
                for (ActivityLogModel.ActivityLog log : logs) {
                    String formattedTime = DateTimeFormatter.formatDateTime(log.getLogTime());

                    tableModel.addRow(new Object[]{
                        log.getLogId(),
                        log.getUsername(),
                        log.getUserId(),
                        log.getAction(),
                        formattedTime,
                        log.getIpAddress()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will delete logs older than 30 days. Continue?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int deleted = ActivityLogModel.clearOldLogs();

                JOptionPane.showMessageDialog(this,
                    String.format("Deleted %d old log entries", deleted),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                loadLogs();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error clearing logs: " + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
