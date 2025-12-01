package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.RoleModel;
import model.RoleModel.Role;
import java.awt.*;
import java.util.List;
import theme.Theme;

public class RolesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtRoleId, txtRoleName;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;

    public RolesPanel() {
        setLayout(new BorderLayout(10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Role"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);

        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");

        styleButton(btnRefresh);

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        // Form Panel
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Role Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Role ID field
        JLabel lblRoleId = new JLabel("Role ID:");
        lblRoleId.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblRoleId);
        txtRoleId = new JTextField();
        txtRoleId.setEditable(false);
        txtRoleId.setBackground(Color.LIGHT_GRAY);
        inputPanel.add(txtRoleId);
        
        // Role Name field
        JLabel lblRoleName = new JLabel("Role Name:");
        lblRoleName.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblRoleName);
        txtRoleName = new JTextField();
        inputPanel.add(txtRoleName);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Role");
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
            new Object[]{"Role ID", "Role Name"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtRoleId.setText(tableModel.getValueAt(row, 0).toString());
                txtRoleName.setText(tableModel.getValueAt(row, 1).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Roles List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadRoles());
        btnAdd.addActionListener(e -> addRole());
        btnUpdate.addActionListener(e -> updateRole());
        btnDelete.addActionListener(e -> deleteRole());
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

        loadRoles();
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

    private void loadRoles() {
        tableModel.setRowCount(0);
        try {
            List<Role> roles = RoleModel.getAllRoles();
            
            if (roles.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No roles found"});
            } else {
                for (Role role : roles) {
                    tableModel.addRow(new Object[]{
                        role.getRoleId(),
                        role.getRoleName()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading roles: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtRoleId.setText("");
        txtRoleName.setText("");
        table.clearSelection();
        txtSearch.setText("");
        loadRoles();
    }

    private void addRole() {
        if (txtRoleName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Role name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = RoleModel.addRole(txtRoleName.getText().trim());

        if (success) {
            JOptionPane.showMessageDialog(this, "Role added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add role!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRole() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a role to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtRoleName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Role name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = RoleModel.updateRole(txtRoleId.getText(), txtRoleName.getText().trim());

        if (success) {
            JOptionPane.showMessageDialog(this, "Role updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update role!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRole() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a role to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this role?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = RoleModel.deleteRole(txtRoleId.getText());
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Role deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete role (may be a system role)!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
