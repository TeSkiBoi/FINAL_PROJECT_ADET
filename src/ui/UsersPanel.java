package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import crypto.PasswordHashing;
import db.DbConnection;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class UsersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public UsersPanel() {
        setLayout(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        btnAdd = new JButton("Add User");
        btnEdit = new JButton("Edit User");
        btnDelete = new JButton("Delete User");
        btnRefresh = new JButton("Refresh");
        style(btnAdd); style(btnEdit); style(btnDelete); style(btnRefresh);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnEdit); top.add(btnDelete);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"User ID","Username","Fullname","Email","Role","Status"}, 0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadUsers());
        btnAdd.addActionListener(e -> openUserDialog(null));
        btnEdit.addActionListener(e -> { int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select a user"); return;} openUserDialog((String)model.getValueAt(r,0)); });
        btnDelete.addActionListener(e -> deleteUser());

        loadUsers();
    }

    private void style(JButton b){ b.setBackground(Theme.PRIMARY); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); }

    private void loadUsers(){
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()){
            String sql = "SELECT u.user_id, u.username, u.fullname, u.email, r.role_name, u.status FROM users u LEFT JOIN roles r ON u.role_id = r.role_id ORDER BY u.user_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                model.addRow(new Object[]{
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("role_name"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading users: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void openUserDialog(String userId){
        boolean isEdit = userId != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isEdit?"Edit User":"Add User", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField txtUserId = new JTextField(); txtUserId.setEnabled(false);
        JTextField txtUsername = new JTextField();
        JTextField txtFullname = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cboRole = new JComboBox<>();
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"active","inactive"});
        JPasswordField pwd = new JPasswordField();

        // populate roles
        try (Connection conn = DbConnection.getConnection()){
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT role_id, role_name FROM roles ORDER BY role_id");
            while (rs.next()){
                cboRole.addItem(rs.getString("role_id")+": "+rs.getString("role_name"));
            }
        } catch (SQLException e){ }

        p.add(new JLabel("User ID:")); p.add(txtUserId);
        p.add(new JLabel("Username:")); p.add(txtUsername);
        p.add(new JLabel("Fullname:")); p.add(txtFullname);
        p.add(new JLabel("Email:")); p.add(txtEmail);
        p.add(new JLabel("Role:")); p.add(cboRole);
        p.add(new JLabel("Status:")); p.add(cboStatus);
        p.add(new JLabel("Password (leave blank to keep):")); p.add(pwd);

        if (isEdit){
            try (Connection conn = DbConnection.getConnection()){
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
                ps.setString(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()){
                    txtUserId.setText(rs.getString("user_id"));
                    txtUsername.setText(rs.getString("username"));
                    txtFullname.setText(rs.getString("fullname"));
                    txtEmail.setText(rs.getString("email"));
                    String roleId = rs.getString("role_id");
                    for (int i=0;i<cboRole.getItemCount();i++){ if (cboRole.getItemAt(i).startsWith(roleId+":")){ cboRole.setSelectedIndex(i); break;} }
                    cboStatus.setSelectedItem(rs.getString("status"));
                }
            } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading user: "+e.getMessage()); }
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save"); JButton cancel = new JButton("Cancel"); style(save); style(cancel);
        btns.add(save); btns.add(cancel);

        save.addActionListener(ae -> {
            String username = txtUsername.getText().trim();
            String fullname = txtFullname.getText().trim();
            String email = txtEmail.getText().trim();
            String roleSel = (String)cboRole.getSelectedItem();
            String roleId = roleSel.split(":")[0];
            String status = (String)cboStatus.getSelectedItem();
            String password = new String(pwd.getPassword());

            if (username.isEmpty() || fullname.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Username and Fullname required"); return; }

            try (Connection conn = DbConnection.getConnection()){
                if (!isEdit){
                    // insert
                    String user_id = generateUserId(conn);
                    String sql = "INSERT INTO users (user_id, username, hashed_password, salt, fullname, email, role_id, status) VALUES (?,?,?,?,?,?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, user_id);
                    ps.setString(2, username);
                    if (!password.isEmpty()){
                        String salt = PasswordHashing.generateSalt();
                        String hash = PasswordHashing.hashPassword(password, salt);
                        ps.setString(3, hash);
                        ps.setString(4, salt);
                    } else { ps.setString(3, ""); ps.setString(4, ""); }
                    ps.setString(5, fullname);
                    ps.setString(6, email);
                    ps.setInt(7, Integer.parseInt(roleId));
                    ps.setString(8, status);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dlg, "User created");
                } else {
                    // update
                    String sql = "UPDATE users SET username=?, fullname=?, email=?, role_id=?, status=? WHERE user_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, username);
                    ps.setString(2, fullname);
                    ps.setString(3, email);
                    ps.setInt(4, Integer.parseInt(roleId));
                    ps.setString(5, status);
                    ps.setString(6, txtUserId.getText());
                    ps.executeUpdate();
                    if (!password.isEmpty()){
                        String salt = PasswordHashing.generateSalt();
                        String hash = PasswordHashing.hashPassword(password, salt);
                        PreparedStatement ps2 = conn.prepareStatement("UPDATE users SET hashed_password=?, salt=? WHERE user_id=?");
                        ps2.setString(1, hash); ps2.setString(2, salt); ps2.setString(3, txtUserId.getText()); ps2.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dlg, "User updated");
                }
                dlg.dispose();
                loadUsers();
            } catch (SQLException ex){ JOptionPane.showMessageDialog(dlg, "DB error: "+ex.getMessage()); }
            catch (NoSuchAlgorithmException | InvalidKeySpecException ex){ JOptionPane.showMessageDialog(dlg, "Hashing error: "+ex.getMessage()); }
        });

        cancel.addActionListener(ae -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.add(p, BorderLayout.CENTER); wrapper.add(btns, BorderLayout.SOUTH);
        dlg.getContentPane().add(wrapper); dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void deleteUser(){
        int r = table.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(this,"Select user"); return; }
        String id = (String)model.getValueAt(r,0);
        int c = JOptionPane.showConfirmDialog(this, "Delete user?","Confirm",JOptionPane.YES_NO_OPTION);
        if (c!=JOptionPane.YES_OPTION) return;
        try (Connection conn = DbConnection.getConnection()){
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?"); ps.setString(1, id); ps.executeUpdate(); loadUsers();
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, "DB error: "+e.getMessage()); }
    }

    private String generateUserId(Connection conn) throws SQLException{
        String sql = "SELECT user_id FROM users WHERE user_id LIKE 'U%' ORDER BY user_id DESC LIMIT 1";
        Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
        if (rs.next()){ String last = rs.getString(1); int num = Integer.parseInt(last.substring(1))+1; return "U"+String.format("%03d", num); }
        return "U001";
    }
}
