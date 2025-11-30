package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import db.DbConnection;
import model.SessionManager;
import model.User;

public class ChildrenPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean isStaff = false;

    public ChildrenPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);

        // Check if user is staff (role_id = 2)
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null && "2".equals(current.getRoleId())) {
            isStaff = true;
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        
        styleButton(btnRefresh);
        
        top.add(lblSearch);
        top.add(txtSearch);
        top.add(btnRefresh);
        
        JLabel lblNote = new JLabel("(Manage through Households)");
        lblNote.setForeground(Theme.TEXT_SECONDARY);
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        top.add(lblNote);
        
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Household", "Guardian"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadChildren());
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        loadChildren();
    }
    
    private void search() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void styleButton(JButton b) {
        b.setBackground(Theme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadChildren() {
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT r.resident_id, CONCAT(r.first_name, ' ', r.last_name) as name, r.age, r.household_id, " +
                        "(SELECT CONCAT(h.first_name, ' ', h.last_name) FROM residents h WHERE h.household_id = r.household_id AND h.age >= 18 LIMIT 1) as guardian " +
                        "FROM residents r WHERE r.age < 18 ORDER BY r.age";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int hId = rs.getInt("household_id");
                String householdDisplay = rs.wasNull() ? "N/A" : String.valueOf(hId);
                model.addRow(new Object[]{
                    rs.getInt("resident_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    householdDisplay,
                    rs.getString("guardian")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading children: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
