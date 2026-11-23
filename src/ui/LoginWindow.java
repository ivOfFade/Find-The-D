import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Login page
class LoginWindow extends GradientPanel {
    private String userRole;

    public LoginWindow(String role) {
        this.userRole = role;
        setLayout(new GridLayout(1, 2));

        add(FindTheDUI.createLeftPanel());

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 0, 6, 0); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField("Email");
        setupFieldStyles(emailField, "Email", false);
        rightPanel.add(emailField, gbc);

        gbc.gridy = 1;
        JPasswordField passwordField = new JPasswordField("Password");
        setupFieldStyles(passwordField, "Password", true);
        rightPanel.add(passwordField, gbc);

        gbc.gridy = 2;
        JButton loginButton = new JButton("Log In");
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        loginButton.setBackground(new Color(0x73, 0x73, 0x73));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        loginButton.addActionListener(e -> {
            try {
                DormService.login(fieldValue(emailField, "Email"), fieldValue(passwordField, "Password"), userRole);
                FindTheDUI.showDashboard(userRole);
            } catch (DormAppException ex) {
                RoundedDialog.showMessage(loginButton, "Message", ex.getMessage());
            }
        });
        rightPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(12, 0, 0, 0); 
        JLabel registerLabel = new JLabel("<html><div style='text-align:center;'><u>No Account? Register here</u></div></html>", SwingConstants.CENTER);
        registerLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        registerLabel.setForeground(GradientBackground.TEXT_SOFT);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 

        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FindTheDUI.showRegister(userRole);
            }
        });
        rightPanel.add(registerLabel, gbc);

        add(rightPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Text the user typed, or "" while the field still shows its hint
    static String fieldValue(JTextField field, String hint) {
        String text = field instanceof JPasswordField ? new String(((JPasswordField) field).getPassword()) : field.getText();
        return text.equals(hint) ? "" : text;
    }

    // text boxes for user input
    public static void setupFieldStyles(JTextField field, String hint, boolean isPassword) {
        field.setPreferredSize(new Dimension(300, 40));
        field.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        field.setForeground(Color.BLACK); 
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        if (isPassword && field instanceof JPasswordField) {
            ((JPasswordField) field).setEchoChar((char) 0);
        }

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String currentText = (isPassword && field instanceof JPasswordField) ? 
                        new String(((JPasswordField) field).getPassword()) : field.getText();
                
                if (currentText.equals(hint)) {
                    field.setText(""); 
                    field.setForeground(Color.BLACK); 
                    if (isPassword && field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar('•');
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = (isPassword && field instanceof JPasswordField) ? 
                        new String(((JPasswordField) field).getPassword()) : field.getText();

                if (currentText.trim().isEmpty()) {
                    if (isPassword && field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                    field.setText(hint); 
                    field.setForeground(Color.BLACK);
                }
            }
        });
    }
}
