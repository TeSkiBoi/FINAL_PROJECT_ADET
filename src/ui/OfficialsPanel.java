package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.OfficialModel;
import model.OfficialModel.Official;
import java.awt.*;
import java.util.List;
import theme.Theme;

public class OfficialsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtOfficialId, txtPosition, txtFullname, txtImage, txtOrder;
    private JComboBox<String> cboActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;

    public OfficialsPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Official"),
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
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Official Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // ID field (read-only)
        JLabel lblId = new JLabel("Official ID:");
        lblId.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblId);
        txtOfficialId = new JTextField();
        txtOfficialId.setEditable(false);
        txtOfficialId.setBackground(Color.LIGHT_GRAY);
        inputPanel.add(txtOfficialId);

        // Position field
        JLabel lblPosition = new JLabel("Position:");
        lblPosition.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblPosition);
        txtPosition = new JTextField();
        inputPanel.add(txtPosition);

        // Full Name field
        JLabel lblFullname = new JLabel("Full Name:");
        lblFullname.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblFullname);
        txtFullname = new JTextField();
        inputPanel.add(txtFullname);

        // Image Path field
        JLabel lblImage = new JLabel("Image Path:");
        lblImage.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblImage);
        txtImage = new JTextField();
        inputPanel.add(txtImage);

        // Display Order field
        JLabel lblOrder = new JLabel("Display Order:");
        lblOrder.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblOrder);
        txtOrder = new JTextField();
        inputPanel.add(txtOrder);

        // Active field
        JLabel lblActive = new JLabel("Active:");
        lblActive.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblActive);
        cboActive = new JComboBox<>(new String[]{"Yes", "No"});
        inputPanel.add(cboActive);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Add Official");
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
            new Object[]{"ID", "Position", "Full Name", "Image", "Order", "Active"}, 0) {
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
                txtOfficialId.setText(tableModel.getValueAt(row, 0).toString());
                txtPosition.setText(tableModel.getValueAt(row, 1).toString());
                txtFullname.setText(tableModel.getValueAt(row, 2).toString());
                txtImage.setText(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "");
                txtOrder.setText(tableModel.getValueAt(row, 4).toString());
                cboActive.setSelectedItem(tableModel.getValueAt(row, 5).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Barangay Officials List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadOfficials());
        btnAdd.addActionListener(e -> addOfficial());
        btnUpdate.addActionListener(e -> updateOfficial());
        btnDelete.addActionListener(e -> deleteOfficial());
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

        loadOfficials();
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

    private void loadOfficials() {
        tableModel.setRowCount(0);
        try {
            List<Official> officials = OfficialModel.getAllOfficials();

            if (officials.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No officials found", "Click 'Add Official' to add", "", "", ""});
            } else {
                for (Official official : officials) {
                    tableModel.addRow(new Object[]{
                        official.getId(),
                        official.getPositionTitle(),
                        official.getFullName(),
                        official.getImagePath(),
                        official.getDisplayOrder(),
                        official.getIsActive()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading officials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtOfficialId.setText("");
        txtPosition.setText("");
        txtFullname.setText("");
        txtImage.setText("");
        txtOrder.setText("");
        cboActive.setSelectedIndex(0);
        table.clearSelection();
        txtSearch.setText("");
        loadOfficials();
    }

    private void addOfficial() {
        if (txtPosition.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Position is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (txtFullname.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (txtOrder.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Display Order is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int displayOrder;
        try {
            displayOrder = Integer.parseInt(txtOrder.getText().trim());
            if (displayOrder < 0) {
                JOptionPane.showMessageDialog(this, "Display Order must be non-negative!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Display Order must be a number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = OfficialModel.addOfficial(
            txtPosition.getText().trim(),
            txtFullname.getText().trim(),
            txtImage.getText().trim(),
            displayOrder,
            cboActive.getSelectedItem().toString()
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Official added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add official!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOfficial() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an official to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtPosition.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Position is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (txtFullname.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (txtOrder.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Display Order is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int displayOrder;
        try {
            displayOrder = Integer.parseInt(txtOrder.getText().trim());
            if (displayOrder < 0) {
                JOptionPane.showMessageDialog(this, "Display Order must be non-negative!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Display Order must be a number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtOfficialId.getText());
        boolean success = OfficialModel.updateOfficial(
            id,
            txtPosition.getText().trim(),
            txtFullname.getText().trim(),
            txtImage.getText().trim(),
            displayOrder,
            cboActive.getSelectedItem().toString()
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Official updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update official!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOfficial() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an official to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this official?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(txtOfficialId.getText());
        boolean success = OfficialModel.deleteOfficial(id);

        if (success) {
            JOptionPane.showMessageDialog(this, "Official deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete official!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
