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
        boolean isEdit = id!=null; JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit Incident":"Add Incident", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField txtCase = new JTextField(); JComboBox<String> cboType = new JComboBox<>(new String[]{"Complaint","Dispute","Noise Complaint","Domestic Issue","Theft","Assault","Vandalism","Public Disturbance","Other"});
        JTextField txtDate = new JTextField("yyyy-MM-dd"); JTextField txtTime = new JTextField("HH:mm:ss"); JTextField txtLocation = new JTextField(); JTextField txtComplainant = new JTextField(); JTextField txtRespondent = new JTextField(); JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Pending","Under Investigation","For Mediation","Resolved","Closed","Escalated"});
        p.add(new JLabel("Case Number:")); p.add(txtCase); p.add(new JLabel("Type:")); p.add(cboType); p.add(new JLabel("Date:")); p.add(txtDate); p.add(new JLabel("Time:")); p.add(txtTime); p.add(new JLabel("Location:")); p.add(txtLocation); p.add(new JLabel("Complainant:")); p.add(txtComplainant); p.add(new JLabel("Respondent:")); p.add(txtRespondent); p.add(new JLabel("Status:")); p.add(cboStatus);

        if (isEdit){ 
            try {
                BlotterModel.Incident incident = BlotterModel.getIncidentById(id);
                if (incident != null) {
                    txtCase.setText(incident.getCaseNumber()); 
                    cboType.setSelectedItem(incident.getIncidentType()); 
                    Date d = incident.getIncidentDate(); 
                    if (d!=null) txtDate.setText(d.toString()); 
                    Time t = incident.getIncidentTime(); 
                    if (t!=null) txtTime.setText(t.toString()); 
                    txtLocation.setText(incident.getLocation()); 
                    txtComplainant.setText(incident.getComplainant()); 
                    txtRespondent.setText(incident.getRespondent()); 
                    cboStatus.setSelectedItem(incident.getStatus());
                }
            } catch (Exception e){ 
                JOptionPane.showMessageDialog(this,"Error loading incident: "+e.getMessage()); 
            } 
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton save = new JButton("Save"); JButton cancel = new JButton("Cancel"); styleButton(save); styleButton(cancel); btns.add(save); btns.add(cancel);
        save.addActionListener(ae->{ 
            // Validate required fields
            String caseNum = txtCase.getText().trim();
            String dateStr = txtDate.getText().trim();
            String timeStr = txtTime.getText().trim();
            String location = txtLocation.getText().trim();
            String complainant = txtComplainant.getText().trim();
            String respondent = txtRespondent.getText().trim();
            
            if (caseNum.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Case Number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtCase.requestFocus();
                return;
            }
            if (dateStr.isEmpty() || dateStr.equals("yyyy-MM-dd")) {
                JOptionPane.showMessageDialog(dlg, "Incident Date is required!\nFormat: yyyy-MM-dd (e.g., 2025-11-30)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtDate.requestFocus();
                return;
            }
            if (timeStr.isEmpty() || timeStr.equals("HH:mm:ss")) {
                JOptionPane.showMessageDialog(dlg, "Incident Time is required!\nFormat: HH:mm:ss (e.g., 14:30:00)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtTime.requestFocus();
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
                Date incidentDate = null;
                Time incidentTime = null;
                
                try {
                    incidentDate = Date.valueOf(dateStr);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(dlg, "Invalid date format!\nExpected format: yyyy-MM-dd (e.g., 2025-11-30)", "Format Error", JOptionPane.ERROR_MESSAGE);
                    txtDate.requestFocus();
                    return;
                }
                
                try {
                    incidentTime = Time.valueOf(timeStr);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(dlg, "Invalid time format!\nExpected format: HH:mm:ss (e.g., 14:30:00)", "Format Error", JOptionPane.ERROR_MESSAGE);
                    txtTime.requestFocus();
                    return;
                }
                
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
