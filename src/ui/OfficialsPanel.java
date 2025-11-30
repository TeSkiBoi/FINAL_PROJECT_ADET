package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;
import theme.Theme;

public class OfficialsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtPosition, txtFullname, txtImage, txtOrder;
    private JComboBox<String> cboActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public OfficialsPanel() {
        setLayout(new BorderLayout(10,10));
        
        // Panel title
        JLabel titleLabel = new JLabel("👔 Barangay Officials");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel lblSearch = new JLabel("Search:");
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh"); btnAdd = new JButton("+ Add"); btnUpdate = new JButton("✏ Edit"); btnDelete = new JButton("🗑 Delete");
        style(btnRefresh); style(btnAdd); style(btnUpdate); style(btnDelete);
        
        top.add(lblSearch);
        top.add(txtSearch);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnUpdate); top.add(btnDelete);
        
        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Position","Full Name","Image","Order","Active"},0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadOfficials());
        btnAdd.addActionListener(e -> openDialog(null));
        btnUpdate.addActionListener(e -> { int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select an official"); return;} openDialog((Integer)table.getValueAt(r,0)); });
        btnDelete.addActionListener(e -> deleteSelected());
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        loadOfficials();
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

    private void loadOfficials(){
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()){
            String sql = "SELECT id, position_title, full_name, image_path, display_order, is_active FROM barangay_officials ORDER BY display_order";
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while (rs.next()){
                hasData = true;
                model.addRow(new Object[]{ rs.getInt("id"), rs.getString("position_title"), rs.getString("full_name"), rs.getString("image_path"), rs.getInt("display_order"), rs.getString("is_active") });
            }
            if (!hasData) {
                model.addRow(new Object[]{"", "No officials found", "Click 'Add' to add a new official", "", "", ""});
            }
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading officials: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(Integer id){
        boolean isEdit = id != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit Official":"Add Official", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        txtPosition = new JTextField(); txtFullname = new JTextField(); txtImage = new JTextField(); txtOrder = new JTextField("0"); cboActive = new JComboBox<>(new String[]{"Yes","No"});
        p.add(new JLabel("Position:")); p.add(txtPosition);
        p.add(new JLabel("Full Name:")); p.add(txtFullname);
        p.add(new JLabel("Image Path:")); p.add(txtImage);
        p.add(new JLabel("Display Order:")); p.add(txtOrder);
        p.add(new JLabel("Active:")); p.add(cboActive);

        if (isEdit){
            try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("SELECT * FROM barangay_officials WHERE id = ?"); ps.setInt(1,id); ResultSet rs = ps.executeQuery(); if (rs.next()){ txtPosition.setText(rs.getString("position_title")); txtFullname.setText(rs.getString("full_name")); txtImage.setText(rs.getString("image_path")); txtOrder.setText(String.valueOf(rs.getInt("display_order"))); cboActive.setSelectedItem(rs.getString("is_active")); } } catch (SQLException e){ JOptionPane.showMessageDialog(this,"Error loading official: "+e.getMessage()); }
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton save = new JButton("Save"); JButton cancel = new JButton("Cancel"); style(save); style(cancel); btns.add(save); btns.add(cancel);
        save.addActionListener(ae -> {
            // Validate required fields
            String position = txtPosition.getText().trim();
            String fullname = txtFullname.getText().trim();
            String orderStr = txtOrder.getText().trim();
            
            if (position.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Position is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtPosition.requestFocus();
                return;
            }
            if (fullname.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Full Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtFullname.requestFocus();
                return;
            }
            if (orderStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Display Order is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtOrder.requestFocus();
                return;
            }
            
            int displayOrder;
            try {
                displayOrder = Integer.parseInt(orderStr);
                if (displayOrder < 0) {
                    JOptionPane.showMessageDialog(dlg, "Display Order must be a non-negative number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    txtOrder.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(dlg, "Invalid Display Order format!\nExpected format: integer (e.g., 0, 1, 2)", "Format Error", JOptionPane.ERROR_MESSAGE);
                txtOrder.requestFocus();
                return;
            }
            
            try (Connection conn = DbConnection.getConnection()){
                if (!isEdit){ 
                    String sql = "INSERT INTO barangay_officials (position_title, full_name, image_path, display_order, is_active) VALUES (?,?,?,?,?)"; 
                    PreparedStatement ps = conn.prepareStatement(sql); 
                    ps.setString(1, position); 
                    ps.setString(2, fullname); 
                    ps.setString(3, txtImage.getText().trim()); 
                    ps.setInt(4, displayOrder); 
                    ps.setString(5, (String)cboActive.getSelectedItem()); 
                    ps.executeUpdate(); 
                    JOptionPane.showMessageDialog(dlg,"✓ Official added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); 
                }
                else { 
                    String sql = "UPDATE barangay_officials SET position_title=?, full_name=?, image_path=?, display_order=?, is_active=? WHERE id=?"; 
                    PreparedStatement ps = conn.prepareStatement(sql); 
                    ps.setString(1, position); 
                    ps.setString(2, fullname); 
                    ps.setString(3, txtImage.getText().trim()); 
                    ps.setInt(4, displayOrder); 
                    ps.setString(5, (String)cboActive.getSelectedItem()); 
                    ps.setInt(6, id); 
                    ps.executeUpdate(); 
                    JOptionPane.showMessageDialog(dlg,"✓ Official updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); 
                }
                dlg.dispose(); loadOfficials();
            } catch (SQLException e){ 
                JOptionPane.showMessageDialog(dlg,"Database error: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
        cancel.addActionListener(ae -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH); dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void deleteSelected(){ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select an official"); return; } int id = (Integer)table.getValueAt(r,0); int c = JOptionPane.showConfirmDialog(this,"Delete official?","Confirm",JOptionPane.YES_NO_OPTION); if (c!=JOptionPane.YES_OPTION) return; try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("DELETE FROM barangay_officials WHERE id = ?"); ps.setInt(1,id); ps.executeUpdate(); loadOfficials(); } catch (SQLException e){ JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); } }
}
