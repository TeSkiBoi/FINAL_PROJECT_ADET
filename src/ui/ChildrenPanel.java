package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import model.ChildrenModel;
import model.SessionManager;
import model.User;
import theme.Theme;

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

        // Panel title
        JLabel titleLabel = new JLabel("👶 Children (Under 18 years)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

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
        
        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Guardian"}, 0) {
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
        
        try {
            List<ChildrenModel.Child> children = ChildrenModel.getAllChildren();
            
            if (children.isEmpty()) {
                model.addRow(new Object[]{"", "No children found", "Add residents through Households", ""});
            } else {
                for (ChildrenModel.Child child : children) {
                    model.addRow(new Object[]{
                        child.getResidentId(),
                        child.getName(),
                        child.getAge(),
                        child.getGuardian()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading children: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
