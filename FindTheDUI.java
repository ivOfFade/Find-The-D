import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

public class FindTheDUI extends JFrame {

    public static final Color LIGHT_GRAY_BG = Color.decode("#5b5f5d");

    // Screen names used by the main window's CardLayout
    private static final String WELCOME = "Welcome";
    private static final String LOGIN = "Login";
    private static final String REGISTER = "Register";
    private static final String DASHBOARD = "Dashboard";

    private static FindTheDUI instance;
    private final CardLayout screens = new CardLayout();
    private final JPanel root = new JPanel(screens);

    // The one and only window: every screen is a panel swapped in here
    public FindTheDUI() {
        setTitle("Find The D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setContentPane(root);
        instance = this;
        showWelcome();
    }

    // Puts a fresh panel on screen under the given name, replacing the old one
    private void display(String name, JPanel panel) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) {
                root.remove(c);
            }
        }
        panel.setName(name);
        root.add(panel, name);
        screens.show(root, name);
        root.revalidate();
        root.repaint();
    }

    // ---------- Navigation (used by every screen) ----------
    public static void showWelcome() {
        instance.display(WELCOME, new WelcomePanel());
    }

    public static void showLogin(String role) {
        instance.display(LOGIN, new LoginWindow(role));
    }

    public static void showRegister(String role) {
        instance.display(REGISTER, new RegisterWindow(role));
    }

    public static void showDashboard(String role) {
        if (role.equalsIgnoreCase("Landlord")) {
            instance.display(DASHBOARD, new LandlordDashboard());
        } else {
            instance.display(DASHBOARD, new TenantDashboard());
        }
    }

    private static ImageIcon LOGO;

    public static JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 10, 20, 10);
        gbcLeft.anchor = GridBagConstraints.CENTER;

        if (LOGO == null) {
            ImageIcon originalIcon = new ImageIcon("Logo.png");
            LOGO = new ImageIcon(originalIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH));
        }

        JLabel logoLabel = new JLabel(LOGO);
        leftPanel.add(logoLabel, gbcLeft);

        gbcLeft.gridy = 1;
        JLabel titleLabel = new JLabel("Find The D");
        titleLabel.setFont(new Font("Broadway", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        leftPanel.add(titleLabel, gbcLeft);

        return leftPanel;
    }

    // Log out pop up confirmation
    public static void showLogoutConfirmationDialog(Component parent) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(500, 220);
        dialog.setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(new Color(0xBC, 0xBC, 0xBC));
        headerBar.setPreferredSize(new Dimension(500, 40));
        JLabel headerTitle = new JLabel("  Log out");
        headerTitle.setFont(new Font("Brittanic Bold", Font.PLAIN, 16));
        headerTitle.setForeground(Color.WHITE);
        headerBar.add(headerTitle, BorderLayout.WEST);

        JPanel mainCanvas = new JPanel(new GridBagLayout());
        mainCanvas.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 30, 15, 30);
        JLabel messageLabel = new JLabel("Are you sure?");
        messageLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 44));
        mainCanvas.add(messageLabel, gbc);

        gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(0, 0, 15, 30);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);

        JButton yesBtn = new JButton("Yes");
        yesBtn.setPreferredSize(new Dimension(80, 36));
        yesBtn.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        yesBtn.setBackground(new Color(0xDD, 0xDD, 0xDD));
        yesBtn.setBorder(new EmptyBorder(0,0,0,0));
        yesBtn.setFocusPainted(false);

        JButton noBtn = new JButton("No");
        noBtn.setPreferredSize(new Dimension(80, 36));
        noBtn.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        noBtn.setBackground(Color.WHITE);
        noBtn.setBorder(new LineBorder(Color.BLACK, 1));
        noBtn.setFocusPainted(false);

        yesBtn.addActionListener(e -> {
            dialog.dispose();
            showWelcome();
        });

        noBtn.addActionListener(e -> dialog.dispose());

        buttonRow.add(yesBtn);
        buttonRow.add(noBtn);
        mainCanvas.add(buttonRow, gbc);

        contentPanel.add(headerBar, BorderLayout.NORTH);
        contentPanel.add(mainCanvas, BorderLayout.CENTER);
        dialog.add(contentPanel);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    // starts the program, para magshow yung main page
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            FloatingButtons.install();
            new FindTheDUI().setVisible(true);
        });
    }
}

// choosing whether youre a landlord or tenant page
class WelcomePanel extends GradientPanel {
    public WelcomePanel() {
        setLayout(new GridLayout(1, 2));

        JPanel leftPanel = FindTheDUI.createLeftPanel();

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.insets = new Insets(10, 0, 10, 0);
        gbcRight.fill = GridBagConstraints.HORIZONTAL;

        JButton landlordButton = new JButton("I'm a Landlord");
        landlordButton.setPreferredSize(new Dimension(300, 45));
        landlordButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        landlordButton.setBackground(new Color(0x73, 0x73, 0x73));
        landlordButton.setForeground(Color.WHITE);
        landlordButton.setFocusPainted(false);
        landlordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightPanel.add(landlordButton, gbcRight);

        gbcRight.gridy = 1;
        JButton tenantButton = new JButton("I'm a Tenant");
        tenantButton.setPreferredSize(new Dimension(300, 45));
        tenantButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        tenantButton.setBackground(Color.WHITE);
        tenantButton.setForeground(Color.BLACK);
        tenantButton.setFocusPainted(false);
        tenantButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        rightPanel.add(tenantButton, gbcRight);

        landlordButton.addActionListener(e -> FindTheDUI.showLogin("Landlord"));
        tenantButton.addActionListener(e -> FindTheDUI.showLogin("Tenant"));

        add(leftPanel);
        add(rightPanel);
    }
}

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
            FindTheDUI.showDashboard(userRole);
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

// Register page
class RegisterWindow extends GradientPanel {
    
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
        if (role.equalsIgnoreCase("Landlord")) {
            gbc.gridy = 4;
            JTextField otherAccountField = new JTextField("Facebook Account");
            LoginWindow.setupFieldStyles(otherAccountField, "Facebook Account", false);
            rightPanel.add(otherAccountField, gbc);
            gbc.gridy = 5;
            JPasswordField passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            rightPanel.add(passwordField, gbc);
        } else {
            gbc.gridy = 4;
            JPasswordField passwordField = new JPasswordField("Password*");
            LoginWindow.setupFieldStyles(passwordField, "Password*", true);
            rightPanel.add(passwordField, gbc);
        }
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
            FindTheDUI.showDashboard(role);
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

// Landlord page
class LandlordDashboard extends JPanel {
    private Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private JLabel totalDormsValue;
    private JLabel totalTenantsValue;
    private JLabel complaintsValue;
    private JPanel dormRowsPanel;

    // My Dorm feed width: share of the available space, and the narrowest it will ever get
    // Same size as the tenant's Browse feed: the tenant's feed sits beside a filter panel, so that
    // panel's room (200px) is taken off before the same percentage/minimum is applied.
    private static final int FEED_WIDTH_PERCENT = 95;
    private static final int FEED_MIN_WIDTH = 492;
    private static final int TENANT_LAYOUT_EXTRA = 200;

    public LandlordDashboard() {
        setLayout(new BorderLayout());
        
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SidebarItem.SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(200, 600));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(25, 12, 25, 12));
        JLabel menuHeader = createMenuLink("Menu", SidebarItem.Glyph.DASHBOARD);
        JLabel linkMyDorm = createMenuLink("My Dorm", SidebarItem.Glyph.BUILDING);
        JLabel linkNotification = createMenuLink("Notification", SidebarItem.Glyph.BELL);
        JLabel linkLogOut = createMenuLink("Log Out", SidebarItem.Glyph.LOGOUT);
        sidebarPanel.add(menuHeader);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(linkMyDorm);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(linkNotification);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(linkLogOut);
        
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        JPanel overviewCard = createOverviewCardView();
        JPanel myDormCard = createMyDormCardView();
        JPanel notificationCard = createNotificationCardView();
        cardsContainer.add(overviewCard, "Overview");
        cardsContainer.add(myDormCard, "MyDorm");
        cardsContainer.add(notificationCard, "Notification");
        
        menuHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshOverview();
                cardLayout.show(cardsContainer, "Overview");
                setUnderlined(menuHeader, true);
                setUnderlined(linkMyDorm, false);
                setUnderlined(linkNotification, false);
            }
        });
        linkMyDorm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsContainer, "MyDorm");
                setUnderlined(menuHeader, false);
                setUnderlined(linkMyDorm, true);
                setUnderlined(linkNotification, false);
            }
        });
        linkNotification.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsContainer, "Notification");
                setUnderlined(menuHeader, false);
                setUnderlined(linkMyDorm, false);
                setUnderlined(linkNotification, true);
            }
        });
        linkLogOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FindTheDUI.showLogoutConfirmationDialog(LandlordDashboard.this);
            }
        });
        add(sidebarPanel, BorderLayout.WEST);
        add(cardsContainer, BorderLayout.CENTER);
        cardLayout.show(cardsContainer, "Overview");
        setUnderlined(menuHeader, true);   // Overview is the page shown first
    }

    // Overview
    private JPanel createOverviewCardView() {
        JPanel mainCanvas = new GradientPanel(new GridBagLayout());
        mainCanvas.setBackground(FindTheDUI.LIGHT_GRAY_BG);
        mainCanvas.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel sectionOverviewTitle = new JLabel("Overview");
        sectionOverviewTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        sectionOverviewTitle.setForeground(GradientBackground.TEXT);
        mainCanvas.add(sectionOverviewTitle, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 25, 0);

        // overview labels
        JPanel metricsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsContainer.setOpaque(false);
        totalDormsValue = new JLabel("00");
        totalTenantsValue = new JLabel("00");
        complaintsValue = new JLabel("00");
        metricsContainer.add(createMetricCard("Total Dorms", totalDormsValue));
        metricsContainer.add(createMetricCard("Total Tenants", totalTenantsValue));
        metricsContainer.add(createMetricCard("Complaints", complaintsValue));
        mainCanvas.add(metricsContainer, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);

        // Dorm List
        JLabel listTitleLabel = new JLabel("Dorm List");
        listTitleLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        listTitleLabel.setForeground(GradientBackground.TEXT);
        mainCanvas.add(listTitleLabel, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(10, 0, 0, 0); gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        RoundedPanel dataTableWrapper = new RoundedPanel(new GridBagLayout());
        dataTableWrapper.setBackground(GradientBackground.GLASS);
        dataTableWrapper.setOutline(GradientBackground.GLASS_LINE);
        dataTableWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints tableGbc = new GridBagConstraints();
        tableGbc.gridx = 0; tableGbc.gridy = 0; tableGbc.weightx = 1.0;
        tableGbc.fill = GridBagConstraints.HORIZONTAL;
        tableGbc.insets = new Insets(0, 0, 8, 0);

        JPanel columnHeaderRow = new JPanel(new GridLayout(1, 5));
        columnHeaderRow.setOpaque(false);
        columnHeaderRow.add(createTableCell("Dorms", SwingConstants.LEFT));
        columnHeaderRow.add(createTableCell("Availability", SwingConstants.CENTER));
        columnHeaderRow.add(createTableCell("Monthly rate", SwingConstants.CENTER));
        columnHeaderRow.add(createTableCell("Interested", SwingConstants.CENTER));
        columnHeaderRow.add(new JLabel(""));
        dataTableWrapper.add(columnHeaderRow, tableGbc);

        tableGbc.gridy = 1; tableGbc.insets = new Insets(0, 0, 10, 0);
        JSeparator headerDivider = new JSeparator();
        headerDivider.setForeground(GradientBackground.GLASS_LINE);
        dataTableWrapper.add(headerDivider, tableGbc);

        tableGbc.gridy = 2; tableGbc.weighty = 1.0; tableGbc.insets = new Insets(0, 0, 0, 0);
        tableGbc.fill = GridBagConstraints.HORIZONTAL; tableGbc.anchor = GridBagConstraints.NORTH;
        dormRowsPanel = new JPanel();
        dormRowsPanel.setLayout(new BoxLayout(dormRowsPanel, BoxLayout.Y_AXIS));
        dormRowsPanel.setOpaque(false);
        dataTableWrapper.add(dormRowsPanel, tableGbc);
        mainCanvas.add(dataTableWrapper, gbc);
        refreshOverview();
        return mainCanvas;
    }

    // Re-reads the shared dorm data and redraws the metric cards and the Dorm List rows
    private void refreshOverview() {
        totalDormsValue.setText(DormStore.twoDigits(DormStore.DORMS.size()));
        totalTenantsValue.setText(DormStore.twoDigits(DormStore.totalTenants()));

        dormRowsPanel.removeAll();
        for (DormStore.Dorm dorm : DormStore.DORMS) {
            dormRowsPanel.add(createDormRow(dorm));
            dormRowsPanel.add(Box.createVerticalStrut(4));
        }
        dormRowsPanel.revalidate();
        dormRowsPanel.repaint();
    }

    // One row of the Dorm List: name | availability | monthly rate | interested | Manage
    private JPanel createDormRow(DormStore.Dorm dorm) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(100, 40));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // rows inside ng tenant list
        JLabel nameCell = createTableCell(dorm.name, SwingConstants.LEFT);
        JLabel availabilityCell = createTableCell(
            DormStore.twoDigits(dorm.available()) + "/" + DormStore.twoDigits(dorm.totalSlots), SwingConstants.CENTER);
        JLabel rateCell = createTableCell(String.format("Php %.2f", (double) dorm.monthlyRate), SwingConstants.CENTER);
        JLabel interestedCell = createTableCell(
            DormStore.twoDigits(dorm.pendingApplicants.size()), SwingConstants.CENTER);
        for (JLabel cell : new JLabel[] {nameCell, availabilityCell, rateCell, interestedCell}) {
            cell.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
            cell.setForeground(Color.BLACK);
        }
        nameCell.setBorder(new EmptyBorder(0, 4, 0, 0));

        // Manage
        JButton manageBtn = new JButton("Manage");
        manageBtn.setPreferredSize(new Dimension(75, 25));
        manageBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
        manageBtn.setBackground(new Color(0xCC, 0xCC, 0xCC));
        manageBtn.setForeground(Color.BLACK);
        manageBtn.setOpaque(true);
        manageBtn.setFocusPainted(false);
        manageBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        manageBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0xA0, 0xA0, 0xA0)),
            new LineBorder(new Color(0x55, 0x55, 0x55), 1)));

        // Manage opens the approve/decline list for this dorm only
        manageBtn.addActionListener(e -> showApproveTenantDialog(dorm));
        JPanel manageWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        manageWrapper.setOpaque(false);
        manageWrapper.add(manageBtn);

        row.add(nameCell);
        row.add(availabilityCell);
        row.add(rateCell);
        row.add(interestedCell);
        row.add(manageWrapper);
        return row;
    }

    private JPanel createMyDormCardView() {
        JPanel container = new GradientPanel(new GridBagLayout());
        container.setBackground(FindTheDUI.LIGHT_GRAY_BG);
        container.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        // Feed of the landlord's own dorm posts
        JPanel myFeedPanel = new JPanel();
        myFeedPanel.setLayout(new BoxLayout(myFeedPanel, BoxLayout.Y_AXIS));
        myFeedPanel.setOpaque(false);
        myFeedPanel.add(new DormPostCard(
            "Benyapa Jeenprasom", "11 Nov 2069",
            "Jeenprasom Dorm",
            "Sampaloc, Manila",
            "7Q8F+M3 Sampaloc, Manila",
            "Clean and quiet dorm near the university, only a 5-minute walk to campus. It has 24/7 security, free Wi-Fi, a shared kitchen, a study lounge and a laundry area. Pets are not allowed and visitors must sign in at the front desk.",
            new String[] {"Wi-Fi", "Air Con", "Laundry", "Kitchen", "Gym"},
            "No pets. Visitors must sign in at the front desk. Curfew at 10:00 PM.",
            "\u20B14,500",
            DormStore.slotsText("Jeenprasom Dorm"),
            "Sampaloc Public Library",
            "Library",
            "0917 123 4567",
            "facebook.com/benyapa.dorm",
            25,
            null));

        myFeedPanel.add(Box.createVerticalStrut(14));
        DormPostCard secondPost = new DormPostCard(
            "Benyapa Jeenprasom", "12 Nov 2069",
            "Benyapa Dorm",
            "Sampaloc, Manila",
            "7Q8F+R7 Sampaloc, Manila",
            "Affordable dorm with 24/7 security and a study area",
            new String[] {"Wi-Fi", "Fan", "Laundry", "Parking", "CCTV"},
            "No smoking inside the dorm. Quiet hours from 9:00 PM to 6:00 AM.",
            "\u20B13,200",
            DormStore.slotsText("Benyapa Dorm"),
            "7-Eleven Espana",
            "Convenience Store",
            "0917 123 4567",
            "facebook.com/benyapa.dorm",
            12,
            null);
        secondPost.setSamplePhotos(7);   // 7 sample photos: the grid shows 4 with "+3" on the last
        myFeedPanel.add(secondPost);
        // More posts: myFeedPanel.add(new DormPostCard(..., null));

        // Let the posts stretch with the window instead of stopping at their default width
        for (Component feedItem : myFeedPanel.getComponents()) {
            if (feedItem instanceof DormPostCard) {
                feedItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
        }

        JPanel myFeedHolder = new JPanel(new BorderLayout());
        myFeedHolder.setOpaque(false);
        myFeedHolder.add(myFeedPanel, BorderLayout.NORTH);
        // The feed is sized like the tenant's (FEED_WIDTH_PERCENT, never narrower than FEED_MIN_WIDTH),
        // kept to the left; the rest is left as margin on the right. Re-applied whenever the window resizes.
        myFeedHolder.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int avail = myFeedHolder.getWidth();
                int tenantAvail = Math.max(0, avail - TENANT_LAYOUT_EXTRA);
                int feedWidth = Math.min(avail, Math.max(FEED_MIN_WIDTH, tenantAvail * FEED_WIDTH_PERCENT / 100));
                int right = Math.max(0, avail - feedWidth);   // left-aligned: the spare room goes on the right
                if (myFeedHolder.getInsets().right != right) {
                    myFeedHolder.setBorder(new EmptyBorder(0, 0, 0, right));
                    myFeedHolder.revalidate();
                }
            }
        });

        JScrollPane myFeedScroll = new JScrollPane(myFeedHolder);
        ModernScrollBarUI.apply(myFeedScroll);
        myFeedScroll.setBorder(null);
        myFeedScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GradientBackground.makeTransparent(myFeedScroll);
        container.add(myFeedScroll, gbc);
        
        gbc.gridy = 1; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        JPanel rightActionsPanel = new JPanel(new GridBagLayout());
        rightActionsPanel.setOpaque(false);
        GridBagConstraints aGbc = new GridBagConstraints();
        
        // Approve Tenant
        aGbc.gridx = 0; aGbc.gridy = 0; aGbc.insets = new Insets(0, 0, 8, 0);
        JButton approveTenantBtn = new JButton("Approve Tenant");
        approveTenantBtn.setPreferredSize(new Dimension(180, 36));
        approveTenantBtn.setMinimumSize(new Dimension(150, 36));
        approveTenantBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        approveTenantBtn.setBackground(new Color(0xBB, 0xBB, 0xBB));
        approveTenantBtn.setBorder(new LineBorder(Color.GRAY));
        approveTenantBtn.setFocusPainted(false);
        approveTenantBtn.addActionListener(e -> showApproveTenantDialog(null));
        rightActionsPanel.add(approveTenantBtn, aGbc);
        
        // Post new dorm
        aGbc.gridy = 1; aGbc.insets = new Insets(0, 0, 0, 0);
        JButton postNewDormBtn = new JButton("Post New Dorm");
        postNewDormBtn.setPreferredSize(new Dimension(180, 36));
        postNewDormBtn.setMinimumSize(new Dimension(150, 36));
        postNewDormBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        postNewDormBtn.setBackground(new Color(0xBB, 0xBB, 0xBB));
        postNewDormBtn.setBorder(new LineBorder(Color.GRAY));
        postNewDormBtn.setFocusPainted(false);
        
        postNewDormBtn.addActionListener(e -> showPostNewDormDialog());
        
        rightActionsPanel.add(postNewDormBtn, aGbc);
        container.add(rightActionsPanel, gbc);
        return container;
    }

    // MANAGE TENANTS dialog. Two parts: APPROVE/DECLINE (pending applicants) and
    // TENANT LIST (current tenants, each with a Remove button).
    // onlyDorm == null shows every dorm; otherwise just that dorm
    private void showApproveTenantDialog(DormStore.Dorm onlyDorm) {
        JDialog dialog = new JDialog(owner(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        final boolean manage = onlyDorm != null;   // Manage button = two-part dialog; Approve Tenant = original
        dialog.setSize(manage ? 520 : 480, manage ? 540 : 340);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        // Small dialog window: gray title bar with a close button, white body
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // top bar once you click manage
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.decode("#E0E0E0"));
        topBar.setPreferredSize(new Dimension(manage ? 520 : 480, 36));

        JLabel titleLbl = new JLabel(manage ? "  Manage Tenants" : "  Approve Tenant");
        titleLbl.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        titleLbl.setForeground(Color.BLACK);
        topBar.add(titleLbl, BorderLayout.WEST);

        JLabel closeBtn = new JLabel("\u00D7  ", SwingConstants.RIGHT);
        closeBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 22));
        closeBtn.setForeground(Color.BLACK);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });
        topBar.add(closeBtn, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        JPanel contentScrollBody = new JPanel();
        contentScrollBody.setOpaque(false);
        contentScrollBody.setBorder(new EmptyBorder(14, 25, 0, 25));
        contentScrollBody.setLayout(new BoxLayout(contentScrollBody, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentScrollBody);
        ModernScrollBarUI.apply(scrollPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Both sections are rebuilt after every approve / decline / remove so they stay in sync
        final Runnable[] refresh = new Runnable[1];
        refresh[0] = () -> {
            refreshOverview();
            populateManageTenants(dialog, contentScrollBody, onlyDorm, refresh[0]);
        };
        populateManageTenants(dialog, contentScrollBody, onlyDorm, refresh[0]);

        dialog.add(mainPanel);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    // manage tenants
    private void populateManageTenants(JDialog dialog, JPanel body, DormStore.Dorm onlyDorm, Runnable refresh) {
        body.removeAll();

        final boolean manage = onlyDorm != null;

        // ---- Part 1: APPROVE/DECLINE ----
        if (manage) {
            addManageSectionHeader(body, "Approve/Decline");
        }
        boolean hasApplicants = false;
        for (DormStore.Dorm dorm : DormStore.DORMS) {
            if ((onlyDorm != null && dorm != onlyDorm) || dorm.pendingApplicants.isEmpty()) {
                continue;
            }
            hasApplicants = true;
            JPanel group = new JPanel();
            group.setOpaque(false);
            group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
            group.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!manage) {
                addTenantHeaderGroup(group, dorm.name);
            }
            for (String applicant : new java.util.ArrayList<>(dorm.pendingApplicants)) {
                addTenantDataRow(dialog, group, refresh, dorm, applicant);
            }
            body.add(group);
        }
        if (!hasApplicants) {
            addManageEmptyLabel(body, onlyDorm != null
                ? "No pending applications for " + onlyDorm.name + "."
                : "No pending applications.");
        }

        // ---- Part 2: TENANT LIST (Manage dialog only) ----
        if (manage) {
            body.add(Box.createVerticalStrut(10));
            addManageSectionHeader(body, "Tenant List");
            boolean hasTenants = false;
            for (DormStore.Dorm dorm : DormStore.DORMS) {
                if (dorm != onlyDorm || dorm.tenants.isEmpty()) {
                    continue;
                }
                hasTenants = true;
                JPanel group = new JPanel();
                group.setOpaque(false);
                group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
                group.setAlignmentX(Component.LEFT_ALIGNMENT);
                for (String tenant : new java.util.ArrayList<>(dorm.tenants)) {
                    addCurrentTenantRow(dialog, group, refresh, dorm, tenant);
                }
                body.add(group);
            }
            if (!hasTenants) {
                addManageEmptyLabel(body, "No current tenants in " + onlyDorm.name + ".");
            }
        }

        body.add(Box.createVerticalStrut(15));
        body.revalidate();
        body.repaint();
    }

    // Bold sub header with a thin divider underneath, manage tenants
    private void addManageSectionHeader(JPanel container, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lbl.setForeground(Color.BLACK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xBC, 0xBC, 0xBC));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(sep);
        container.add(Box.createVerticalStrut(10));
    }

    private void addManageEmptyLabel(JPanel container, String text) {
        JLabel noneLabel = new JLabel(text);
        noneLabel.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        noneLabel.setForeground(Color.BLACK);
        noneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(noneLabel);
    }

    private void addTenantHeaderGroup(JPanel container, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 16));
        lbl.setForeground(Color.BLACK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createVerticalStrut(8));
    }

    // Pending applicant: name + Approve / Decline
    private void addTenantDataRow(JDialog parentDialog, JPanel group, Runnable refresh, DormStore.Dorm dorm, String name) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        nameLbl.setForeground(Color.BLACK);
        row.add(nameLbl, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton approve = new JButton("Approve");
        styleRowButton(approve);
        approve.setBorder(new EmptyBorder(0, 0, 0, 0));
        JButton decline = new JButton("Decline");
        styleRowButton(decline);
        decline.setBackground(Color.WHITE);

        approve.addActionListener(e -> {
            if (dorm.available() <= 0) {
                RoundedDialog.showMessage(parentDialog, "Message", dorm.name + " is already full.");
                return;
            }
            showActionConfirmationDialog(parentDialog, "Approve/Decline", () -> {
                DormStore.approve(dorm, name);      // tenant takes a slot, Total Tenants goes up
                refresh.run();
            });
        });
        decline.addActionListener(e -> showActionConfirmationDialog(parentDialog, "Approve/Decline", () -> {
            DormStore.decline(dorm, name);
            refresh.run();
        }));

        btnRow.add(approve);
        btnRow.add(decline);
        row.add(btnRow, BorderLayout.EAST);

        group.add(row);
        group.add(Box.createVerticalStrut(12));
    }

    // Current tenant: name + Remove
    private void addCurrentTenantRow(JDialog parentDialog, JPanel group, Runnable refresh, DormStore.Dorm dorm, String name) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tenant names once you click manage
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        nameLbl.setForeground(Color.BLACK);
        row.add(nameLbl, BorderLayout.WEST);

        // Remove button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton remove = new JButton("Remove");
        remove.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        styleRowButton(remove);
        remove.addActionListener(e -> showActionConfirmationDialog(parentDialog, "Remove Tenant", () -> {
            DormStore.removeTenant(dorm, name);     // frees the slot, Total Tenants goes down
            refresh.run();
        }));
        btnRow.add(remove);
        row.add(btnRow, BorderLayout.EAST);

        group.add(row);
        group.add(Box.createVerticalStrut(12));
    }

    private void styleRowButton(JButton btn) {
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(0xDD, 0xDD, 0xDD));
        btn.setBorder(new LineBorder(Color.BLACK, 1));
        btn.setFocusPainted(false);
    }

    private void showActionConfirmationDialog(JDialog parentDialog, String dialogTitle, Runnable onConfirm) {
        JDialog confirm = new JDialog(parentDialog, true);
        confirm.setUndecorated(true);
        confirm.setSize(480, 200);
        confirm.setLocationRelativeTo(parentDialog);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xBC, 0xBC, 0xBC));
        header.setPreferredSize(new Dimension(480, 36));
        JLabel title = new JLabel("  " + dialogTitle);
        title.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        header.add(title, BorderLayout.WEST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);
        
        JLabel msg = new JLabel("Are you sure?");
        msg.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 44));
        body.add(msg, gbc);

        gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(0, 0, 15, 25);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton yes = new JButton("Yes");
        yes.setPreferredSize(new Dimension(75, 34));
        yes.setBackground(new Color(0xDD, 0xDD, 0xDD));
        yes.setBorder(new EmptyBorder(0,0,0,0));
        yes.setFocusPainted(false);

        JButton no = new JButton("No");
        no.setPreferredSize(new Dimension(75, 34));
        no.setBackground(Color.WHITE);
        no.setBorder(new LineBorder(Color.BLACK, 1));
        no.setFocusPainted(false);

        yes.addActionListener(e -> {
            confirm.dispose();
            onConfirm.run();
        });
        no.addActionListener(e -> confirm.dispose());

        actions.add(yes);
        actions.add(no);
        body.add(actions, gbc);

        content.add(header, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        confirm.add(content);
        RoundedDialog.apply(confirm);
        confirm.setVisible(true);
    }

    // "Post New Dorm" form, styled after the mockup: flat grey page, inset text fields,
    // a dropdown-style Amenities field (with multiple choices), raised Post / Cancel buttons
    private void showPostNewDormDialog() {
        JDialog dialog = new JDialog(owner(), "Post New Dorm", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(820, 720);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        final Color pageBg = new Color(0xB8, 0xB8, 0xB8);
        final Color fieldBg = InsetFieldBorder.FILL;
        final int leftW = 265;      // left column width
        final int rightW = 283;     // right column width
        final int labelGap = 12;    // label -> field
        final int rowGap = 20;      // field -> next label

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(pageBg);
        mainPanel.setBorder(new EmptyBorder(36, 88, 14, 19));

        // ================= LEFT COLUMN =================
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createMockLabel("Dorm Name*"));
        leftCol.add(Box.createVerticalStrut(labelGap));
        JTextField nameField = createMockTextField(leftW, 38);
        leftCol.add(nameField);

        leftCol.add(Box.createVerticalStrut(rowGap));
        leftCol.add(createMockLabel("Location*"));
        leftCol.add(Box.createVerticalStrut(labelGap));
        JTextField locationField = createMockTextField(leftW, 38);
        leftCol.add(locationField);

        leftCol.add(Box.createVerticalStrut(rowGap));
        leftCol.add(createMockLabel("Full Plus Code*"));
        leftCol.add(Box.createVerticalStrut(labelGap));
        JTextField plusCodeField = createMockTextField(leftW, 38);
        leftCol.add(plusCodeField);

        leftCol.add(Box.createVerticalStrut(rowGap));
        leftCol.add(createMockLabel("Description*"));
        leftCol.add(Box.createVerticalStrut(labelGap));
        JTextArea descriptionField = new JTextArea();
        descriptionField.setBackground(fieldBg);
        descriptionField.setForeground(Color.BLACK);
        descriptionField.setCaretColor(Color.BLACK);
        descriptionField.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane descScroll = new JScrollPane(descriptionField);
        ModernScrollBarUI.apply(descScroll);
        descScroll.getViewport().setBackground(fieldBg);
        descScroll.setBorder(new InsetFieldBorder());
        Dimension descSize = new Dimension(leftW, 126);
        descScroll.setPreferredSize(descSize);
        descScroll.setMinimumSize(descSize);
        descScroll.setMaximumSize(descSize);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(descScroll);

        // Amenities: looks like the dropdown in the mockup, but opens a list of check boxes
        // (same amenities as the tenant's filter) so several can be picked at once
        leftCol.add(Box.createVerticalStrut(rowGap));
        leftCol.add(createMockLabel("Amenities*"));
        leftCol.add(Box.createVerticalStrut(labelGap));

        DropdownField amenitiesField = new DropdownField("", leftW, 40);
        amenitiesField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPopupMenu amenitiesPopup = amenitiesField.getPopup();
        java.util.List<JCheckBox> amenityChecks = new java.util.ArrayList<>();
        Runnable updateAmenitiesSummary = () -> {
            StringBuilder sb = new StringBuilder();
            for (JCheckBox cb : amenityChecks) {
                if (cb.isSelected()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(cb.getText());
                }
            }
            amenitiesField.setValue(sb.length() == 0 ? null : sb.toString());
        };
        for (String amenity : DormStore.AMENITIES) {
            OneUiCheckItem cb = new OneUiCheckItem(amenity);
            cb.addActionListener(e -> updateAmenitiesSummary.run());  // popup stays open for more picks
            amenityChecks.add(cb);
            amenitiesPopup.add(cb);
        }
        leftCol.add(amenitiesField);

        // ================= RIGHT COLUMN =================
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        rightCol.add(createMockLabel("Dorm Rules*"));
        rightCol.add(Box.createVerticalStrut(labelGap));
        JTextField rulesField = createMockTextField(266, 38);
        rightCol.add(rulesField);

        // Available Slot + Monthly Rate side by side
        rightCol.add(Box.createVerticalStrut(rowGap));
        JTextField slotField = createMockTextField(114, 40);
        JTextField rateField = createMockTextField(114, 40);
        JPanel slotBox = new JPanel();
        slotBox.setLayout(new BoxLayout(slotBox, BoxLayout.Y_AXIS));
        slotBox.setOpaque(false);
        slotBox.add(createMockLabel("Available Slot*"));
        slotBox.add(Box.createVerticalStrut(10));
        slotBox.add(slotField);
        JPanel rateBox = new JPanel();
        rateBox.setLayout(new BoxLayout(rateBox, BoxLayout.Y_AXIS));
        rateBox.setOpaque(false);
        rateBox.add(createMockLabel("Monthly Rate*"));
        rateBox.add(Box.createVerticalStrut(10));
        rateBox.add(rateField);
        JPanel slotRateRow = new JPanel();
        slotRateRow.setLayout(new BoxLayout(slotRateRow, BoxLayout.X_AXIS));
        slotRateRow.setOpaque(false);
        slotRateRow.add(slotBox);
        slotRateRow.add(Box.createHorizontalStrut(37));
        slotRateRow.add(rateBox);
        slotRateRow.add(Box.createHorizontalGlue());
        slotRateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        slotRateRow.setMaximumSize(new Dimension(rightW, 75));
        rightCol.add(slotRateRow);

        // Photos label + Upload button, then a strip of thumbnail boxes (no limit)
        rightCol.add(Box.createVerticalStrut(24));
        JPanel photoHeader = new JPanel(new BorderLayout());
        photoHeader.setOpaque(false);
        JPanel photosLabelWrap = new JPanel(new BorderLayout());   // keeps "Photos" at the bottom-left
        photosLabelWrap.setOpaque(false);
        photosLabelWrap.add(createMockLabel("Photos"), BorderLayout.SOUTH);
        photoHeader.add(photosLabelWrap, BorderLayout.WEST);
        JButton uploadBtn = new FlatButton("Upload");
        uploadBtn.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 10));
        uploadBtn.setForeground(Color.BLACK);
        uploadBtn.setBackground(new Color(0xF6, 0xF6, 0xF6));
        uploadBtn.setOpaque(true);
        uploadBtn.setContentAreaFilled(false);
        uploadBtn.setFocusPainted(false);
        uploadBtn.setMargin(new Insets(0, 0, 0, 0));
        uploadBtn.setBorder(new DropShadowBorder(new Color(0x55, 0x55, 0x55), new Color(0x22, 0x22, 0x22)));
        uploadBtn.setPreferredSize(new Dimension(56, 24));
        uploadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel uploadWrap = new JPanel(new BorderLayout());
        uploadWrap.setOpaque(false);
        uploadWrap.add(uploadBtn, BorderLayout.NORTH);   // keeps the button at its own height, top-right
        photoHeader.add(uploadWrap, BorderLayout.EAST);
        photoHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        photoHeader.setMaximumSize(new Dimension(277, 28));
        photoHeader.setPreferredSize(new Dimension(277, 28));
        rightCol.add(photoHeader);
        rightCol.add(Box.createVerticalStrut(11));

        // Photos: four empty boxes to start; every photo added after those gets a new box,
        // so there is no limit. The strip scrolls sideways when it gets long.
        final int thumbW = 64;
        final int thumbGap = 7;
        final int thumbH = 77;
        java.util.List<PhotoThumb> photoThumbs = new java.util.ArrayList<>();
        JPanel thumbRow = new JPanel(new GridLayout(1, 0, thumbGap, 0));
        thumbRow.setOpaque(false);
        Runnable resizeThumbRow = () -> {
            int n = Math.max(1, thumbRow.getComponentCount());
            thumbRow.setPreferredSize(new Dimension(n * thumbW + (n - 1) * thumbGap, thumbH));
            thumbRow.revalidate();
            thumbRow.repaint();
        };
        java.util.function.Supplier<PhotoThumb> addThumb = () -> {
            PhotoThumb t = new PhotoThumb();
            t.setOnRemoved(() -> {
                // the first four boxes stay as empty slots; extra boxes go away with their photo
                if (photoThumbs.size() > 4) {
                    photoThumbs.remove(t);
                    thumbRow.remove(t);
                    thumbRow.getParent().revalidate();
                }
            });
            photoThumbs.add(t);
            thumbRow.add(t);
            return t;
        };
        for (int i = 0; i < 4; i++) {
            addThumb.get();
        }
        resizeThumbRow.run();

        JScrollPane thumbScroll = new JScrollPane(thumbRow,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        thumbScroll.setBorder(null);
        thumbScroll.setOpaque(false);
        thumbScroll.getViewport().setOpaque(false);
        ModernScrollBarUI.apply(thumbScroll);
        thumbScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        thumbScroll.setPreferredSize(new Dimension(277, thumbH + 12));
        thumbScroll.setMaximumSize(new Dimension(277, thumbH + 12));

        // Upload: opens the photo picker (pick as many as you like); chosen pictures fill the empty
        // boxes first, then new boxes are added. Click a picture to remove it.
        uploadBtn.addActionListener(e -> {
            boolean[] unreadable = {false};
            for (File file : PhotoPickerDialog.choose(dialog, Integer.MAX_VALUE)) {
                PhotoThumb target = null;
                for (PhotoThumb t : photoThumbs) {
                    if (!t.hasPhoto()) {
                        target = t;
                        break;
                    }
                }
                boolean isNew = target == null;
                if (isNew) {
                    target = addThumb.get();
                }
                if (!target.setPhoto(file)) {
                    unreadable[0] = true;
                    if (isNew) {   // don't leave an empty extra box behind
                        photoThumbs.remove(target);
                        thumbRow.remove(target);
                    }
                }
            }
            resizeThumbRow.run();
            SwingUtilities.invokeLater(() -> {      // show the newest photos
                JScrollBar bar = thumbScroll.getHorizontalScrollBar();
                bar.setValue(bar.getMaximum());
            });
            if (unreadable[0]) {
                RoundedDialog.showMessage(dialog, "Photos", "One of the selected files could not be opened as a picture.");
            }
        });
        rightCol.add(thumbScroll);

        // Nearby Establishment (Name + Type, indented)
        rightCol.add(Box.createVerticalStrut(24));
        rightCol.add(createMockLabel("Nearby Establishment"));
        rightCol.add(Box.createVerticalStrut(19));
        JPanel nearbyBox = new JPanel();
        nearbyBox.setLayout(new BoxLayout(nearbyBox, BoxLayout.Y_AXIS));
        nearbyBox.setOpaque(false);
        nearbyBox.setBorder(new EmptyBorder(0, 15, 0, 0));
        nearbyBox.add(createMockLabel("Name"));
        nearbyBox.add(Box.createVerticalStrut(9));
        JTextField estNameField = createMockTextField(266, 38);
        nearbyBox.add(estNameField);
        nearbyBox.add(Box.createVerticalStrut(11));
        nearbyBox.add(createMockLabel("Type"));
        nearbyBox.add(Box.createVerticalStrut(9));
        JTextField estTypeField = createMockTextField(266, 38);
        nearbyBox.add(estTypeField);
        nearbyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCol.add(nearbyBox);

        // ================= Columns side by side =================
        JPanel formGrid = new JPanel(new BorderLayout(85, 0));
        formGrid.setOpaque(false);
        formGrid.add(leftCol, BorderLayout.WEST);
        formGrid.add(rightCol, BorderLayout.CENTER);
        mainPanel.add(formGrid, BorderLayout.NORTH);

        // ================= Post / Cancel =================
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 13, 0));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton postBtn = new FlatButton("Post");
        postBtn.setPreferredSize(new Dimension(114, 46));
        postBtn.setFont(new Font("Yu GOthic UI Light", Font.PLAIN, 17));
        postBtn.setForeground(Color.BLACK);
        postBtn.setBackground(Color.WHITE);
        postBtn.setOpaque(true);
        postBtn.setContentAreaFilled(false);
        postBtn.setFocusPainted(false);
        postBtn.setBorder(new DropShadowBorder(new Color(0x9A, 0x9A, 0x9A), null));
        postBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new FlatButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(114, 46));
        cancelBtn.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 17));
        cancelBtn.setForeground(Color.BLACK);
        cancelBtn.setBackground(new Color(0xDF, 0xDF, 0xDF));
        cancelBtn.setOpaque(true);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new DropShadowBorder(new Color(0x55, 0x55, 0x55), new Color(0x33, 0x33, 0x33)));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        postBtn.addActionListener(e -> {
            dialog.dispose();
            showCustomPostedNotificationDialog();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonBar.add(postBtn);
        buttonBar.add(cancelBtn);
        mainPanel.add(buttonBar, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    // Label used in the Post New Dorm form
    private JLabel createMockLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 15));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // Flat grey text field with a darker inset edge on top and left (as in the mockup)
    private JTextField createMockTextField(int width, int height) {
        JTextField field = new JTextField();
        field.setBackground(InsetFieldBorder.FILL);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        field.setBorder(new InsetFieldBorder());
        Dimension size = new Dimension(width, height);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        field.setMaximumSize(size);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    // Post Announcement
    private void showPostAnnouncementDialog() {
        JDialog dialog = new JDialog(owner(), "Post Announcement", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(175, 175, 175)); 
        mainPanel.setBorder(new EmptyBorder(25, 35, 20, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        Color fieldBg = new Color(210, 210, 210);
        LineBorder insetBorder = new LineBorder(new Color(150, 150, 150), 1);

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        mainPanel.add(createFormLabel("Dorm"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        DropdownField dormCombo = new DropdownField("Select a Dorm...", 0, 38);
        for (DormStore.Dorm dorm : DormStore.DORMS) {
            dormCombo.addChoice(dorm.name);
        }
        mainPanel.add(dormCombo, gbc);

        // Message
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
        mainPanel.add(createFormLabel("Message"), gbc);

        gbc.gridy = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(0, 0, 0, 0);
        JTextArea msgArea = new JTextArea();
        msgArea.setBackground(fieldBg);
        msgArea.setBorder(BorderFactory.createCompoundBorder(insetBorder, new EmptyBorder(8, 8, 8, 8)));
        msgArea.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(msgArea);
        ModernScrollBarUI.apply(scrollPane);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, gbc);

        gbc.gridy = 4; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHWEST; gbc.insets = new Insets(20, 0, 0, 0);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonBar.setOpaque(false);

        // Post button for post announcement
        JButton postBtn = new JButton("Post");
        postBtn.setPreferredSize(new Dimension(100, 36));
        postBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        postBtn.setBackground(new Color(220, 220, 220));
        postBtn.setFocusPainted(false);
        postBtn.setBorder(new LineBorder(new Color(160, 160, 160)));

        // Cancel button for post announcement
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(220, 220, 220));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new LineBorder(Color.BLACK, 1));

        postBtn.addActionListener(e -> {
            dialog.dispose();
            showCustomPostedNotificationDialog(); 
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonBar.add(postBtn);
        buttonBar.add(cancelBtn);
        mainPanel.add(buttonBar, gbc);

        dialog.add(mainPanel);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    // Posted pop up after posting
    private void showCustomPostedNotificationDialog() {
        JDialog successDialog = new JDialog(owner(), "Message", Dialog.ModalityType.APPLICATION_MODAL);
        successDialog.setSize(480, 200);
        successDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 240, 240)); 
        contentPanel.setBorder(new EmptyBorder(20, 30, 15, 30));

        JLabel statusMsgLabel = new JLabel("Posted!", SwingConstants.LEFT);
        statusMsgLabel.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 44));
        statusMsgLabel.setForeground(Color.BLACK);
        contentPanel.add(statusMsgLabel, BorderLayout.CENTER);

        JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionWrapper.setOpaque(false);
        
        // Close button sa pop up after you post an announcement
        JButton closeNotificationBtn = new JButton("Close");
        closeNotificationBtn.setPreferredSize(new Dimension(90, 35));
        closeNotificationBtn.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 14));
        closeNotificationBtn.setBackground(new Color(225, 225, 225));
        closeNotificationBtn.setForeground(Color.BLACK);
        closeNotificationBtn.setFocusPainted(false);
        closeNotificationBtn.setBorder(new LineBorder(new Color(140, 140, 140), 1));
        
        closeNotificationBtn.addActionListener(event -> successDialog.dispose());
        actionWrapper.add(closeNotificationBtn);
        
        contentPanel.add(actionWrapper, BorderLayout.SOUTH);
        successDialog.add(contentPanel);
        RoundedDialog.apply(successDialog);
        successDialog.setVisible(true);
    }

    private static final Color NOTIF_PAGE_BG         = Color.decode("#E5E5E5"); // page behind the boxes
    private static final Color NOTIF_BOX_BG          = new Color(255, 255, 255, 28); // Complaints / Announcement boxes
    private static final Color NOTIF_BOX_OUTLINE     = new Color(255, 255, 255, 55);
    private static final Color NOTIF_TITLE_TEXT      = Color.decode("#000000");
    private static final Color NOTIF_DROPDOWN_BG     = Color.decode("#FFFFFF"); // dorm filter dropdown
    private static final Color NOTIF_DROPDOWN_BORDER = Color.decode("#999999");
    private static final Color NOTIF_DROPDOWN_TEXT   = Color.BLACK; // text in the dropdown
    private static final Color NOTIF_DROPDOWN_HOVER  = Color.decode("#E8E8E8"); // hovered item in the list
    private static final Color NOTIF_BUTTON_BG       = Color.decode("#BBBBBB"); // Post Announcement button
    private static final Color NOTIF_BUTTON_BORDER   = Color.decode("#808080");

    // Notification part
    private JPanel createNotificationCardView() {
        JPanel container = new GradientPanel(new GridBagLayout());
        container.setBackground(NOTIF_PAGE_BG);
        container.setBorder(new EmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Complaints Outer Box
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(0, 0, 15, 0);
        
        RoundedPanel complaintsOuterBox = new RoundedPanel(new BorderLayout());
        complaintsOuterBox.setBackground(NOTIF_BOX_BG);
        complaintsOuterBox.setOutline(NOTIF_BOX_OUTLINE);
        complaintsOuterBox.setBorder(new EmptyBorder(13, 16, 13, 16));

        // Setup CardLayout contents for filtered messages
        CardLayout filterLayout = new CardLayout();
        JPanel compCardsPanel = new JPanel(filterLayout);
        compCardsPanel.setOpaque(false);

        // Complaints section
        // Header: title on the left, dorm filter dropdown in the upper right corner
        JPanel compHeaderPanel = new JPanel(new BorderLayout());
        compHeaderPanel.setOpaque(false);
        JLabel compTitle = new JLabel("Complaints");
        compTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        compTitle.setForeground(NOTIF_TITLE_TEXT);
        compHeaderPanel.add(compTitle, BorderLayout.WEST);

        String[] filterDorms = {"Jeenprasom Dorm", "Benyapa Dorm"};
        String[] filterCards = {"D1", "D2"};
        CompactDropdown dormFilter = new CompactDropdown(filterDorms, 170, 28,
            NOTIF_DROPDOWN_BG, NOTIF_DROPDOWN_BORDER, NOTIF_DROPDOWN_TEXT, NOTIF_DROPDOWN_HOVER,
            index -> filterLayout.show(compCardsPanel, filterCards[index]));
        compHeaderPanel.add(dormFilter, BorderLayout.EAST);

        complaintsOuterBox.add(compHeaderPanel, BorderLayout.NORTH);

        // Dorm #1 content (Empty panel)
        JPanel d1Content = new JPanel();
        d1Content.setOpaque(false);
        compCardsPanel.add(d1Content, "D1");

        // Dorm #2 content panel
        JPanel d2Content = new JPanel(new GridBagLayout());
        d2Content.setOpaque(false);
        GridBagConstraints d2Gbc = new GridBagConstraints();
        d2Gbc.gridx = 0; d2Gbc.gridy = 0; d2Gbc.weightx = 1.0;
        d2Gbc.fill = GridBagConstraints.HORIZONTAL; d2Gbc.anchor = GridBagConstraints.NORTHWEST;
        d2Gbc.insets = new Insets(10, 0, 0, 0);

        MessageBubble d2Bubble = new MessageBubble("Benyapa Dorm", "11 Nov 2069", "11:11 pm", false);
        d2Content.add(d2Bubble, d2Gbc);
        compCardsPanel.add(d2Content, "D2");

        complaintsOuterBox.add(compCardsPanel, BorderLayout.CENTER);
        container.add(complaintsOuterBox, gbc);

        // Announcement Container
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        RoundedPanel announceOuterBox = new RoundedPanel(new BorderLayout());
        announceOuterBox.setBackground(NOTIF_BOX_BG);
        announceOuterBox.setOutline(NOTIF_BOX_OUTLINE);
        announceOuterBox.setBorder(new EmptyBorder(13, 16, 13, 16));

        // title "Announcement"
        JPanel annHeaderPanel = new JPanel(new BorderLayout());
        annHeaderPanel.setOpaque(false);
        JLabel annTitle = new JLabel("Announcement");
        annTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        annTitle.setForeground(NOTIF_TITLE_TEXT);
        annHeaderPanel.add(annTitle, BorderLayout.WEST);

        // Post Announcement button
        JButton postAnnounceBtn = new JButton("Post Announcement");
        postAnnounceBtn.setPreferredSize(new Dimension(160, 32));
        postAnnounceBtn.setBackground(NOTIF_BUTTON_BG);
        postAnnounceBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
        postAnnounceBtn.setBorder(new LineBorder(NOTIF_BUTTON_BORDER));
        postAnnounceBtn.setFocusPainted(false);
        postAnnounceBtn.addActionListener(e -> showPostAnnouncementDialog());
        annHeaderPanel.add(postAnnounceBtn, BorderLayout.EAST);

        announceOuterBox.add(annHeaderPanel, BorderLayout.NORTH);
        
        JPanel annBodyPanel = new JPanel(new GridBagLayout());
        annBodyPanel.setOpaque(false);
        GridBagConstraints annBodyGbc = new GridBagConstraints();
        annBodyGbc.gridx = 0; annBodyGbc.weightx = 1.0;
        annBodyGbc.fill = GridBagConstraints.HORIZONTAL; annBodyGbc.anchor = GridBagConstraints.NORTHWEST;
        
        annBodyGbc.gridy = 0; annBodyGbc.insets = new Insets(10, 0, 10, 0);
        MessageBubble d6Bubble = new MessageBubble("Benyapa Dorm", "", "11:11 pm", true);
        annBodyPanel.add(d6Bubble, annBodyGbc);

        annBodyGbc.gridy = 1; annBodyGbc.insets = new Insets(0, 0, 0, 0);
        MessageBubble d5Bubble = new MessageBubble("Jeenprasom Dorm", "2d ago", "", false);
        annBodyPanel.add(d5Bubble, annBodyGbc);

        announceOuterBox.add(annBodyPanel, BorderLayout.CENTER);
        container.add(announceOuterBox, gbc);

        return container;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // For Overview
    private JPanel createMetricCard(String labelText, JLabel digitsValue) {
        RoundedPanel containerCard = new RoundedPanel(new BorderLayout(0, 8));
        containerCard.setBackground(GradientBackground.GLASS);
        containerCard.setOutline(GradientBackground.GLASS_LINE);
        containerCard.setBorder(new EmptyBorder(11, 16, 13, 16));

    // titles sa overview, para machange yung color to white
        JLabel titleLabel = new JLabel(labelText) {
            @Override
            public void paintComponent(Graphics g) {
                setForeground(Color.BLACK);
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 15));

        digitsValue.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 46));
        digitsValue.setForeground(Color.BLACK);
        titleLabel.setForeground(Color.BLACK);
        containerCard.add(titleLabel, BorderLayout.NORTH);
        containerCard.add(digitsValue, BorderLayout.CENTER);
        return containerCard;
    }

    // Underlines (or un-underlines) a sidebar link to show which page is open
    private void setUnderlined(JLabel label, boolean underlined) {
        if (label instanceof SidebarItem) {
            ((SidebarItem) label).setSelected(underlined);
            return;
        }
        java.util.Map<java.awt.font.TextAttribute, Object> attrs =
            new java.util.HashMap<java.awt.font.TextAttribute, Object>(label.getFont().getAttributes());
        attrs.put(java.awt.font.TextAttribute.UNDERLINE,
            underlined ? java.awt.font.TextAttribute.UNDERLINE_ON : Integer.valueOf(-1));
        label.setFont(label.getFont().deriveFont(attrs));
    }

    private JLabel createMenuLink(String text, SidebarItem.Glyph glyph) {
        return new SidebarItem(text, glyph);
    }

    // Headers ng Dorm List
    private JLabel createTableCell(String val, int alignment) {
        JLabel lbl = new JLabel(val, alignment);
        lbl.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        lbl.setForeground(Color.BLACK);
        return lbl;
    }
}

