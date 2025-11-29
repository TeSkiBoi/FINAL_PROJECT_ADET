package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.HouseholdModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class HouseholdPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public HouseholdPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(new Object[]{"ID","Household No","Family No","Head","Address","Income"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Add Household");
        btnEdit = new JButton("Edit Household");
        btnDelete = new JButton("Delete Household");
        btnRefresh = new JButton("Refresh");
        topPanel.add(btnAdd); topPanel.add(btnEdit); topPanel.add(btnDelete); topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showCreateDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadHouseholds());

        loadHouseholds();
    }

    private void loadHouseholds() {
        tableModel.setRowCount(0);
        List<HouseholdModel> households = HouseholdModel.getAll();
        for (HouseholdModel h : households) {
            tableModel.addRow(new Object[] { h.getHouseholdId(), h.getHouseholdNo(), h.getFamilyNo(), h.getFullName(), h.getAddress(), h.getIncome() });
        }
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Household", true);
        JPanel panel = new JPanel(new GridLayout(0,2,8,8));
        JTextField tfFamilyNo = new JTextField();
        JTextField tfHead = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfIncome = new JTextField();
        panel.add(new JLabel("Family No:")); panel.add(tfFamilyNo);
        panel.add(new JLabel("Head Full Name:")); panel.add(tfHead);
        panel.add(new JLabel("Address:")); panel.add(tfAddress);
        panel.add(new JLabel("Income:")); panel.add(tfIncome);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnSave); btnPanel.add(btnCancel);
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnSave.addActionListener(e -> {
            try {
                HouseholdModel h = new HouseholdModel();
                h.setFamilyNo(Integer.parseInt(tfFamilyNo.getText().trim()));
                h.setFullName(tfHead.getText().trim());
                h.setAddress(tfAddress.getText().trim());
                h.setIncome(Double.parseDouble(tfIncome.getText().trim()));
                if (h.create()) {
                    JOptionPane.showMessageDialog(dialog, "Household created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadHouseholds();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create household.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select household to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);

        // Use final array to wrap the HouseholdModel
        final HouseholdModel[] hWrapper = new HouseholdModel[1];
        for (HouseholdModel hm : HouseholdModel.getAll()) {
            if (hm.getHouseholdId() == id) {
                hWrapper[0] = hm;
                break;
            }
        }

        if (hWrapper[0] == null) {
            JOptionPane.showMessageDialog(this, "Household not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Household", true);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField tfFamilyNo = new JTextField(String.valueOf(hWrapper[0].getFamilyNo()));
        JTextField tfHead = new JTextField(hWrapper[0].getFullName());
        JTextField tfAddress = new JTextField(hWrapper[0].getAddress());
        JTextField tfIncome = new JTextField(String.valueOf(hWrapper[0].getIncome()));

        panel.add(new JLabel("Family No:")); panel.add(tfFamilyNo);
        panel.add(new JLabel("Head Full Name:")); panel.add(tfHead);
        panel.add(new JLabel("Address:")); panel.add(tfAddress);
        panel.add(new JLabel("Income:")); panel.add(tfIncome);

        JButton btnSave = new JButton("Update");
        JButton btnCancel = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnSave.addActionListener(e -> {
            try {
                HouseholdModel h = hWrapper[0]; // safely reference final wrapper
                h.setFamilyNo(Integer.parseInt(tfFamilyNo.getText().trim()));
                h.setFullName(tfHead.getText().trim());
                h.setAddress(tfAddress.getText().trim());
                h.setIncome(Double.parseDouble(tfIncome.getText().trim()));

                if (h.update()) {
                    JOptionPane.showMessageDialog(dialog, "Household updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadHouseholds();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update household.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select household to delete.", "No selection", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int)tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm delete household id " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            HouseholdModel h = new HouseholdModel(); h.setHouseholdId(id);
            if (h.delete()) { JOptionPane.showMessageDialog(this, "Household deleted.", "Success", JOptionPane.INFORMATION_MESSAGE); loadHouseholds(); }
            else { JOptionPane.showMessageDialog(this, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
