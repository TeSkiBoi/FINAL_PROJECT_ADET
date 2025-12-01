low# Panel Pattern Quick Reference

## Standard Panel Structure Template

```java
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import model.XxxModel;
import theme.Theme;

public class XxxPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtId, txtField1, txtField2;
    private JComboBox<String> cboField;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;

    public XxxPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Xxx"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        
        styleButton(btnRefresh);
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        // Form Panel (if CRUD panel)
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridLayout(X, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Xxx Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // ID field (read-only)
        JLabel lblId = new JLabel("ID:");
        lblId.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblId);
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(Color.LIGHT_GRAY);
        inputPanel.add(txtId);

        // Add more fields...

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Xxx");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        styleButton(btnAdd);
        styleButton(btnUpdate);
        styleButton(btnDelete);
        styleButton(btnClear);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formPanel.add(inputPanel, BorderLayout.CENTER);
        formPanel.add(btnPanel, BorderLayout.SOUTH);

        // Top Panel (contains search and form)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Column1", "Column2", "Column3"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Table selection listener - populates form when row clicked
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtField1.setText(tableModel.getValueAt(row, 1).toString());
                // ... populate other fields
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Xxx List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> add());
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());

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

        loadData();
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

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<XxxModel.Xxx> items = XxxModel.getAll();

            if (items.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No items found", "Click 'Add' to add", ""});
            } else {
                for (XxxModel.Xxx item : items) {
                    tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getField1(),
                        item.getField2(),
                        item.getField3()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtField1.setText("");
        txtField2.setText("");
        table.clearSelection();
        txtSearch.setText("");
        loadData();
    }

    private void add() {
        // Validation
        if (txtField1.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Field1 is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Business logic
        boolean success = XxxModel.add(
            txtField1.getText().trim(),
            txtField2.getText().trim()
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add item!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validation
        if (txtField1.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Field1 is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Business logic
        boolean success = XxxModel.update(
            Integer.parseInt(txtId.getText()),
            txtField1.getText().trim(),
            txtField2.getText().trim()
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update item!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(txtId.getText());
        boolean success = XxxModel.delete(id);

        if (success) {
            JOptionPane.showMessageDialog(this, "Item deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete item!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

## Key Points

1. **Always use `tableModel`** not `model` for DefaultTableModel
2. **Consistent button styling** with `styleButton()` method
3. **Search functionality** with case-insensitive regex filter
4. **Table selection listener** to populate form fields
5. **Validation** before add/update operations
6. **Confirmation dialogs** for delete operations
7. **Success/Error messages** for all operations
8. **clearForm()** resets all fields and reloads data

## View-Only Panel (No CRUD)

For panels that only display data (like AdultPanel, ChildrenPanel, SeniorPanel):
- Remove form panel
- Remove add/update/delete buttons
- Keep search and refresh functionality
- Add note about management through parent entity (e.g., "Manage through Households")

## Dialog-Based Input (Alternative to Form)

Some panels use dialogs instead of forms (like BlotterPanel, FinancialPanel):
- Still follow the same structure for search and table
- Use `openDialog(Integer id)` method for add/edit
- Dialog contains all input fields
- Validation happens in dialog's save handler
