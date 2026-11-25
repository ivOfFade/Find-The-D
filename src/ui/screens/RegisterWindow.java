package ui.screens;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import ui.FindTheDUI;
import ui.components.GradientBackground;
import ui.components.GradientPanel;
import ui.components.RoundedDialog;
import exception.ServiceOperationException;
import service.DormService;
import ui.components.UiFormat;
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
        if (role.equalsIgnoreCase("Landlord")) {
            gbc.gridy = 4;
            otherAccountField = new JTextField("Facebook Account");
            LoginWindow.setupFieldStyles(otherAccountField, "Facebook Account", false);
            rightPanel.add(otherAccountField, gbc);
            gbc.gridy = 5;
            passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            rightPanel.add(passwordField, gbc);
        } else {
            gbc.gridy = 4;
            passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            rightPanel.add(passwordField, gbc);
        }
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
                int age = UiFormat.parseWhole(LoginWindow.fieldValue(ageField, "Age*"), "Age");
                DormService.register(role,
                    LoginWindow.fieldValue(nameField, "Name*"), age,
                    LoginWindow.fieldValue(phoneField, "Mobile Number*"),
                    LoginWindow.fieldValue(emailField, "Email*"),
                    LoginWindow.fieldValue(pwField, "Password*"),
                    facebookField == null ? null : LoginWindow.fieldValue(facebookField, "Facebook Account"));
                FindTheDUI.showDashboard(role);
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
