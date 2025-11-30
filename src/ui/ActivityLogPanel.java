package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import db.DbConnection;

public class ActivityLogPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(10,10));
        model = new DefaultTableModel(new String[]{"ID","User ID","Action","Time","IP"}, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadLogs();
    }

    private void loadLogs(){
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()){
            String sql = "SELECT log_id, user_id, action, log_time, ip_address FROM user_logs ORDER BY log_time DESC LIMIT 200";
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                model.addRow(new Object[]{ rs.getInt("log_id"), rs.getString("user_id"), rs.getString("action"), rs.getString("log_time"), rs.getString("ip_address") });
            }
        } catch (SQLException e){ JOptionPane.showMessageDialog(this, "Error loading logs: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
    }
}
