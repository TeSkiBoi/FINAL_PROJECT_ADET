package ui;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import db.DbConnection;

public class SupplierPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtContact, txtAddress, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JComboBox<String> statusCombo;

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Supplier"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        txtSearch = new JTextField(20);
        btnSearch = new JButton("🔍 Search");
        styleButton(btnSearch);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        // Form Panel
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Supplier Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        inputPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        inputPanel.add(txtName);
        
        inputPanel.add(new JLabel("Contact:"));
        txtContact = new JTextField();
        inputPanel.add(txtContact);
        
        inputPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        inputPanel.add(txtAddress);
        
        inputPanel.add(new JLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        inputPanel.add(statusCombo);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Supplier");
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
        model = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact", "Address", "Status"}, 
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Add table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtName.setText(model.getValueAt(row, 1).toString());
                txtContact.setText(model.getValueAt(row, 2).toString());
                txtAddress.setText(model.getValueAt(row, 3).toString());
                statusCombo.setSelectedItem(model.getValueAt(row, 4).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Suppliers List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Initialize data and event handlers
        //createSuppliersTableIfNeeded();
        loadSuppliers();
        
        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSupplier());
        btnDelete.addActionListener(e -> deleteSupplier());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchSuppliers());
        
        // Add search on enter key
        txtSearch.addActionListener(e -> searchSuppliers());
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

//    private void createSuppliersTableIfNeeded() {
//        try (Connection conn = DbConnection.getConnection()) {
//            DatabaseMetaData meta = conn.getMetaData();
//            ResultSet tables = meta.getTables(null, null, "suppliers", null);
//            
//            if (!tables.next()) {
//                Statement stmt = conn.createStatement();
//                String sql = """
//                    CREATE TABLE suppliers (
//                        id INT PRIMARY KEY AUTO_INCREMENT,
//                        name VARCHAR(100) NOT NULL,
//                        contact VARCHAR(50),
//                        address TEXT,
//                        status VARCHAR(20) DEFAULT 'Active'
//                    )
//                """;
//                stmt.executeUpdate(sql);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this,
//                "Error creating suppliers table: " + e.getMessage(),
//                "Database Error",
//                JOptionPane.ERROR_MESSAGE);
//        }
//    }

    private void clearForm() {
        txtName.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        statusCombo.setSelectedIndex(0);
        table.clearSelection();
        txtSearch.setText("");
        loadSuppliers(); // Reset search results
    }

    private boolean validateInputs() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Supplier name is required!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void loadSuppliers() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM suppliers ORDER BY name");
            updateTableModel(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading suppliers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchSuppliers() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadSuppliers();
            return;
        }

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM suppliers WHERE name LIKE ? OR contact LIKE ? OR address LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            String term = "%" + searchTerm + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            
            ResultSet rs = ps.executeQuery();
            updateTableModel(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching suppliers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableModel(ResultSet rs) throws SQLException {
        model.setRowCount(0);
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("contact"),
                rs.getString("address"),
                rs.getString("status")
            });
        }
    }

    private void addSupplier() {
        if (!validateInputs()) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO suppliers (name, contact, address, status) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtContact.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setString(4, statusCombo.getSelectedItem().toString());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Supplier added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadSuppliers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error adding supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSupplier() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier to update",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputs()) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE suppliers SET name=?, contact=?, address=?, status=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtContact.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setString(4, statusCombo.getSelectedItem().toString());
            ps.setInt(5, (Integer) model.getValueAt(row, 0));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Supplier updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadSuppliers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error updating supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSupplier() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier to delete",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this supplier?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM suppliers WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, (Integer) model.getValueAt(row, 0));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Supplier deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadSuppliers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}