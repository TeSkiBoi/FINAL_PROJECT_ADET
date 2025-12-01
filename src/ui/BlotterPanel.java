package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import model.BlotterModel;
import theme.Theme;

public class BlotterPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public BlotterPanel() {
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Incident"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        
        styleButton(btnRefresh);
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);
        
        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        btnAdd = new JButton("+ Add Incident");
        btnEdit = new JButton("✏ Edit Incident");
        btnDelete = new JButton("🗑 Delete Incident");
        
        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        
        // Top Panel (contains search and actions)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Case#", "Type", "Date", "Time", "Location", "Complainant", "Respondent", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Blotter / Incident Reports List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadIncidents());
        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select incident");
                return;
            }
            openDialog((Integer) table.getValueAt(r, 0));
        });
        btnDelete.addActionListener(e -> deleteSelected());

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

        loadIncidents();
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

    private void loadIncidents() {
        tableModel.setRowCount(0);
        try {
            List<BlotterModel.Incident> incidents = BlotterModel.getAllIncidents();
            if (incidents.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No incidents found", "Click 'Add' to create a new incident", "", "", "", "", "", ""});
            } else {
                for (BlotterModel.Incident incident : incidents) {
                    tableModel.addRow(new Object[]{
                        incident.getIncidentId(),
                        incident.getCaseNumber(),
                        incident.getIncidentType(),
                        incident.getIncidentDate(),
                        incident.getIncidentTime(),
                        incident.getLocation(),
                        incident.getComplainant(),
                        incident.getRespondent(),
                        incident.getStatus()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading incidents: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDialog(Integer id){
        boolean isEdit = id!=null; 
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit Incident":"Add Incident", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 450);
        dlg.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); 
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        JTextField txtCase = new JTextField(); 
        JComboBox<String> cboType = new JComboBox<>(new String[]{"Complaint","Dispute","Noise Complaint","Domestic Issue","Theft","Assault","Vandalism","Public Disturbance","Other"});
        
        // Date Spinner with yyyy-MM-dd format
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinDate, "yyyy-MM-dd");
        spinDate.setEditor(dateEditor);
        spinDate.setValue(new java.util.Date());
        
        // Time Spinner with 12-hour format and AM/PM
        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner spinTime = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinTime, "hh:mm:ss a");
        spinTime.setEditor(timeEditor);
        spinTime.setValue(new java.util.Date());
        
        JTextField txtLocation = new JTextField(); 
        JTextField txtComplainant = new JTextField(); 
        JTextField txtRespondent = new JTextField(); 
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Pending","Under Investigation","For Mediation","Resolved","Closed","Escalated"});
        
        p.add(new JLabel("Case Number: *")); p.add(txtCase); 
        p.add(new JLabel("Type: *")); p.add(cboType); 
        p.add(new JLabel("Date: *")); p.add(spinDate); 
        p.add(new JLabel("Time: *")); p.add(spinTime); 
        p.add(new JLabel("Location: *")); p.add(txtLocation); 
        p.add(new JLabel("Complainant: *")); p.add(txtComplainant); 
        p.add(new JLabel("Respondent: *")); p.add(txtRespondent); 
        p.add(new JLabel("Status: *")); p.add(cboStatus);

        if (isEdit){ 
            try {
                BlotterModel.Incident incident = BlotterModel.getIncidentById(id);
                if (incident != null) {
                    txtCase.setText(incident.getCaseNumber()); 
                    cboType.setSelectedItem(incident.getIncidentType()); 
                    
                    // Set date spinner value
                    Date d = incident.getIncidentDate(); 
                    if (d != null) {
                        spinDate.setValue(new java.util.Date(d.getTime()));
                    }
                    
                    // Set time spinner value
                    Time t = incident.getIncidentTime(); 
                    if (t != null) {
                        // Combine today's date with the time from database
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        java.util.Calendar timeCal = java.util.Calendar.getInstance();
                        timeCal.setTime(t);
                        cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
                        cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
                        cal.set(java.util.Calendar.SECOND, timeCal.get(java.util.Calendar.SECOND));
                        spinTime.setValue(cal.getTime());
                    }
                    
                    txtLocation.setText(incident.getLocation()); 
                    txtComplainant.setText(incident.getComplainant()); 
                    txtRespondent.setText(incident.getRespondent()); 
                    cboStatus.setSelectedItem(incident.getStatus());
                }
            } catch (Exception e){ 
                JOptionPane.showMessageDialog(this,"Error loading incident: "+e.getMessage()); 
            } 
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        JButton save = new JButton("Save"); 
        JButton cancel = new JButton("Cancel"); 
        styleButton(save); 
        styleButton(cancel); 
        btns.add(save); 
        btns.add(cancel);
        
        save.addActionListener(ae->{ 
            // Validate required fields
            String caseNum = txtCase.getText().trim();
            String location = txtLocation.getText().trim();
            String complainant = txtComplainant.getText().trim();
            String respondent = txtRespondent.getText().trim();
            
            if (caseNum.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Case Number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtCase.requestFocus();
                return;
            }
            if (location.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Location is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtLocation.requestFocus();
                return;
            }
            if (complainant.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Complainant Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtComplainant.requestFocus();
                return;
            }
            if (respondent.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Respondent Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtRespondent.requestFocus();
                return;
            }
            
            try { 
                // Get date from spinner
                java.util.Date dateValue = (java.util.Date) spinDate.getValue();
                Date incidentDate = new Date(dateValue.getTime());
                
                // Get time from spinner
                java.util.Date timeValue = (java.util.Date) spinTime.getValue();
                Time incidentTime = new Time(timeValue.getTime());
                
                boolean success;
                if (!isEdit){ 
                    success = BlotterModel.addIncident(caseNum, (String)cboType.getSelectedItem(), 
                        incidentDate, incidentTime, location, complainant, respondent, 
                        (String)cboStatus.getSelectedItem());
                    if (success) {
                        JOptionPane.showMessageDialog(dlg,"✓ Incident added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else { 
                    success = BlotterModel.updateIncident(id, caseNum, (String)cboType.getSelectedItem(), 
                        incidentDate, incidentTime, location, complainant, respondent, 
                        (String)cboStatus.getSelectedItem());
                    if (success) {
                        JOptionPane.showMessageDialog(dlg,"✓ Incident updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } 
                dlg.dispose(); 
                loadIncidents(); 
            } catch (Exception e){ 
                JOptionPane.showMessageDialog(dlg,"Database error: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
            } 
        });
        cancel.addActionListener(ae->dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH); dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select an incident");
            return;
        }
        int id = (Integer) table.getValueAt(r, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete incident?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            boolean success = BlotterModel.deleteIncident(id);
            if (success) {
                loadIncidents();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage());
        }
    }
}
