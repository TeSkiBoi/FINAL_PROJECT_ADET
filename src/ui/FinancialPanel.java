package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.SessionManager;
import model.User;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import db.DbConnection;
import util.ErrorHandler;
import util.Logger;
import util.DateTimeFormatter;
import theme.Theme;

public class FinancialPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnAdd, btnEdit, btnDelete;
    private JTextField txtSearch;
    private JComboBox<String> cboFilterType;
    private TableRowSorter<DefaultTableModel> sorter;

    public FinancialPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);

        // Panel title
        JLabel titleLabel = new JLabel("💰 Financial Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Top panel with search and buttons
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        
        // Search components
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(20);
        
        // Filter by type
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setForeground(Theme.TEXT_PRIMARY);
        cboFilterType = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        
        btnRefresh = new JButton("🔄 Refresh");
        btnAdd = new JButton("➕ Add Transaction");
        btnEdit = new JButton("✏️ Edit");
        btnDelete = new JButton("🗑️ Delete");
        
        styleButton(btnRefresh);
        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        
        top.add(lblSearch);
        top.add(txtSearch);
        top.add(lblFilter);
        top.add(cboFilterType);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);

        // Check user permissions
        User current = SessionManager.getInstance().getCurrentUser();
        boolean canModify = false;
        if (current != null) {
            String role = current.getRoleId();
            canModify = "1".equals(role) || "2".equals(role);
        }
        btnAdd.setEnabled(canModify);
        btnEdit.setEnabled(canModify);
        btnDelete.setEnabled(canModify);

        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Table with sorter
        model = new DefaultTableModel(
            new String[]{"ID", "Date & Time", "Type", "Category", "Amount", "Description", "Method"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Date & Time
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Type
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Category
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Amount
        table.getColumnModel().getColumn(5).setPreferredWidth(200);  // Description
        table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Method
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event listeners
        btnRefresh.addActionListener(e -> loadTransactions());
        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) {
                ErrorHandler.showWarning(this, "Please select a transaction to edit.");
                return;
            }
            int rowModel = table.convertRowIndexToModel(r);
            openDialog((Integer) model.getValueAt(rowModel, 0));
        });
        btnDelete.addActionListener(e -> deleteSelected());
        
        // Search listener
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });
        
        // Filter listener
        cboFilterType.addActionListener(e -> search());

        loadTransactions();
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
        String filterType = (String) cboFilterType.getSelectedItem();
        
        RowFilter<DefaultTableModel, Object> rf = null;
        
        try {
            if (!text.isEmpty() && !"All".equals(filterType)) {
                // Both search and filter
                RowFilter<DefaultTableModel, Object> searchFilter = RowFilter.regexFilter("(?i)" + text);
                RowFilter<DefaultTableModel, Object> typeFilter = RowFilter.regexFilter("(?i)" + filterType, 2); // Column 2 is Type
                rf = RowFilter.andFilter(java.util.Arrays.asList(searchFilter, typeFilter));
            } else if (!text.isEmpty()) {
                // Search only
                rf = RowFilter.regexFilter("(?i)" + text);
            } else if (!"All".equals(filterType)) {
                // Filter only
                rf = RowFilter.regexFilter("(?i)" + filterType, 2); // Column 2 is Type
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        
        sorter.setRowFilter(rf);
    }

    private void loadTransactions() {
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT transaction_id, transaction_date, transaction_type, category, " +
                        "amount, description, payment_method " +
                        "FROM financial_transactions " +
                        "ORDER BY transaction_date DESC, transaction_id DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("transaction_date");
                String formattedDateTime = timestamp != null ? 
                    DateTimeFormatter.formatDateTime12H(timestamp) : "N/A";
                
                model.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    formattedDateTime,
                    rs.getString("transaction_type"),
                    rs.getString("category"),
                    String.format("₱%.2f", rs.getDouble("amount")),
                    rs.getString("description"),
                    rs.getString("payment_method")
                });
            }
        } catch (SQLException e) {
            ErrorHandler.showError(this, "loading financial transactions", e);
        }
    }

    private void openDialog(Integer id) {
        boolean isEdit = id != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Transaction" : "Add Transaction", true);
        dlg.setSize(500, 450);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Date Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinDate, "yyyy-MM-dd");
        spinDate.setEditor(dateEditor);
        spinDate.setValue(new java.util.Date());

        // Hour Spinner (1-12)
        SpinnerNumberModel hourModel = new SpinnerNumberModel(12, 1, 12, 1);
        JSpinner spinHour = new JSpinner(hourModel);

        // Minute Spinner (0-59)
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        JSpinner spinMinute = new JSpinner(minuteModel);
        JSpinner.NumberEditor minuteEditor = new JSpinner.NumberEditor(spinMinute, "00");
        spinMinute.setEditor(minuteEditor);

        // AM/PM Spinner
        SpinnerListModel ampmModel = new SpinnerListModel(new String[]{"AM", "PM"});
        JSpinner spinAMPM = new JSpinner(ampmModel);

        // Time panel
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.add(spinHour);
        timePanel.add(new JLabel(":"));
        timePanel.add(spinMinute);
        timePanel.add(spinAMPM);

        // Other fields
        JComboBox<String> cboType = new JComboBox<>(new String[]{"Income", "Expense"});
        JTextField txtCategory = new JTextField();
        JTextField txtAmount = new JTextField();
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JComboBox<String> cboMethod = new JComboBox<>(
            new String[]{"Cash", "Check", "Bank Transfer", "Online Payment", "GCash", "PayMaya", "Other"});

        panel.add(new JLabel("Date:*"));
        panel.add(spinDate);
        panel.add(new JLabel("Time:*"));
        panel.add(timePanel);
        panel.add(new JLabel("Type:*"));
        panel.add(cboType);
        panel.add(new JLabel("Category:*"));
        panel.add(txtCategory);
        panel.add(new JLabel("Amount (₱):*"));
        panel.add(txtAmount);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(txtDesc));
        panel.add(new JLabel("Payment Method:*"));
        panel.add(cboMethod);

        // Load data if editing
        if (isEdit) {
            try (Connection conn = DbConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM financial_transactions WHERE transaction_id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("transaction_date");
                    if (timestamp != null) {
                        spinDate.setValue(new java.util.Date(timestamp.getTime()));
                        
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(timestamp);
                        
                        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
                        int minute = cal.get(Calendar.MINUTE);
                        
                        // Convert to 12-hour format
                        int hour12 = hour24 % 12;
                        if (hour12 == 0) hour12 = 12;
                        String ampm = hour24 < 12 ? "AM" : "PM";
                        
                        spinHour.setValue(hour12);
                        spinMinute.setValue(minute);
                        spinAMPM.setValue(ampm);
                    }
                    
                    cboType.setSelectedItem(rs.getString("transaction_type"));
                    txtCategory.setText(rs.getString("category"));
                    txtAmount.setText(String.valueOf(rs.getDouble("amount")));
                    txtDesc.setText(rs.getString("description"));
                    cboMethod.setSelectedItem(rs.getString("payment_method"));
                }
            } catch (SQLException e) {
                ErrorHandler.showError(dlg, "loading transaction details", e);
            }
        }

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnSave);
        styleButton(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(ae -> saveTransaction(dlg, id, isEdit, spinDate, spinHour, 
            spinMinute, spinAMPM, cboType, txtCategory, txtAmount, txtDesc, cboMethod));
        btnCancel.addActionListener(ae -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        
        dlg.getContentPane().add(wrapper);
        dlg.setVisible(true);
    }

    private void saveTransaction(JDialog dlg, Integer id, boolean isEdit, JSpinner spinDate,
            JSpinner spinHour, JSpinner spinMinute, JSpinner spinAMPM, JComboBox<String> cboType,
            JTextField txtCategory, JTextField txtAmount, JTextArea txtDesc, JComboBox<String> cboMethod) {
        
        try {
            // Validation
            String category = txtCategory.getText().trim();
            String amountStr = txtAmount.getText().trim();
            String description = txtDesc.getText().trim();
            
            if (category.isEmpty()) {
                ErrorHandler.showValidationError(dlg, "Category");
                txtCategory.requestFocus();
                return;
            }
            
            if (amountStr.isEmpty()) {
                ErrorHandler.showValidationError(dlg, "Amount");
                txtAmount.requestFocus();
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    ErrorHandler.showError(dlg, "Amount must be greater than zero.");
                    txtAmount.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                Logger.logError("Transaction validation", "Invalid amount format: " + amountStr, e);
                ErrorHandler.showFormatError(dlg, "Amount", "number (e.g., 1000, 2500.50)");
                txtAmount.requestFocus();
                return;
            }
            
            // Combine date and time
            java.util.Date dateValue = (java.util.Date) spinDate.getValue();
            int hour12 = (Integer) spinHour.getValue();
            int minute = (Integer) spinMinute.getValue();
            String ampm = (String) spinAMPM.getValue();
            
            // Convert to 24-hour format
            int hour24 = hour12;
            if ("PM".equals(ampm) && hour12 != 12) {
                hour24 = hour12 + 12;
            } else if ("AM".equals(ampm) && hour12 == 12) {
                hour24 = 0;
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateValue);
            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
            
            // Save to database
            try (Connection conn = DbConnection.getConnection()) {
                String sql;
                PreparedStatement ps;
                
                if (!isEdit) {
                    sql = "INSERT INTO financial_transactions (transaction_date, transaction_type, " +
                          "category, amount, description, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                } else {
                    sql = "UPDATE financial_transactions SET transaction_date=?, transaction_type=?, " +
                          "category=?, amount=?, description=?, payment_method=? WHERE transaction_id=?";
                    ps = conn.prepareStatement(sql);
                }
                
                ps.setTimestamp(1, timestamp);
                ps.setString(2, (String) cboType.getSelectedItem());
                ps.setString(3, category);
                ps.setDouble(4, amount);
                ps.setString(5, description);
                ps.setString(6, (String) cboMethod.getSelectedItem());
                
                if (isEdit) {
                    ps.setInt(7, id);
                }
                
                ps.executeUpdate();
                
                // Log operation
                String newId = id != null ? String.valueOf(id) : "new";
                if (!isEdit) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) newId = String.valueOf(rs.getInt(1));
                }
                
                Logger.logCRUDOperation(
                    isEdit ? "UPDATE" : "CREATE",
                    "Financial_Transaction",
                    newId,
                    String.format("Type: %s, Amount: %.2f, Category: %s", 
                        cboType.getSelectedItem(), amount, category)
                );
                
                ErrorHandler.showSuccess(dlg, 
                    isEdit ? "Transaction updated successfully!" : "Transaction added successfully!");
                dlg.dispose();
                loadTransactions();
                
            } catch (SQLException e) {
                ErrorHandler.showError(dlg, "saving transaction", e);
            }
            
        } catch (Exception e) {
            ErrorHandler.showError(dlg, "saving transaction", e);
        }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) {
            ErrorHandler.showWarning(this, "Please select a transaction to delete.");
            return;
        }
        
        int rowModel = table.convertRowIndexToModel(r);
        int id = (Integer) model.getValueAt(rowModel, 0);
        String dateTime = (String) model.getValueAt(rowModel, 1);
        String type = (String) model.getValueAt(rowModel, 2);
        
        if (!ErrorHandler.confirm(this,
                "Delete this transaction?\n\n" +
                "Date/Time: " + dateTime + "\n" +
                "Type: " + type,
                "Confirm Delete")) {
            return;
        }
        
        try (Connection conn = DbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM financial_transactions WHERE transaction_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            
            Logger.logCRUDOperation("DELETE", "Financial_Transaction", String.valueOf(id),
                String.format("Type: %s, DateTime: %s", type, dateTime));
            
            ErrorHandler.showSuccess(this, "Transaction deleted successfully!");
            loadTransactions();
            
        } catch (SQLException e) {
            ErrorHandler.showError(this, "deleting transaction", e);
        }
    }
}