// Tenant Page
class TenantDashboard extends JPanel {
    private Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private static final String ANNOUNCEMENT_TEXT =
        "I love you... In a way that all my thoughts are consumed by you. " +
        "I love you... In a way that I long to be close to you..to see your face, " +
        "to hear your voice , and to touch you";

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private JLabel linkMyDorm;      // hidden until the tenant applies to a dorm
    private Component myDormGap;    // spacing under the My Dorm link (hidden with it)

    // Browse feed width: share of the available space, and the narrowest it will ever get
    private static final int FEED_WIDTH_PERCENT = 95;
    private static final int FEED_MIN_WIDTH = 492;

    public TenantDashboard() {
        setLayout(new BorderLayout());

        // ---------- Sidebar: My Dorm / Browse / Log Out ----------
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SidebarItem.SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(200, 600));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(40, 12, 25, 12));

        linkMyDorm = createSidebarLink("My Dorm", SidebarItem.Glyph.HOME);
        JLabel linkBrowse = createSidebarLink("Browse", SidebarItem.Glyph.SEARCH);
        JLabel linkLogOut = createSidebarLink("Log Out", SidebarItem.Glyph.LOGOUT);

        myDormGap = Box.createVerticalStrut(4);
        sidebarPanel.add(linkMyDorm);
        sidebarPanel.add(myDormGap);
        // "My Dorm" only appears after the tenant successfully applies to a dorm post
        linkMyDorm.setVisible(false);
        myDormGap.setVisible(false);
        sidebarPanel.add(linkBrowse);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(linkLogOut);

        // ---------- Center cards ----------
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.add(createBrowseCardView(), "Browse");
        cardsContainer.add(createMyDormCardView(), "MyDorm");

        linkMyDorm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsContainer, "MyDorm");
                setUnderlined(linkMyDorm, true);
                setUnderlined(linkBrowse, false);
            }
        });
        linkBrowse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsContainer, "Browse");
                setUnderlined(linkBrowse, true);
                setUnderlined(linkMyDorm, false);
            }
        });
        linkLogOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FindTheDUI.showLogoutConfirmationDialog(TenantDashboard.this);
            }
        });

        add(sidebarPanel, BorderLayout.WEST);
        add(cardsContainer, BorderLayout.CENTER);

        // Tenants still land on Browse first (same as before)
        cardLayout.show(cardsContainer, "Browse");
        setUnderlined(linkBrowse, true);
    }

    // Shows the "My Dorm" link in the sidebar (called after a successful application)
    private void revealMyDorm() {
        linkMyDorm.setVisible(true);
        myDormGap.setVisible(true);
        Container sidebar = linkMyDorm.getParent();
        if (sidebar != null) {
            sidebar.revalidate();
            sidebar.repaint();
        }
    }

    // ---------- Sidebar helpers ----------
    private JLabel createSidebarLink(String text, SidebarItem.Glyph glyph) {
        return new SidebarItem(text, glyph);
    }

    private void setUnderlined(JLabel label, boolean underlined) {
        if (label instanceof SidebarItem) {
            ((SidebarItem) label).setSelected(underlined);
            return;
        }
        java.util.Map<java.awt.font.TextAttribute, Object> attrs =
            new java.util.HashMap<java.awt.font.TextAttribute, Object>(label.getFont().getAttributes());
        attrs.put(java.awt.font.TextAttribute.UNDERLINE,
            underlined ? java.awt.font.TextAttribute.UNDERLINE_ON : Integer.valueOf(-1));
        label.setFont(label.getFont().deriveFont(attrs));
    }

    // ---------- Browse view (dorm feed + filters) ----------
    private JPanel createBrowseCardView() {
        JPanel browsePanel = new GradientPanel(new BorderLayout());

        // Feed: a vertical list of dorm posts inside a scroll pane
        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setOpaque(false);

        feedPanel.add(createDormPostCard(
            "Benyapa Jeenprasom", "11 Nov 2069",
            "Jeenprasom Dorm",
            "Sampaloc, Manila",
            "7Q8F+M3 Sampaloc, Manila",
            "Clean and quiet dorm near the university, only a 5-minute walk to campus. It has 24/7 security, free Wi-Fi, a shared kitchen, a study lounge and a laundry area. Pets are not allowed and visitors must sign in at the front desk.",
            new String[] {"Wi-Fi", "Air Con", "Laundry", "Kitchen", "Gym"},
            "No pets. Visitors must sign in at the front desk. Curfew at 10:00 PM.",
            "\u20B14,500",
            DormStore.slotsText("Jeenprasom Dorm"),
            "Sampaloc Public Library",
            "Library",
            "0917 123 4567",
            "facebook.com/benyapa.dorm",
            25));
        feedPanel.add(Box.createVerticalStrut(14));
        JPanel secondPost = createDormPostCard(
            "Lalina Schuett", "12 Nov 2069",
            "Schuett Dorm",
            "Sta. Mesa, Manila",
            "7Q9G+2H Sta. Mesa, Manila",
            "Affordable dorm with 24/7 security and a study area",
            new String[] {"Wi-Fi", "Fan", "Laundry", "Parking", "CCTV"},
            "No smoking inside the dorm. Quiet hours from 9:00 PM to 6:00 AM.",
            "\u20B13,200",
            DormStore.slotsText("Schuett Dorm"),
            "Sta. Mesa Market",
            "Market",
            "0918 765 4321",
            "facebook.com/schuett.dorms",
            12);
        ((DormPostCard) secondPost).setSamplePhotos(7);   // 7 sample photos: the grid shows 4 with "+3" on the last
        feedPanel.add(secondPost);

        // More posts can be added here the same way: feedPanel.add(createDormPostCard(...));

        JPanel feedHolder = new JPanel(new BorderLayout());
        feedHolder.setOpaque(false);
        feedHolder.add(feedPanel, BorderLayout.NORTH);
        // The posts grow with the window (the full screen layout needs the room) and stay centred
        feedHolder.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int avail = feedHolder.getWidth();
                int feedWidth = Math.min(avail, Math.max(FEED_MIN_WIDTH, avail * FEED_WIDTH_PERCENT / 100));
                int side = Math.max(0, (avail - feedWidth) / 2);
                if (feedHolder.getInsets().left != side) {
                    feedHolder.setBorder(new EmptyBorder(0, side, 0, side));
                    feedHolder.revalidate();
                }
            }
        });

        JScrollPane feedScroll = new JScrollPane(feedHolder);

        ModernScrollBarUI.apply(feedScroll);
        feedScroll.setBorder(null);
        feedScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        feedScroll.getVerticalScrollBar().setUnitIncrement(16);
        GradientBackground.makeTransparent(feedScroll);

        JPanel centerFeedWrapper = new JPanel(new BorderLayout());
        centerFeedWrapper.setOpaque(false);
        centerFeedWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        centerFeedWrapper.add(feedScroll, BorderLayout.CENTER);

        JPanel rightFiltersPanel = createFilterPanel(feedPanel);

        browsePanel.add(centerFeedWrapper, BorderLayout.CENTER);
        browsePanel.add(rightFiltersPanel, BorderLayout.EAST);
        return browsePanel;
    }

    // Builds one dorm post for the tenant feed (with an Apply button)
    private JPanel createDormPostCard(String posterName, String postDate, String dormName, String location,
                                      String fullPlusCode, String description, String[] amenities, String dormRules,
                                      String monthly, String availableSlot, String nearbyName, String nearbyType,
                                      String contactNumber, String facebookAccount, int initialLikes) {
        JButton applyBtn = new JButton("Apply");
        applyBtn.setPreferredSize(new Dimension(104, 26));
        applyBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        applyBtn.setBackground(new Color(0xD6, 0xD6, 0xD6));
        applyBtn.setForeground(Color.BLACK);
        applyBtn.setOpaque(true);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorder(new LineBorder(new Color(0x55, 0x55, 0x55), 1));
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBtn.addActionListener(e -> {
            if (showConfirmDialog(owner(), "Apply")) {
                applyBtn.setText("Applied");
                applyBtn.setEnabled(false);
                DormStore.addApplicant(dormName, DormStore.currentTenantName); // shows up in the landlord's Approve Tenant list
                revealMyDorm();
            }
        });

        JPanel postCard = new DormPostCard(posterName, postDate, dormName, location, fullPlusCode, description, amenities,
            dormRules, monthly, availableSlot, nearbyName, nearbyType, contactNumber, facebookAccount, initialLikes, applyBtn);
        postCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));   // let it stretch with the feed
        feedEntries.add(new FeedEntry(postCard, amenities, parseRate(monthly)));
        return postCard;
    }

    // ---------- My Dorm view (announcements + Post Complain) ----------
    private JPanel createMyDormCardView() {
        JPanel page = new GradientPanel(new BorderLayout());
        page.setBackground(new Color(0xE8, 0xE8, 0xE8));

        // Outer "Announcement" box
        RoundedPanel outerBox = new RoundedPanel();
        outerBox.setLayout(new BoxLayout(outerBox, BoxLayout.Y_AXIS));
        outerBox.setBackground(GradientBackground.GLASS);
        outerBox.setOutline(GradientBackground.GLASS_LINE);
        outerBox.setBorder(new EmptyBorder(15, 30, 14, 24));

        outerBox.add(createAnnouncementEntry("11:11 pm"));
        outerBox.add(Box.createVerticalStrut(8));
        outerBox.add(createAnnouncementEntry("1w ago"));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(60, 45, 0, 55));
        topWrapper.add(outerBox, BorderLayout.NORTH);
        page.add(topWrapper, BorderLayout.CENTER);

        // "Post Complain" button, bottom right
        JButton postComplainBtn = new JButton("Post Complain");
        postComplainBtn.setPreferredSize(new Dimension(152, 40));
        postComplainBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        postComplainBtn.setBackground(new Color(0xCC, 0xCC, 0xCC));
        postComplainBtn.setForeground(Color.BLACK);
        postComplainBtn.setOpaque(true);
        postComplainBtn.setFocusPainted(false);
        postComplainBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0xA0, 0xA0, 0xA0)),
            new LineBorder(new Color(0x77, 0x77, 0x77), 1)));
        postComplainBtn.addActionListener(e -> showPostComplainDialog());

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(0, 0, 25, 25));
        bottomBar.add(postComplainBtn);
        page.add(bottomBar, BorderLayout.SOUTH);

        return page;
    }

    // One announcement: title above, gray text box with shadow, timestamp below on the right
    private JPanel createAnnouncementEntry(String timestamp) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
        entry.setOpaque(false);
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Announcement");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        titleLabel.setForeground(GradientBackground.TEXT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        entry.add(titleLabel);

        JTextArea messageArea = new JTextArea(ANNOUNCEMENT_TEXT);
        messageArea.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        messageArea.setForeground(new Color(0xE6, 0xE6, 0xF2));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);

        RoundedPanel messageBox = new RoundedPanel(new BorderLayout());
        messageBox.setBackground(new Color(255, 255, 255, 30));
        messageBox.setOutline(GradientBackground.GLASS_LINE);
        messageBox.setBorder(new EmptyBorder(18, 100, 14, 62));
        messageBox.add(messageArea, BorderLayout.CENTER);
        messageBox.setPreferredSize(new Dimension(100, 105));
        messageBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        messageBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        entry.add(messageBox);

        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 10));
        timeLabel.setForeground(GradientBackground.TEXT_SOFT);
        JPanel timeRow = new JPanel(new BorderLayout());
        timeRow.setOpaque(false);
        timeRow.setBorder(new EmptyBorder(4, 0, 0, 8));
        timeRow.add(timeLabel, BorderLayout.EAST);
        timeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        timeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        entry.add(timeRow);

        return entry;
    }

    // Dialog opened by "Post Complain" (styled like the Message mockup)
    private void showPostComplainDialog() {
        JDialog dialog = new JDialog(owner(), "Post Complain", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 430);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(new Color(0xBB, 0xBB, 0xBB));
        content.setBorder(new EmptyBorder(25, 50, 22, 50));

        JLabel messageLabel = new JLabel("Message");
        messageLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setBorder(new EmptyBorder(0, 6, 0, 0));
        content.add(messageLabel, BorderLayout.NORTH);

        JTextArea complaintArea = new JTextArea();
        complaintArea.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
        complaintArea.setForeground(Color.BLACK);
        complaintArea.setBackground(new Color(0xC8, 0xC8, 0xC8));
        complaintArea.setLineWrap(true);
        complaintArea.setWrapStyleWord(true);
        complaintArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(complaintArea);

        ModernScrollBarUI.apply(scroll);
        scroll.setBorder(BorderFactory.createMatteBorder(2, 2, 0, 0, new Color(0x99, 0x99, 0x99))); // inset look
        scroll.getViewport().setBackground(new Color(0xC8, 0xC8, 0xC8));
        content.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        // "Post" - flat gray button
        JButton postBtn = new JButton("Post");
        postBtn.setPreferredSize(new Dimension(80, 40));
        postBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        postBtn.setBackground(new Color(0xCC, 0xCC, 0xCC));
        postBtn.setForeground(Color.BLACK);
        postBtn.setOpaque(true);
        postBtn.setFocusPainted(false);
        postBtn.setBorder(new LineBorder(new Color(0xC0, 0xC0, 0xC0), 1));
        postBtn.addActionListener(ev -> {
            if (complaintArea.getText().trim().isEmpty()) {
                showCustomWarningDialog(dialog, "Please write your complaint first.");
                return;
            }
            // Ask for confirmation before posting
            if (showConfirmDialog(dialog, "Post Complain")) {
                dialog.dispose();
            }
        });

        // "Cancel" - light button with dark outline and drop shadow
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(80, 40));
        cancelBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(0xF2, 0xF2, 0xF2));
        cancelBtn.setForeground(Color.BLACK);
        cancelBtn.setOpaque(true);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0x88, 0x88, 0x88)),
            new LineBorder(new Color(0x55, 0x55, 0x55), 1)));
        cancelBtn.addActionListener(ev -> dialog.dispose());

        actions.add(postBtn);
        actions.add(cancelBtn);
        content.add(actions, BorderLayout.SOUTH);

        dialog.add(content);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    private void showCustomWarningDialog(Window owner, String message) {
        JDialog warnDialog = new JDialog(owner, "Message", Dialog.ModalityType.APPLICATION_MODAL);
        warnDialog.setUndecorated(true);
        warnDialog.setSize(480, 200);
        warnDialog.setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(new Color(0xBC, 0xBC, 0xBC));
        headerBar.setPreferredSize(new Dimension(480, 36));
        JLabel headerTitle = new JLabel("  Message");
        headerTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        headerTitle.setForeground(Color.BLACK);
        headerBar.add(headerTitle, BorderLayout.WEST);

        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);

        JLabel msgLabel = new JLabel("<html><div style='width: 380px;'>" + message + "</div></html>");
        msgLabel.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 22));
        msgLabel.setForeground(Color.BLACK);
        bodyPanel.add(msgLabel, gbc);


        // "OK" button
        gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(25, 0, 0, 25);
        FlatButton okBtn = new FlatButton("OK");
        okBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        okBtn.setForeground(Color.BLACK);
        okBtn.setBackground(new Color(0xDF, 0xDF, 0xDF));
        okBtn.setFocusPainted(false);
        okBtn.setMargin(new Insets(0, 0, 0, 0));
        okBtn.setBorder(new DropShadowBorder(new Color(0x9A, 0x9A, 0x9A), null));
        okBtn.setPreferredSize(new Dimension(92, 42));
        okBtn.setMinimumSize(new Dimension(92, 42));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> warnDialog.dispose());
        warnDialog.getRootPane().setDefaultButton(okBtn);

        bodyPanel.add(okBtn, gbc);

        mainPanel.add(headerBar, BorderLayout.NORTH);
        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        warnDialog.add(mainPanel);
        RoundedDialog.apply(warnDialog);
        warnDialog.setVisible(true);
    }

    // "Are you sure?" Yes / No confirmation dialog. Returns true only on "Yes"
    private boolean showConfirmDialog(Window owner, String title) {
        JDialog confirm = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        confirm.setSize(480, 190);
        confirm.setResizable(false);
        confirm.setLocationRelativeTo(owner);

        final boolean[] confirmed = {false};

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(0xF2, 0xF2, 0xF2));
        contentPanel.setBorder(new EmptyBorder(15, 40, 12, 20));

        JLabel questionLabel = new JLabel("Are you sure?", SwingConstants.LEFT);
        questionLabel.setFont(new Font("YU Gothic UI Light", Font.BOLD, 44));
        questionLabel.setForeground(Color.BLACK);
        contentPanel.add(questionLabel, BorderLayout.CENTER);

        JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionWrapper.setOpaque(false);

        JButton yesBtn = new JButton("Yes");
        yesBtn.setPreferredSize(new Dimension(86, 36));
        yesBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        yesBtn.setBackground(new Color(0xE0, 0xE0, 0xE0));
        yesBtn.setForeground(Color.BLACK);
        yesBtn.setOpaque(true);
        yesBtn.setFocusPainted(false);
        yesBtn.setBorder(new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1));
        yesBtn.addActionListener(ev -> {
            confirmed[0] = true;
            confirm.dispose();
        });

        JButton noBtn = new JButton("No");
        noBtn.setPreferredSize(new Dimension(86, 36));
        noBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        noBtn.setBackground(new Color(0xF2, 0xF2, 0xF2));
        noBtn.setForeground(Color.BLACK);
        noBtn.setOpaque(true);
        noBtn.setFocusPainted(false);
        noBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0xB0, 0xB0, 0xB0)),
            new LineBorder(new Color(0x55, 0x55, 0x55), 1)));
        noBtn.addActionListener(ev -> confirm.dispose());

        actionWrapper.add(yesBtn);
        actionWrapper.add(noBtn);
        contentPanel.add(actionWrapper, BorderLayout.SOUTH);

        confirm.add(contentPanel);
        confirm.getRootPane().setDefaultButton(noBtn);
        RoundedDialog.apply(confirm);
        confirm.setVisible(true);
        return confirmed[0];
    }

    // ---------- Filters (Amenities + Budget) ----------
    private static final String[] BUDGET_LABELS = {
        "Under \u20B13,000", "\u20B13,000 - \u20B15,000", "\u20B15,000 - \u20B18,000",
        "\u20B18,000 - \u20B110,000", "\u20B110,000+"};
    private static final int[] BUDGET_MIN = {0, 3000, 5000, 8000, 10000};
    private static final int[] BUDGET_MAX = {2999, 5000, 8000, 10000, Integer.MAX_VALUE};

    private final java.util.List<FeedEntry> feedEntries = new java.util.ArrayList<>();

    // One dorm post in the feed plus what the filters need to know about it
    private static class FeedEntry {
        final JPanel card;
        final String[] amenities;
        final int rate;

        FeedEntry(JPanel card, String[] amenities, int rate) {
            this.card = card;
            this.amenities = amenities;
            this.rate = rate;
        }

        boolean hasAmenity(String wanted) {
            for (String a : amenities) {
                if (a.equalsIgnoreCase(wanted)) {
                    return true;
                }
                if (wanted.equalsIgnoreCase("Air Conditioning") && a.equalsIgnoreCase("Air Con")) {
                    return true;
                }
            }
            return false;
        }

        // every ticked amenity must be offered; budget < 0 means "any budget"
        boolean matches(java.util.List<String> wanted, int budget) {
            for (String w : wanted) {
                if (!hasAmenity(w)) {
                    return false;
                }
            }
            return budget < 0 || (rate >= BUDGET_MIN[budget] && rate <= BUDGET_MAX[budget]);
        }
    }

    private static int parseRate(String monthly) {
        try {
            return Integer.parseInt(monthly.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // Two slim dropdown fields (same style as the landlord's Post New Dorm form). The feed updates right away.
    private JPanel createFilterPanel(JPanel feedPanel) {
        final int fieldW = 190;
        final int[] budget = {-1};                                  // -1 = any budget
        java.util.List<JCheckBox> amenityChecks = new java.util.ArrayList<>();
        java.util.List<JLabel> budgetRows = new java.util.ArrayList<>();

        DropdownField amenitiesField = new DropdownField("Amenities", fieldW);
        DropdownField budgetField = new DropdownField("Budget", fieldW);
        amenitiesField.setGlass(true);
        budgetField.setGlass(true);

        JLabel resultsLabel = new JLabel(" ");
        resultsLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 12));
        resultsLabel.setForeground(GradientBackground.TEXT_SOFT);
        JLabel clearLink = new JLabel("<html><u>Clear</u></html>");
        clearLink.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 12));
        clearLink.setForeground(GradientBackground.TEXT);
        clearLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearLink.setVisible(false);

        Runnable refresh = () -> {
            java.util.List<String> picked = new java.util.ArrayList<>();
            for (JCheckBox cb : amenityChecks) {
                if (cb.isSelected()) {
                    picked.add(cb.getText());
                }
            }
            amenitiesField.setValue(picked.isEmpty() ? null
                : picked.size() == 1 ? picked.get(0) : picked.get(0) + " +" + (picked.size() - 1));
            budgetField.setValue(budget[0] < 0 ? null : BUDGET_LABELS[budget[0]]);
            for (int i = 0; i < budgetRows.size(); i++) {
                budgetRows.get(i).setFont(new Font("Yu Gothic UI Light", i - 1 == budget[0] && budget[0] >= 0 ? Font.BOLD : Font.PLAIN, 13));
            }
            clearLink.setVisible(!picked.isEmpty() || budget[0] >= 0);
            applyFeedFilters(feedPanel, picked, budget[0], resultsLabel);
        };

        // Amenities: list of check boxes, stays open so several can be ticked
        JPopupMenu amenityPopup = amenitiesField.getPopup();
        for (String amenity : DormStore.AMENITIES) {
            OneUiCheckItem cb = new OneUiCheckItem(amenity);
            cb.addActionListener(e -> refresh.run());
            amenityChecks.add(cb);
            amenityPopup.add(cb);
        }

        // Budget: pick one range (or "Any budget"), the list closes after choosing
        JPopupMenu budgetPopup = budgetField.getPopup();
        String[] rowTexts = new String[BUDGET_LABELS.length + 1];
        rowTexts[0] = "Any budget";
        System.arraycopy(BUDGET_LABELS, 0, rowTexts, 1, BUDGET_LABELS.length);
        for (int i = 0; i < rowTexts.length; i++) {
            final int choice = i - 1;
            OneUiItem row = new OneUiItem(rowTexts[i], () -> {
                budget[0] = choice;
                budgetPopup.setVisible(false);
                refresh.run();
            });
            budgetRows.add(row);
            budgetPopup.add(row);
        }

        clearLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (JCheckBox cb : amenityChecks) {
                    cb.setSelected(false);
                }
                budget[0] = -1;
                refresh.run();
            }
        });

        // small line under the fields: "Showing 2 of 2 dorms" ........ Clear
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);
        infoRow.add(resultsLabel, BorderLayout.WEST);
        infoRow.add(clearLink, BorderLayout.EAST);
        infoRow.setPreferredSize(new Dimension(fieldW, 20));
        infoRow.setMaximumSize(new Dimension(fieldW, 20));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        amenitiesField.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetField.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(amenitiesField);
        stack.add(Box.createVerticalStrut(10));
        stack.add(budgetField);
        stack.add(Box.createVerticalStrut(8));
        stack.add(infoRow);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(fieldW + 20, 600));
        wrapper.setBorder(new EmptyBorder(20, 0, 20, 20));
        wrapper.add(stack, BorderLayout.NORTH);

        refresh.run();
        return wrapper;
    }

    // Rebuilds the feed so it only holds the dorms that match every picked filter
    private void applyFeedFilters(JPanel feedPanel, java.util.List<String> wanted, int budget, JLabel resultsLabel) {
        feedPanel.removeAll();
        int shown = 0;
        for (FeedEntry entry : feedEntries) {
            if (!entry.matches(wanted, budget)) {
                continue;
            }
            if (shown > 0) {
                feedPanel.add(Box.createVerticalStrut(14));
            }
            feedPanel.add(entry.card);
            shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No dorms match your filters.");
            empty.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
            empty.setForeground(GradientBackground.TEXT_SOFT);
            empty.setBorder(new EmptyBorder(10, 4, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            feedPanel.add(empty);
        }
        resultsLabel.setText("Showing " + shown + " of " + feedEntries.size()
            + (feedEntries.size() == 1 ? " dorm" : " dorms"));
        feedPanel.revalidate();
        feedPanel.repaint();
    }
}

class MessageBubble extends RoundedPanel {
    // Colours: edit the hex codes here
    private static final Color BUBBLE_BG   = new Color(255, 255, 255, 30);
    private static final Color TITLE_TEXT  = Color.decode("#F2F2FA");
    private static final Color DATE_TEXT   = Color.decode("#C9C9DC");
    private static final Color DOTS        = Color.decode("#C9C9DC");
    private static final Color DOTS_HOVER  = Color.decode("#FFFFFF");
    private static final Color BODY_TEXT   = Color.decode("#E6E6F2");
    private static final Color TIME_TEXT   = Color.decode("#A9A9C2");

    public MessageBubble(String title, String mainHeaderDate, String bottomTimestamp, boolean timeOnTop) {
        setBackground(BUBBLE_BG);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int verticalIndex = 0;

        // 1. Explicit Header Date Text (e.g., "11 Nov 2069" or "2d ago") on the very top left edge if provided and not timeOnTop
        if (!mainHeaderDate.isEmpty() && !timeOnTop) {
            gbc.gridy = verticalIndex++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel dateLbl = new JLabel(mainHeaderDate);
            dateLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 11));
            dateLbl.setForeground(DATE_TEXT);
            add(dateLbl, gbc);
        }

        // 1b. Time on Top scenario (e.g. 11:11 pm above the box contents)
        if (timeOnTop && !bottomTimestamp.isEmpty()) {
            gbc.gridy = verticalIndex++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel topTimeLbl = new JLabel(bottomTimestamp);
            topTimeLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 11));
            topTimeLbl.setForeground(DATE_TEXT);
            add(topTimeLbl, gbc);
        }

        // 2. Main content layer row (Title text + Options icon placeholder)
        gbc.gridy = verticalIndex++;
        gbc.insets = new Insets(0, 0, 6, 0);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        titleLabel.setForeground(TITLE_TEXT);
        titleRow.add(titleLabel, BorderLayout.WEST);

        // Three-dot options button -> asks for confirmation, then deletes this message
        JButton dotsButton = new JButton("\u2022\u2022\u2022");
        dotsButton.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 12));
        dotsButton.setForeground(DOTS);
        dotsButton.setContentAreaFilled(false);
        dotsButton.setBorderPainted(false);
        dotsButton.setFocusPainted(false);
        dotsButton.setMargin(new Insets(0, 4, 0, 4));
        dotsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dotsButton.setToolTipText("Delete message");
        dotsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                dotsButton.setForeground(DOTS_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                dotsButton.setForeground(DOTS);
            }
        });
        dotsButton.addActionListener(e -> {
            if (showDeleteConfirmationDialog()) {
                deleteSelf();
            }
        });
        titleRow.add(dotsButton, BorderLayout.EAST);
        add(titleRow, gbc);

        // 3. Main Text Area matching user dimensions dynamically
        gbc.gridy = verticalIndex++;
        gbc.insets = new Insets(0, 0, 4, 0);
        JTextArea messageContentArea = new JTextArea(
            "I love you... In a way that all my thoughts are consumed by you. " +
            "I love you... In a way that I long to be close to you..to see your face, " +
            "to hear your voice , and to touch you."
        );
        messageContentArea.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        messageContentArea.setForeground(BODY_TEXT);
        messageContentArea.setLineWrap(true);
        messageContentArea.setWrapStyleWord(true);
        messageContentArea.setEditable(false);
        messageContentArea.setOpaque(false);
        add(messageContentArea, gbc);

        // 4. Bottom Timestamp label if not printed on top
        if (!timeOnTop && !bottomTimestamp.isEmpty()) {
            gbc.gridy = verticalIndex++;
            gbc.insets = new Insets(4, 0, 0, 0);
            JLabel bottomTimeLbl = new JLabel(bottomTimestamp, SwingConstants.RIGHT);
            bottomTimeLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 10));
            bottomTimeLbl.setForeground(TIME_TEXT);
            
            JPanel rightAlignWrapper = new JPanel(new BorderLayout());
            rightAlignWrapper.setOpaque(false);
            rightAlignWrapper.add(bottomTimeLbl, BorderLayout.EAST);
            add(rightAlignWrapper, gbc);
        }
    }

    // Removes this message bubble from whatever panel it sits in and refreshes the layout
    private void deleteSelf() {
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
            Container top = parent.getParent();
            if (top != null) {
                top.revalidate();
                top.repaint();
            }
        }
    }

    // Confirmation dialog styled like the "Remove / Are you sure?" mockup.
    // Returns true only if the user clicks "Yes"
    private boolean showDeleteConfirmationDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Remove", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 190);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        final boolean[] confirmed = {false};

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(0xF2, 0xF2, 0xF2));
        contentPanel.setBorder(new EmptyBorder(15, 40, 12, 20));

        JLabel questionLabel = new JLabel("Are you sure?", SwingConstants.LEFT);
        questionLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 44));
        questionLabel.setForeground(Color.BLACK);
        contentPanel.add(questionLabel, BorderLayout.CENTER);

        JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionWrapper.setOpaque(false);

        // "Yes" - flat light gray button
        JButton yesBtn = new JButton("Yes");
        yesBtn.setPreferredSize(new Dimension(86, 36));
        yesBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        yesBtn.setBackground(new Color(0xE0, 0xE0, 0xE0));
        yesBtn.setForeground(Color.BLACK);
        yesBtn.setOpaque(true);
        yesBtn.setFocusPainted(false);
        yesBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1), new EmptyBorder(0, 0, 0, 0)));
        yesBtn.addActionListener(ev -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        // "No" - outlined button with a darker border and a soft drop shadow
        JButton noBtn = new JButton("No");
        noBtn.setPreferredSize(new Dimension(86, 36));
        noBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        noBtn.setBackground(new Color(0xF2, 0xF2, 0xF2));
        noBtn.setForeground(Color.BLACK);
        noBtn.setOpaque(true);
        noBtn.setFocusPainted(false);
        noBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0xB0, 0xB0, 0xB0)),
            new LineBorder(new Color(0x55, 0x55, 0x55), 1)));
        noBtn.addActionListener(ev -> dialog.dispose());

        actionWrapper.add(yesBtn);
        actionWrapper.add(noBtn);
        contentPanel.add(actionWrapper, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.getRootPane().setDefaultButton(noBtn); // Enter = "No" (safe default)
        RoundedDialog.apply(dialog);
        dialog.setVisible(true); // blocks until closed
        return confirmed[0];
    }
}

