package ui;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import db.DbConnection;

public class Dashboard extends JFrame {
    private JPanel sidePanel, mainPanel, statsPanel;
    private JButton btnHome, btnProducts, btnSuppliers, btnTransactions, btnLogout, btnReports;
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
        setTitle("Inventory System Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main container with padding
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainContainer);

        // Sidebar
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(200, getHeight()));
        sidePanel.setBackground(new Color(45, 52, 54));
        sidePanel.setLayout(new GridLayout(6, 1, 10, 10)); // Increased to accommodate logout button
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        btnHome = createMenuButton("Home", e -> showHomePanel());
        btnProducts = createMenuButton("Products", e -> showProductsPanel());
        btnSuppliers = createMenuButton("Suppliers", e -> showSuppliersPanel());
        btnTransactions = createMenuButton("Transactions", e -> showTransactionsPanel());
        
        
        // Create Logout button with different style
        btnLogout = createMenuButton("Logout", e -> performLogout());
        btnLogout.setBackground(new Color(192, 57, 43)); // Red color for logout
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(231, 76, 60));
            }
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(192, 57, 43));
            }
        });

        sidePanel.add(btnHome);
        sidePanel.add(btnProducts);
        sidePanel.add(btnSuppliers);
        sidePanel.add(btnTransactions);
        sidePanel.add(new JPanel()); // Spacer
        sidePanel.add(btnLogout);

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Add panels to frame
        mainContainer.add(sidePanel, BorderLayout.WEST);
        mainContainer.add(mainPanel, BorderLayout.CENTER);

        // Show home panel by default
        showHomePanel();
    }

    private JButton createMenuButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 180));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }

    private void showHomePanel() {
        mainPanel.removeAll();
        
        // Create statistics panel
        JPanel homePanel = new JPanel(new BorderLayout(10, 10));
        homePanel.setBackground(Color.WHITE);
        
        // Welcome header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        JLabel welcomeLabel = new JLabel("Welcome to Inventory Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);
        
        // Stats Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        statsGrid.setBackground(Color.WHITE);
        statsGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Total Products Card
        JPanel productsCard = createStatCard("Total Products", loadTotalProducts());
        statsGrid.add(productsCard);

        // Total Suppliers Card
        JPanel suppliersCard = createStatCard("Total Suppliers", loadTotalSuppliers());
        statsGrid.add(suppliersCard);

        // Category Stats Panel
        JPanel categoryStats = new JPanel(new BorderLayout(10, 10));
        categoryStats.setBorder(BorderFactory.createTitledBorder("Products by Category"));
        categoryStats.setBackground(Color.WHITE);
        
        // Create category breakdown
        JPanel categoryBreakdown = new JPanel(new GridLayout(0, 1, 5, 5));
        categoryBreakdown.setBackground(Color.WHITE);
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

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(240, 240, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(new Color(70, 130, 180));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private void loadCategoryStats(JPanel panel) {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT category, COUNT(*) as count FROM products GROUP BY category";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String category = rs.getString("category");
                int count = rs.getInt("count");
                
                JPanel categoryRow = new JPanel(new BorderLayout(10, 0));
                categoryRow.setBackground(Color.WHITE);
                
                JLabel categoryLabel = new JLabel(category);
                categoryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                
                JLabel countLabel = new JLabel(String.valueOf(count));
                countLabel.setFont(new Font("Arial", Font.BOLD, 14));
                
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

    private int loadTotalProducts() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM products");
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int loadTotalSuppliers() {
        try (Connection conn = DbConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM suppliers");
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
        mainPanel.add(new SupplierPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showTransactionsPanel() {
        mainPanel.removeAll();
        JLabel label = new JLabel("Transactions Panel (Coming Soon)", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(label);
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