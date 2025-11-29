package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.ResidentModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.List;

public class ResidentPanel extends JPanel {
    private JTable residentTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public ResidentPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(
            new Object[] {"ID", "Household", "First Name", "Middle", "Last", "Birthdate", "Age", "Gender", "Contact", "Email"}, 
            0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        residentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(residentTable);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Add Resident");
        btnEdit = new JButton("Edit Resident");
        btnDelete = new JButton("Delete Resident");
        btnRefresh = new JButton("Refresh");

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showCreateDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadResidents());

        loadResidents();
    }

    private void loadResidents() {
        tableModel.setRowCount(0);
        List<ResidentModel> residents = ResidentModel.getAll();
        for (ResidentModel residentItem : residents) {
            tableModel.addRow(new Object[] {
                residentItem.getResidentId(),
                residentItem.getHouseholdId(),
                residentItem.getFirstName(),
                residentItem.getMiddleName(),
                residentItem.getLastName(),
                residentItem.getBirthDate(),
                residentItem.getAge(),
                residentItem.getGender(),
                residentItem.getContactNo(),
                residentItem.getEmail()
            });
        }
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Resident", true);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField tfHousehold = new JTextField();
        JTextField tfFirst = new JTextField();
        JTextField tfMiddle = new JTextField();
        JTextField tfLast = new JTextField();
        JTextField tfBirth = new JTextField();
        JTextField tfAge = new JTextField();
        JTextField tfGender = new JTextField();
        JTextField tfContact = new JTextField();
        JTextField tfEmail = new JTextField();

        panel.add(new JLabel("Household ID:")); panel.add(tfHousehold);
        panel.add(new JLabel("First Name:")); panel.add(tfFirst);
        panel.add(new JLabel("Middle Name:")); panel.add(tfMiddle);
        panel.add(new JLabel("Last Name:")); panel.add(tfLast);
        panel.add(new JLabel("Birthdate (YYYY-MM-DD):")); panel.add(tfBirth);
        panel.add(new JLabel("Age:")); panel.add(tfAge);
        panel.add(new JLabel("Gender:")); panel.add(tfGender);
        panel.add(new JLabel("Contact No:")); panel.add(tfContact);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);

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
                ResidentModel newResident = new ResidentModel();
                newResident.setHouseholdId(Integer.parseInt(tfHousehold.getText().trim()));
                newResident.setFirstName(tfFirst.getText().trim());
                newResident.setMiddleName(tfMiddle.getText().trim());
                newResident.setLastName(tfLast.getText().trim());
                newResident.setBirthDate(Date.valueOf(tfBirth.getText().trim()));
                newResident.setAge(Integer.parseInt(tfAge.getText().trim()));
                newResident.setGender(tfGender.getText().trim());
                newResident.setContactNo(tfContact.getText().trim());
                newResident.setEmail(tfEmail.getText().trim());
                if (newResident.create()) {
                    JOptionPane.showMessageDialog(dialog, "Resident created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadResidents();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create resident.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = residentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resident to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int residentId = (int) tableModel.getValueAt(row, 0);
        ResidentModel selectedResidentTemp = null;

        for (ResidentModel rm : ResidentModel.getAll()) {
            if (rm.getResidentId() == residentId) {
                selectedResidentTemp = rm;
                break;
            }
        }

        if (selectedResidentTemp == null) {
            JOptionPane.showMessageDialog(this, "Resident not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final ResidentModel selectedResident = selectedResidentTemp;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Resident", true);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField tfHousehold = new JTextField(String.valueOf(selectedResident.getHouseholdId()));
        JTextField tfFirst = new JTextField(selectedResident.getFirstName());
        JTextField tfMiddle = new JTextField(selectedResident.getMiddleName());
        JTextField tfLast = new JTextField(selectedResident.getLastName());
        JTextField tfBirth = new JTextField(selectedResident.getBirthDate() != null ? selectedResident.getBirthDate().toString() : "");
        JTextField tfAge = new JTextField(String.valueOf(selectedResident.getAge()));
        JTextField tfGender = new JTextField(selectedResident.getGender());
        JTextField tfContact = new JTextField(selectedResident.getContactNo());
        JTextField tfEmail = new JTextField(selectedResident.getEmail());

        panel.add(new JLabel("Household ID:")); panel.add(tfHousehold);
        panel.add(new JLabel("First Name:")); panel.add(tfFirst);
        panel.add(new JLabel("Middle Name:")); panel.add(tfMiddle);
        panel.add(new JLabel("Last Name:")); panel.add(tfLast);
        panel.add(new JLabel("Birthdate (YYYY-MM-DD):")); panel.add(tfBirth);
        panel.add(new JLabel("Age:")); panel.add(tfAge);
        panel.add(new JLabel("Gender:")); panel.add(tfGender);
        panel.add(new JLabel("Contact No:")); panel.add(tfContact);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);

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
                selectedResident.setHouseholdId(Integer.parseInt(tfHousehold.getText().trim()));
                selectedResident.setFirstName(tfFirst.getText().trim());
                selectedResident.setMiddleName(tfMiddle.getText().trim());
                selectedResident.setLastName(tfLast.getText().trim());
                selectedResident.setBirthDate(Date.valueOf(tfBirth.getText().trim()));
                selectedResident.setAge(Integer.parseInt(tfAge.getText().trim()));
                selectedResident.setGender(tfGender.getText().trim());
                selectedResident.setContactNo(tfContact.getText().trim());
                selectedResident.setEmail(tfEmail.getText().trim());
                if (selectedResident.update()) {
                    JOptionPane.showMessageDialog(dialog, "Resident updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadResidents();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update resident.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = residentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resident to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int residentId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete resident ID " + residentId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ResidentModel delResident = new ResidentModel();
            delResident.setResidentId(residentId);
            if (delResident.delete()) {
                JOptionPane.showMessageDialog(this, "Resident deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadResidents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete resident.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