// Round avatar with the poster's initial
class AvatarCircle extends JComponent {
    private final String letter;

    public AvatarCircle(String letter, int size) {
        this.letter = letter;
        setPreferredSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0xEE, 0xEE, 0xEE));
        g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, getHeight() / 2));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(letter)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(letter, x, y);
        g2.dispose();
    }
}

// Dark rounded square with a simple "picture" icon (sun + mountains)
class ImagePlaceholder extends JComponent {
    public ImagePlaceholder() {
        setPreferredSize(new Dimension(153, 123));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0x55, 0x55, 0x55));
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        g2.setColor(new Color(0xEE, 0xEE, 0xEE));
        int sun = (int) (w * 0.16);
        g2.fillOval((int) (w * 0.27), (int) (h * 0.20), sun, sun);

        // small hill (left)
        g2.fillPolygon(
            new int[] {(int) (w * 0.08), (int) (w * 0.20), (int) (w * 0.34), (int) (w * 0.20)},
            new int[] {(int) (h * 0.88), (int) (h * 0.66), (int) (h * 0.66), (int) (h * 0.88)}, 4);

        // big mountain (right)
        g2.fillPolygon(
            new int[] {(int) (w * 0.26), (int) (w * 0.56), (int) (w * 0.68), (int) (w * 0.94), (int) (w * 0.94)},
            new int[] {(int) (h * 0.88), (int) (h * 0.46), (int) (h * 0.46), (int) (h * 0.74), (int) (h * 0.88)}, 5);
        g2.dispose();
    }
}

// Small house icon (the "\u2302" interest symbol); filled when the tenant has marked interest
class HouseIcon extends JComponent {
    private boolean interested = false;
    private boolean interactive = true;

    public HouseIcon() {
        setPreferredSize(new Dimension(22, 22));
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
        repaint();
    }

    // Landlords can see the icon but cannot click it (drawn in a lighter grey)
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        repaint();
    }

    public boolean isInteractive() {
        return interactive;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color c = interactive ? GradientBackground.TEXT : new Color(0xA9, 0xA9, 0xC2);

        // House outline: roof peak, eaves, walls
        java.awt.geom.Path2D.Double house = new java.awt.geom.Path2D.Double();
        house.moveTo(11, 3);     // roof peak
        house.lineTo(20, 11);    // right eave
        house.lineTo(17.5, 11);  // right wall top
        house.lineTo(17.5, 19);  // right wall bottom
        house.lineTo(4.5, 19);   // left wall bottom
        house.lineTo(4.5, 11);   // left wall top
        house.lineTo(2, 11);     // left eave
        house.closePath();

        g2.setColor(c);
        if (interested) {
            g2.fill(house);
        }
        g2.draw(house);
        g2.dispose();
    }
}

// Mockup-style field border: flat fill with a darker inset edge along the top and left
class InsetFieldBorder extends javax.swing.border.AbstractBorder {
    static final Color FILL = new Color(0xC6, 0xC6, 0xC6);

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(3, 10, 2, 8);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(3, 10, 2, 8);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        g.setColor(new Color(0x9D, 0x9D, 0x9D));
        g.fillRect(x, y, w, 3);       // darker top edge
        g.setColor(new Color(0xA7, 0xA7, 0xA7));
        g.fillRect(x, y, 3, h);       // darker left edge
    }
}

