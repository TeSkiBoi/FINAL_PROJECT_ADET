package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.HouseholdModel;
import model.ResidentModel;
import model.SessionManager;
import model.User;
import java.awt.*;
import java.sql.*;
import java.util.List;
import db.DbConnection;
import theme.Theme;

public class HouseholdPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnManageMembers;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean isStaff = false;

    public HouseholdPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);

        // Check if user is staff
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null && "2".equals(current.getRoleId())) {
            isStaff = true;
        }

        // Panel title
        JLabel titleLabel = new JLabel("🏠 Household Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Top toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        btnAdd = new JButton("+ Add Household");
        btnEdit = new JButton("✏ Edit Household");
        btnDelete = new JButton("🗑 Delete Household");
        btnManageMembers = new JButton("👥 Manage Members");

        styleButton(btnRefresh);
        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        styleButton(btnManageMembers);

        top.add(lblSearch);
        top.add(txtSearch);
        top.add(btnRefresh);
        top.add(Box.createHorizontalStrut(20));
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnManageMembers);
        top.add(btnDelete);

        // Staff can only view
        if (isStaff) {
            btnAdd.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
            btnManageMembers.setEnabled(false);
        }

        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Family No", "Head", "Address", "Income", "Members"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadHouseholds());
        btnAdd.addActionListener(e -> openHouseholdDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a household to edit", "Select", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (Integer) table.getValueAt(row, 0);
            openHouseholdDialog(id);
        });
        btnDelete.addActionListener(e -> deleteHousehold());
        btnManageMembers.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a household to manage members", "Select", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (Integer) table.getValueAt(row, 0);
            openMembersDialog(id);
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        loadHouseholds();
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

    private void loadHouseholds() {
        tableModel.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT h.household_id, h.family_no, " +
                        "CONCAT(COALESCE(r.first_name, ''), ' ', COALESCE(r.middle_name, ''), ' ', COALESCE(r.last_name, '')) AS head_name, " +
                        "h.address, h.income, " +
                        "(SELECT COUNT(*) FROM residents r2 WHERE r2.household_id = h.household_id) as member_count " +
                        "FROM households h " +
                        "LEFT JOIN residents r ON h.household_id = r.household_id AND r.resident_id = " +
                        "(SELECT MIN(r3.resident_id) FROM residents r3 WHERE r3.household_id = h.household_id) " +
                        "ORDER BY h.household_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String headName = rs.getString("head_name");
                
                // Show placeholder if no head assigned yet
                if (headName == null || headName.trim().isEmpty()) {
                    headName = "Not assigned yet";
                } else {
                    headName = headName.trim();
                }
                
                tableModel.addRow(new Object[]{
                    rs.getInt("household_id"),
                    rs.getInt("family_no"),
                    headName,
                    rs.getString("address"),
                    rs.getDouble("income"),
                    rs.getInt("member_count")
                });
            }
            if (!hasData) {
                tableModel.addRow(new Object[]{"", "", "No households found", "Click 'Add Household' to create a new household", "", ""});
            }
        } catch (SQLException e) {
            util.ErrorHandler.showError(this, "loading households", e);
        }
    }

    private void openHouseholdDialog(Integer id) {
        boolean isEdit = id != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Household" : "Add Household", true);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtFamilyNo = new JTextField();
        JTextField txtAddress = new JTextField();
        JTextField txtIncome = new JTextField("0");

        panel.add(new JLabel("Family No:"));
        panel.add(txtFamilyNo);
        panel.add(new JLabel("Address:"));
        panel.add(txtAddress);
        panel.add(new JLabel("Income:"));
        panel.add(txtIncome);

        if (isEdit) {
            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT household_id, family_no, address, income FROM households WHERE household_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtFamilyNo.setText(String.valueOf(rs.getInt("family_no")));
                    txtAddress.setText(rs.getString("address"));
                    txtIncome.setText(String.valueOf(rs.getDouble("income")));
                }
            } catch (SQLException e) {
                util.ErrorHandler.showError(this, "loading household details", e);
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnSave);
        styleButton(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(ae -> {
            try {
                // Validate all required fields
                String familyNoStr = txtFamilyNo.getText().trim();
                String address = txtAddress.getText().trim();
                String incomeStr = txtIncome.getText().trim();
                
                // Check if required fields are empty
                if (familyNoStr.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "Family No");
                    txtFamilyNo.requestFocus();
                    return;
                }
                
                if (address.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "Address");
                    txtAddress.requestFocus();
                    return;
                }
                
                if (incomeStr.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "Income");
                    txtIncome.requestFocus();
                    return;
                }
                
                // Validate data types
                int familyNo;
                double income;
                
                try {
                    familyNo = Integer.parseInt(familyNoStr);
                    if (familyNo <= 0) {
                        util.ErrorHandler.showError(dialog, "Family No must be a positive number.");
                        txtFamilyNo.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    util.Logger.logError("Household validation", "Invalid Family No format: " + familyNoStr, e);
                    util.ErrorHandler.showFormatError(dialog, "Family No", "positive integer (e.g., 1, 100)");
                    txtFamilyNo.requestFocus();
                    return;
                }
                
                try {
                    income = Double.parseDouble(incomeStr);
                    if (income < 0) {
                        util.ErrorHandler.showError(dialog, "Income cannot be negative.");
                        txtIncome.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    util.Logger.logError("Household validation", "Invalid Income format: " + incomeStr, e);
                    util.ErrorHandler.showFormatError(dialog, "Income", "number (e.g., 15000, 25000.50)");
                    txtIncome.requestFocus();
                    return;
                }
                
                // All validation passed, proceed with save
                try (Connection conn = DbConnection.getConnection()) {
                    if (!isEdit) {
                        String sql = "INSERT INTO households (family_no, address, income) VALUES (?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setInt(1, familyNo);
                        ps.setString(2, address);
                        ps.setDouble(3, income);
                        ps.executeUpdate();
                        
                        ResultSet rs = ps.getGeneratedKeys();
                        String newId = rs.next() ? String.valueOf(rs.getInt(1)) : "unknown";
                        
                        // Log user activity
                        util.Logger.logCRUDOperation("CREATE", "Household", newId, 
                            String.format("Family No: %d, Address: %s", familyNo, address));
                        
                        util.ErrorHandler.showSuccess(dialog, 
                            "Household added successfully!\n\n" +
                            "Next step: Click 'Manage Members' to add household members.\n" +
                            "The first member added will be the household head.");
                    } else {
                        String sql = "UPDATE households SET family_no=?, address=?, income=? WHERE household_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setInt(1, familyNo);
                        ps.setString(2, address);
                        ps.setDouble(3, income);
                        ps.setInt(4, id);
                        ps.executeUpdate();
                        
                        // Log user activity
                        util.Logger.logCRUDOperation("UPDATE", "Household", String.valueOf(id),
                            String.format("Family No: %d, Address: %s", familyNo, address));
                        
                        util.ErrorHandler.showSuccess(dialog, "Household updated successfully!");
                    }
                    dialog.dispose();
                    loadHouseholds();
                } catch (SQLException e) {
                    util.ErrorHandler.showError(dialog, "saving household", e);
                }
            } catch (Exception e) {
                util.ErrorHandler.showError(dialog, "saving household", e);
            }
        });

        btnCancel.addActionListener(ae -> dialog.dispose());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(wrapper);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openMembersDialog(int householdId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Manage Household Members", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Members table
        DefaultTableModel membersModel = new DefaultTableModel(
            new Object[]{"ID", "First", "Middle", "Last", "Birthdate", "Age", "Gender", "Contact", "Email"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable membersTable = new JTable(membersModel);
        JScrollPane scroll = new JScrollPane(membersTable);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddMember = new JButton("+ Add Member");
        JButton btnEditMember = new JButton("✏ Edit Member");
        JButton btnDeleteMember = new JButton("🗑 Delete Member");
        JButton btnClose = new JButton("Close");

        styleButton(btnAddMember);
        styleButton(btnEditMember);
        styleButton(btnDeleteMember);
        styleButton(btnClose);

        btnPanel.add(btnAddMember);
        btnPanel.add(btnEditMember);
        btnPanel.add(btnDeleteMember);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(btnClose);

        mainPanel.add(btnPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Load members
        Runnable loadMembers = () -> {
            membersModel.setRowCount(0);
            List<ResidentModel> members = ResidentModel.getAll();
            for (ResidentModel r : members) {
                // Check if household_id matches or is null (show all if managing specific household)
                if (r.getHouseholdId() != null && r.getHouseholdId() == householdId) {
                    membersModel.addRow(new Object[]{
                        r.getResidentId(), r.getFirstName(), r.getMiddleName(), r.getLastName(),
                        r.getBirthDate(), r.getAge(), r.getGender(), r.getContactNo(), r.getEmail()
                    });
                }
            }
        };
        loadMembers.run();

        btnAddMember.addActionListener(e -> openMemberDialog(householdId, null, loadMembers));
        btnEditMember.addActionListener(e -> {
            int row = membersTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a member to edit");
                return;
            }
            int memberId = (Integer) membersModel.getValueAt(row, 0);
            openMemberDialog(householdId, memberId, loadMembers);
        });
        btnDeleteMember.addActionListener(e -> {
            int row = membersTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a member to delete");
                return;
            }
            int memberId = (Integer) membersModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(dialog, "Delete this member?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ResidentModel r = new ResidentModel();
                r.setResidentId(memberId);
                if (r.delete()) {
                    JOptionPane.showMessageDialog(dialog, "Member deleted");
                    loadMembers.run();
                    loadHouseholds();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete member");
                }
            }
        });
        btnClose.addActionListener(e -> dialog.dispose());

        dialog.getContentPane().add(mainPanel);
        dialog.setVisible(true);
    }

    private void openMemberDialog(int householdId, Integer memberId, Runnable refreshCallback) {
        boolean isEdit = memberId != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Member" : "Add Member", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtFirst = new JTextField();
        JTextField txtMiddle = new JTextField();
        JTextField txtLast = new JTextField();
        JComboBox<String> cboSuffix = new JComboBox<>(new String[]{"", "Jr.", "Sr.", "II", "III", "IV", "V"});
        cboSuffix.setEditable(true);
        
        // Create date spinner for birthdate
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinBirth = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinBirth, "yyyy-MM-dd");
        spinBirth.setEditor(dateEditor);
        
        // Set default date to 25 years ago for convenience
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -25);
        spinBirth.setValue(cal.getTime());
        
        JTextField txtAge = new JTextField();
        txtAge.setEditable(false);
        txtAge.setBackground(Color.LIGHT_GRAY);
        txtAge.setToolTipText("Age is automatically calculated from birthdate");
        JComboBox<String> cboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField txtContact = new JTextField();
        JTextField txtEmail = new JTextField();
        JCheckBox chkIsHead = new JCheckBox("Set as Household Head");
        
        // Add listener to birthdate spinner to auto-calculate age
        spinBirth.addChangeListener(e -> {
            try {
                java.util.Date dateValue = (java.util.Date) spinBirth.getValue();
                Date birthDate = new Date(dateValue.getTime());
                int age = ResidentModel.calculateAge(birthDate);
                txtAge.setText(String.valueOf(age));
            } catch (Exception ex) {
                txtAge.setText("");
            }
        });
        
        // Trigger initial age calculation
        java.util.Date initialDate = (java.util.Date) spinBirth.getValue();
        Date initialBirthDate = new Date(initialDate.getTime());
        int initialAge = ResidentModel.calculateAge(initialBirthDate);
        txtAge.setText(String.valueOf(initialAge));

        panel.add(new JLabel("First Name:*"));
        panel.add(txtFirst);
        panel.add(new JLabel("Middle Name:"));
        panel.add(txtMiddle);
        panel.add(new JLabel("Last Name:*"));
        panel.add(txtLast);
        panel.add(new JLabel("Suffix:"));
        panel.add(cboSuffix);
        panel.add(new JLabel("Birthdate:*"));
        panel.add(spinBirth);
        panel.add(new JLabel("Age (Auto):*"));
        panel.add(txtAge);
        panel.add(new JLabel("Gender:*"));
        panel.add(cboGender);
        panel.add(new JLabel("Contact No:"));
        panel.add(txtContact);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel(""));
        panel.add(chkIsHead);

        // Check if this is the first member (should be head)
        boolean isFirstMember = false;
        try (Connection conn = DbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) as cnt FROM residents WHERE household_id = ?");
            ps.setInt(1, householdId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("cnt") == 0) {
                isFirstMember = true;
                chkIsHead.setSelected(true);
                chkIsHead.setEnabled(false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (isEdit) {
            for (ResidentModel r : ResidentModel.getAll()) {
                if (r.getResidentId() == memberId) {
                    txtFirst.setText(r.getFirstName());
                    txtMiddle.setText(r.getMiddleName());
                    txtLast.setText(r.getLastName());
                    
                    // Set suffix
                    if (r.getSuffix() != null && !r.getSuffix().isEmpty()) {
                        cboSuffix.setSelectedItem(r.getSuffix());
                    }
                    
                    // Set spinner value for birthdate
                    if (r.getBirthDate() != null) {
                        spinBirth.setValue(new java.util.Date(r.getBirthDate().getTime()));
                        int age = ResidentModel.calculateAge(r.getBirthDate());
                        txtAge.setText(String.valueOf(age));
                    } else {
                        txtAge.setText(String.valueOf(r.getAge()));
                    }
                    
                    cboGender.setSelectedItem(r.getGender());
                    txtContact.setText(r.getContactNo());
                    txtEmail.setText(r.getEmail());
                    break;
                }
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnSave);
        styleButton(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        final boolean finalIsFirstMember = isFirstMember;
        btnSave.addActionListener(ae -> {
            try {
                // Validate all required fields
                String firstName = txtFirst.getText().trim();
                String middleName = txtMiddle.getText().trim();
                String lastName = txtLast.getText().trim();
                String contact = txtContact.getText().trim();
                String email = txtEmail.getText().trim();
                
                // Check required fields
                if (firstName.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "First Name");
                    txtFirst.requestFocus();
                    return;
                }
                
                if (lastName.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "Last Name");
                    txtLast.requestFocus();
                    return;
                }
                
                // Validate birthdate
                java.util.Date spinnerDate = (java.util.Date) spinBirth.getValue();
                if (spinnerDate == null) {
                    util.ErrorHandler.showValidationError(dialog, "Birthdate");
                    return;
                }
                
                // Check if birthdate is in the future
                if (spinnerDate.after(new java.util.Date())) {
                    util.ErrorHandler.showError(dialog, "Birthdate cannot be in the future.\nPlease select a valid date.");
                    return;
                }
                
                // Validate gender selection
                String gender = (String) cboGender.getSelectedItem();
                if (gender == null || gender.isEmpty()) {
                    util.ErrorHandler.showValidationError(dialog, "Gender");
                    return;
                }
                
                // Email validation (if provided)
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    util.ErrorHandler.showFormatError(dialog, "Email", "valid email (e.g., user@example.com)");
                    txtEmail.requestFocus();
                    return;
                }
                
                // All validation passed, proceed with save
                ResidentModel r = isEdit ? null : new ResidentModel();
                if (isEdit) {
                    for (ResidentModel rm : ResidentModel.getAll()) {
                        if (rm.getResidentId() == memberId) {
                            r = rm;
                            break;
                        }
                    }
                }
                if (r == null && isEdit) {
                    util.ErrorHandler.showError(dialog, "Member not found in database.");
                    return;
                }

                r.setHouseholdId(householdId);
                r.setFirstName(firstName);
                r.setMiddleName(middleName);
                r.setLastName(lastName);
                
                // Get suffix from combobox
                String suffix = (String) cboSuffix.getSelectedItem();
                r.setSuffix(suffix != null && !suffix.trim().isEmpty() ? suffix.trim() : null);
                
                r.setBirthDate(new Date(spinnerDate.getTime()));
                r.setGender(gender);
                r.setContactNo(contact);
                r.setEmail(email);

                boolean success = isEdit ? r.update() : r.create();
                if (success) {
                    // Log the operation
                    util.Logger.logCRUDOperation(
                        isEdit ? "UPDATE" : "CREATE", 
                        "Resident", 
                        String.valueOf(r.getResidentId()),
                        String.format("Name: %s %s, Household: %d", firstName, lastName, householdId)
                    );
                    
                    util.ErrorHandler.showSuccess(dialog, 
                        isEdit ? "Member updated successfully!" : "Member added successfully!");
                    dialog.dispose();
                    refreshCallback.run();
                    loadHouseholds();
                } else {
                    util.ErrorHandler.showError(dialog, "Failed to save member to database.\nPlease try again.");
                }
            } catch (Exception ex) {
                util.ErrorHandler.showError(dialog, "saving member information", ex);
            }
        });

        btnCancel.addActionListener(ae -> dialog.dispose());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(wrapper);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteHousehold() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a household to delete");
            return;
        }
        int id = (Integer) table.getValueAt(row, 0);
        String familyNo = String.valueOf(table.getValueAt(row, 1));
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete household and all its members?", 
            "Confirm", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbConnection.getConnection()) {
                // Delete members first
                PreparedStatement ps = conn.prepareStatement("DELETE FROM residents WHERE household_id = ?");
                ps.setInt(1, id);
                int membersDeleted = ps.executeUpdate();
                
                // Delete household
                ps = conn.prepareStatement("DELETE FROM households WHERE household_id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                
                // Log user activity
                util.Logger.logCRUDOperation("DELETE", "Household", String.valueOf(id),
                    String.format("Family No: %s, Members deleted: %d", familyNo, membersDeleted));
                
                JOptionPane.showMessageDialog(this, "Household deleted");
                loadHouseholds();
            } catch (SQLException e) {
                util.ErrorHandler.showError(this, "deleting household", e);
            }
        }
    }
}
