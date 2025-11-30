package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;
import theme.Theme;

public class BlotterPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public BlotterPanel(){
        setLayout(new BorderLayout(10,10));
        
        // Panel title
        JLabel titleLabel = new JLabel("🚨 Blotter / Incident Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel lblSearch = new JLabel("Search:");
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh"); btnAdd = new JButton("+ Add"); btnEdit = new JButton("✏ Edit"); btnDelete = new JButton("🗑 Delete");
        style(btnRefresh); style(btnAdd); style(btnEdit); style(btnDelete);
        
        top.add(lblSearch);
        top.add(txtSearch);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnEdit); top.add(btnDelete);
        
        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Case#","Type","Date","Time","Location","Complainant","Respondent","Status"},0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e->loadIncidents());
        btnAdd.addActionListener(e->openDialog(null));
        btnEdit.addActionListener(e->{ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select incident"); return;} openDialog((Integer)table.getValueAt(r,0)); });
        btnDelete.addActionListener(e->deleteSelected());
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        loadIncidents();
    }
    
    private void search() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void style(JButton b){ b.setBackground(Theme.PRIMARY); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); }

    private void loadIncidents(){
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()){
            String sql = "SELECT incident_id, case_number, incident_type, incident_date, incident_time, incident_location, complainant_name, respondent_name, incident_status FROM blotter_incidents ORDER BY incident_date DESC";
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while (rs.next()){
                hasData = true;
                model.addRow(new Object[]{ rs.getInt("incident_id"), rs.getString("case_number"), rs.getString("incident_type"), rs.getDate("incident_date"), rs.getTime("incident_time"), rs.getString("incident_location"), rs.getString("complainant_name"), rs.getString("respondent_name"), rs.getString("incident_status") });
            }
            if (!hasData) {
                model.addRow(new Object[]{"", "No incidents found", "Click 'Add' to create a new incident", "", "", "", "", "", ""});
            }
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading incidents: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(Integer id){
        boolean isEdit = id!=null; JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit Incident":"Add Incident", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField txtCase = new JTextField(); JComboBox<String> cboType = new JComboBox<>(new String[]{"Complaint","Dispute","Noise Complaint","Domestic Issue","Theft","Assault","Vandalism","Public Disturbance","Other"});
        JTextField txtDate = new JTextField("yyyy-MM-dd"); JTextField txtTime = new JTextField("HH:mm:ss"); JTextField txtLocation = new JTextField(); JTextField txtComplainant = new JTextField(); JTextField txtRespondent = new JTextField(); JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Pending","Under Investigation","For Mediation","Resolved","Closed","Escalated"});
        p.add(new JLabel("Case Number:")); p.add(txtCase); p.add(new JLabel("Type:")); p.add(cboType); p.add(new JLabel("Date:")); p.add(txtDate); p.add(new JLabel("Time:")); p.add(txtTime); p.add(new JLabel("Location:")); p.add(txtLocation); p.add(new JLabel("Complainant:")); p.add(txtComplainant); p.add(new JLabel("Respondent:")); p.add(txtRespondent); p.add(new JLabel("Status:")); p.add(cboStatus);

        if (isEdit){ try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("SELECT * FROM blotter_incidents WHERE incident_id = ?"); ps.setInt(1,id); ResultSet rs = ps.executeQuery(); if (rs.next()){ txtCase.setText(rs.getString("case_number")); cboType.setSelectedItem(rs.getString("incident_type")); Date d = rs.getDate("incident_date"); if (d!=null) txtDate.setText(d.toString()); Time t = rs.getTime("incident_time"); if (t!=null) txtTime.setText(t.toString()); txtLocation.setText(rs.getString("incident_location")); txtComplainant.setText(rs.getString("complainant_name")); txtRespondent.setText(rs.getString("respondent_name")); cboStatus.setSelectedItem(rs.getString("incident_status")); } } catch (SQLException e){ JOptionPane.showMessageDialog(this,"Error loading incident: "+e.getMessage()); } }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton save = new JButton("Save"); JButton cancel = new JButton("Cancel"); style(save); style(cancel); btns.add(save); btns.add(cancel);
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
            
            try (Connection conn = DbConnection.getConnection()){ 
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
                
                if (!isEdit){ 
                    String sql = "INSERT INTO blotter_incidents (case_number, incident_type, incident_date, incident_time, incident_location, complainant_name, respondent_name, incident_description, incident_status) VALUES (?,?,?,?,?,?,?,?,?)"; 
                    PreparedStatement ps = conn.prepareStatement(sql); 
                    ps.setString(1, caseNum); 
                    ps.setString(2, (String)cboType.getSelectedItem()); 
                    ps.setDate(3, incidentDate); 
                    ps.setTime(4, incidentTime); 
                    ps.setString(5, location); 
                    ps.setString(6, complainant); 
                    ps.setString(7, respondent); 
                    ps.setString(8, ""); 
                    ps.setString(9, (String)cboStatus.getSelectedItem()); 
                    ps.executeUpdate(); 
                    JOptionPane.showMessageDialog(dlg,"✓ Incident added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); 
                } else { 
                    String sql = "UPDATE blotter_incidents SET case_number=?, incident_type=?, incident_date=?, incident_time=?, incident_location=?, complainant_name=?, respondent_name=?, incident_status=? WHERE incident_id=?"; 
                    PreparedStatement ps = conn.prepareStatement(sql); 
                    ps.setString(1, caseNum); 
                    ps.setString(2, (String)cboType.getSelectedItem()); 
                    ps.setDate(3, incidentDate); 
                    ps.setTime(4, incidentTime); 
                    ps.setString(5, location); 
                    ps.setString(6, complainant); 
                    ps.setString(7, respondent); 
                    ps.setString(8, (String)cboStatus.getSelectedItem()); 
                    ps.setInt(9, id); 
                    ps.executeUpdate(); 
                    JOptionPane.showMessageDialog(dlg,"✓ Incident updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); 
                } 
                dlg.dispose(); 
                loadIncidents(); 
            } catch (SQLException e){ 
                JOptionPane.showMessageDialog(dlg,"Database error: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
            } 
        });
        cancel.addActionListener(ae->dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH); dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void deleteSelected(){ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select an incident"); return;} int id = (Integer)table.getValueAt(r,0); int c = JOptionPane.showConfirmDialog(this,"Delete incident?","Confirm",JOptionPane.YES_NO_OPTION); if (c!=JOptionPane.YES_OPTION) return; try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("DELETE FROM blotter_incidents WHERE incident_id = ?"); ps.setInt(1,id); ps.executeUpdate(); loadIncidents(); } catch (SQLException e){ JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); } }
}