// Raised look: a drop shadow along the bottom and right, plus an optional thin outline
class DropShadowBorder extends javax.swing.border.AbstractBorder {
    private final Color outline;

    DropShadowBorder(Color shadow, Color outline) {
        this.outline = outline;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, ButtonShadowBorder.SIZE, ButtonShadowBorder.SIZE);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(1, 1, ButtonShadowBorder.SIZE, ButtonShadowBorder.SIZE);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        ButtonShadowBorder.paintShadow(c, g, x, y, w, h);
        if (outline != null) {
            g.setColor(outline);
            g.drawRect(x, y, w - ButtonShadowBorder.SIZE - 1, h - ButtonShadowBorder.SIZE - 1);
        }
    }
}

// Small outlined down-pointing triangle (the dropdown arrow in the mockup)
class OutlineTriangle extends JComponent {
    public OutlineTriangle() {
        setPreferredSize(new Dimension(40, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(new Color(0x44, 0x44, 0x44));
        int triW = 20, triH = 11;
        int x = (getWidth() - triW) / 2;
        int y = (getHeight() - triH) / 2;
        g2.drawPolygon(new int[] {x, x + triW, x + triW / 2}, new int[] {y, y, y + triH}, 3);
        g2.dispose();
    }
}

// ======================= One UI style (shared by every dropdown) =======================
// Soft rounded fields, a thin chevron, a floating rounded list with a gentle shadow, and pill-shaped
// row highlights. Change the colours here and every dropdown in the app follows.
final class OneUi {
    private OneUi() { }

    static final Color FIELD_BG    = new Color(0xF5, 0xF5, 0xF5);
    static final Color FIELD_HOVER = new Color(0xEC, 0xEC, 0xEC);
    static final Color FIELD_LINE  = new Color(0xD8, 0xD8, 0xD8);
    static final Color POPUP_BG    = new Color(0xFC, 0xFC, 0xFC);
    static final Color ROW_HOVER   = new Color(0xEE, 0xEE, 0xEE);
    static final Color TEXT        = new Color(0x1C, 0x1C, 0x1C);
    static final Color HINT        = new Color(0x80, 0x80, 0x80);
    static final Color ACCENT      = new Color(0x3E, 0x91, 0xFF);   // One UI blue (ticks, chosen item)

    static final int FIELD_ARC = 32;   // closed dropdown corner (diameter)
    static final int POPUP_ARC = 36;   // open list corner (diameter)
    static final int ROW_ARC   = 26;   // highlighted row corner (diameter)
    static final int ROW_HEIGHT = 36;

    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }
}

// Thin rounded chevron (points down, or up while the list is open)
class OneUiChevron extends JComponent {
    private Color color;
    private boolean up;

    void setColor(Color color) {
        this.color = color;
        repaint();
    }

    OneUiChevron(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(30, 20));
    }

    void setUp(boolean up) {
        this.up = up;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double dy = up ? 2.5 : -2.5;
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        p.moveTo(cx - 5, cy + dy);
        p.lineTo(cx, cy - dy);
        p.lineTo(cx + 5, cy + dy);
        g2.draw(p);
        g2.dispose();
    }
}

// The floating list: a rounded white card with a soft shadow that opens right under its field
class OneUiPopup extends JPopupMenu {
    private static final int SIDE = 6, TOP = 4, BOTTOM = 10;   // room around the card for the shadow
    private static final int PAD = 6;                          // space between the card edge and its rows

    OneUiPopup() {
        setOpaque(false);
        setBorder(new EmptyBorder(TOP + PAD, SIDE + PAD, BOTTOM + PAD, SIDE + PAD));
        setLightWeightPopupEnabled(true);
        addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                SwingUtilities.invokeLater(OneUiPopup.this::makeSeeThrough);
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
    }

    // The window/panel that carries the popup is opaque by default, which would show square corners
    private void makeSeeThrough() {
        Container parent = getParent();
        if (parent instanceof JComponent) {
            ((JComponent) parent).setOpaque(false);
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JWindow) {
            try {
                w.setBackground(new Color(0, 0, 0, 0));
            } catch (RuntimeException ignored) {
                // this platform cannot do see-through windows: the corners stay square, nothing else breaks
            }
        }
    }

    // Opens the list under the given field, as wide as the field
    void showUnder(JComponent field) {
        setPreferredSize(null);
        int h = getPreferredSize().height;
        setPreferredSize(new Dimension(field.getWidth() + 2 * SIDE, h));
        show(field, -SIDE, field.getHeight() + 4 - TOP);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        int x = SIDE, y = TOP, w = getWidth() - 2 * SIDE, h = getHeight() - TOP - BOTTOM;
        for (int i = 1; i <= 6; i++) {
            g2.setColor(new Color(0, 0, 0, 6));
            g2.fillRoundRect(x - i, y - i + 3, w + 2 * i, h + 2 * i, OneUi.POPUP_ARC + 2 * i, OneUi.POPUP_ARC + 2 * i);
        }
        g2.setColor(OneUi.POPUP_BG);
        g2.fillRoundRect(x, y, w, h, OneUi.POPUP_ARC, OneUi.POPUP_ARC);
        g2.setColor(new Color(0, 0, 0, 22));
        g2.drawRoundRect(x, y, w - 1, h - 1, OneUi.POPUP_ARC, OneUi.POPUP_ARC);
        g2.dispose();
    }
}

// One choice in a list: rounded pill highlight on hover, optional blue "chosen" look
class OneUiItem extends JLabel {
    private final Color hoverColor;
    private boolean hovering;

    OneUiItem(String text, Runnable onPick) {
        this(text, OneUi.ROW_HOVER, onPick);
    }

    OneUiItem(String text, Color hoverColor, Runnable onPick) {
        super(text);
        this.hoverColor = hoverColor;
        setOpaque(false);
        setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        setForeground(OneUi.TEXT);
        setBorder(new EmptyBorder(0, 14, 0, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                hovering = false;
                repaint();
                if (onPick != null && contains(e.getPoint())) {
                    onPick.run();
                }
            }
        });
    }

    void setChosen(boolean chosen) {
        setFont(getFont().deriveFont(chosen ? Font.BOLD : Font.PLAIN));
        setForeground(chosen ? OneUi.ACCENT : OneUi.TEXT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(OneUi.ROW_HEIGHT, d.height));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovering) {
            Graphics2D g2 = OneUi.smooth(g);
            g2.setColor(hoverColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), OneUi.ROW_ARC, OneUi.ROW_ARC);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

// Round tick-box used by the check lists (blue circle with a white tick when ticked)
class OneUiCheckIcon implements Icon {
    private static final int SIZE = 20;

    @Override public int getIconWidth() { return SIZE; }
    @Override public int getIconHeight() { return SIZE; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        boolean on = c instanceof AbstractButton && ((AbstractButton) c).isSelected();
        Graphics2D g2 = OneUi.smooth(g);
        if (on) {
            g2.setColor(OneUi.ACCENT);
            g2.fillOval(x + 1, y + 1, SIZE - 2, SIZE - 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Double tick = new java.awt.geom.Path2D.Double();
            tick.moveTo(x + 5.6, y + 10.4);
            tick.lineTo(x + 8.7, y + 13.4);
            tick.lineTo(x + 14.4, y + 6.8);
            g2.draw(tick);
        } else {
            g2.setColor(new Color(0xB4, 0xB4, 0xB4));
            g2.setStroke(new BasicStroke(1.7f));
            g2.drawOval(x + 1, y + 1, SIZE - 2, SIZE - 2);
        }
        g2.dispose();
    }
}

// A check box row for the multi-pick lists (stays open so several can be ticked)
class OneUiCheckItem extends JCheckBox {
    private boolean hovering;

    OneUiCheckItem(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        setForeground(OneUi.TEXT);
        setIcon(new OneUiCheckIcon());
        setSelectedIcon(new OneUiCheckIcon());
        setIconTextGap(12);
        setBorder(new EmptyBorder(0, 14, 0, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(OneUi.ROW_HEIGHT, d.height));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovering) {
            Graphics2D g2 = OneUi.smooth(g);
            g2.setColor(OneUi.ROW_HOVER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), OneUi.ROW_ARC, OneUi.ROW_ARC);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

// Closed dropdown box in the One UI style (soft rounded field + chevron). Clicking opens its rounded list.
class DropdownField extends JPanel {
    private final JLabel textLabel = new JLabel();
    private final OneUiPopup popup = new OneUiPopup();
    private final OneUiChevron chevron = new OneUiChevron(OneUi.HINT);
    private final String placeholder;
    private boolean glass;
    private String currentValue;
    private long closedAt = 0;
    private boolean hovering;

    public DropdownField(String placeholder, int width) {
        this(placeholder, width, 38);
    }

    // width 0 = stretch to whatever room the layout gives it
    public DropdownField(String placeholder, int width, int height) {
        super(new BorderLayout());
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(0, 16, 0, 4));
        textLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        add(textLabel, BorderLayout.CENTER);
        add(chevron, BorderLayout.EAST);

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width > 0 ? width : Integer.MAX_VALUE, height));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // remember when the list closed, so clicking the field while it is open just closes it
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                chevron.setUp(true);
                repaint();
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (System.currentTimeMillis() - closedAt < 250) {
                    return;
                }
                popup.showUnder(DropdownField.this);
            }
        });
        setValue(null);
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    // A simple pick-one choice: shows it in the field and closes the list
    public void addChoice(String text) {
        popup.add(new OneUiItem(text, () -> {
            setValue(text);
            popup.setVisible(false);
        }));
    }

    // null shows the grey placeholder, anything else shows the chosen value
    public void setValue(String value) {
        currentValue = value;
        textLabel.setText(value == null ? placeholder : value);
        if (glass) {
            textLabel.setForeground(value == null ? GradientBackground.TEXT_SOFT : GradientBackground.TEXT);
        } else {
            textLabel.setForeground(Color.BLACK);
        }
    }

    // Translucent look for fields that sit on the gradient background
    public void setGlass(boolean glass) {
        this.glass = glass;
        chevron.setColor(glass ? GradientBackground.TEXT_SOFT : OneUi.HINT);
        setValue(currentValue);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        boolean lit = hovering || popup.isVisible();
        if (glass) {
            g2.setColor(new Color(255, 255, 255, lit ? 52 : 32));
        } else {
            g2.setColor(lit ? OneUi.FIELD_HOVER : OneUi.FIELD_BG);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), OneUi.FIELD_ARC, OneUi.FIELD_ARC);
        g2.setColor(glass ? new Color(255, 255, 255, 70) : OneUi.FIELD_LINE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, OneUi.FIELD_ARC, OneUi.FIELD_ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}

// Button that always paints its own flat background colour (so the colour shows on every look-and-feel)
class FlatButton extends JButton {
    public FlatButton(String text) {
        super(text);
        setOpaque(true);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}

// Minimalist square check box icon: outlined rounded square, dark fill + white tick when selected
class SquareCheckIcon implements Icon {
    private static final int SIZE = 16;

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        paint(g2, x, y, c instanceof AbstractButton && ((AbstractButton) c).isSelected());
        g2.dispose();
    }

    // Draws the square (used by check boxes and by the photo picker tiles)
    static void paint(Graphics2D g2, int x, int y, boolean on) {
        Object oldAa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Stroke oldStroke = g2.getStroke();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.geom.RoundRectangle2D box =
            new java.awt.geom.RoundRectangle2D.Double(x + 0.75, y + 0.75, SIZE - 1.5, SIZE - 1.5, 4, 4);
        if (on) {
            g2.setColor(new Color(0x33, 0x33, 0x33));
            g2.fill(box);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Double tick = new java.awt.geom.Path2D.Double();
            tick.moveTo(x + 4.2, y + 8.4);
            tick.lineTo(x + 7.0, y + 11.2);
            tick.lineTo(x + 11.8, y + 4.9);
            g2.draw(tick);
        } else {
            g2.setColor(new Color(0xE2, 0xE2, 0xE2));
            g2.fill(box);
            g2.setColor(new Color(0x66, 0x66, 0x66));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(box);
        }
        g2.setStroke(oldStroke);
        if (oldAa != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }
}

// One photo box in the Post New Dorm form: empty grey square until a picture is uploaded.
// The picture fills the box (cropped to fit); clicking a filled box removes the picture.
class PhotoThumb extends JPanel {
    private java.awt.image.BufferedImage image;
    private java.io.File file;
    private Runnable onRemoved;

    public PhotoThumb() {
        setBackground(InsetFieldBorder.FILL);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hasPhoto()) {
                    clearPhoto();
                    if (onRemoved != null) {
                        onRemoved.run();
                    }
                }
            }
        });
    }

    // Called after the user clicks a photo to remove it
    public void setOnRemoved(Runnable onRemoved) {
        this.onRemoved = onRemoved;
    }

    public boolean hasPhoto() {
        return image != null;
    }

    public java.io.File getFile() {
        return file;
    }

    // Loads the picture; returns false if the file is not a readable image
    public boolean setPhoto(java.io.File f) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) {
                return false;
            }
            // Keep only a small copy in memory (these are just thumbnails)
            int max = 400;
            int w = img.getWidth();
            int h = img.getHeight();
            if (Math.max(w, h) > max) {
                double scale = (double) max / Math.max(w, h);
                int nw = Math.max(1, (int) Math.round(w * scale));
                int nh = Math.max(1, (int) Math.round(h * scale));
                java.awt.image.BufferedImage small =
                    new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = small.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(img, 0, 0, nw, nh, null);
                g2.dispose();
                img = small;
            }
            image = img;
            file = f;
            setToolTipText("Click to remove this photo");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            repaint();
            return true;
        } catch (java.io.IOException | RuntimeException ex) {
            return false;
        }
    }

    public void clearPhoto() {
        image = null;
        file = null;
        setToolTipText(null);
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double scale = Math.max((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
        g2.dispose();
    }
}

// One tile in the photo picker: a folder, or a picture thumbnail with its file name.
// Selected pictures get a dark frame and a tick; the tick box also appears when hovering.
class PickerTile extends JComponent {
    static final int W = 120;
    static final int IMG_H = 100;
    static final int H = 126;

    final File file;
    final boolean folder;
    private volatile java.awt.image.BufferedImage thumb;
    private volatile boolean broken;
    private boolean selected;
    private boolean hover;

    PickerTile(File file, boolean folder, java.util.function.Consumer<PickerTile> onClick) {
        this.file = file;
        this.folder = folder;
        setPreferredSize(new Dimension(W, H));
        setMinimumSize(new Dimension(W, H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(file.getName());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.accept(PickerTile.this);
            }
        });
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    void setThumb(java.awt.image.BufferedImage thumb) {
        this.thumb = thumb;
    }

    void markBroken() {
        this.broken = true;
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && fm.stringWidth(cut + "...") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(InsetFieldBorder.FILL);
        g2.fillRect(0, 0, w, h);

        // Picture area
        Shape oldClip = g2.getClip();
        g2.clipRect(0, 0, w, IMG_H);   // narrows the clip (keeps the scroll area's own clip)
        java.awt.image.BufferedImage img = thumb;
        if (folder) {
            g2.setColor(new Color(0xD4, 0xD4, 0xD4));
            g2.fillRect(0, 0, w, IMG_H);
            int fw = 58;
            int fh = 40;
            int x = (w - fw) / 2;
            int y = (IMG_H - fh) / 2 + 4;
            g2.setColor(new Color(0x7A, 0x7A, 0x7A));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y - 7, 26, 14, 5, 5));   // tab
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y, fw, fh, 7, 7));       // back
            g2.setColor(new Color(0x92, 0x92, 0x92));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y + 8, fw, fh - 8, 7, 7)); // front
        } else if (img != null) {
            double scale = Math.max((double) w / img.getWidth(), (double) IMG_H / img.getHeight());
            int dw = (int) Math.round(img.getWidth() * scale);
            int dh = (int) Math.round(img.getHeight() * scale);
            g2.drawImage(img, (w - dw) / 2, (IMG_H - dh) / 2, dw, dh, null);
        } else {
            g2.setColor(new Color(0xB4, 0xB4, 0xB4));
            g2.fillRect(0, 0, w, IMG_H);
            if (broken) {
                g2.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 11));
                g2.setColor(new Color(0x55, 0x55, 0x55));
                String msg = "No preview";
                g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, IMG_H / 2 + 4);
            }
        }
        g2.setClip(oldClip);

        // File / folder name
        g2.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 11));
        g2.setColor(new Color(0x1E, 0x1E, 0x1E));
        g2.drawString(fit(file.getName(), g2.getFontMetrics(), w - 16), 8, IMG_H + 17);

        // Frame + tick box
        if (selected) {
            g2.setColor(new Color(0x33, 0x33, 0x33));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(1, 1, w - 3, h - 3);
        } else if (hover) {
            g2.setColor(new Color(0x77, 0x77, 0x77));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(0, 0, w - 1, h - 1);
        }
        if (!folder && (selected || hover)) {
            SquareCheckIcon.paint(g2, w - 26, 8, selected);
        }
        g2.dispose();
    }
}

// Modern in-app photo picker: browse folders, see picture thumbnails, tick several, press Add Photos.
// Used instead of the plain system file chooser so it matches the rest of the system.
class PhotoPickerPanel extends JPanel {
    private static final Color PAGE_BG = Color.decode("#32568f");
    private static final int COLUMNS = 5;
    private static File lastDir;

    private final int maxPhotos;
    private final java.util.Set<File> selected = new java.util.LinkedHashSet<>();
    private final JTextField pathField = new JTextField();
    private final JPanel grid = new JPanel(new GridLayout(0, COLUMNS, 10, 10));
    private final JLabel emptyLabel = new JLabel(" ");
    private final JLabel countLabel = new JLabel();
    private final FlatButton upBtn = new FlatButton("Up");
    private final FlatButton addBtn = new FlatButton("Add Photos");
    private final JScrollPane scroll;
    private File currentDir;
    private SwingWorker<Void, PickerTile> loader;

    public PhotoPickerPanel(int maxPhotos, java.util.function.Consumer<java.util.List<File>> onDone, Runnable onCancel) {
        this.maxPhotos = maxPhotos;
        setLayout(new BorderLayout(0, 12));
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(22, 24, 18, 24));
        setPreferredSize(new Dimension(720, 540));

