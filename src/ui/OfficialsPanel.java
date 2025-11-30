package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;

public class OfficialsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtPosition, txtFullname, txtImage, txtOrder;
    private JComboBox<String> cboActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;

    public OfficialsPanel() {
        setLayout(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRefresh = new JButton("Refresh"); btnAdd = new JButton("Add"); btnUpdate = new JButton("Edit"); btnDelete = new JButton("Delete");
        style(btnRefresh); style(btnAdd); style(btnUpdate); style(btnDelete);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnUpdate); top.add(btnDelete);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Position","Full Name","Image","Order","Active"},0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadOfficials());
        btnAdd.addActionListener(e -> openDialog(null));
        btnUpdate.addActionListener(e -> { int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select an official"); return;} openDialog((Integer)model.getValueAt(r,0)); });
        btnDelete.addActionListener(e -> deleteSelected());

        loadOfficials();
    }

    private void style(JButton b){ b.setBackground(Theme.PRIMARY); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); }

    private void loadOfficials(){
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()){
            String sql = "SELECT id, position_title, full_name, image_path, display_order, is_active FROM barangay_officials ORDER BY display_order";
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                model.addRow(new Object[]{ rs.getInt("id"), rs.getString("position_title"), rs.getString("full_name"), rs.getString("image_path"), rs.getInt("display_order"), rs.getString("is_active") });
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
            try (Connection conn = DbConnection.getConnection()){
                if (!isEdit){ String sql = "INSERT INTO barangay_officials (position_title, full_name, image_path, display_order, is_active) VALUES (?,?,?,?,?)"; PreparedStatement ps = conn.prepareStatement(sql); ps.setString(1, txtPosition.getText().trim()); ps.setString(2, txtFullname.getText().trim()); ps.setString(3, txtImage.getText().trim()); ps.setInt(4, Integer.parseInt(txtOrder.getText().trim())); ps.setString(5, (String)cboActive.getSelectedItem()); ps.executeUpdate(); JOptionPane.showMessageDialog(dlg,"Official added"); }
                else { String sql = "UPDATE barangay_officials SET position_title=?, full_name=?, image_path=?, display_order=?, is_active=? WHERE id=?"; PreparedStatement ps = conn.prepareStatement(sql); ps.setString(1, txtPosition.getText().trim()); ps.setString(2, txtFullname.getText().trim()); ps.setString(3, txtImage.getText().trim()); ps.setInt(4, Integer.parseInt(txtOrder.getText().trim())); ps.setString(5, (String)cboActive.getSelectedItem()); ps.setInt(6, id); ps.executeUpdate(); JOptionPane.showMessageDialog(dlg,"Official updated"); }
                dlg.dispose(); loadOfficials();
            } catch (SQLException e){ JOptionPane.showMessageDialog(dlg,"DB error: "+e.getMessage()); }
        });
        cancel.addActionListener(ae -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH); dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void deleteSelected(){ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select an official"); return; } int id = (Integer)model.getValueAt(r,0); int c = JOptionPane.showConfirmDialog(this,"Delete official?","Confirm",JOptionPane.YES_NO_OPTION); if (c!=JOptionPane.YES_OPTION) return; try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("DELETE FROM barangay_officials WHERE id = ?"); ps.setInt(1,id); ps.executeUpdate(); loadOfficials(); } catch (SQLException e){ JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); } }
}
