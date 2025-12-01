package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import theme.Theme;
import model.UserModel;
import model.UserModel.UserDisplay;

public class UsersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtUserId, txtUsername, txtFullname, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cboRole, cboStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private JCheckBox chkShowPassword;
    private TableRowSorter<DefaultTableModel> sorter;

    public UsersPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search User"),
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
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("User Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // User ID field (read-only)
        JLabel lblUserId = new JLabel("User ID:");
        lblUserId.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblUserId);
        txtUserId = new JTextField();
        txtUserId.setEditable(false);
        txtUserId.setBackground(Color.LIGHT_GRAY);
        inputPanel.add(txtUserId);

        // Username field - REQUIRED
        JLabel lblUsername = new JLabel("Username: *");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 12));
        lblUsername.setForeground(Color.RED);
        inputPanel.add(lblUsername);
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        // Password field - REQUIRED (for add, optional for update)
        JLabel lblPassword = new JLabel("Password: *");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 12));
        lblPassword.setForeground(Color.RED);
        inputPanel.add(lblPassword);
        
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        txtPassword = new JPasswordField();
        chkShowPassword = new JCheckBox("Show");
        chkShowPassword.setFont(new Font("Arial", Font.PLAIN, 10));
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Show password
            } else {
                txtPassword.setEchoChar('•'); // Hide password
            }
        });
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        passwordPanel.add(chkShowPassword, BorderLayout.EAST);
        inputPanel.add(passwordPanel);

        // Fullname field - REQUIRED
        JLabel lblFullname = new JLabel("Full Name: *");
        lblFullname.setFont(new Font("Arial", Font.BOLD, 12));
        lblFullname.setForeground(Color.RED);
        inputPanel.add(lblFullname);
        txtFullname = new JTextField();
        inputPanel.add(txtFullname);

        // Email field - Optional
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblEmail);
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        // Role dropdown - REQUIRED
        JLabel lblRole = new JLabel("Role: *");
        lblRole.setFont(new Font("Arial", Font.BOLD, 12));
        lblRole.setForeground(Color.RED);
        inputPanel.add(lblRole);
        cboRole = new JComboBox<>();
        cboRole.setBackground(Color.WHITE);
        loadRolesCombo();
        inputPanel.add(cboRole);

        // Status dropdown - REQUIRED
        JLabel lblStatus = new JLabel("Status: *");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setForeground(Color.RED);
        inputPanel.add(lblStatus);
        cboStatus = new JComboBox<>(new String[]{"active", "inactive"});
        cboStatus.setBackground(Color.WHITE);
        inputPanel.add(cboStatus);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnAdd = new JButton("Add User");
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
            new Object[]{"User ID", "Username", "Fullname", "Email", "Role", "Status"}, 0) {
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
                txtUserId.setText(tableModel.getValueAt(row, 0).toString());
                txtUsername.setText(tableModel.getValueAt(row, 1).toString());
                txtPassword.setText(""); // Don't show password
                txtFullname.setText(tableModel.getValueAt(row, 2) != null ? tableModel.getValueAt(row, 2).toString() : "");
                txtEmail.setText(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "");
                cboRole.setSelectedItem(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
                cboStatus.setSelectedItem(tableModel.getValueAt(row, 5) != null ? tableModel.getValueAt(row, 5).toString() : "Active");
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Users List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadUsers());
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
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

        loadUsers();
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

    private void loadRolesCombo() {
        cboRole.removeAllItems();
        List<String> roleNames = UserModel.getAllRoleNames();
        for (String roleName : roleNames) {
            cboRole.addItem(roleName);
        }
    }

    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        chkShowPassword.setSelected(false);
        txtPassword.setEchoChar('•'); // Reset to hidden
        txtFullname.setText("");
        txtEmail.setText("");
        if (cboRole.getItemCount() > 0) cboRole.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
        table.clearSelection();
        txtSearch.setText("");
        loadUsers();
    }

    private void addUser() {
        // Validate required fields
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (txtPassword.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Password is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        if (txtFullname.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFullname.requestFocus();
            return;
        }
        if (cboRole.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Role is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            cboRole.requestFocus();
            return;
        }

        boolean success = UserModel.addUser(
            txtUsername.getText().trim(),
            new String(txtPassword.getPassword()),
            txtFullname.getText().trim(),
            txtEmail.getText().trim(),
            (String) cboRole.getSelectedItem(),
            cboStatus.getSelectedItem().toString()
        );
        
        if (success) {
            JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to add user!\nPossible reasons:\n- Username already exists\n- Database connection issue", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate required fields
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (txtFullname.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFullname.requestFocus();
            return;
        }
        if (cboRole.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Role is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            cboRole.requestFocus();
            return;
        }

        // Password is optional for update - only update if provided
        String password = txtPassword.getPassword().length > 0 ? new String(txtPassword.getPassword()) : null;
        
        boolean success = UserModel.updateUser(
            txtUserId.getText(),
            txtUsername.getText().trim(),
            password,  // null if empty - won't update password
            txtFullname.getText().trim(),
            txtEmail.getText().trim(),
            (String) cboRole.getSelectedItem(),
            cboStatus.getSelectedItem().toString()
        );
        
        if (success) {
            String message = "User updated successfully!";
            if (password == null) {
                message += "\n\nNote: Password was not changed (leave blank to keep current password).";
            }
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to update user!\nPossible reasons:\n- Username already exists\n- Database connection issue", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = UserModel.deleteUser(txtUserId.getText());
        
        if (success) {
            JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete user!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            List<UserDisplay> users = UserModel.getAllUsers();

            if (users.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No users found", "Click 'Add User' to add", "", "", ""});
            } else {
                for (UserDisplay user : users) {
                    tableModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullname(),
                        user.getEmail(),
                        user.getRoleName(),  // Role NAME, not ID
                        user.getStatus()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