        // ----- Header: title, Up + path, quick places -----
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        
        JLabel title = new JLabel("Select Photos");
        title.setFont(new Font("YU Gothic UI Light", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(12));

        styleButton(upBtn, Color.WHITE, 60, 36, 13, new Color(0x9A, 0x9A, 0x9A), null);
        upBtn.addActionListener(e -> {
            if (currentDir != null && currentDir.getParentFile() != null) {
                navigate(currentDir.getParentFile());
            }
        }); 
        
        pathField.setBackground(InsetFieldBorder.FILL);
        pathField.setForeground(Color.BLACK);
        pathField.setCaretColor(Color.BLACK);
        pathField.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        pathField.setBorder(new InsetFieldBorder());
        pathField.setPreferredSize(new Dimension(100, 36));
        pathField.addActionListener(e -> {
            File typed = new File(pathField.getText().trim());
            if (typed.isDirectory()) {
                navigate(typed);
            } else {
                Toolkit.getDefaultToolkit().beep();
                pathField.setText(currentDir == null ? "" : currentDir.getAbsolutePath());
            }
        });
        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setOpaque(false);
        pathRow.add(upBtn, BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.add(pathRow);
        header.add(Box.createVerticalStrut(10));

        JPanel places = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        places.setOpaque(false);
        File home = new File(System.getProperty("user.home", "."));
        addPlace(places, "Pictures", new File(home, "Pictures"));
        addPlace(places, "Desktop", new File(home, "Desktop"));
        addPlace(places, "Downloads", new File(home, "Downloads"));
        addPlace(places, "Home", home);
        places.setAlignmentX(Component.LEFT_ALIGNMENT);
        places.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.add(places);
        add(header, BorderLayout.NORTH);

        // ----- Grid of folders + pictures -----
        emptyLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        emptyLabel.setForeground(Color.WHITE);
        emptyLabel.setBorder(new EmptyBorder(0, 2, 10, 0));
        emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyLabel.setVisible(false);
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(emptyLabel);
        stack.add(grid);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(PAGE_BG);
        holder.setBorder(new EmptyBorder(10, 10, 10, 10));
        holder.add(stack, BorderLayout.NORTH);

        scroll = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ModernScrollBarUI.apply(scroll);
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0x9D, 0x9D, 0x9D)));
        add(scroll, BorderLayout.CENTER);

        // ----- Footer: selection count + Cancel / Add Photos -----
        countLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);

        FlatButton cancelBtn = new FlatButton("Cancel");
        styleButton(cancelBtn, new Color(0xDF, 0xDF, 0xDF), 104, 42, 15,
            new Color(0x55, 0x55, 0x55), new Color(0x33, 0x33, 0x33));
        cancelBtn.addActionListener(e -> onCancel.run());
        styleButton(addBtn, Color.WHITE, 124, 42, 15, new Color(0x9A, 0x9A, 0x9A), null);
        addBtn.addActionListener(e -> onDone.accept(new java.util.ArrayList<>(selected)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(addBtn);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(countLabel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        updateFooter(null);
        File start = lastDir;
        if (start == null || !start.isDirectory()) {
            File pictures = new File(home, "Pictures");
            start = pictures.isDirectory() ? pictures : home;
        }
        navigate(start);
    }

    // Stop loading thumbnails when the picker is closed
    public void dispose() {
        if (loader != null) {
            loader.cancel(true);
        }
    }

    private void addPlace(JPanel row, String label, File dir) {
        if (!dir.isDirectory()) {
            return;
        }
        FlatButton b = new FlatButton(label);
        styleButton(b, new Color(0xD6, 0xD6, 0xD6), 84, 28, 12, new Color(0xA0, 0xA0, 0xA0), null);
        b.addActionListener(e -> navigate(dir));
        row.add(b);
    }

    private static void styleButton(FlatButton b, Color bg, int w, int h, int fontSize, Color shadow, Color outline) {
        b.setFont(new Font("Arial", Font.PLAIN, fontSize));
        b.setForeground(Color.BLACK);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(new DropShadowBorder(shadow, outline));
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static boolean isPicture(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
            || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    private void updateFooter(String message) {
        countLabel.setText(message != null ? message
            : (maxPhotos == Integer.MAX_VALUE ? selected.size() + " selected"
                                              : selected.size() + " of " + maxPhotos + " selected"));
        addBtn.setEnabled(!selected.isEmpty());
    }

    private void onTileClicked(PickerTile tile) {
        if (tile.folder) {
            navigate(tile.file);
            return;
        }
        if (selected.contains(tile.file)) {
            selected.remove(tile.file);
            tile.setSelected(false);
            updateFooter(null);
        } else if (selected.size() >= maxPhotos) {
            updateFooter("You can add up to " + maxPhotos + (maxPhotos == 1 ? " photo." : " photos."));
        } else {
            selected.add(tile.file);
            tile.setSelected(true);
            updateFooter(null);
        }
    }

    private void navigate(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        if (loader != null) {
            loader.cancel(true);
        }
        currentDir = dir;
        lastDir = dir;
        pathField.setText(dir.getAbsolutePath());
        upBtn.setEnabled(dir.getParentFile() != null);
        grid.removeAll();

        File[] items = dir.listFiles();
        java.util.List<File> folders = new java.util.ArrayList<>();
        java.util.List<File> pictures = new java.util.ArrayList<>();
        if (items != null) {
            for (File f : items) {
                if (f.isHidden() || f.getName().startsWith(".")) {
                    continue;
                }
                if (f.isDirectory()) {
                    folders.add(f);
                } else if (isPicture(f)) {
                    pictures.add(f);
                }
            }
        }
        java.util.Comparator<File> byName = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
        folders.sort(byName);
        pictures.sort(byName);

        java.util.List<PickerTile> pictureTiles = new java.util.ArrayList<>();
        for (File f : folders) {
            grid.add(new PickerTile(f, true, this::onTileClicked));
        }
        for (File f : pictures) {
            PickerTile t = new PickerTile(f, false, this::onTileClicked);
            t.setSelected(selected.contains(f));
            pictureTiles.add(t);
            grid.add(t);
        }
        if (items == null) {
            emptyLabel.setText("This folder can't be opened.");
        } else if (folders.isEmpty() && pictures.isEmpty()) {
            emptyLabel.setText("No folders or pictures here.");
        }
        emptyLabel.setVisible(items == null || (folders.isEmpty() && pictures.isEmpty()));
        grid.revalidate();
        grid.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));

        // Load thumbnails in the background so big folders do not freeze the window
        loader = new SwingWorker<Void, PickerTile>() {
            @Override
            protected Void doInBackground() {
                for (PickerTile t : pictureTiles) {
                    if (isCancelled()) {
                        break;
                    }
                    java.awt.image.BufferedImage img = readThumb(t.file, PickerTile.W);
                    if (img != null) {
                        t.setThumb(img);
                    } else {
                        t.markBroken();
                    }
                    publish(t);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<PickerTile> done) {
                for (PickerTile t : done) {
                    t.repaint();
                }
            }
        };
        loader.execute();
    }

    // Reads a reduced-size copy of a picture (sub-sampled while reading, so large photos load quickly)
    static java.awt.image.BufferedImage readThumb(File f, int target) {
        try (javax.imageio.stream.ImageInputStream in = javax.imageio.ImageIO.createImageInputStream(f)) {
            if (in == null) {
                return null;
            }
            java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                return null;
            }
            javax.imageio.ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
                int sub = Math.max(1, Math.min(w, h) / (target * 2));
                param.setSourceSubsampling(sub, sub, 0, 0);
                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        } catch (Exception | OutOfMemoryError ex) {
            return null;
        }
    }
}

// Shows the photo picker in a modal window and returns the pictures the user ticked (empty if cancelled)
class PhotoPickerDialog {
    static java.util.List<File> choose(Window owner, int maxPhotos) {
        JDialog d = new JDialog(owner, "Select Photos", Dialog.ModalityType.APPLICATION_MODAL);
        java.util.List<File> result = new java.util.ArrayList<>();
        PhotoPickerPanel panel = new PhotoPickerPanel(maxPhotos, files -> {
            result.addAll(files);
            d.dispose();
        }, d::dispose);
        d.setContentPane(panel);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(owner);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setVisible(true);
        panel.dispose();
        return result;
    }
}

// Thin, rounded, minimalist scroll bar (no arrow buttons, transparent track)
class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
    private static final int THICKNESS = 10;
    private static final Color THUMB_NORMAL = new Color(0xB4, 0xB4, 0xB4);
    private static final Color THUMB_HOVER = new Color(0x8C, 0x8C, 0x8C);
    private static final Color THUMB_DRAG = new Color(0x66, 0x66, 0x66);

    // Applies the modern look to both scroll bars of a scroll pane
    public static void apply(JScrollPane scrollPane) {
        Component view = scrollPane.getViewport().getView();
        if (view != null) {
            scrollPane.setBackground(view.getBackground());
        }
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        vBar.setUI(new ModernScrollBarUI());
        vBar.setPreferredSize(new Dimension(THICKNESS, 0));
        vBar.setOpaque(false);
        vBar.setUnitIncrement(16);

        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        hBar.setUI(new ModernScrollBarUI());
        hBar.setPreferredSize(new Dimension(0, THICKNESS));
        hBar.setOpaque(false);
        hBar.setUnitIncrement(16);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setFocusable(false);
        return button;
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(THICKNESS, 32);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // transparent track
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isDragging ? THUMB_DRAG : (isThumbRollover() ? THUMB_HOVER : THUMB_NORMAL));

        int pad = 2;
        int arc = THICKNESS - pad * 2;
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            g2.fillRoundRect(thumbBounds.x + pad, thumbBounds.y + pad,
                thumbBounds.width - pad * 2, thumbBounds.height - pad * 2, arc, arc);
        } else {
            g2.fillRoundRect(thumbBounds.x + pad, thumbBounds.y + pad,
                thumbBounds.width - pad * 2, thumbBounds.height - pad * 2, arc, arc);
        }
        g2.dispose();
    }
}

// Reusable Facebook-style dorm post (used in the tenant Browse feed and the landlord My Dorm feed).
// actionComponent is optional: pass an "Apply" button for tenants, or null for the landlord.
class DormPostCard extends RoundedPanel {
    public DormPostCard(String posterName, String postDate, String dormName, String location,
                        String fullPlusCode, String description, String[] amenities, String dormRules,
                        String monthly, String availableSlot, String nearbyName, String nearbyType,
                        String contactNumber, String facebookAccount, int initialLikes,
                        JComponent actionComponent) {
        setLayout(new BorderLayout(0, 0));
        setBackground(GradientBackground.GLASS);
        setOutline(GradientBackground.GLASS_LINE);
        setBorder(new EmptyBorder(15, 18, 12, 18));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(492, Integer.MAX_VALUE));

        // ----- Header: avatar + poster name + date -----
        JPanel header = new JPanel(new BorderLayout(21, 0));
        header.setOpaque(false);
        header.add(new AvatarCircle(posterName.substring(0, 1), 64), BorderLayout.WEST);

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setOpaque(false);
        nameBlock.setBorder(new EmptyBorder(12, 0, 0, 0));
        JLabel nameLabel = new JLabel(posterName);
        nameLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 16));
        nameLabel.setForeground(Color.BLACK);
        JLabel dateLabel = new JLabel(postDate);
        dateLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 9));
        dateLabel.setForeground(Color.BLACK);
        nameBlock.add(nameLabel);
        nameBlock.add(dateLabel);
        header.add(nameBlock, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ----- Body: details, photos, actions -----
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 92, 0, 0));

        body.add(createPostField("Dorm Name : " + dormName));
        body.add(createPostField("Location : " + location));

        // Everything else is hidden until the user clicks "See more"
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setVisible(false);

        JLabel seeMoreLink = new JLabel("See more");
        seeMoreLink.setFont(new Font("YU Gothic UI Light", Font.BOLD, 13));
        seeMoreLink.setForeground(Color.BLACK);
        seeMoreLink.setBorder(new EmptyBorder(0, 8, 3, 0));
        seeMoreLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        seeMoreLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // "See less" sits under all the text, at the bottom of the expanded details
        JLabel seeLessLink = new JLabel("See less");
        seeLessLink.setFont(new Font("YU Gothic UI Light", Font.BOLD, 13));
        seeLessLink.setForeground(Color.BLACK);
        seeLessLink.setBorder(new EmptyBorder(1, 8, 3, 0));
        seeLessLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        seeLessLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.detailsPanel = detailsPanel;
        this.seeMoreLink = seeMoreLink;
        this.seeLessLink = seeLessLink;

        seeMoreLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userExpanded = true;
                detailsPanel.setVisible(true);
                seeMoreLink.setVisible(false);
                relayoutWindow();
            }
        });
        seeLessLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userExpanded = false;
                detailsPanel.setVisible(false);
                seeMoreLink.setVisible(true);
                relayoutWindow();
            }
        });
        body.add(seeMoreLink);
        body.add(detailsPanel);

        detailsPanel.add(createDetailField("Full Plus Code : " + fullPlusCode));
        detailsPanel.add(createDetailField("Description : " + description));

        JPanel amenitiesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        amenitiesRow.setOpaque(false);
        amenitiesRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel amenitiesLabel = new JLabel("Amenities :");
        amenitiesLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 15));
        amenitiesLabel.setForeground(Color.BLACK);
        amenitiesLabel.setBorder(new EmptyBorder(0, 6, 0, 12));
        amenitiesRow.add(amenitiesLabel);
        for (String amenity : amenities) {
            JLabel chip = new GlassChip(amenity);
            chip.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 9));
            chip.setForeground(Color.BLACK);
            chip.setPreferredSize(new Dimension(42, 20));
            amenitiesRow.add(chip);
        }
        amenitiesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        detailsPanel.add(amenitiesRow);

        detailsPanel.add(createDetailField("Dorm Rules : " + dormRules));
        detailsPanel.add(createDetailField("Available Slot : " + availableSlot));
        detailsPanel.add(createDetailField("Monthly Rate : " + monthly));
        detailsPanel.add(createDetailField("Nearby Establishment"));
        detailsPanel.add(createDetailField("    Name : " + nearbyName));
        detailsPanel.add(createDetailField("    Type : " + nearbyType));
        detailsPanel.add(createDetailField("Contact Number : " + contactNumber));
        detailsPanel.add(createDetailField("Facebook Account : " + facebookAccount));
        detailsPanel.add(seeLessLink);
        body.add(Box.createVerticalStrut(3));

        // 2 x 2 photo grid (placeholders until real images are supported).
        // Clicking a photo opens the viewer, where the arrows swipe through all of them.
        dormTitle = dormName;
        photoGrid = new JPanel(new GridLayout(2, 2, 4, 4));
        photoGrid.setOpaque(false);
        rebuildPhotoGrid();
        photoGrid.setPreferredSize(new Dimension(250, 250));
        photoGrid.setMaximumSize(new Dimension(250, 250));
        photoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        // The photos live in a slot under the text in the small layout. The slot stays in place even
        // when the photos move to the right (full screen), so going back puts everything back exactly.
        photoSlot = new JPanel(new BorderLayout());
        photoSlot.setOpaque(false);
        photoSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        photoSlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, NARROW_PHOTOS.height));
        photoSlot.add(photoGrid, BorderLayout.WEST);
        photoGap = Box.createVerticalStrut(8);
        body.add(photoSlot);
        body.add(photoGap);

        // Optional action button (e.g. Apply) + interest icon with count
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        if (actionComponent != null) {
            actionRow.add(actionComponent);
        }

        // Only tenants (who get an action button such as Apply) can mark interest.
        // The landlord's dashboard passes no action component, so the icon is display-only there.
        final boolean canMarkInterest = actionComponent != null;
        final int[] likeCount = {initialLikes};
        final boolean[] liked = {false};
        HouseIcon thumb = new HouseIcon();
        thumb.setInteractive(canMarkInterest);
        JLabel likeCountLabel = new JLabel(String.valueOf(likeCount[0]));
        likeCountLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
        likeCountLabel.setForeground(Color.BLACK);
        if (canMarkInterest) {
            thumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            thumb.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    liked[0] = !liked[0];
                    likeCount[0] += liked[0] ? 1 : -1;
                    thumb.setInterested(liked[0]);
                    likeCountLabel.setText(String.valueOf(likeCount[0]));
                }
            });
        }
        actionRow.add(thumb);
        actionRow.add(likeCountLabel);
        body.add(actionRow);

        add(body, BorderLayout.CENTER);

        this.body = body;
        // Right-hand photo column (full screen). It asks for no height of its own, so the card is as tall
        // as the text, and the photos are stretched from the top of "Dorm Name" to the bottom of the text.
        photoWrap = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(widePhotoWidth + 32, 0);
            }
        };
        photoWrap.setOpaque(false);
        photoWrap.setBorder(new EmptyBorder(4, 24, 0, 8));
        // On a wide (full screen) feed the photos move to the right of the text
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePhotoLayout();
            }
        });
    }

    // Width from which the card puts text on the left and photos on the right
    private static final int WIDE_WIDTH = 820;

    private static final Dimension NARROW_PHOTOS = new Dimension(250, 250);   // small layout: a square below the text
    private int widePhotoWidth = 300;

    private JPanel photoGrid;
    private JPanel photoSlot;
    private Component photoGap;
    private JPanel photoWrap;
    private JPanel body;
    private boolean wideMode;
    private JPanel detailsPanel;
    private JLabel seeMoreLink;
    private JLabel seeLessLink;
    private boolean userExpanded;   // what the reader chose in the narrow layout

    // Photos take whatever is left beside the text (300px up to 460px wide), 5:4 ratio
    private String dormTitle = "";
    private java.util.List<java.awt.image.BufferedImage> photoImages;   // null = placeholder pictures

    // Shows the dorm's real photos (any number). The grid shows the first 4, with "+N" on the last
    // one when there are more; clicking any photo opens the viewer with every photo in it.
    public void setPhotos(java.util.List<File> files) {
        java.util.List<java.awt.image.BufferedImage> loaded = new java.util.ArrayList<>();
        for (File f : files) {
            java.awt.image.BufferedImage img = PhotoTile.load(f);
            if (img != null) {
                loaded.add(img);
            }
        }
        photoImages = loaded.isEmpty() ? null : loaded;
        rebuildPhotoGrid();
    }

    // Makes up some coloured sample pictures so a post with many photos ("+N" on the 4th) can be previewed
    public void setSamplePhotos(int count) {
        java.util.List<java.awt.image.BufferedImage> made = new java.util.ArrayList<>();
        for (int n = 0; n < count; n++) {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(800, 800, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            float hue = (n * 0.13f + 0.55f) % 1f;
            g2.setPaint(new GradientPaint(0, 0, Color.getHSBColor(hue, 0.35f, 0.95f),
                                          800, 800, Color.getHSBColor(hue, 0.65f, 0.60f)));
            g2.fillRect(0, 0, 800, 800);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fillOval(520, 120, 130, 130);
            g2.fillPolygon(new int[] {0, 260, 520, 800, 800, 0}, new int[] {800, 420, 640, 380, 800, 800}, 6);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("YU Gothic UI Light", Font.BOLD, 120));
            g2.drawString("Photo " + (n + 1), 60, 150);
            g2.dispose();
            made.add(img);
        }
        photoImages = made.isEmpty() ? null : made;
        rebuildPhotoGrid();
    }

    private void rebuildPhotoGrid() {
        int total = photoImages == null ? 4 : photoImages.size();
        int shown = Math.min(4, total);
        photoGrid.removeAll();
        photoGrid.setLayout(total == 1 ? new GridLayout(1, 1, 4, 4)
                          : total == 2 ? new GridLayout(1, 2, 4, 4)
                          : new GridLayout(2, 2, 4, 4));
        for (int i = 0; i < shown; i++) {
            final int start = i;
            JComponent tile = photoImages == null
                ? new ImagePlaceholder()
                : new PhotoTile(photoImages.get(i), (i == shown - 1 && total > shown) ? total - shown : 0);
            tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tile.setToolTipText("Click to view bigger");
            tile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openPhotoViewer(start);
                }
            });
            photoGrid.add(tile);
        }
        photoGrid.revalidate();
        photoGrid.repaint();
    }

    private void openPhotoViewer(int start) {
        java.util.List<JComponent> slides = new java.util.ArrayList<>();
        if (photoImages == null) {
            for (int n = 0; n < 4; n++) {
                slides.add(new ImagePlaceholder());
            }
        } else {
            for (java.awt.image.BufferedImage img : photoImages) {
                slides.add(new PhotoSlide(img));
            }
        }
        PhotoViewerDialog.show(SwingUtilities.getWindowAncestor(this), dormTitle, slides, start);
    }

    // Full screen: the photos are a perfect square, top-aligned with "Dorm Name". The side follows the
    // height of the text beside it (300 to 460px) and never takes room the text column needs.
    private void sizeWidePhotos() {
        int textHeight = Math.max(250, body.getPreferredSize().height - 4);   // minus the 4px top padding
        int room = Math.max(300, getWidth() - 520);
        int side = Math.min(room, Math.max(300, Math.min(460, textHeight)));
        Dimension size = new Dimension(side, side);
        if (side != widePhotoWidth || !size.equals(photoGrid.getPreferredSize())) {
            widePhotoWidth = side;
            photoGrid.setPreferredSize(size);
            photoGrid.setMaximumSize(size);
            revalidate();
        }
    }

    private void updatePhotoLayout() {
        boolean wide = getWidth() >= WIDE_WIDTH;
        if (wide == wideMode) {
            if (wide) {
                sizeWidePhotos();   // keep the photos in step while the window is resized or the text grows
            }
            return;
        }
        wideMode = wide;
        if (wide) {
            // Full screen: text on the left, photos on the right, both starting at the same top line
            photoSlot.remove(photoGrid);
            photoGap.setVisible(false);
            detailsPanel.setVisible(true);   // show all the details so the text fills the space beside the photos
            seeMoreLink.setVisible(false);
            seeLessLink.setVisible(false);
            photoWrap.add(photoGrid, BorderLayout.NORTH);   // NORTH keeps it at its own square size
            add(photoWrap, BorderLayout.EAST);
            sizeWidePhotos();
        } else {
            // Back to the small layout: photos under the text again, details as the reader had left them
            remove(photoWrap);
            photoWrap.remove(photoGrid);
            detailsPanel.setVisible(userExpanded);
            seeMoreLink.setVisible(!userExpanded);
            seeLessLink.setVisible(true);
            photoGrid.setPreferredSize(NARROW_PHOTOS);
            photoGrid.setMaximumSize(NARROW_PHOTOS);
            photoSlot.add(photoGrid, BorderLayout.WEST);
            photoGap.setVisible(true);
        }
        revalidate();
        repaint();
    }

    // Always-visible lines (Dorm Name, Location): cut after 2 lines with "... See more" if very long
    private JComponent createPostField(String text) {
        return new CollapsibleText(text, new Font("YU Gothic UI Light", Font.PLAIN, 15), 350, 2);
    }

    // Lines inside the hidden section: shown in full once "See more" is clicked
    private JComponent createDetailField(String text) {
        return new CollapsibleText(text, new Font("YU Gothic UI Light", Font.PLAIN, 15), 350, Integer.MAX_VALUE);
    }

    // Makes the whole window re-layout so the post grows/shrinks right away
    private void relayoutWindow() {
        Container parent = getParent();
        while (parent != null) {
            parent.invalidate();
            parent = parent.getParent();
        }
        invalidate();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.validate();
            window.repaint();
        }
    }
}

// Shared sample data so the landlord and tenant dashboards stay connected while the app is running
class DormStore {
    // Amenity choices shared by the tenant's Amenities filter and the landlord's Post New Dorm form
    static final String[] AMENITIES = {"Wi-Fi", "Air Conditioning", "Laundry", "Kitchen", "Gym"};

    static class Dorm {
        final String name;
        final int totalSlots;
        int occupied;                 // approved tenants
        final java.util.List<String> tenants = new java.util.ArrayList<>();   // names of current tenants
        final int monthlyRate;
        final java.util.List<String> pendingApplicants = new java.util.ArrayList<>();

        Dorm(String name, int totalSlots, int occupied, int monthlyRate) {
            this.name = name;
            this.totalSlots = totalSlots;
            this.occupied = occupied;
            this.monthlyRate = monthlyRate;
        }

        // Seeds placeholder tenant names so "occupied" always matches the tenant list
        void seedTenants(String... names) {
            for (String n : names) {
                tenants.add(n);
            }
            occupied = tenants.size();
        }

        int available() {
            return totalSlots - occupied;
        }
    }

    static final java.util.List<Dorm> DORMS = new java.util.ArrayList<>();

    // (no real accounts yet)
    static String currentTenantName = "Demo Tenant";

    static {
        Dorm d1 = new Dorm("Jeenprasom Dorm", 10, 7, 4500);
        d1.seedTenants("Pimchanok Suwan", "Natthaya Boonmee", "Kanokwan Srisai", "Thanaporn Meechai",
            "Chanida Rattana", "Wipada Saetang", "Orawan Prasert");
        d1.pendingApplicants.add("Sarocha Chankimha");
        d1.pendingApplicants.add("Lapisara Intarasut");
        Dorm d2 = new Dorm("Benyapa Dorm", 12, 7, 3200);
        d2.seedTenants("Araya Kaewkla", "Ploypailin Dee", "Ratchanee Wong", "Supansa Thong",
            "Napat Chaiyo", "Jiraporn Lamai", "Mintra Sukjai");
        d2.pendingApplicants.add("Sirilak Kwong");
        DORMS.add(d1);
        DORMS.add(d2);
    }

