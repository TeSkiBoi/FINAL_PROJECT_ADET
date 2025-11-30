package ui;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import db.DbConnection;
import model.SessionManager;
import model.User;
import model.UserModel;

public class Dashboard extends JFrame {
    private JPanel sidePanel, mainPanel, statsPanel;
    private JButton btnHome, btnProducts, btnSuppliers, btnTransactions, btnLogout, btnReports, btnStudents, btnResidents, btnHouseholds, btnUsers, btnLogs;
    private JLabel lblTotalProducts, lblTotalSuppliers;
    private JPanel categoryStatsPanel;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Always start with login screen
                Login loginFrame = new Login();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Dashboard() {
        setTitle("BMS Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container with padding
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainContainer);

        // Sidebar
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(220, getHeight()));
        sidePanel.setBackground(new Color(44,44,44)); // dark sidebar
        sidePanel.setLayout(new GridLayout(14, 1, 8, 8)); // accommodate buttons
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        btnHome = createMenuButton("Home", e -> showHomePanel());
        btnProducts = createMenuButton("Projects", e -> showProductsPanel());
        btnSuppliers = createMenuButton("Officials", e -> showSuppliersPanel());
        btnTransactions = createMenuButton("Blotter/Incidents", e -> showTransactionsPanel());
        btnStudents = createMenuButton("Projects (alt)", e -> showTransactionsPanel());
        btnResidents = createMenuButton("Residents", e -> showResidentsPanel());
        btnHouseholds = createMenuButton("Households", e -> showHouseholdsPanel());
        btnReports = createMenuButton("Financial", e -> showReportsPanel());
        btnUsers = createMenuButton("Users", e -> showUsersPanel());
        btnLogs = createMenuButton("Activity Log", e -> showLogsPanel());

