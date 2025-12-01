package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import model.ChildrenModel;
import theme.Theme;

public class ChildrenPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public ChildrenPanel() {
        setLayout(new BorderLayout(10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Children (Under 18 years)"),
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
            new Object[]{"ID", "Name", "Age", "Guardian"}, 0) {
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
            BorderFactory.createTitledBorder("Children List (Under 18 years)"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadChildren());

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

        loadChildren();
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

    private void loadChildren() {
        tableModel.setRowCount(0);

        try {
            List<ChildrenModel.Child> children = ChildrenModel.getAllChildren();

            if (children.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No children found", "Add residents through Households", ""});
            } else {
                for (ChildrenModel.Child child : children) {
                    tableModel.addRow(new Object[]{
                        child.getResidentId(),
                        child.getName(),
                        child.getAge(),
                        child.getGuardian()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading children: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
