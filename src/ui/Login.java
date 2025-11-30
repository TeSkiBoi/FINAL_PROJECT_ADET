package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import model.LoginModel;
import model.SessionManager;
import model.User;
import model.UserModel;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import theme.Theme;

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
        setTitle("BMS Admin Dashboard - Login");
        setResizable(false);
        setBounds(100, 100, 420, 320);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBackground(Theme.BACKGROUND);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblTitle = new JLabel("Barangay Biga - Login");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setBounds(10, 11, 384, 36);
        lblTitle.setForeground(Theme.PRIMARY);
        contentPane.add(lblTitle);

        JLabel lblUsername = new JLabel("Username or Email:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsername.setBounds(40, 70, 130, 25);
        contentPane.add(lblUsername);

        usernameField = new JTextField();
        usernameField.setBounds(180, 70, 180, 25);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                usernameField.getBorder(),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        contentPane.add(usernameField);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPassword.setBounds(40, 110, 130, 25);
        contentPane.add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(180, 110, 180, 25);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                passwordField.getBorder(),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        contentPane.add(passwordField);

        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        showPasswordCheckbox.setBounds(180, 140, 180, 25);
        showPasswordCheckbox.setBackground(Theme.BACKGROUND);
        showPasswordCheckbox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? '\0' : '•');
        });
        contentPane.add(showPasswordCheckbox);

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBounds(180, 180, 180, 40);
        btnLogin.setBackground(Theme.PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contentPane.add(btnLogin);

        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(Theme.PRIMARY.darker());
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(Theme.PRIMARY);
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

        LoginModel loginModel = new LoginModel();

        if (!loginModel.hasConnection()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to database. Please check configuration.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Password reset flow
            if (loginModel.userNeedsPasswordReset(usernameOrEmail)) {
                JPanel panel = new JPanel(new GridLayout(0, 1));
                JPasswordField pwd1 = new JPasswordField();
                JPasswordField pwd2 = new JPasswordField();
                panel.add(new JLabel("Set a new password for your account:"));
                panel.add(new JLabel("Password:")); panel.add(pwd1);
                panel.add(new JLabel("Confirm Password:")); panel.add(pwd2);
                int res = JOptionPane.showConfirmDialog(this, panel, "Set Password",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (res != JOptionPane.OK_OPTION) return;

                String p1 = new String(pwd1.getPassword());
                String p2 = new String(pwd2.getPassword());
                if (p1.isEmpty() || !p1.equals(p2)) {
                    JOptionPane.showMessageDialog(this,
                            "Passwords do not match or are empty.", "Password Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok = loginModel.setUserPassword(usernameOrEmail, p1);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to set password. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                password = p1;
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error setting password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Login attempt
        boolean success = loginModel.login(usernameOrEmail, password);

        if (success) {
            // Set current user in session
            User currentUser = new UserModel().getUserByUsernameOrEmail(usernameOrEmail);
            SessionManager.getInstance().setCurrentUser(currentUser);

            // Log successful login
            util.Logger.logAuthenticationAttempt(currentUser.getUsername(), true, "127.0.0.1");
            
            JOptionPane.showMessageDialog(this,
                    "Login successful! Welcome " + currentUser.getFullname(), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

            // Open dashboard based on role
            String roleId = currentUser.getRoleId();
            
            if ("1".equals(roleId)) {
                // Admin - Full access to Dashboard
                util.Logger.logInfo("Admin user " + currentUser.getUsername() + " accessed full dashboard");
                new Dashboard().setVisible(true);
            } else if ("2".equals(roleId)) {
                // Staff - Limited access to Dashboard
                // The Dashboard already checks role and shows limited menu
                util.Logger.logInfo("Staff user " + currentUser.getUsername() + " accessed limited dashboard");
                new Dashboard().setVisible(true);
            } else {
                // Other roles - ClientDashboard (if exists) or default Dashboard
                try {
                    util.Logger.logInfo("User " + currentUser.getUsername() + " accessed client dashboard");
                    new ClientDashboard().setVisible(true);
                } catch (Exception ex) {
                    // If ClientDashboard doesn't work, use regular Dashboard
                    util.Logger.logWarning("ClientDashboard not available, using default Dashboard for " + currentUser.getUsername());
                    new Dashboard().setVisible(true);
                }
            }

            dispose(); // close login window
        } else {
            // Log failed login attempt
            util.Logger.logAuthenticationAttempt(usernameOrEmail, false, "127.0.0.1");
            util.Logger.logWarning("Failed login attempt for username/email: " + usernameOrEmail);
            
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password!", "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
}
