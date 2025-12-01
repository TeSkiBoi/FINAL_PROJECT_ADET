package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.Map;
import model.ProjectModel;
import model.SessionManager;
import model.User;
import theme.Theme;
import util.ErrorHandler;
import util.Logger;

public class ProjectsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean isStaff = false;

    public ProjectsPanel() {
        setLayout(new BorderLayout(10, 10));

        // Check if user is staff (role_id = 2)
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null && "2".equals(current.getRoleId())) {
            isStaff = true;
        }

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Project"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        searchPanel.setBackground(Theme.PRIMARY_LIGHT);

        txtSearch = new JTextField(30);
        btnRefresh = new JButton("🔄 Refresh");

        styleButton(btnRefresh);

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(Theme.PRIMARY_LIGHT);

        btnAdd = new JButton("+ Add Project");
        btnEdit = new JButton("✏ Edit Project");
        btnDelete = new JButton("🗑 Delete Project");

        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        // Top Panel (contains search and actions)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Project Name", "Status", "Start Date", "End Date", "Proponent", "Budget", "Progress %"}, 0) {
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
            BorderFactory.createTitledBorder("Barangay Projects List"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(scrollPane, BorderLayout.CENTER);

        // Event handlers
        btnRefresh.addActionListener(e -> loadProjects());
        btnAdd.addActionListener(e -> openProjectDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a project to edit", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int rowModel = table.convertRowIndexToModel(row);
            openProjectDialog((Integer) tableModel.getValueAt(rowModel, 0));
        });
        btnDelete.addActionListener(e -> deleteProject());

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

        loadProjects();
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

    private void loadProjects() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> projects = ProjectModel.getAllProjects();

            if (projects.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No projects found", "Click 'Add Project' to add", "", "", "", "", ""});
            } else {
                for (Map<String, Object> project : projects) {
                    Date startDate = (Date) project.get("start_date");
                    Date endDate = (Date) project.get("end_date");

                    tableModel.addRow(new Object[]{
                        project.get("project_id"),
                        project.get("project_name"),
                        project.get("project_status"),
                        startDate != null ? startDate.toString() : "",
                        endDate != null ? endDate.toString() : "",
                        project.get("proponent"),
                        String.format("₱%.2f", project.get("total_budget")),
                        project.get("progress_percentage") + "%"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading projects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openProjectDialog(Integer projectId) {
        boolean isEdit = projectId != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Project" : "Add Project", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtProjectName = new JTextField();
        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Planning", "Ongoing", "Completed", "On Hold", "Cancelled"});

        // Date spinners
        SpinnerDateModel startDateModel = new SpinnerDateModel();
        JSpinner spinStartDate = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(spinStartDate, "yyyy-MM-dd");
        spinStartDate.setEditor(startDateEditor);
        spinStartDate.setValue(new java.util.Date());

        SpinnerDateModel endDateModel = new SpinnerDateModel();
        JSpinner spinEndDate = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(spinEndDate, "yyyy-MM-dd");
        spinEndDate.setEditor(endDateEditor);
        spinEndDate.setValue(new java.util.Date());

        JTextField txtProponent = new JTextField();
        JTextField txtBudget = new JTextField("0.00");
        JTextField txtProgress = new JTextField("0");
        JTextArea txtRemarks = new JTextArea(2, 20);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);

        panel.add(new JLabel("Project Name:*"));
        panel.add(txtProjectName);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(txtDescription));
        panel.add(new JLabel("Status:*"));
        panel.add(cboStatus);
        panel.add(new JLabel("Start Date:*"));
        panel.add(spinStartDate);
        panel.add(new JLabel("End Date:"));
        panel.add(spinEndDate);
        panel.add(new JLabel("Proponent:*"));
        panel.add(txtProponent);
        panel.add(new JLabel("Total Budget (₱):*"));
        panel.add(txtBudget);
        panel.add(new JLabel("Progress (%):"));
        panel.add(txtProgress);
        panel.add(new JLabel("Remarks:"));
        panel.add(new JScrollPane(txtRemarks));

        // Load data if editing
        if (isEdit) {
            try {
                Map<String, Object> project = ProjectModel.getProjectById(projectId);
                if (project != null) {
                    txtProjectName.setText((String) project.get("project_name"));
                    txtDescription.setText((String) project.get("project_description"));
                    cboStatus.setSelectedItem(project.get("project_status"));

                    Date startDate = (Date) project.get("start_date");
                    if (startDate != null) {
                        spinStartDate.setValue(new java.util.Date(startDate.getTime()));
                    }

                    Date endDate = (Date) project.get("end_date");
                    if (endDate != null) {
                        spinEndDate.setValue(new java.util.Date(endDate.getTime()));
                    }

                    txtProponent.setText((String) project.get("proponent"));
                    txtBudget.setText(String.valueOf(project.get("total_budget")));
                    txtProgress.setText(String.valueOf(project.get("progress_percentage")));
                    txtRemarks.setText((String) project.get("remarks"));
                }
            } catch (Exception e) {
                ErrorHandler.showError(dialog, "loading project details", e);
            }
        }

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnSave);
        styleButton(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(ae -> saveProject(dialog, projectId, isEdit, txtProjectName, txtDescription,
            cboStatus, spinStartDate, spinEndDate, txtProponent, txtBudget, txtProgress, txtRemarks));
        btnCancel.addActionListener(ae -> dialog.dispose());

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(wrapper);
        dialog.setVisible(true);
    }

    private void saveProject(JDialog dialog, Integer projectId, boolean isEdit, JTextField txtProjectName,
            JTextArea txtDescription, JComboBox<String> cboStatus, JSpinner spinStartDate, JSpinner spinEndDate,
            JTextField txtProponent, JTextField txtBudget, JTextField txtProgress, JTextArea txtRemarks) {

        try {
            // Validation
            String projectName = txtProjectName.getText().trim();
            String description = txtDescription.getText().trim();
            String proponent = txtProponent.getText().trim();
            String budgetStr = txtBudget.getText().trim();
            String progressStr = txtProgress.getText().trim();

            if (projectName.isEmpty()) {
                ErrorHandler.showValidationError(dialog, "Project Name");
                txtProjectName.requestFocus();
                return;
            }

            if (proponent.isEmpty()) {
                ErrorHandler.showValidationError(dialog, "Proponent");
                txtProponent.requestFocus();
                return;
            }

            if (budgetStr.isEmpty()) {
                ErrorHandler.showValidationError(dialog, "Total Budget");
                txtBudget.requestFocus();
                return;
            }

            double totalBudget;
            try {
                totalBudget = Double.parseDouble(budgetStr);
                if (totalBudget < 0) {
                    ErrorHandler.showError(dialog, "Total Budget must be non-negative.");
                    txtBudget.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                ErrorHandler.showFormatError(dialog, "Total Budget", "number (e.g., 100000.00)");
                txtBudget.requestFocus();
                return;
            }

            int progress = 0;
            if (!progressStr.isEmpty()) {
                try {
                    progress = Integer.parseInt(progressStr);
                    if (progress < 0 || progress > 100) {
                        ErrorHandler.showError(dialog, "Progress must be between 0 and 100.");
                        txtProgress.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    ErrorHandler.showFormatError(dialog, "Progress", "number between 0 and 100");
                    txtProgress.requestFocus();
                    return;
                }
            }

            // Get date values
            java.util.Date startDateValue = (java.util.Date) spinStartDate.getValue();
            java.util.Date endDateValue = (java.util.Date) spinEndDate.getValue();

            Date sqlStartDate = new Date(startDateValue.getTime());
            Date sqlEndDate = endDateValue != null ? new Date(endDateValue.getTime()) : null;

            // Save to database
            try {
                boolean success;
                if (!isEdit) {
                    success = ProjectModel.addProject(
                        projectName,
                        description,
                        (String) cboStatus.getSelectedItem(),
                        sqlStartDate,
                        sqlEndDate,
                        proponent,
                        totalBudget,
                        0.0, // budget_utilized starts at 0
                        progress,
                        txtRemarks.getText().trim()
                    );
                } else {
                    success = ProjectModel.updateProject(
                        projectId,
                        projectName,
                        description,
                        (String) cboStatus.getSelectedItem(),
                        sqlStartDate,
                        sqlEndDate,
                        proponent,
                        totalBudget,
                        progress,
                        txtRemarks.getText().trim()
                    );
                }

                if (success) {
                    String newId = projectId != null ? String.valueOf(projectId) : "new";
                    Logger.logCRUDOperation(
                        isEdit ? "UPDATE" : "CREATE",
                        "Barangay_Project",
                        newId,
                        String.format("Project: %s, Status: %s", projectName, cboStatus.getSelectedItem())
                    );

                    ErrorHandler.showSuccess(dialog,
                        isEdit ? "Project updated successfully!" : "Project added successfully!");
                    dialog.dispose();
                    loadProjects();
                }
            } catch (SQLException e) {
                ErrorHandler.showError(dialog, "saving project", e);
            }

        } catch (Exception e) {
            ErrorHandler.showError(dialog, "saving project", e);
        }
    }

    private void deleteProject() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a project to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rowModel = table.convertRowIndexToModel(row);
        int projectId = (Integer) tableModel.getValueAt(rowModel, 0);
        String projectName = (String) tableModel.getValueAt(rowModel, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this project?\n\nProject: " + projectName,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = ProjectModel.deleteProject(projectId);

            if (success) {
                Logger.logCRUDOperation("DELETE", "Barangay_Project", String.valueOf(projectId),
                    String.format("Project: %s", projectName));

                JOptionPane.showMessageDialog(this, "Project deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProjects();
            }
        } catch (SQLException e) {
            ErrorHandler.showError(this, "deleting project", e);
        }
    }
}