    static Dorm find(String name) {
        for (Dorm d : DORMS) {
            if (d.name.equals(name)) {
                return d;
            }
        }
        return null;
    }

    static int totalTenants() {
        int total = 0;
        for (Dorm d : DORMS) {
            total += d.occupied;
        }
        return total;
    }

    // Called when a tenant clicks Apply on a dorm post
    static void addApplicant(String dormName, String tenantName) {
        Dorm d = find(dormName);
        if (d != null && !d.pendingApplicants.contains(tenantName)) {
            d.pendingApplicants.add(tenantName);
        }
    }

    // Landlord approves: the applicant becomes a tenant and takes one slot
    static boolean approve(Dorm d, String tenantName) {
        if (d.available() <= 0) {
            return false;
        }
        if (d.pendingApplicants.remove(tenantName)) {
            d.tenants.add(tenantName);
            d.occupied = d.tenants.size();
            return true;
        }
        return false;
    }

    // Landlord removes a current tenant: their slot becomes available again
    static void removeTenant(Dorm d, String tenantName) {
        if (d.tenants.remove(tenantName)) {
            d.occupied = d.tenants.size();
        }
    }

    static void decline(Dorm d, String tenantName) {
        d.pendingApplicants.remove(tenantName);
    }

    static String slotsText(String dormName) {
        Dorm d = find(dormName);
        if (d == null) {
            return "-";
        }
        int free = d.available();
        return free <= 0 ? "Full" : free + (free == 1 ? " slot" : " slots");
    }

    static String twoDigits(int n) {
        return String.format("%02d", n);
    }
}


// see more/less
// One field of a post, e.g. "Location : Sampaloc, Manila". The name before the colon is drawn in bold.
class CollapsibleText extends JComponent {
    private final int maxCollapsedLines;
    private static final int PAD_TOP = 3;       // gap between two fields is PAD_TOP + PAD_BOTTOM (half of the old 10)
    private static final int PAD_BOTTOM = 2;
    private static final int PAD_LEFT = 8;
    private static final String ELLIPSIS = "... ";
    private static final String SEE_MORE = "See more";
    private static final String SEE_LESS = "See less";
    private static final Color LINK_COLOR = Color.BLACK;

    private static class Word {
        final String text;
        final boolean bold;

        Word(String text, boolean bold) {
            this.text = text;
            this.bold = bold;
        }
    }

    private final int wrapWidth;
    private final Font plainFont;
    private final Font boldFont;
    private final FontMetrics plainFm;
    private final FontMetrics boldFm;
    private final java.util.List<java.util.List<Word>> allLines;
    private boolean expanded = false;

    public CollapsibleText(String text, Font font, int wrapWidth) {
        this(text, font, wrapWidth, 2);
    }

    // maxLines = how many lines to show before "... See more" (Integer.MAX_VALUE = never cut)
    public CollapsibleText(String text, Font font, int wrapWidth, int maxLines) {
        this.maxCollapsedLines = maxLines;
        this.wrapWidth = wrapWidth;
        this.plainFont = font;
        this.boldFont = font.deriveFont(Font.BOLD);
        setFont(font);
        setForeground(Color.BLACK);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        this.plainFm = getFontMetrics(plainFont);
        this.boldFm = getFontMetrics(boldFont);
        this.allLines = wrapWords(splitWords(text), wrapWidth);

        if (isTruncatable()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggle();
                }
            });
        }
    }

    private boolean isTruncatable() {
        return allLines.size() > maxCollapsedLines;
    }

    // Expand / collapse and force the whole window to re-layout so the post grows or shrinks
    private void toggle() {
        expanded = !expanded;
        invalidate();
        Container parent = getParent();
        while (parent != null) {
            parent.invalidate();
            parent = parent.getParent();
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.validate();
            window.repaint();
        } else {
            revalidate();
            repaint();
        }
    }

    // Splits the text into words; everything up to the first colon (the field name) is bold
    private static java.util.List<Word> splitWords(String text) {
        String label;
        String rest;
        int colon = text.indexOf(':');
        if (colon >= 0 && colon <= 30) {
            label = text.substring(0, colon + 1);
            rest = text.substring(colon + 1);
        } else if (colon < 0) {
            label = text;          // a heading without a value, e.g. "Nearby Establishment"
            rest = "";
        } else {
            label = "";
            rest = text;
        }
        java.util.List<Word> words = new java.util.ArrayList<>();
        for (String w : label.trim().split("\\s+")) {
            if (!w.isEmpty()) {
                words.add(new Word(w, true));
            }
        }
        for (String w : rest.trim().split("\\s+")) {
            if (!w.isEmpty()) {
                words.add(new Word(w, false));
            }
        }
        return words;
    }

    private int wordWidth(Word w) {
        return (w.bold ? boldFm : plainFm).stringWidth(w.text);
    }

    private int lineWidth(java.util.List<Word> line) {
        int width = 0;
        for (int i = 0; i < line.size(); i++) {
            width += wordWidth(line.get(i));
            if (i > 0) {
                width += plainFm.charWidth(' ');
            }
        }
        return width;
    }

    // Breaks the words into lines that fit the given pixel width
    private java.util.List<java.util.List<Word>> wrapWords(java.util.List<Word> words, int width) {
        java.util.List<java.util.List<Word>> lines = new java.util.ArrayList<>();
        java.util.List<Word> current = new java.util.ArrayList<>();
        int space = plainFm.charWidth(' ');
        int currentW = 0;
        for (Word word : words) {
            Word w = word;
            // a single word wider than the line gets split by characters
            while (wordWidth(w) > width && w.text.length() > 1) {
                FontMetrics fm = w.bold ? boldFm : plainFm;
                int cut = w.text.length();
                while (cut > 1 && fm.stringWidth(w.text.substring(0, cut)) > width) {
                    cut--;
                }
                if (!current.isEmpty()) {
                    lines.add(current);
                    current = new java.util.ArrayList<>();
                    currentW = 0;
                }
                java.util.List<Word> piece = new java.util.ArrayList<>();
                piece.add(new Word(w.text.substring(0, cut), w.bold));
                lines.add(piece);
                w = new Word(w.text.substring(cut), w.bold);
            }
            int ww = wordWidth(w);
            int needed = current.isEmpty() ? ww : currentW + space + ww;
            if (needed <= width) {
                current.add(w);
                currentW = needed;
            } else {
                lines.add(current);
                current = new java.util.ArrayList<>();
                current.add(w);
                currentW = ww;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }

    private java.util.List<Word> collapsedLastLine() {
        int room = wrapWidth - plainFm.stringWidth(ELLIPSIS) - boldFm.stringWidth(SEE_MORE);
        java.util.List<Word> last = new java.util.ArrayList<>(allLines.get(maxCollapsedLines - 1));
        while (!last.isEmpty() && lineWidth(last) > room) {
            Word w = last.get(last.size() - 1);
            if (w.text.length() <= 1) {
                last.remove(last.size() - 1);
            } else {
                last.set(last.size() - 1, new Word(w.text.substring(0, w.text.length() - 1), w.bold));
            }
        }
        return last;
    }

    // Draws one line word by word (bold words in bold) and returns the x where it ended
    private int drawLine(Graphics2D g2, java.util.List<Word> line, int y) {
        int x = PAD_LEFT;
        int space = plainFm.charWidth(' ');
        g2.setColor(getForeground());
        for (int i = 0; i < line.size(); i++) {
            Word w = line.get(i);
            if (i > 0) {
                x += space;
            }
            g2.setFont(w.bold ? boldFont : plainFont);
            g2.drawString(w.text, x, y);
            x += wordWidth(w);
        }
        return x;
    }

    @Override
    public Dimension getPreferredSize() {
        int lineCount;
        if (!isTruncatable()) {
            lineCount = allLines.size();
        } else if (expanded) {
            lineCount = allLines.size() + 1;      // + "See less" line
        } else {
            lineCount = maxCollapsedLines;
        }
        return new Dimension(PAD_LEFT + wrapWidth, PAD_TOP + PAD_BOTTOM + lineCount * plainFm.getHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int lineHeight = plainFm.getHeight();
        int y = PAD_TOP + plainFm.getAscent();

        if (!isTruncatable()) {
            for (java.util.List<Word> line : allLines) {
                drawLine(g2, line, y);
                y += lineHeight;
            }
        } else if (expanded) {
            for (java.util.List<Word> line : allLines) {
                drawLine(g2, line, y);
                y += lineHeight;
            }
            g2.setFont(boldFont);
            g2.setColor(LINK_COLOR);
            g2.drawString(SEE_LESS, PAD_LEFT, y);
        } else {
            
            for (int i = 0; i < maxCollapsedLines - 1; i++) {
                drawLine(g2, allLines.get(i), y);
                y += lineHeight;
            }
            int x = drawLine(g2, collapsedLastLine(), y);
            g2.setFont(plainFont);
            g2.setColor(getForeground());
            g2.drawString(ELLIPSIS, x, y);
            x += plainFm.stringWidth(ELLIPSIS);
            g2.setFont(boldFont);
            g2.setColor(LINK_COLOR);
            g2.drawString(SEE_MORE, x, y);
        }
        g2.dispose();
    }
}

// Gives the small message / confirmation dialogs rounded corners (radius 10).
// Call RoundedDialog.apply(dialog) after the dialog's content is added and before setVisible(true).
final class RoundedDialog {
    static final int RADIUS = 10;
    private static final Color HEADER_BG = new Color(0xBC, 0xBC, 0xBC);
    private static final Color OUTLINE = new Color(255, 255, 255, 60);

    // Modern dialog theme: solid black header over a nearly flat blue -> purple body
    static final Color DIALOG_HEADER = new Color(0x0B, 0x0B, 0x10);
    static final Color DIALOG_BLUE   = Color.decode("#32568f");
    static final Color DIALOG_PURPLE = Color.decode("#6c8ab9");
    static final Color DIALOG_LINE   = new Color(255, 255, 255, 60);

    private RoundedDialog() {}

    static void apply(JDialog d) {
        apply(d, DIALOG_BLUE, OUTLINE, true);
    }

    // Same, with a custom background and outline colour (e.g. the dark photo viewer)
    static void apply(JDialog d, Color background, Color outline) {
        apply(d, background, outline, false);
    }

    private static void apply(JDialog d, Color background, Color outline, boolean themed) {
        boolean wasDecorated = !d.isUndecorated();
        String title = d.getTitle();
        Container inner = d.getContentPane();
        d.setUndecorated(true);

        RoundedRoot root = new RoundedRoot(background, outline, themed);
        if (wasDecorated) {
            // rounded corners need an undecorated window, so draw the title bar ourselves
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(themed ? DIALOG_HEADER : HEADER_BG);
            header.setPreferredSize(new Dimension(480, 36));
            JLabel headerTitle = new JLabel("  " + (title == null ? "" : title));
            headerTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
            headerTitle.setForeground(themed ? Color.WHITE : Color.BLACK);
            header.add(headerTitle, BorderLayout.WEST);
            root.add(header, BorderLayout.NORTH);
        }
        if (themed) {
            themeTree(inner);
        }
        root.add(inner, BorderLayout.CENTER);
        d.setContentPane(root);

        try {
            d.setBackground(new Color(0, 0, 0, 0)); // transparent window so the corners show through
        } catch (Exception ex) {
            // per-pixel transparency unavailable: clip the window to a rounded shape instead
            final Runnable clip = () -> {
                try {
                    d.setShape(new java.awt.geom.RoundRectangle2D.Float(
                        0, 0, d.getWidth(), d.getHeight(), RADIUS * 2f, RADIUS * 2f));
                } catch (Exception ignored) { }
            };
            d.addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { clip.run(); }
                @Override public void componentShown(ComponentEvent e) { clip.run(); }
            });
        }
    }

    // Replacement for JOptionPane.showMessageDialog with the same rounded look
    static void showMessage(Component parent, String title, String message) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 210);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);

        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel msgLabel = new JLabel("<html><div style='width: 400px;'>" + safe + "</div></html>");
        msgLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 18));
        msgLabel.setForeground(Color.BLACK);
        body.add(msgLabel, gbc);

        gbc.gridy = 1; gbc.weighty = 0; gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 15, 25);
        FlatButton okBtn = new FlatButton("OK");
        okBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        okBtn.setForeground(Color.BLACK);
        okBtn.setBackground(new Color(0xDF, 0xDF, 0xDF));
        okBtn.setFocusPainted(false);
        okBtn.setMargin(new Insets(0, 0, 0, 0));
        okBtn.setBorder(new DropShadowBorder(new Color(0x9A, 0x9A, 0x9A), null));
        okBtn.setPreferredSize(new Dimension(92, 42));
        okBtn.setMinimumSize(new Dimension(92, 42));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());
        body.add(okBtn, gbc);

        dialog.add(body);
        dialog.getRootPane().setDefaultButton(okBtn);
        apply(dialog);
        dialog.setVisible(true);
    }

    // ----- recolours a dialog's existing content for the blue/purple theme -----
    private static boolean isLight(Color c) {
        return c != null && c.getAlpha() == 255 && (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 > 140;
    }

    private static Color readableText(Color fg) {
        if (fg == null) {
            return GradientBackground.TEXT;
        }
        int lum = (fg.getRed() * 299 + fg.getGreen() * 587 + fg.getBlue() * 114) / 1000;
        if (lum < 110) {
            return GradientBackground.TEXT;        // black / dark text -> near white
        }
        if (lum < 170 && Math.abs(fg.getRed() - fg.getBlue()) < 30) {
            return GradientBackground.TEXT_SOFT;   // gray captions -> soft white
        }
        return fg;
    }

    private static void theme(Component c) {
        if (c instanceof JLabel) {
            c.setForeground(readableText(c.getForeground()));
        } else if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            b.setForeground(readableText(b.getForeground()));
            if (b instanceof JButton) {
                if (isLight(b.getBackground())) {
                    b.setBackground(new Color(0, 0, 0, 0));   // glass pill is drawn by GlassButtonUI
                }
            } else {
                b.setOpaque(false);
            }
        } else if (c instanceof JScrollPane) {
            ((JScrollPane) c).setOpaque(false);
            ((JScrollPane) c).getViewport().setOpaque(false);
        } else if (c instanceof RoundedPanel) {
            if (isLight(c.getBackground())) {
                c.setBackground(new Color(255, 255, 255, 34));
            }
        } else if (c instanceof JPanel) {
            Color bg = c.getBackground();
            if (HEADER_BG.equals(bg)) {
                c.setBackground(DIALOG_HEADER);            // old gray title bar -> solid black
            } else if (((JPanel) c).isOpaque() && isLight(bg)) {
                ((JPanel) c).setOpaque(false);             // let the blue/purple show through
            }
        }
    }

    // Themes a component and everything below it, and keeps theming rows added later
    // (the tenant lists are rebuilt after every approve / decline / remove)
    private static void themeTree(Component c) {
        if (c instanceof javax.swing.text.JTextComponent || c instanceof JComboBox
                || c instanceof JList || c instanceof JScrollBar) {
            return;
        }
        theme(c);
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (jc.getClientProperty("dialogThemeWatch") == null) {
                jc.putClientProperty("dialogThemeWatch", Boolean.TRUE);
                jc.addContainerListener(new ContainerAdapter() {
                    @Override public void componentAdded(ContainerEvent e) {
                        themeTree(e.getChild());
                    }
                });
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                themeTree(child);
            }
        }
    }

    // Content pane that paints a white rounded background, clips its children to the
    // rounded shape and draws a rounded outline on top
    static class RoundedRoot extends JPanel {
        private final Color outline;
        final boolean themed;

        RoundedRoot(Color background, Color outline) {
            this(background, outline, false);
        }

        RoundedRoot(Color background, Color outline, boolean themed) {
            super(new BorderLayout());
            this.outline = outline;
            this.themed = themed;
            setOpaque(false);
            setBackground(background);
        }

        private java.awt.geom.RoundRectangle2D.Float shape(float inset) {
            return new java.awt.geom.RoundRectangle2D.Float(inset, inset,
                getWidth() - 2 * inset, getHeight() - 2 * inset, RADIUS * 2f, RADIUS * 2f);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (themed) {
                // almost solid: blue on one side, purple on the other, with only a short blend between
                float[] stops = {0f, 0.30f, 0.70f, 1f};
                Color[] cols = {DIALOG_BLUE, DIALOG_BLUE, DIALOG_PURPLE, DIALOG_PURPLE};
                g2.setPaint(new LinearGradientPaint(0, 0, Math.max(1, getWidth()), Math.max(1, getHeight()), stops, cols));
            } else {
                g2.setColor(getBackground());
            }
            g2.fill(shape(0));
            g2.dispose();
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.clip(shape(0));
            super.paintChildren(g2);
            g2.dispose();
        }

        @Override public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(outline);
            g2.draw(shape(0.5f));
            g2.dispose();
        }
    }
}

// Panel with 8px rounded corners: paints its own background (and optional outline) so
// any box-style container can use it in place of a plain JPanel
class RoundedPanel extends JPanel {
    static final int RADIUS = 8;
    private Color outline;

    RoundedPanel() {
        setOpaque(false);
    }

    RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    void setOutline(Color outline) {
        this.outline = outline;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS * 2f, RADIUS * 2f));
        if (outline != null) {
            g2.setColor(outline);
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                RADIUS * 2f, RADIUS * 2f));
        }
        g2.dispose();
    }
}

// Soft "floating" drop shadow under the bottom/right edge of a button. Wraps whatever border the
// button already has (outline, padding...) and adds room for the shadow next to it.
class ButtonShadowBorder extends javax.swing.border.AbstractBorder {
    static final int SIZE = 8;   // room reserved for the shadow (offset + blur)
    private final javax.swing.border.Border inner;

    ButtonShadowBorder(javax.swing.border.Border inner) {
        this.inner = inner;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets i = inner == null ? new Insets(0, 0, 0, 0) : inner.getBorderInsets(c);
        insets.set(i.top, i.left, i.bottom + SIZE, i.right + SIZE);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        paintShadow(c, g, x, y, w, h);
        if (inner != null) {
            inner.paintBorder(c, g, x, y, w - SIZE, h - SIZE);
        }
    }

    // Colour of whatever the button sits on, so the shadow strip can be cleared before drawing
    private static Color backdrop(Component c) {
        for (Component p = c.getParent(); p != null; p = p.getParent()) {
            if (p instanceof RoundedPanel || p instanceof RoundedDialog.RoundedRoot || p.isOpaque()) {
                return p.getBackground();
            }
        }
        return Color.LIGHT_GRAY;
    }

    static void paintShadow(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        int fw = w - SIZE;
        int fh = h - SIZE;
        // only the strip to the right of / below the button face
        java.awt.geom.Area strips = new java.awt.geom.Area(new Rectangle(x, y, w, h));
        strips.subtract(new java.awt.geom.Area(new Rectangle(x, y, fw, fh)));
        g2.clip(strips);
        g2.setColor(backdrop(c));
        g2.fillRect(x, y, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean pressed = c instanceof AbstractButton && ((AbstractButton) c).getModel().isPressed();
        final float offset = 2f;      // how far the shadow is pushed down/right
        final float blur = 6f;        // how far it fades out
        final float peak = pressed ? 0.14f : 0.30f;   // darkest point, right next to the button
        // Stack many thin, translucent layers (outermost first) so the opacity falls off smoothly
        // with distance, like a real blurred shadow, instead of in visible steps.
        float covered = 0f;
        for (float e = blur; e >= 0f; e -= 0.5f) {
            float t = 1f - e / (blur + 0.5f);
            float target = peak * t * t;                         // smooth ease-in toward the button
            float layer = (target - covered) / (1f - covered);   // alpha that brings the total to target
            covered = target;
            if (layer <= 0f) {
                continue;
            }
            g2.setColor(new Color(0f, 0f, 0f, Math.min(1f, layer)));
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(x + offset - e, y + offset - e,
                fw + 2f * e, fh + 2f * e, 3f + 2f * e, 3f + 2f * e));
        }
        g2.dispose();
    }
}

// Gives every JButton in the app the floating shadow as soon as it is shown, including buttons
// created later inside dialogs. Set a "noShadow" client property on a button to opt out.
final class FloatingButtons {
    private FloatingButtons() {}

    static void install() {
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (!(ev instanceof HierarchyEvent)) {
                return;
            }
            HierarchyEvent he = (HierarchyEvent) ev;
            if ((he.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                    && he.getComponent() instanceof JButton
                    && he.getComponent().isShowing()) {
                addShadow((JButton) he.getComponent());
            }
        }, AWTEvent.HIERARCHY_EVENT_MASK);
    }

    private static void addShadow(JButton b) {
        if (b.getClientProperty("noShadow") != null || b.getParent() instanceof JScrollBar) {
            return;
        }
        if (GlassButtons.isOnGradient(b)) {
            GlassButtons.apply(b);   // buttons on the gradient: translucent pill instead of a gray box with a shadow
            return;
        }
        if (!b.isOpaque() && !b.isContentAreaFilled()) {
            return;   // text-only / transparent buttons have no face to float
        }
        javax.swing.border.Border cur = b.getBorder();
        if (cur instanceof ButtonShadowBorder || cur instanceof DropShadowBorder) {
            return;
        }
        // older hard 2-3px matte "shadows" are replaced by the soft one
        if (cur instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder cb = (javax.swing.border.CompoundBorder) cur;
            if (cb.getOutsideBorder() instanceof javax.swing.border.MatteBorder) {
                Insets mi = cb.getOutsideBorder().getBorderInsets(b);
                if (mi.top == 0 && mi.left == 0 && mi.bottom > 0 && mi.bottom == mi.right) {
                    cur = cb.getInsideBorder();
                }
            }
        }
        boolean sized = b.isPreferredSizeSet();
        Dimension d = b.getPreferredSize();
        b.setBorder(new ButtonShadowBorder(cur));
        if (sized) {
            // keep the button face the size it was designed at
            b.setPreferredSize(new Dimension(d.width + ButtonShadowBorder.SIZE, d.height + ButtonShadowBorder.SIZE));
        }
        b.revalidate();
        b.repaint();
    }
}

