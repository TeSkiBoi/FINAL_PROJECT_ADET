package ui;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DbConnection;
import model.SessionManager;
import model.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import theme.Theme;

/**
 * Projects panel adapted from ProductPanel: manages barangay_projects table.
 * Uses a modal dialog for Add/Edit since projects have multiple fields.
 */
public class ProductPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh, btnSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));

        // Top toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.PRIMARY_LIGHT);
        txtSearch = new JTextField(30);
        btnSearch = new JButton("🔍 Search");
        btnRefresh = new JButton("🔄 Refresh");
        btnAdd = new JButton("+ Add Project");
        btnUpdate = new JButton("✏ Edit Project");
        btnDelete = new JButton("🗑 Delete Project");

        styleButton(btnSearch);
        styleButton(btnRefresh);
        styleButton(btnAdd);
        styleButton(btnUpdate);
        styleButton(btnDelete);

        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnRefresh);
        top.add(Box.createHorizontalStrut(20));
        top.add(btnAdd);
        top.add(btnUpdate);
        top.add(btnDelete);

        add(top, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID","Name","Status","Start Date","End Date","Proponent","Budget","Progress"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadProjects());
        btnSearch.addActionListener(e -> searchProjects());
        txtSearch.addActionListener(e -> searchProjects());
        btnAdd.addActionListener(e -> openProjectDialog(null));
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a project to edit", "Select", JOptionPane.WARNING_MESSAGE); return; }
            int id = (Integer)model.getValueAt(row,0);
            openProjectDialog(id);
        });
        btnDelete.addActionListener(e -> deleteProject());

        // Privilege: only Admin or Staff can modify
        User current = SessionManager.getInstance().getCurrentUser();
        boolean canModify = false;
        if (current != null) {
            String role = current.getRoleId();
            if ("1".equals(role) || "2".equals(role)) canModify = true;
        }
        btnAdd.setEnabled(canModify);
        btnUpdate.setEnabled(canModify);
        btnDelete.setEnabled(canModify);

        loadProjects();
    }

    private void styleButton(JButton b) {
        b.setBackground(Theme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadProjects() {
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT project_id, project_name, project_status, start_date, end_date, proponent, total_budget, progress_percentage FROM barangay_projects ORDER BY project_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("project_id"),
                    rs.getString("project_name"),
                    rs.getString("project_status"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("proponent"),
                    rs.getDouble("total_budget"),
                    rs.getInt("progress_percentage")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading projects: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProjects() {
        String term = txtSearch.getText().trim();
        if (term.isEmpty()) { loadProjects(); return; }
        model.setRowCount(0);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT project_id, project_name, project_status, start_date, end_date, proponent, total_budget, progress_percentage FROM barangay_projects WHERE project_name LIKE ? OR proponent LIKE ? ORDER BY project_id";
            PreparedStatement ps = conn.prepareStatement(sql);
            String t = "%"+term+"%";
            ps.setString(1,t); ps.setString(2,t);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("project_id"),
                    rs.getString("project_name"),
                    rs.getString("project_status"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("proponent"),
                    rs.getDouble("total_budget"),
                    rs.getInt("progress_percentage")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching projects: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openProjectDialog(Integer projectId) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), projectId==null?"Add Project":"Edit Project", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(0,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextField txtName = new JTextField();
        JTextArea txtDesc = new JTextArea(4,20);
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Planning","Ongoing","Completed","On Hold","Cancelled"});
        JTextField txtStart = new JTextField("yyyy-MM-dd");
        JTextField txtEnd = new JTextField("yyyy-MM-dd");
        JTextField txtProponent = new JTextField();
        JTextField txtBudget = new JTextField();
        JTextField txtProgress = new JTextField();
        JTextArea txtRemarks = new JTextArea(3,20);

        p.add(new JLabel("Project Name:")); p.add(txtName);
        p.add(new JLabel("Description:")); p.add(new JScrollPane(txtDesc));
        p.add(new JLabel("Status:")); p.add(cboStatus);
        p.add(new JLabel("Start Date:")); p.add(txtStart);
        p.add(new JLabel("End Date:")); p.add(txtEnd);
        p.add(new JLabel("Proponent:")); p.add(txtProponent);
        p.add(new JLabel("Total Budget:")); p.add(txtBudget);
        p.add(new JLabel("Progress (%):")); p.add(txtProgress);
        p.add(new JLabel("Remarks:")); p.add(new JScrollPane(txtRemarks));

        if (projectId != null) {
            // load existing
            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT * FROM barangay_projects WHERE project_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, projectId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtName.setText(rs.getString("project_name"));
                    txtDesc.setText(rs.getString("project_description"));
                    cboStatus.setSelectedItem(rs.getString("project_status"));
                    Date sd = rs.getDate("start_date"); if (sd!=null) txtStart.setText(sd.toString());
                    Date ed = rs.getDate("end_date"); if (ed!=null) txtEnd.setText(ed.toString());
                    txtProponent.setText(rs.getString("proponent"));
                    txtBudget.setText(String.valueOf(rs.getDouble("total_budget")));
                    txtProgress.setText(String.valueOf(rs.getInt("progress_percentage")));
                    txtRemarks.setText(rs.getString("remarks"));
                }
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error loading project: "+e.getMessage()); }
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        styleButton(save); styleButton(cancel);
        btns.add(save); btns.add(cancel);

        save.addActionListener(e -> {
            // validate minimal
            if (txtName.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "Project name required"); return; }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.sql.Date sdate = null, edate = null;
            try {
                if (!txtStart.getText().trim().isEmpty() && !txtStart.getText().equals("yyyy-MM-dd")) sdate = java.sql.Date.valueOf(txtStart.getText().trim());
                if (!txtEnd.getText().trim().isEmpty() && !txtEnd.getText().equals("yyyy-MM-dd")) edate = java.sql.Date.valueOf(txtEnd.getText().trim());
            } catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(dlg, "Dates must be yyyy-MM-dd"); return; }

            try (Connection conn = DbConnection.getConnection()) {
                if (projectId == null) {
                    String sql = "INSERT INTO barangay_projects (project_name, project_description, project_status, start_date, end_date, proponent, total_budget, budget_utilized, progress_percentage, remarks) VALUES (?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtName.getText().trim());
                    ps.setString(2, txtDesc.getText().trim());
                    ps.setString(3, cboStatus.getSelectedItem().toString());
                    ps.setDate(4, sdate);
                    ps.setDate(5, edate);
                    ps.setString(6, txtProponent.getText().trim());
                    ps.setDouble(7, parseDoubleSafe(txtBudget.getText().trim()));
                    ps.setDouble(8, 0.0);
                    ps.setInt(9, parseIntSafe(txtProgress.getText().trim()));
                    ps.setString(10, txtRemarks.getText().trim());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dlg, "Project added");
                } else {
                    String sql = "UPDATE barangay_projects SET project_name=?, project_description=?, project_status=?, start_date=?, end_date=?, proponent=?, total_budget=?, progress_percentage=?, remarks=? WHERE project_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtName.getText().trim());
                    ps.setString(2, txtDesc.getText().trim());
                    ps.setString(3, cboStatus.getSelectedItem().toString());
                    ps.setDate(4, sdate);
                    ps.setDate(5, edate);
                    ps.setString(6, txtProponent.getText().trim());
                    ps.setDouble(7, parseDoubleSafe(txtBudget.getText().trim()));
                    ps.setInt(8, parseIntSafe(txtProgress.getText().trim()));
                    ps.setString(9, txtRemarks.getText().trim());
                    ps.setInt(10, projectId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dlg, "Project updated");
                }
                dlg.dispose();
                loadProjects();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(dlg, "Database error: "+ex.getMessage()); }
        });

        cancel.addActionListener(e -> dlg.dispose());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(btns, BorderLayout.SOUTH);
        dlg.getContentPane().add(wrapper);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void deleteProject() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a project to delete", "Select", JOptionPane.WARNING_MESSAGE); return; }
        int id = (Integer)model.getValueAt(row,0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected project?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM barangay_projects WHERE project_id = ?");
            ps.setInt(1, id); ps.executeUpdate();
            loadProjects();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error deleting: "+e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); }
    }

    private double parseDoubleSafe(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; } }
    private int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
}