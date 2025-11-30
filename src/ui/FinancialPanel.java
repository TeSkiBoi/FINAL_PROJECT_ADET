package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.SessionManager;
import model.User;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;

public class FinancialPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnAdd, btnEdit, btnDelete;

    public FinancialPanel(){
        setLayout(new BorderLayout(10,10));
        setBackground(Theme.PRIMARY_LIGHT);
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        btnRefresh = new JButton("Refresh"); 
        btnAdd = new JButton("Add"); 
        btnEdit = new JButton("Edit"); 
        btnDelete = new JButton("Delete");
        style(btnRefresh); style(btnAdd); style(btnEdit); style(btnDelete);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnEdit); top.add(btnDelete);
        
        // Check if user can modify (Admin or Staff)
        User current = SessionManager.getInstance().getCurrentUser();
        boolean canModify = false;
        if (current != null) {
            String role = current.getRoleId();
            if ("1".equals(role) || "2".equals(role)) canModify = true;
        }
        btnAdd.setEnabled(canModify);
        btnEdit.setEnabled(canModify);
        btnDelete.setEnabled(canModify);
        
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Date","Type","Category","Amount","Description","Method"},0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e->loadTransactions());
        btnAdd.addActionListener(e->openDialog(null));
        btnEdit.addActionListener(e->{ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select transaction"); return;} openDialog((Integer)model.getValueAt(r,0)); });
        btnDelete.addActionListener(e->deleteSelected());

        loadTransactions();
    }

    private void style(JButton b){ b.setBackground(Theme.PRIMARY); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); }

    private void loadTransactions(){ model.setRowCount(0); try (Connection conn = DbConnection.getConnection()){ String sql = "SELECT transaction_id, transaction_date, transaction_type, category, amount, description, payment_method FROM financial_transactions ORDER BY transaction_date DESC"; Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql); while (rs.next()){ model.addRow(new Object[]{ rs.getInt("transaction_id"), rs.getDate("transaction_date"), rs.getString("transaction_type"), rs.getString("category"), rs.getDouble("amount"), rs.getString("description"), rs.getString("payment_method") }); } } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading transactions: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); } }

    private void openDialog(Integer id){ boolean isEdit = id!=null; JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit Transaction":"Add Transaction", Dialog.ModalityType.APPLICATION_MODAL); JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); JTextField txtDate = new JTextField("yyyy-MM-dd"); JComboBox<String> cboType = new JComboBox<>(new String[]{"Income","Expense"}); JTextField txtCategory = new JTextField(); JTextField txtAmount = new JTextField(); JTextArea txtDesc = new JTextArea(3,20); JComboBox<String> cboMethod = new JComboBox<>(new String[]{"Cash","Check","Bank Transfer","Online Payment","Other"}); p.add(new JLabel("Date:")); p.add(txtDate); p.add(new JLabel("Type:")); p.add(cboType); p.add(new JLabel("Category:")); p.add(txtCategory); p.add(new JLabel("Amount:")); p.add(txtAmount); p.add(new JLabel("Description:")); p.add(new JScrollPane(txtDesc)); p.add(new JLabel("Method:")); p.add(cboMethod);
        if (isEdit){ try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("SELECT * FROM financial_transactions WHERE transaction_id = ?"); ps.setInt(1,id); ResultSet rs = ps.executeQuery(); if (rs.next()){ Date d = rs.getDate("transaction_date"); if (d!=null) txtDate.setText(d.toString()); cboType.setSelectedItem(rs.getString("transaction_type")); txtCategory.setText(rs.getString("category")); txtAmount.setText(String.valueOf(rs.getDouble("amount"))); txtDesc.setText(rs.getString("description")); cboMethod.setSelectedItem(rs.getString("payment_method")); } } catch (SQLException e){ JOptionPane.showMessageDialog(this,"Error loading transaction: "+e.getMessage()); } }
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton save = new JButton("Save"); JButton cancel = new JButton("Cancel"); style(save); style(cancel); btns.add(save); btns.add(cancel);
        save.addActionListener(ae->{ try (Connection conn = DbConnection.getConnection()){ if (!isEdit){ String sql = "INSERT INTO financial_transactions (transaction_date, transaction_type, category, amount, description, payment_method) VALUES (?,?,?,?,?,?)"; PreparedStatement ps = conn.prepareStatement(sql); ps.setDate(1, txtDate.getText().trim().isEmpty()?null:Date.valueOf(txtDate.getText().trim())); ps.setString(2, (String)cboType.getSelectedItem()); ps.setString(3, txtCategory.getText().trim()); ps.setDouble(4, Double.parseDouble(txtAmount.getText().trim())); ps.setString(5, txtDesc.getText().trim()); ps.setString(6, (String)cboMethod.getSelectedItem()); ps.executeUpdate(); JOptionPane.showMessageDialog(dlg,"Transaction added"); } else { String sql = "UPDATE financial_transactions SET transaction_date=?, transaction_type=?, category=?, amount=?, description=?, payment_method=? WHERE transaction_id=?"; PreparedStatement ps = conn.prepareStatement(sql); ps.setDate(1, txtDate.getText().trim().isEmpty()?null:Date.valueOf(txtDate.getText().trim())); ps.setString(2, (String)cboType.getSelectedItem()); ps.setString(3, txtCategory.getText().trim()); ps.setDouble(4, Double.parseDouble(txtAmount.getText().trim())); ps.setString(5, txtDesc.getText().trim()); ps.setString(6, (String)cboMethod.getSelectedItem()); ps.setInt(7, id); ps.executeUpdate(); JOptionPane.showMessageDialog(dlg,"Transaction updated"); } dlg.dispose(); loadTransactions(); } catch (SQLException e){ JOptionPane.showMessageDialog(dlg,"DB error: "+e.getMessage()); } }); cancel.addActionListener(ae->dlg.dispose()); JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH); dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true); }

    private void deleteSelected(){ int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select transaction"); return;} int id = (Integer)model.getValueAt(r,0); int c = JOptionPane.showConfirmDialog(this,"Delete transaction?","Confirm",JOptionPane.YES_NO_OPTION); if (c!=JOptionPane.YES_OPTION) return; try (Connection conn = DbConnection.getConnection()){ PreparedStatement ps = conn.prepareStatement("DELETE FROM financial_transactions WHERE transaction_id = ?"); ps.setInt(1,id); ps.executeUpdate(); loadTransactions(); } catch (SQLException e){ JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); } }
}
