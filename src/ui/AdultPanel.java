package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import model.AdultModel;
import theme.Theme;

public class AdultPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public AdultPanel() {
        setLayout(new BorderLayout(10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Adult Resident (18-59 years)"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);

        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");

        styleButton(btnRefresh);

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);
        searchPanel.add(Box.createHorizontalStrut(10));

        JLabel lblNote = new JLabel("(Manage through Households)");
        lblNote.setForeground(Theme.TEXT_SECONDARY);
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11));
        searchPanel.add(lblNote);

        add(searchPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Age", "Gender", "Contact", "Email"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Adult Residents List (18-59 years)"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadAdults());

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
        });

        loadAdults();
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

    private void loadAdults() {
        tableModel.setRowCount(0);

        try {
            List<AdultModel.Adult> adults = AdultModel.getAllAdults();

            if (adults.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No adults found", "Add residents through Households", "", "", ""});
            } else {
                for (AdultModel.Adult adult : adults) {
                    tableModel.addRow(new Object[]{
                        adult.getResidentId(),
                        adult.getName(),
                        adult.getAge(),
                        adult.getGender(),
                        adult.getContactNo(),
                        adult.getEmail()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading adults: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
