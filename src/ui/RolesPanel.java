package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.SessionManager;
import model.User;
import java.awt.*;
import java.sql.*;
import db.DbConnection;

public class RolesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;

    public RolesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);

        // Top toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        btnAdd = new JButton("+ Add Role");
        btnEdit = new JButton("✏ Edit Role");
        btnDelete = new JButton("🗑 Delete Role");

        styleButton(btnRefresh);
        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);

        top.add(lblSearch);
        top.add(txtSearch);
        top.add(btnRefresh);
        top.add(Box.createHorizontalStrut(20));
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);

        add(top, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"Role ID", "Role Name", "Description", "Permissions"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadRoles());
        btnAdd.addActionListener(e -> openRoleDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a role to edit", "Select", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String roleId = (String) table.getValueAt(row, 0);
            openRoleDialog(roleId);
        });
        btnDelete.addActionListener(e -> deleteRole());

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
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT role_id, role_name, description, permissions FROM roles ORDER BY role_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("role_id"),
                    rs.getString("role_name"),
                    rs.getString("description"),
                    rs.getString("permissions")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading roles: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRoleDialog(String roleId) {
        boolean isEdit = roleId != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            isEdit ? "Edit Role" : "Add Role", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtRoleName = new JTextField();
        JTextArea txtDescription = new JTextArea(3, 20);
        JTextArea txtPermissions = new JTextArea(5, 20);

        panel.add(new JLabel("Role Name:*"));
        panel.add(txtRoleName);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(txtDescription));
        panel.add(new JLabel("Permissions:"));
        panel.add(new JScrollPane(txtPermissions));

        if (isEdit) {
            try (Connection conn = DbConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM roles WHERE role_id = ?");
                ps.setString(1, roleId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtRoleName.setText(rs.getString("role_name"));
                    txtDescription.setText(rs.getString("description"));
                    txtPermissions.setText(rs.getString("permissions"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading role: " + e.getMessage());
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
                    String sql = "INSERT INTO roles (role_name, description, permissions) VALUES (?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtRoleName.getText().trim());
                    ps.setString(2, txtDescription.getText().trim());
                    ps.setString(3, txtPermissions.getText().trim());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Role added successfully");
                } else {
                    String sql = "UPDATE roles SET role_name=?, description=?, permissions=? WHERE role_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtRoleName.getText().trim());
                    ps.setString(2, txtDescription.getText().trim());
                    ps.setString(3, txtPermissions.getText().trim());
                    ps.setString(4, roleId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Role updated successfully");
                }
                dialog.dispose();
                loadRoles();
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

    private void deleteRole() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a role to delete");
            return;
        }
        String roleId = (String) table.getValueAt(row, 0);
        
        // Prevent deleting Admin and Staff roles
        if ("1".equals(roleId) || "2".equals(roleId)) {
            JOptionPane.showMessageDialog(this, "Cannot delete system roles (Admin/Staff)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this role?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM roles WHERE role_id = ?");
                ps.setString(1, roleId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Role deleted");
                loadRoles();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
