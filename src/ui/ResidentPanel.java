package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.ResidentModel;
import java.awt.*;
import java.util.List;
import theme.Theme;

public class ResidentPanel extends JPanel {
    private JTable residentTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public ResidentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.PRIMARY_LIGHT);

        // Panel title
        JLabel titleLabel = new JLabel("👥 All Residents");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Top panel with search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Theme.PRIMARY_LIGHT);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Theme.TEXT_PRIMARY);
        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");
        styleButton(btnRefresh);

        topPanel.add(lblSearch);
        topPanel.add(txtSearch);
        topPanel.add(btnRefresh);
        
        JLabel lblNote = new JLabel("(Manage residents through Households)");
        lblNote.setForeground(Theme.TEXT_SECONDARY);
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        topPanel.add(lblNote);

        // Combine title and toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(
            new Object[] {"ID", "Household", "First Name", "Middle", "Last", "Birthdate", "Age", "Gender", "Contact", "Email"}, 
            0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        residentTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        residentTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(residentTable);
        add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        btnRefresh.addActionListener(e -> loadResidents());
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        loadResidents();
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

    private void loadResidents() {
        tableModel.setRowCount(0);
        List<ResidentModel> residents = ResidentModel.getAll();
        if (residents.isEmpty()) {
            tableModel.addRow(new Object[]{"", "", "No residents found", "Add residents through Households", "", "", "", "", "", ""});
        } else {
            for (ResidentModel residentItem : residents) {
                tableModel.addRow(new Object[] {
                    residentItem.getResidentId(),
                    residentItem.getHouseholdId() != null ? residentItem.getHouseholdId() : "N/A",
                    residentItem.getFirstName(),
                    residentItem.getMiddleName(),
                    residentItem.getLastName(),
                    residentItem.getBirthDate(),
                    residentItem.getAge(),
                    residentItem.getGender(),
                    residentItem.getContactNo(),
                    residentItem.getEmail()
                });
            }
        }
    }
}