// Small dropdown (label + chevron) in the One UI style that opens a rounded list of choices underneath it.
// onSelect receives the index of the chosen item. Colours are passed in so they are easy to edit.
class CompactDropdown extends JPanel {
    private final JLabel textLabel = new JLabel();
    private final OneUiPopup popup = new OneUiPopup();
    private final OneUiChevron chevron;
    private final Color fill;
    private final Color line;
    private long closedAt = 0;
    private boolean hovering;

    CompactDropdown(String[] items, int width, int height, Color bg, Color border, Color text, Color hover,
                    java.util.function.IntConsumer onSelect) {
        super(new BorderLayout());
        this.fill = bg;
        this.line = new Color(border.getRed(), border.getGreen(), border.getBlue(), 110);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 14, 0, 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        
        textLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        textLabel.setForeground(text);
        textLabel.setText(items.length > 0 ? items[0] : "");
        add(textLabel, BorderLayout.CENTER);

        chevron = new OneUiChevron(text);
        chevron.setPreferredSize(new Dimension(26, height));
        add(chevron, BorderLayout.EAST);

        java.util.List<OneUiItem> rows = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            final int index = i;
            OneUiItem row = new OneUiItem(items[i], hover, () -> {
                textLabel.setText(items[index]);
                for (int r = 0; r < rows.size(); r++) {
                    rows.get(r).setChosen(r == index);
                }
                popup.setVisible(false);
                onSelect.accept(index);
            });
            row.setChosen(i == 0);
            rows.add(row);
            popup.add(row);
        }
        // clicking the field while the list is open should just close it
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                chevron.setUp(true);
                repaint();
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (System.currentTimeMillis() - closedAt < 250) {
                    return;
                }
                popup.showUnder(CompactDropdown.this);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        int arc = Math.min(OneUi.FIELD_ARC, getHeight());
        if (hovering) {
            g2.setColor(new Color(Math.max(0, fill.getRed() - 10), Math.max(0, fill.getGreen() - 10), Math.max(0, fill.getBlue() - 10)));
        } else {
            g2.setColor(fill);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setColor(line);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}

// Sidebar row: line icon + label. The open page gets a rounded highlight and bold text,
// and rows light up slightly on hover.
class SidebarItem extends JLabel {
    // ----- Colours: edit the hex codes here -----
    // (dark theme: SIDEBAR_BG #0C1014, TEXT #F5F5F5, SELECTED_BG #262A31, HOVER_BG #181C22)
    static final Color SIDEBAR_BG  = Color.decode("#0B0D13");
    static final Color TEXT        = Color.decode("#F2F2FA");
    static final Color SELECTED_BG = new Color(255, 255, 255, 38);
    static final Color HOVER_BG    = new Color(255, 255, 255, 20);
    static final int   RADIUS      = 10;

    enum Glyph { DASHBOARD, BUILDING, BELL, LOGOUT, HOME, SEARCH }

    private boolean selected;
    private boolean hover;

    SidebarItem(String text, Glyph glyph) {
        super(text);
        setIcon(new GlyphIcon(glyph));
        setIconTextGap(14);
        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        setForeground(TEXT);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        setFont(getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);   // stretch to the sidebar width
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (selected || hover) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? SELECTED_BG : HOVER_BG);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS * 2f, RADIUS * 2f));
            g2.dispose();
        }
        super.paintComponent(g);
    }

    // 22x22 line icons drawn with Java2D in the label's text colour
    private static class GlyphIcon implements Icon {
        private static final int SIZE = 22;
        private final Glyph glyph;

        GlyphIcon(Glyph glyph) {
            this.glyph = glyph;
        }

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
            switch (glyph) {
                case HOME:
                    p.moveTo(3, 11); p.lineTo(11, 4); p.lineTo(19, 11);
                    p.moveTo(5.5, 9.5); p.lineTo(5.5, 18); p.lineTo(16.5, 18); p.lineTo(16.5, 9.5);
                    p.moveTo(9, 18); p.lineTo(9, 13); p.lineTo(13, 13); p.lineTo(13, 18);
                    g2.draw(p);
                    break;
                case DASHBOARD:
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(3, 3, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(12.5f, 3, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(3, 12.5f, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(12.5f, 12.5f, 6.5f, 6.5f, 2, 2));
                    break;
                case BUILDING:
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(4.5f, 3, 13, 16, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(8, 6.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(12, 6.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(8, 10.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(12, 10.5f, 2, 2));
                    p.moveTo(9.5f, 19); p.lineTo(9.5f, 15.5f); p.lineTo(12.5f, 15.5f); p.lineTo(12.5f, 19);
                    g2.draw(p);
                    break;
                case BELL:
                    p.moveTo(4.5f, 16); p.curveTo(6, 15, 6.5f, 13.5f, 6.5f, 10);
                    p.curveTo(6.5f, 6.5f, 8.5f, 4.5f, 11, 4.5f);
                    p.curveTo(13.5f, 4.5f, 15.5f, 6.5f, 15.5f, 10);
                    p.curveTo(15.5f, 13.5f, 16, 15, 17.5f, 16); p.closePath();
                    p.moveTo(9, 18.5f); p.curveTo(9.5f, 20, 12.5f, 20, 13, 18.5f);
                    g2.draw(p);
                    break;
                case LOGOUT:
                    p.moveTo(9, 4); p.lineTo(4.5f, 4); p.lineTo(4.5f, 18); p.lineTo(9, 18);
                    p.moveTo(8.5f, 11); p.lineTo(19, 11);
                    p.moveTo(15, 7); p.lineTo(19, 11); p.lineTo(15, 15);
                    g2.draw(p);
                    break;
                case SEARCH:
                    g2.draw(new java.awt.geom.Ellipse2D.Float(3.5f, 3.5f, 11.5f, 11.5f));
                    p.moveTo(13.5f, 13.5f); p.lineTo(18.5f, 18.5f);
                    g2.draw(p);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }
}

// Instagram-style photo viewer: dark window, big photo, arrows (or drag / arrow keys) to swipe
// left and right, dots and a counter showing where you are.
class PhotoViewerDialog {
    // ----- Colours: edit the hex codes here -----
    static final Color BG      = Color.decode("#32568f");
    static final Color BAR     = Color.decode("#32568f");
    static final Color TEXT    = Color.decode("#FFFFFF");
    static final Color OUTLINE = Color.decode("#333333");

    static void show(Window owner, String title, java.util.List<? extends JComponent> slides, int start) {
        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        int w = 900;
        int h = 680;
        if (owner != null) {
            w = Math.max(420, Math.min(w, owner.getWidth() - 60));
            h = Math.max(360, Math.min(h, owner.getHeight() - 60));
        }
        d.setSize(w, h);
        d.setLocationRelativeTo(owner);

        PhotoSlidePane pane = new PhotoSlidePane(slides, start);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BAR);
        bar.setPreferredSize(new Dimension(100, 48));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("YU Gothic UI Light", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        titleLabel.setBorder(new EmptyBorder(0, 18, 0, 0));
        bar.add(titleLabel, BorderLayout.WEST);
        JLabel close = new JLabel("\u00D7   ", SwingConstants.RIGHT);
        close.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 28));
        close.setForeground(TEXT);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                d.dispose();
            }
        });
        bar.add(close, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        content.add(bar, BorderLayout.NORTH);
        content.add(pane, BorderLayout.CENTER);
        d.add(content);

        JRootPane root = d.getRootPane();
        root.registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> pane.previous(),
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> pane.next(),
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        RoundedDialog.apply(d, BG, OUTLINE);
        d.setVisible(true);
    }
}

// The photo area of the viewer: draws the current photo, slides to the next/previous one,
// and paints the arrows, counter and dots on top.
class PhotoSlidePane extends JComponent {
    // ----- Colours: edit the hex codes here -----
    private static final Color BG          = Color.decode("#111111");
    private static final Color ARROW_FILL  = Color.decode("#F2F2F2");
    private static final Color ARROW_ICON  = Color.decode("#111111");
    private static final Color DOT_ACTIVE  = Color.decode("#FFFFFF");
    private static final Color DOT_IDLE    = Color.decode("#6B6B6B");
    private static final Color COUNTER_BG  = Color.decode("#000000");
    private static final Color COUNTER_TXT = Color.decode("#FFFFFF");

    private static final int PAD = 20;          // margin around the photo
    private static final int STRIP = 44;        // bottom strip that holds the dots
    private static final int ARROW_R = 20;      // arrow button radius

    private final java.util.List<? extends JComponent> slides;
    private int index;
    private int previous;
    private int direction = 1;
    private float progress = 1f;               // 1 = not animating
    private final javax.swing.Timer timer;
    private int pressX = -1;

    PhotoSlidePane(java.util.List<? extends JComponent> slides, int start) {
        this.slides = slides;
        this.index = Math.max(0, Math.min(start, slides.size() - 1));
        this.previous = index;
        timer = new javax.swing.Timer(12, e -> {
            progress = Math.min(1f, progress + 0.08f);
            repaint();
            if (progress >= 1f) {
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressX = e.getX();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int dx = e.getX() - pressX;
                if (pressX >= 0 && Math.abs(dx) > 50) {          // dragged: swipe
                    go(dx < 0 ? index + 1 : index - 1);
                } else if (overArrow(e.getX(), e.getY(), true)) {
                    previous();
                } else if (overArrow(e.getX(), e.getY(), false)) {
                    next();
                }
                pressX = -1;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hand = overArrow(e.getX(), e.getY(), true) || overArrow(e.getX(), e.getY(), false);
                setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    void next() { go(index + 1); }

    void previous() { go(index - 1); }

    private void go(int target) {
        if (target < 0 || target >= slides.size() || target == index) {
            return;
        }
        previous = index;
        index = target;
        direction = target > previous ? 1 : -1;
        progress = 0f;
        timer.restart();
        repaint();
    }

    private int areaHeight() {
        return getHeight() - STRIP;
    }

    // Arrows are hidden on the first / last photo, like Instagram
    private boolean overArrow(int mx, int my, boolean left) {
        if (left ? index <= 0 : index >= slides.size() - 1) {
            return false;
        }
        int cx = left ? PAD + ARROW_R + 6 : getWidth() - PAD - ARROW_R - 6;
        int cy = areaHeight() / 2;
        return Math.hypot(mx - cx, my - cy) <= ARROW_R + 2;
    }

    private void paintSlide(Graphics2D g2, JComponent slide, int offsetX) {
        int w = getWidth() - 2 * PAD;
        int h = areaHeight() - 2 * PAD;
        if (w <= 0 || h <= 0) {
            return;
        }
        Graphics2D c = (Graphics2D) g2.create(offsetX + PAD, PAD, w, h);
        slide.setSize(w, h);
        slide.print(c);
        c.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        g2.setColor(BG);
        g2.fillRect(0, 0, w, getHeight());

        if (progress < 1f) {
            float e = 1f - (1f - progress) * (1f - progress) * (1f - progress);   // ease out
            paintSlide(g2, slides.get(previous), Math.round(-direction * e * w));
            paintSlide(g2, slides.get(index), Math.round(direction * (1f - e) * w));
        } else {
            paintSlide(g2, slides.get(index), 0);
        }

        // counter pill, top right of the photo
        String counter = (index + 1) + " / " + slides.size();
        g2.setFont(new Font("YU Gothic UI Light", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int pw = fm.stringWidth(counter) + 20;
        int px = w - PAD - pw - 10;
        int py = PAD + 10;
        g2.setColor(new Color(COUNTER_BG.getRed(), COUNTER_BG.getGreen(), COUNTER_BG.getBlue(), 150));
        g2.fillRoundRect(px, py, pw, 24, 24, 24);
        g2.setColor(COUNTER_TXT);
        g2.drawString(counter, px + 10, py + 16);

        // arrows
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = 0; side < 2; side++) {
            boolean left = side == 0;
            if (left ? index <= 0 : index >= slides.size() - 1) {
                continue;
            }
            int cx = left ? PAD + ARROW_R + 6 : w - PAD - ARROW_R - 6;
            int cy = areaHeight() / 2;
            g2.setColor(new Color(ARROW_FILL.getRed(), ARROW_FILL.getGreen(), ARROW_FILL.getBlue(), 225));
            g2.fillOval(cx - ARROW_R, cy - ARROW_R, ARROW_R * 2, ARROW_R * 2);
            g2.setColor(ARROW_ICON);
            int dir = left ? -1 : 1;
            g2.drawPolyline(new int[] {cx - dir * 4, cx + dir * 4, cx - dir * 4},
                            new int[] {cy - 9, cy, cy + 9}, 3);
        }

        // dots
        int n = slides.size();
        int gap = 14;
        int startX = (w - (n - 1) * gap) / 2;
        int cy = getHeight() - STRIP / 2;
        for (int i = 0; i < n; i++) {
            boolean active = i == index;
            int r = active ? 4 : 3;
            g2.setColor(active ? DOT_ACTIVE : DOT_IDLE);
            g2.fillOval(startX + i * gap - r, cy - r, r * 2, r * 2);
        }
        g2.dispose();
    }
}

// One photo in the post's grid: the picture cropped to fill the tile, with "+N" on top when
// the post has more photos than the grid can show
class PhotoTile extends JComponent {
    private final java.awt.image.BufferedImage image;
    private final int more;

    PhotoTile(java.awt.image.BufferedImage image, int more) {
        this.image = image;
        this.more = more;
        setPreferredSize(new Dimension(153, 123));
    }

    // Reads a picture, scaled down to at most 1600px on its longest side (keeps memory in check
    // when someone uploads lots of photos). Returns null if the file is not a readable image.
    static java.awt.image.BufferedImage load(File f) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) {
                return null;
            }
            int max = 1600;
            int w = img.getWidth();
            int h = img.getHeight();
            if (Math.max(w, h) > max) {
                double scale = (double) max / Math.max(w, h);
                int nw = Math.max(1, (int) Math.round(w * scale));
                int nh = Math.max(1, (int) Math.round(h * scale));
                java.awt.image.BufferedImage small =
                    new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = small.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(img, 0, 0, nw, nh, null);
                g2.dispose();
                img = small;
            }
            return img;
        } catch (java.io.IOException | RuntimeException ex) {
            return null;
        }
    }
     // Draws the picture cropped to fill the tile, with a dark overlay and "+N" on top when the post has more photos than the grid can show
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();
        g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 16, 16));
        double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        if (more > 0) {
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("YU Gothic UI Light", Font.BOLD, 28));
            String text = "+" + more;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}

// One photo in the viewer: the whole picture, fitted inside the available space
class PhotoSlide extends JComponent {
    private final java.awt.image.BufferedImage image;

    PhotoSlide(java.awt.image.BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();
        double scale = Math.min((double) w / image.getWidth(), (double) h / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        g2.dispose();
    }
}

// ===== Gradient background =====

// Every GradientPanel uses this renderer, so the same background appears throughout the app.
final class GradientBackground {
    private GradientBackground() { }

    // Deep blue/navy base with soft purple and magenta light blooms.
    static final Color BASE   = new Color(0x9BAFD9);
    static final Color WHITE = new Color(0x9BAFD9);
    static final Color BLUE   = new Color(0x103783);

    static final Color GLASS      = new Color(0x9BAFD9);
    static final Color GLASS_LINE = new Color(0x9BAFD9);

    static final Color TEXT      = new Color(0xF7, 0xF2, 0xFF);
    static final Color TEXT_SOFT = new Color(0xDC, 0xD0, 0xF5);

    // Smooth blend: a long diagonal sweep of deep navy -> royal blue -> violet -> navy, with large
    // Gaussian-faded colour pools on top. There are no hard-edged shapes, so nothing shows a seam.
    static java.awt.image.BufferedImage render(int w, int h) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float[] stops = {0.00f, 0.25f, 0.50f, 0.75f, 1.00f};
        Color[] colors = {
            new Color(0x2A, 0x1F, 0x8C),
            new Color(0x0B, 0x33, 0x96),
            new Color(0x3C, 0x1E, 0x98),
            new Color(0x08, 0x27, 0x78),
            new Color(0x00, 0x12, 0x3C)
        };
        g2.setPaint(new LinearGradientPaint(0, 0, w * 0.60f, Math.max(1, h), stops, colors));
        g2.fillRect(0, 0, w, h);

        // Very wide, very soft colour pools (strength is the centre opacity).
        blob(g2, w, h, 0.05f, 0.02f, 0.95f, new Color(0x103783), 0.55f);
        blob(g2, w, h, 0.88f, 0.08f, 0.80f, new Color(0x9BAFD9), 0.45f);
        blob(g2, w, h, 0.04f, 0.62f, 0.80f, new Color(0x9BAFD9), 0.45f);
        blob(g2, w, h, 0.28f, 1.00f, 0.80f, new Color(0x9BAFD9), 0.80f);
        blob(g2, w, h, 0.62f, 0.30f, 0.70f, new Color(0x103783), 0.22f);
        blob(g2, w, h, 1.00f, 0.95f, 0.75f, new Color(0x103783), 0.65f);

        g2.dispose();
        dither(img);
        return img;
    }

    // Adds +/-1 of noise so the long, gentle fades do not show 8-bit colour bands.
    private static void dither(java.awt.image.BufferedImage img) {
        java.util.Random rnd = new java.util.Random(7);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int n = rnd.nextInt(3) - 1;
                int r = Math.max(0, Math.min(255, ((rgb >> 16) & 0xFF) + n));
                int g = Math.max(0, Math.min(255, ((rgb >> 8) & 0xFF) + n));
                int b = Math.max(0, Math.min(255, (rgb & 0xFF) + n));
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
    }

    // Radial pool whose opacity follows a Gaussian curve and reaches exactly 0 at the edge.
    private static void blob(Graphics2D g2, int w, int h, float cx, float cy,
                             float radius, Color c, float strength) {
        float r = Math.max(1f, Math.max(w, h) * radius);
        final int n = 14;
        final double k = 3.2;
        double edge = Math.exp(-k);
        float[] stops = new float[n];
        Color[] colors = new Color[n];
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            double a = strength * (Math.exp(-k * t * t) - edge) / (1.0 - edge);
            stops[i] = (float) t;
            colors[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(),
                Math.max(0, Math.min(255, (int) Math.round(255 * a))));
        }
        g2.setPaint(new RadialGradientPaint(w * cx, h * cy, r, stops, colors));
        g2.fillRect(0, 0, w, h);
    }

    // Lets the gradient behind a scroll pane show through it.
    static void makeTransparent(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    }
}

// Panel that paints the gradient as its background (drawn small and scaled up, so resizing stays fast).
class GradientPanel extends JPanel {
    private static final int SCALE = 4;
    private java.awt.image.BufferedImage cache;

    GradientPanel() {
        super();
        setOpaque(false);
    }

    GradientPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int sw = Math.max(1, w / SCALE);
        int sh = Math.max(1, h / SCALE);
        if (cache == null || cache.getWidth() != sw || cache.getHeight() != sh) {
            cache = GradientBackground.render(sw, sh);
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(cache, 0, 0, w, h, null);
        g2.dispose();
    }
}

// Small translucent pill (amenity tags on a post)
// g = graphics, g2 = graphics 2d
class GlassChip extends JLabel {
    GlassChip(String text) {
        super(text, SwingConstants.CENTER);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}

// Translucent rounded button look, used for every button that sits on the gradient
class GlassButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
    private static final int ARC = 14;

    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        ButtonModel model = button.getModel();
        Graphics2D g2 = OneUi.smooth(graphics);

        int width = component.getWidth();
        int height = component.getHeight();
        boolean enabled = button.isEnabled();

        int alpha = !enabled ? 14 : model.isPressed() ? 74
                : model.isRollover() ? 54 : 34;

        g2.setColor(new Color(255, 255, 255, alpha));
        g2.fillRoundRect(0, 0, width, height, ARC, ARC);

        g2.setColor(new Color(255, 255, 255, enabled ? 75 : 35));
        g2.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);

        String text = button.getText();

        if (text != null && !text.isEmpty()) {
            FontMetrics metrics = g2.getFontMetrics(button.getFont());

            g2.setFont(button.getFont());
            g2.setColor(enabled ? button.getForeground()
                    : new Color(255, 255, 255, 120));

            g2.drawString(text,
                    (width - metrics.stringWidth(text)) / 2,
                    (height - metrics.getHeight()) / 2 + metrics.getAscent());
        }

        g2.dispose();
    }
}


// c = component, p = parent
final class GlassButtons {
    private GlassButtons() { }

    static boolean isOnGradient(Component c) {
        for (Component p = c.getParent(); p != null; p = p.getParent()) {
            if (p instanceof GradientPanel
                    || (p instanceof RoundedDialog.RoundedRoot && ((RoundedDialog.RoundedRoot) p).themed)) {
                return true;
            }
        }
        return false;
    }

    // Appy Button
    static void apply(JButton applyButton) {
        if (applyButton.getUI() instanceof GlassButtonUI) {
            return;
        }
        if (!applyButton.isBorderPainted() && !applyButton.isContentAreaFilled()) {
            return;   // text-only buttons (like the three dots) stay as they are
        }
        applyButton.setUI(new GlassButtonUI());
        applyButton.setOpaque(false);
        applyButton.setContentAreaFilled(false);
        applyButton.setBorder(new EmptyBorder(0, 12, 0, 12));
        applyButton.setFocusPainted(false);
        applyButton.setRolloverEnabled(true);
        applyButton.setForeground(Color.BLACK);
        applyButton.revalidate();
        applyButton.repaint();
    }
}