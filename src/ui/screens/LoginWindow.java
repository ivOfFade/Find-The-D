package ui.screens;
import ui.components.*;
import exception.ServiceOperationException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import service.DormService;
import ui.components.GradientPanel;
import ui.FindTheDUI;

// Login page
public class LoginWindow extends GradientPanel {
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

        JLabel roleLabel = new JLabel("Logging in as " + userRole);
        roleLabel.setFont(new Font("Georgia", Font.BOLD, 15));
        roleLabel.setForeground(Color.WHITE);
        rightPanel.add(roleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 6, 0);

        JTextField emailField = new JTextField("Email");
        setupFieldStyles(emailField, "Email", false);
        rightPanel.add(emailField, gbc);

        gbc.gridy = 2;
        JPasswordField passwordField = new JPasswordField("Password");
        setupFieldStyles(passwordField, "Password", true);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(0xD0, 0xD0, 0xD0)),
            new EmptyBorder(5, 10, 5, 6)
        ));
        // Keep the hint readable; masking starts when a real password is entered.
        passwordField.setEchoChar((char) 0);
        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setOpaque(false);
        passwordRow.setPreferredSize(new Dimension(300, 40));
        passwordRow.add(passwordField, BorderLayout.CENTER);
        EyeButton visibilityButton = new EyeButton();
        visibilityButton.setToolTipText("Show password");
        visibilityButton.setFocusable(false);
        visibilityButton.setEnabled(false);
        final char passwordEcho = '\u2022';
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateVisibilityControl() {
                String value = new String(passwordField.getPassword());
                boolean hasPassword = !value.isEmpty() && !value.equals("Password");
                boolean isVisible = visibilityButton.isEnabled()
                    && passwordField.getEchoChar() == 0;
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
        passwordRow.add(visibilityButton, BorderLayout.EAST);
        rightPanel.add(passwordRow, gbc);

        gbc.gridy = 3;
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
            } catch (ServiceOperationException ex) {
                ex.printStackTrace();
                RoundedDialog.showMessage(loginButton, "Message", ex.getMessage());
            }
        });
        rightPanel.add(loginButton, gbc);

        gbc.gridy = 4;
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

        gbc.gridy = 5;
        gbc.insets = new Insets(16, 0, 0, 0);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel backLabel = new JLabel("<html><u>\u2190 Back</u></html>");
        backLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        backLabel.setForeground(GradientBackground.TEXT_SOFT);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FindTheDUI.showWelcome();
            }
        });
        rightPanel.add(backLabel, gbc);


        add(rightPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Text the user typed, or "" while the field still shows its hint
    static String fieldValue(JTextField field, String hint) {
        String text = field instanceof JPasswordField ? new String(((JPasswordField) field).getPassword()) : field.getText();
        return text.equals(hint) ? "" : text;
    }

    /** Compact, consistently rendered eye control for the password field. */
    static final class EyeButton extends JButton {
        private boolean passwordVisible;

        EyeButton() {
            setPreferredSize(new Dimension(42, 40));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new MatteBorder(1, 0, 1, 1, new Color(0xD0, 0xD0, 0xD0)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setPasswordVisible(boolean visible) {
            passwordVisible = visible;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getModel().isPressed() ? new Color(0xE9, 0xEC, 0xF2)
                : getModel().isRollover() ? new Color(0xF4, 0xF6, 0xF9) : Color.WHITE;
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0x5D, 0x66, 0x73));
            g.draw(new java.awt.geom.Arc2D.Double(cx - 9, cy - 6, 18, 12, 0, 180, java.awt.geom.Arc2D.OPEN));
            g.draw(new java.awt.geom.Arc2D.Double(cx - 9, cy - 6, 18, 12, 180, 180, java.awt.geom.Arc2D.OPEN));
            g.drawOval(cx - 2, cy - 2, 4, 4);
            if (!passwordVisible) {
                g.drawLine(cx - 10, cy + 9, cx + 10, cy - 9);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
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

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String currentText = (isPassword && field instanceof JPasswordField) ? 
                        new String(((JPasswordField) field).getPassword()) : field.getText();
                
                if (currentText.equals(hint)) {
                    field.setText(""); 
                    field.setForeground(Color.BLACK); 
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = (isPassword && field instanceof JPasswordField) ? 
                        new String(((JPasswordField) field).getPassword()) : field.getText();

                if (currentText.trim().isEmpty()) {
                    field.setText(hint); 
                    field.setForeground(Color.BLACK);
                }
            }
        });
    }
}
