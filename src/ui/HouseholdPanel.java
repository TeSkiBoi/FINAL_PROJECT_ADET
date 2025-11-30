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

        add(top, BorderLayout.NORTH);

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
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("household_id"),
                    rs.getInt("family_no"),
                    rs.getString("head_name") != null ? rs.getString("head_name").trim() : "",
                    rs.getString("address"),
                    rs.getDouble("income"),
                    rs.getInt("member_count")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading households: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Error loading household: " + e.getMessage());
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
            try (Connection conn = DbConnection.getConnection()) {
                if (!isEdit) {
                    String sql = "INSERT INTO households (family_no, address, income) VALUES (?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt(txtFamilyNo.getText().trim()));
                    ps.setString(2, txtAddress.getText().trim());
                    ps.setDouble(3, Double.parseDouble(txtIncome.getText().trim()));
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Household added successfully. Please add members to this household.");
                } else {
                    String sql = "UPDATE households SET family_no=?, address=?, income=? WHERE household_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt(txtFamilyNo.getText().trim()));
                    ps.setString(2, txtAddress.getText().trim());
                    ps.setDouble(3, Double.parseDouble(txtIncome.getText().trim()));
                    ps.setInt(4, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Household updated successfully");
                }
                dialog.dispose();
                loadHouseholds();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Error: " + e.getMessage());
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
        JTextField txtBirth = new JTextField("YYYY-MM-DD");
        JTextField txtAge = new JTextField();
        JComboBox<String> cboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField txtContact = new JTextField();
        JTextField txtEmail = new JTextField();
        JCheckBox chkIsHead = new JCheckBox("Set as Household Head");

        panel.add(new JLabel("First Name:*"));
        panel.add(txtFirst);
        panel.add(new JLabel("Middle Name:"));
        panel.add(txtMiddle);
        panel.add(new JLabel("Last Name:*"));
        panel.add(txtLast);
        panel.add(new JLabel("Birthdate:*"));
        panel.add(txtBirth);
        panel.add(new JLabel("Age:*"));
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
                    txtBirth.setText(r.getBirthDate() != null ? r.getBirthDate().toString() : "");
                    txtAge.setText(String.valueOf(r.getAge()));
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
                    JOptionPane.showMessageDialog(dialog, "Member not found");
                    return;
                }

                r.setHouseholdId(householdId);
                r.setFirstName(txtFirst.getText().trim());
                r.setMiddleName(txtMiddle.getText().trim());
                r.setLastName(txtLast.getText().trim());
                r.setBirthDate(Date.valueOf(txtBirth.getText().trim()));
                r.setAge(Integer.parseInt(txtAge.getText().trim()));
                r.setGender((String) cboGender.getSelectedItem());
                r.setContactNo(txtContact.getText().trim());
                r.setEmail(txtEmail.getText().trim());

                boolean success = isEdit ? r.update() : r.create();
                if (success) {
                    // No need to update households table - head name is now retrieved via JOIN from residents
                    JOptionPane.showMessageDialog(dialog, isEdit ? "Member updated" : "Member added");
                    dialog.dispose();
                    refreshCallback.run();
                    loadHouseholds();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save member");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
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
        int confirm = JOptionPane.showConfirmDialog(this, "Delete household and all its members?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbConnection.getConnection()) {
                // Delete members first
                PreparedStatement ps = conn.prepareStatement("DELETE FROM residents WHERE household_id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                
                // Delete household
                ps = conn.prepareStatement("DELETE FROM households WHERE household_id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Household deleted");
                loadHouseholds();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
