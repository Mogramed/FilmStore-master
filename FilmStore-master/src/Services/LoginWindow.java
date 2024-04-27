package Services;

import Entities.Admin;
import Entities.User;
import Manager.CSVManager;
import Manager.FilmManager;
import Manager.UserManager;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {
    private JButton adminLoginButton;
    private JButton userLoginButton;
    private JButton createAccountButton;
    private static final String ADMIN_TOKEN = "mabelle";

    private static FilmManager filmManager = new FilmManager();

    public LoginWindow(FilmManager filmManager) {
        super("Login or Sign Up");
        this.filmManager = filmManager;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUI() {
        setLayout(new FlowLayout());

        adminLoginButton = new JButton("Login as Admin");
        userLoginButton = new JButton("Login as User");
        createAccountButton = new JButton("Sign Up");

        adminLoginButton.addActionListener(e -> showLoginDialog(true));
        userLoginButton.addActionListener(e -> showLoginDialog(false));
        createAccountButton.addActionListener(e -> showSignUpDialog());

        add(adminLoginButton);
        add(userLoginButton);
        add(createAccountButton);
    }

    private void showLoginDialog(boolean isAdmin) {
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField secretOrTokenField = new JTextField(20);
        JButton loginButton = new JButton("Login");

        // Set up fields for email and password
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginDialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        loginDialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginDialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginDialog.add(passwordField, gbc);

        // Configure field based on account type
        String label = isAdmin ? "Token:" : "Secret Phrase:";
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginDialog.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        loginDialog.add(secretOrTokenField, gbc);

        // Configure login button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginDialog.add(loginButton, gbc);

        // Set action on login button click
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String secretOrToken = secretOrTokenField.getText();
            User loggedInUser = UserManager.authenticate(email, password, secretOrToken, isAdmin);

            boolean validToken = isAdmin && ADMIN_TOKEN.equals(secretOrToken);
            boolean validUser = loggedInUser != null && (isAdmin ? validToken : true);

            if (validUser) {
                SessionContext.setCurrentUser(loggedInUser);  // Store the logged-in user in session context.
                JOptionPane.showMessageDialog(loginDialog, "Login Successful");
                loginDialog.dispose();

                // Display appropriate UI based on the user type
                if (isAdmin) {
                    SwingUtilities.invokeLater(() -> new AdminFilmDisplay(filmManager));
                } else {
                    SwingUtilities.invokeLater(() -> new FilmDisplay(filmManager));
                }
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Login Failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginDialog.pack();
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setVisible(true);
    }


    private void showSignUpDialog() {
        JDialog signUpDialog = new JDialog(this, "Sign Up", true);
        signUpDialog.setLayout(new GridBagLayout());
        signUpDialog.setSize(1200, 800);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField emailField = new JTextField(20);
        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField secretPhraseField = new JTextField(20);
        JTextField tokenField = new JTextField(20);
        JButton signUpButton = new JButton("Sign Up");

        JRadioButton adminRadioButton = new JRadioButton("Admin Account");
        JRadioButton userRadioButton = new JRadioButton("User Account", true);
        ButtonGroup accountTypeGroup = new ButtonGroup();
        accountTypeGroup.add(adminRadioButton);
        accountTypeGroup.add(userRadioButton);

        // Configure layout for fields
        // Email, First Name, Last Name, Password are common to both admin and user
        int gridY = 0;
        for (JComponent[] pair : new JComponent[][]{
                {new JLabel("Email:"), emailField},
                {new JLabel("First Name:"), firstNameField},
                {new JLabel("Last Name:"), lastNameField},
                {new JLabel("Password:"), passwordField},
                {new JLabel("Address:"), addressField},
                {new JLabel("Phone Number:"), phoneField},
                {new JLabel("Secret Phrase:"), secretPhraseField},
                {new JLabel("Token (Admins only):"), tokenField}
        }) {
            gbc.gridx = 0;
            gbc.gridy = gridY;
            signUpDialog.add(pair[0], gbc);
            gbc.gridx = 1;
            signUpDialog.add(pair[1], gbc);
            gridY++;

        }

        // Add account type radios
        gbc.gridx = 0;
        gbc.gridy = gridY;
        signUpDialog.add(new JLabel("Account Type:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(adminRadioButton, gbc);
        gbc.gridx = 2;
        signUpDialog.add(userRadioButton, gbc);

        // Adjust visibility based on account type
        adminRadioButton.addActionListener(e -> {
            boolean isSelected = adminRadioButton.isSelected();
            tokenField.setVisible(isSelected);
            addressField.setVisible(!isSelected);
            phoneField.setVisible(!isSelected);
            secretPhraseField.setVisible(!isSelected);
            signUpDialog.revalidate();
        });

        userRadioButton.addActionListener(e -> {
            boolean isSelected = userRadioButton.isSelected();
            tokenField.setVisible(!isSelected);
            addressField.setVisible(isSelected);
            phoneField.setVisible(isSelected);
            secretPhraseField.setVisible(isSelected);
        });

        // Setup button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        signUpDialog.add(signUpButton, gbc);

        signUpButton.addActionListener(e -> {
            boolean isAdmin = adminRadioButton.isSelected();
            String token = tokenField.getText();
            if (isAdmin && !ADMIN_TOKEN.equals(token)) {
                JOptionPane.showMessageDialog(signUpDialog, "Invalid token for admin account.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean success = false;
            if (isAdmin) {
                Admin admin = new Admin(null, firstNameField.getText(), lastNameField.getText(), emailField.getText(), new String(passwordField.getPassword()), token, true);
                success = UserManager.addAdmin(admin);
            } else {
                int phoneNumber = Integer.parseInt(phoneField.getText());
                User user = new User(null, firstNameField.getText(), lastNameField.getText(), emailField.getText(), new String(passwordField.getPassword()), addressField.getText(), secretPhraseField.getText(), phoneNumber, false);
                success = UserManager.addUser(user);
            }
            if (success) {
                JOptionPane.showMessageDialog(signUpDialog, "Account Created Successfully");
                signUpDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(signUpDialog, "Failed to Create Account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Initialize dialog visibility settings
        tokenField.setVisible(adminRadioButton.isSelected());
        addressField.setVisible(userRadioButton.isSelected());
        phoneField.setVisible(userRadioButton.isSelected());
        secretPhraseField.setVisible(userRadioButton.isSelected());

        signUpDialog.pack();
        signUpDialog.setLocationRelativeTo(this);
        signUpDialog.setVisible(true);
    }
}
