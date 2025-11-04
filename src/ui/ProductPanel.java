package ui;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;

public class ProductPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtQuantity, txtPrice, txtSearch;
    private JComboBox<String> categoryCombo;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private static final String[] CATEGORIES = {"Electronics", "Clothing", "Food", "Books", "Furniture", "Other"};

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Product"),
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
            BorderFactory.createTitledBorder("Product Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Name field
        JLabel lblName = new JLabel("Name:");
        lblName.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblName);
        txtName = new JTextField();
        inputPanel.add(txtName);
        
        // Category dropdown
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblCategory);
        categoryCombo = new JComboBox<>(CATEGORIES);
        categoryCombo.setBackground(Color.WHITE);
        inputPanel.add(categoryCombo);
        
        // Quantity field
        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblQuantity);
        txtQuantity = new JTextField();
        inputPanel.add(txtQuantity);
        
        // Price field
        JLabel lblPrice = new JLabel("Price:");
        lblPrice.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblPrice);
        txtPrice = new JTextField();
        inputPanel.add(txtPrice);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Product");
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

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Quantity", "Price"}, 
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
        
        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtName.setText(model.getValueAt(row, 1).toString());
                categoryCombo.setSelectedItem(model.getValueAt(row, 2).toString());
                txtQuantity.setText(model.getValueAt(row, 3).toString());
                txtPrice.setText(model.getValueAt(row, 4).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Products List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Initialize data and event handlers
        loadProducts();
        
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchProducts());
        
        // Add search on enter key
        txtSearch.addActionListener(e -> searchProducts());
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void clearForm() {
        txtName.setText("");
        categoryCombo.setSelectedIndex(0);
        txtQuantity.setText("");
        txtPrice.setText("");
        table.clearSelection();
        txtSearch.setText("");
        loadProducts(); // Reset search results
    }

    private boolean validateInputs() {
        if (txtName.getText().trim().isEmpty()) {
            showError("Product name is required!");
            return false;
        }

        try {
            int qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty < 0) {
                showError("Quantity must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Quantity must be a valid number!");
            return false;
        }

        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price < 0) {
                showError("Price must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number!");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadProducts() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM products ORDER BY name");
            updateTableModel(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading products: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM products WHERE name LIKE ? OR category LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            String term = "%" + searchTerm + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            
            ResultSet rs = ps.executeQuery();
            updateTableModel(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + e.getMessage(),
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
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("price")
            });
        }
    }

    private void addProduct() {
        if (!validateInputs()) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO products (name, category, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, categoryCombo.getSelectedItem().toString());
            ps.setInt(3, Integer.parseInt(txtQuantity.getText().trim()));
            ps.setDouble(4, Double.parseDouble(txtPrice.getText().trim()));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Product added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error adding product: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to update",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputs()) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE products SET name=?, category=?, quantity=?, price=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, categoryCombo.getSelectedItem().toString());
            ps.setInt(3, Integer.parseInt(txtQuantity.getText().trim()));
            ps.setDouble(4, Double.parseDouble(txtPrice.getText().trim()));
            ps.setInt(5, (Integer) model.getValueAt(row, 0));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Product updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error updating product: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to delete",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this product?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM products WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, (Integer) model.getValueAt(row, 0));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this,
                "Product deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting product: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}