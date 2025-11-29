package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import model.LoginModel;

public class Login extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckbox;

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

    public Login() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Inventory Management System - Login");
        setResizable(false);
        setBounds(100, 100, 400, 300);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(245, 245, 245));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblTitle = new JLabel("System Login");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setBounds(10, 11, 364, 36);
        contentPane.add(lblTitle);

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

        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        showPasswordCheckbox.setBounds(140, 140, 200, 25);
        showPasswordCheckbox.setBackground(new Color(245, 245, 245));
        showPasswordCheckbox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? '\0' : '•');
        });
        contentPane.add(showPasswordCheckbox);

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBounds(140, 180, 200, 35);
        btnLogin.setBackground(new Color(60, 179, 113));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contentPane.add(btnLogin);

        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(51, 102, 153));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(60, 179, 113));
            }
        });

        btnLogin.addActionListener(e -> performLogin());

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
    }

    private void performLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password!",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use LoginModel no-arg constructor which obtains its own connection.
        LoginModel loginModel = new LoginModel();

        if (!loginModel.hasConnection()) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to database. Please check configuration.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = loginModel.login(usernameOrEmail, password);

        if (success) {
            String role = loginModel.getRole(usernameOrEmail);

            JOptionPane.showMessageDialog(this,
                "Login successful!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            if ("1".equalsIgnoreCase(role)) {
                Dashboard dashboard = new Dashboard();
                dashboard.setVisible(true);
            } else {
                ClientDashboard dashboardUser = new ClientDashboard();
                dashboardUser.setVisible(true);
            }
            dispose(); // close login window

        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password!",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
}