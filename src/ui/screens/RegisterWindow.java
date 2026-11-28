package ui.screens;
import ui.components.*;
import exception.ServiceOperationException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import service.DormService;
import ui.components.GradientPanel;
import ui.FindTheDUI;

// Register page
public class RegisterWindow extends GradientPanel {
    
    public RegisterWindow(String role) {
        setLayout(new GridLayout(1, 2));

        add(FindTheDUI.createLeftPanel());

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.insets = new Insets(5, 0, 5, 0); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField("Name*");
        LoginWindow.setupFieldStyles(nameField, "Name*", false);
        rightPanel.add(nameField, gbc);

        gbc.gridy = 1;
        JTextField ageField = new JTextField("Age*");
        LoginWindow.setupFieldStyles(ageField, "Age*", false);
        rightPanel.add(ageField, gbc);

        gbc.gridy = 2;
        JTextField phoneField = new JTextField("Mobile Number*");
        LoginWindow.setupFieldStyles(phoneField, "Mobile Number*", false);
        rightPanel.add(phoneField, gbc);

        gbc.gridy = 3;
        JTextField emailField = new JTextField("Email*");
        LoginWindow.setupFieldStyles(emailField, "Email*", false);
        rightPanel.add(emailField, gbc);
        JTextField otherAccountField = null;
        JPasswordField passwordField;
        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setOpaque(false);
        passwordRow.setPreferredSize(new Dimension(300, 40));
        LoginWindow.EyeButton visibilityButton = new LoginWindow.EyeButton();
        visibilityButton.setToolTipText("Show password");
        visibilityButton.setFocusable(false);
        visibilityButton.setEnabled(false);
        if (role.equalsIgnoreCase("Landlord")) {
            gbc.gridy = 4;
            otherAccountField = new JTextField("Facebook Account");
            LoginWindow.setupFieldStyles(otherAccountField, "Facebook Account", false);
            rightPanel.add(otherAccountField, gbc);
            gbc.gridy = 5;
            passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            passwordRow.add(passwordField, BorderLayout.CENTER);
            passwordRow.add(visibilityButton, BorderLayout.EAST);
            rightPanel.add(passwordRow, gbc);
        } else {
            gbc.gridy = 4;
            passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            passwordRow.add(passwordField, BorderLayout.CENTER);
            passwordRow.add(visibilityButton, BorderLayout.EAST);
            rightPanel.add(passwordRow, gbc);
        }
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(0xD0, 0xD0, 0xD0)),
            new EmptyBorder(5, 10, 5, 6)
        ));
        passwordField.setEchoChar((char) 0);
        final char passwordEcho = '\u2022';
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateVisibilityControl() {
                String value = new String(passwordField.getPassword());
                boolean hasPassword = !value.isEmpty() && !value.equals("Password*");
                boolean isVisible = visibilityButton.isEnabled() && passwordField.getEchoChar() == 0;
                passwordField.setEchoChar(hasPassword && !isVisible ? passwordEcho : (char) 0);
                visibilityButton.setEnabled(hasPassword);
                if (!hasPassword) {
                    visibilityButton.setPasswordVisible(false);
                    visibilityButton.setToolTipText("Show password");
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateVisibilityControl(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateVisibilityControl(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateVisibilityControl(); }
        });
        visibilityButton.addActionListener(e -> {
            if (!visibilityButton.isEnabled()) return;
            boolean showPassword = passwordField.getEchoChar() != 0;
            passwordField.setEchoChar(showPassword ? (char) 0 : passwordEcho);
            visibilityButton.setPasswordVisible(showPassword);
            visibilityButton.setToolTipText(showPassword ? "Show password" : "Hide password");
        });
        final JTextField facebookField = otherAccountField;
        final JPasswordField pwField = passwordField;
        gbc.gridy = 6;
        gbc.insets = new Insets(12, 0, 0, 0);
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(300, 45));
        registerButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        registerButton.setBackground(new Color(0x73, 0x73, 0x73));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        registerButton.addActionListener(e -> {
            try {
                int age = Integer.parseInt(LoginWindow.fieldValue(ageField, "Age*").trim());
                DormService.register(role,
                    LoginWindow.fieldValue(nameField, "Name*"), age,
                    LoginWindow.fieldValue(phoneField, "Mobile Number*"),
                    LoginWindow.fieldValue(emailField, "Email*"),
                    LoginWindow.fieldValue(pwField, "Password*"),
                    facebookField == null ? null : LoginWindow.fieldValue(facebookField, "Facebook Account"));
                FindTheDUI.showDashboard(role);
            } catch (NumberFormatException ex) {
                RoundedDialog.showMessage(registerButton, "Message", "Please enter a valid age.");
            } catch (ServiceOperationException ex) {
                RoundedDialog.showMessage(registerButton, "Message", ex.getMessage());
            }
        });
        rightPanel.add(registerButton, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(12, 0, 0, 0);
        JLabel loginLabel = new JLabel("<html><div style='text-align:center;'><u>Already have an account? Log in here</u></div></html>", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        loginLabel.setForeground(GradientBackground.TEXT_SOFT);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FindTheDUI.showLogin(role);
            }
        });
        rightPanel.add(loginLabel, gbc);

        add(rightPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }
}
