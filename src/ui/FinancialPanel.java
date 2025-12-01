package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.FinancialModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import util.ErrorHandler;
import util.Logger;
import theme.Theme;

public class FinancialPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefresh, btnAdd, btnEdit, btnDelete;
    private JTextField txtSearch;
    private JComboBox<String> cboFilterType;
    private TableRowSorter<DefaultTableModel> sorter;

    public FinancialPanel() {
        setLayout(new BorderLayout(10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Financial Transaction"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        txtSearch = new JTextField(20);
        cboFilterType = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        btnRefresh = new JButton("🔄 Refresh");
        
        styleButton(btnRefresh);
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(cboFilterType);
        searchPanel.add(btnRefresh);
        
        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        btnAdd = new JButton("➕ Add Transaction");
        btnEdit = new JButton("✏️ Edit Transaction");
        btnDelete = new JButton("🗑️ Delete Transaction");
        
        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        // Top Panel (contains search and actions)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Date", "Type", "Category", "Amount", "Description", "Method"}, 0) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Type
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Category
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Amount
        table.getColumnModel().getColumn(5).setPreferredWidth(200);  // Description
        table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Method
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Financial Transactions List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadTransactions());
        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) {
                ErrorHandler.showWarning(this, "Please select a transaction to edit.");
                return;
            }
            int rowModel = table.convertRowIndexToModel(r);
            openDialog((Integer) tableModel.getValueAt(rowModel, 0));
        });
        btnDelete.addActionListener(e -> deleteSelected());

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
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> transactions = FinancialModel.getAllTransactions();

            for (Map<String, Object> transaction : transactions) {
                Date date = (Date) transaction.get("transaction_date");
                String formattedDate = date != null ? date.toString() : "N/A";

                tableModel.addRow(new Object[]{
                    transaction.get("transaction_id"),
                    formattedDate,
                    transaction.get("transaction_type"),
                    transaction.get("category"),
                    String.format("₱%.2f", transaction.get("amount")),
                    transaction.get("description"),
                    transaction.get("payment_method")
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

        // Other fields
        JComboBox<String> cboType = new JComboBox<>(new String[]{"Income", "Expense"});
        JTextField txtCategory = new JTextField();
        JTextField txtAmount = new JTextField();
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JComboBox<String> cboMethod = new JComboBox<>(
            new String[]{"Cash", "Check", "Bank Transfer", "Online Payment", "Other"});
        JTextField txtPayeePayer = new JTextField();
        JTextField txtReferenceNo = new JTextField();

        panel.add(new JLabel("Date: *"));
        panel.add(spinDate);
        panel.add(new JLabel("Type: *"));
        panel.add(cboType);
        panel.add(new JLabel("Category: *"));
        panel.add(txtCategory);
        panel.add(new JLabel("Amount (₱): *"));
        panel.add(txtAmount);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(txtDesc));
        panel.add(new JLabel("Payment Method: *"));
        panel.add(cboMethod);
        panel.add(new JLabel("Payee/Payer:"));
        panel.add(txtPayeePayer);
        panel.add(new JLabel("Reference No:"));
        panel.add(txtReferenceNo);

        // Load data if editing
        if (isEdit) {
            try {
                Map<String, Object> transaction = FinancialModel.getTransactionById(id);
                
                if (transaction != null) {
                    Date date = (Date) transaction.get("transaction_date");
                    if (date != null) {
                        spinDate.setValue(new java.util.Date(date.getTime()));
                    }

                    cboType.setSelectedItem((String) transaction.get("transaction_type"));
                    txtCategory.setText((String) transaction.get("category"));
                    txtAmount.setText(String.valueOf(transaction.get("amount")));
                    txtDesc.setText((String) transaction.get("description"));
                    String paymentMethod = (String) transaction.get("payment_method");
                    if (paymentMethod != null) {
                        cboMethod.setSelectedItem(paymentMethod);
                    }
                    txtPayeePayer.setText((String) transaction.get("payee_payer"));
                    txtReferenceNo.setText((String) transaction.get("reference_number"));
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

        btnSave.addActionListener(ae -> saveTransaction(dlg, id, isEdit, spinDate, 
            cboType, txtCategory, txtAmount, txtDesc, cboMethod, txtPayeePayer, txtReferenceNo));
        btnCancel.addActionListener(ae -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        
        dlg.getContentPane().add(wrapper);
        dlg.setVisible(true);
    }

    private void saveTransaction(JDialog dlg, Integer id, boolean isEdit, JSpinner spinDate,
            JComboBox<String> cboType, JTextField txtCategory, JTextField txtAmount, JTextArea txtDesc, 
            JComboBox<String> cboMethod, JTextField txtPayeePayer, JTextField txtReferenceNo) {
        
        try {
            // Validation
            String category = txtCategory.getText().trim();
            String amountStr = txtAmount.getText().trim();
            String description = txtDesc.getText().trim();
            String payeePayer = txtPayeePayer.getText().trim();
            String referenceNo = txtReferenceNo.getText().trim();
            
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
            
            // Get date value (time components are ignored since DB only stores DATE)
            java.util.Date dateValue = (java.util.Date) spinDate.getValue();
            
            // Convert to java.sql.Date (removes time component)
            java.sql.Date sqlDate = new java.sql.Date(dateValue.getTime());
            
            // Save to database using FinancialModel
            try {
                boolean success;
                if (!isEdit) {
                    success = FinancialModel.addTransaction(
                        sqlDate,
                        (String) cboType.getSelectedItem(),
                        category,
                        amount,
                        description,
                        (String) cboMethod.getSelectedItem(),
                        payeePayer,
                        referenceNo
                    );
                } else {
                    success = FinancialModel.updateTransaction(
                        id,
                        sqlDate,
                        (String) cboType.getSelectedItem(),
                        category,
                        amount,
                        description,
                        (String) cboMethod.getSelectedItem(),
                        payeePayer,
                        referenceNo
                    );
                }
                
                if (success) {
                    // Log operation
                    String newId = id != null ? String.valueOf(id) : "new";
                    
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
                }
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
        int id = (Integer) tableModel.getValueAt(rowModel, 0);
        String dateTime = (String) tableModel.getValueAt(rowModel, 1);
        String type = (String) tableModel.getValueAt(rowModel, 2);

        if (!ErrorHandler.confirm(this,
                "Delete this transaction?\n\n" +
                "Date/Time: " + dateTime + "\n" +
                "Type: " + type,
                "Confirm Delete")) {
            return;
        }

        try {
            boolean success = FinancialModel.deleteTransaction(id);

            if (success) {
                Logger.logCRUDOperation("DELETE", "Financial_Transaction", String.valueOf(id),
                    String.format("Type: %s, DateTime: %s", type, dateTime));

                ErrorHandler.showSuccess(this, "Transaction deleted successfully!");
                loadTransactions();
            }
        } catch (SQLException e) {
            ErrorHandler.showError(this, "deleting transaction", e);
        }
    }
}