        // Create Logout button with different style
        btnLogout = createMenuButton("Logout", e -> performLogout());
        btnLogout.setBackground(Theme.ACCENT);
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(Theme.ACCENT.darker());
            }
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(Theme.ACCENT);
            }
        });

        sidePanel.add(btnHome);
        sidePanel.add(btnProducts);
        sidePanel.add(btnSuppliers);
        sidePanel.add(btnTransactions);
        sidePanel.add(btnResidents);
        sidePanel.add(btnHouseholds);
        sidePanel.add(btnReports);
        sidePanel.add(new JPanel()); // Spacer

        // Show Users & Logs only to Admin (role_id == 1)
        User current = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = false;
        if (current != null && "1".equals(current.getRoleId())) isAdmin = true;
        if (isAdmin) {
            sidePanel.add(btnUsers);
            sidePanel.add(btnLogs);
        } else {
            // keep space alignment
            sidePanel.add(new JPanel());
            sidePanel.add(new JPanel());
        }

        sidePanel.add(new JPanel());
        sidePanel.add(btnLogout);

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.PRIMARY_LIGHT);

        // Add panels to frame
        mainContainer.add(sidePanel, BorderLayout.WEST);
        mainContainer.add(mainPanel, BorderLayout.CENTER);

        // Show home panel by default
        showHomePanel();
    }

    private JButton createMenuButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.PRIMARY);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }

    private void showHomePanel() {
        mainPanel.removeAll();
        JPanel homePanel = new JPanel(new BorderLayout(10, 10));
        homePanel.setBackground(Theme.PRIMARY_LIGHT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String welcomeText = "Welcome";
        String subText = "";
        if (currentUserAvailable()) {
            User current = SessionManager.getInstance().getCurrentUser();
            welcomeText = "Welcome, " + current.getFullname();
            UserModel um = new UserModel();
            String roleName = um.getRoleName(current.getRoleId());
            subText = roleName != null ? roleName : "";
        }

        JPanel titleWrap = new JPanel(new GridLayout(2,1));
        titleWrap.setBackground(Theme.PRIMARY_LIGHT);
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setForeground(Theme.PRIMARY);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleWrap.add(welcomeLabel);
        if (!subText.isEmpty()) {
            JLabel roleLabel = new JLabel(subText);
            roleLabel.setForeground(Theme.SECONDARY);
            roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            titleWrap.add(roleLabel);
        }
        headerPanel.add(titleWrap, BorderLayout.WEST);

        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        statsGrid.setBackground(Theme.PRIMARY_LIGHT);
        statsGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel projectsCard = createStatCard("Total Projects", loadTotalProjects());
        statsGrid.add(projectsCard);

        JPanel residentsCard = createStatCard("Total Residents", loadTotalResidents());
        statsGrid.add(residentsCard);

        JPanel categoryStats = new JPanel(new BorderLayout(10, 10));
        categoryStats.setBorder(BorderFactory.createTitledBorder("Projects Breakdown"));
        categoryStats.setBackground(Theme.PRIMARY_LIGHT);

        JPanel categoryBreakdown = new JPanel(new GridLayout(0, 1, 5, 5));
        categoryBreakdown.setBackground(Theme.PRIMARY_LIGHT);
        loadCategoryStats(categoryBreakdown);

        JScrollPane categoryScroll = new JScrollPane(categoryBreakdown);
        categoryScroll.setPreferredSize(new Dimension(300, 200));
        categoryStats.add(categoryScroll);

        statsGrid.add(categoryStats);

        homePanel.add(headerPanel, BorderLayout.NORTH);
        homePanel.add(statsGrid, BorderLayout.CENTER);

        mainPanel.add(homePanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private boolean currentUserAvailable() {
        return SessionManager.getInstance().getCurrentUser() != null;
    }

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Theme.PRIMARY_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.PRIMARY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Theme.PRIMARY);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Theme.SECONDARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadCategoryStats(JPanel panel) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT project_category AS category, COUNT(*) as count FROM barangay_projects GROUP BY project_category";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String category = rs.getString("category");
                int count = rs.getInt("count");

                JPanel categoryRow = new JPanel(new BorderLayout(10, 0));
                categoryRow.setBackground(Theme.PRIMARY_LIGHT);

                JLabel categoryLabel = new JLabel(category);
                categoryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                categoryLabel.setForeground(Theme.PRIMARY);

                JLabel countLabel = new JLabel(String.valueOf(count));
                countLabel.setFont(new Font("Arial", Font.BOLD, 14));
                countLabel.setForeground(Theme.PRIMARY);

                categoryRow.add(categoryLabel, BorderLayout.WEST);
                categoryRow.add(countLabel, BorderLayout.EAST);

                panel.add(categoryRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading category statistics: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private int loadTotalProjects() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM barangay_projects");
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int loadTotalResidents() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM residents");
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showProductsPanel() {
        mainPanel.removeAll();
        mainPanel.add(new ProductPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showSuppliersPanel() {
        mainPanel.removeAll();
        // show Barangay Officials panel instead of supplier wording
        mainPanel.add(new OfficialsPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showTransactionsPanel() {
        mainPanel.removeAll();
        // show Blotter/Incidents panel
        mainPanel.add(new BlotterPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showReportsPanel() {
        mainPanel.removeAll();
        // show Financial panel
        mainPanel.add(new FinancialPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showResidentsPanel() {
        mainPanel.removeAll();
        mainPanel.add(new ui.ResidentPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showHouseholdsPanel() {
        mainPanel.removeAll();
        mainPanel.add(new ui.HouseholdPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showUsersPanel() {
        mainPanel.removeAll();
        mainPanel.add(new UsersPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showLogsPanel() {
        mainPanel.removeAll();
        mainPanel.add(new ActivityLogPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Log out via SessionManager to record logout
            SessionManager.getInstance().logout();

            // Show goodbye message
            JOptionPane.showMessageDialog(
                this,
                "Thank you for using the system!",
                "Goodbye",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Create and show login form
            Login loginFrame = new Login();
            loginFrame.setVisible(true);

            // Close dashboard
            this.dispose();
        }
    }
}