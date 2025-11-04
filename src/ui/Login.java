package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import db.DbConnection;

public class Login extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckbox;

    /**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Login frame = new Login();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Inventory Management System - Login");
		setResizable(false);
		setBounds(100, 100, 400, 300);
		setLocationRelativeTo(null);
		
		// Main Panel
		contentPane = new JPanel();
		contentPane.setBackground(new Color(245, 245, 245));
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Title Label
		JLabel lblTitle = new JLabel("System Login");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
		lblTitle.setBounds(10, 11, 364, 36);
		contentPane.add(lblTitle);

		// Username Panel
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
		lblUsername.setBounds(50, 70, 80, 25);
		contentPane.add(lblUsername);

		usernameField = new JTextField();
		usernameField.setBounds(140, 70, 200, 25);
		usernameField.setBorder(BorderFactory.createCompoundBorder(
            usernameField.getBorder(), 
            BorderFactory.createEmptyBorder(0, 5, 0, 5)));
		contentPane.add(usernameField);

		// Password Panel
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
		lblPassword.setBounds(50, 110, 80, 25);
		contentPane.add(lblPassword);

		passwordField = new JPasswordField();
		passwordField.setBounds(140, 110, 200, 25);
		passwordField.setBorder(BorderFactory.createCompoundBorder(
            passwordField.getBorder(), 
            BorderFactory.createEmptyBorder(0, 5, 0, 5)));
		contentPane.add(passwordField);

		// Show Password Checkbox
		showPasswordCheckbox = new JCheckBox("Show Password");
		showPasswordCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
		showPasswordCheckbox.setBounds(140, 140, 200, 25);
		showPasswordCheckbox.setBackground(new Color(245, 245, 245));
		showPasswordCheckbox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? '\0' : '•');
        });
		contentPane.add(showPasswordCheckbox);

		// Login Button
		JButton btnLogin = new JButton("Login");
		btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
		btnLogin.setBounds(140, 180, 200, 35);
		btnLogin.setBackground(new Color(70, 130, 180));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFocusPainted(false);
		btnLogin.setBorderPainted(false);
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		contentPane.add(btnLogin);

		// Add hover effect
		btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(51, 102, 153));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(70, 130, 180));
            }
        });

		// Login action
		btnLogin.addActionListener(e -> performLogin());

		// Add key listeners for Enter key
		usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });

        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // Create users table if it doesn't exist
        //createUsersTableIfNeeded();
	}

//    private void createUsersTableIfNeeded() {
//        try (Connection conn = DbConnection.getConnection()) {
//            DatabaseMetaData meta = conn.getMetaData();
//            ResultSet tables = meta.getTables(null, null, "users", null);
//            
//            if (!tables.next()) {
//                Statement stmt = conn.createStatement();
//                String sql = """
//                    CREATE TABLE users (
//                        id INT PRIMARY KEY AUTO_INCREMENT,
//                        username VARCHAR(50) NOT NULL UNIQUE,
//                        password VARCHAR(255) NOT NULL,
//                        full_name VARCHAR(100) NOT NULL,
//                        email VARCHAR(100),
//                        role VARCHAR(20) DEFAULT 'user',
//                        status VARCHAR(20) DEFAULT 'Active',
//                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//                    )
//                """;
//                stmt.executeUpdate(sql);
//                
//                // Insert default admin user
//                sql = """
//                    INSERT INTO users (username, password, full_name, role, email)
//                    VALUES ('admin', 'admin123', 'System Administrator', 'admin', 'admin@system.com')
//                """;
//                stmt.executeUpdate(sql);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this,
//                "Error initializing database: " + e.getMessage(),
//                "Database Error",
//                JOptionPane.ERROR_MESSAGE);
//        }
//    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password!",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'Active'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Show success message with user's full name
                String fullName = rs.getString("full_name");
                JOptionPane.showMessageDialog(this,
                    "Welcome, " + fullName + "!\nLogin successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Create and show dashboard
                Dashboard dashboard = new Dashboard();
                dashboard.setVisible(true);
                dispose(); // Close login window
            } else {
                // Login failed
                JOptionPane.showMessageDialog(this,
                    "Invalid username or password!",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}