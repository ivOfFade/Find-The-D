import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import service.DormService;

public class FindTheDUI extends JFrame {

    // set LIGHT_GRAY_BG as yung hex code
    public static final Color LIGHT_GRAY_BG = Color.decode("#5b5f5d");

    // Screen names used by the main window's CardLayout
    // instead of typing multiple times, gagamitin nalang ung variables
    private static final String welcomeText = "Welcome";
    private static final String loginText = "Login";
    private static final String registerText = "Register";
    private static final String dashboardText = "Dashboard";

    private static FindTheDUI instance;

    // manages how makikita ung diff screens
    // chooses between those screens
    private final CardLayout screens = new CardLayout();
    // holds all screens
    private final JPanel root = new JPanel(screens);

    // The one and only window: every screen is a panel swapped in here
    public FindTheDUI() {
        setTitle("Find The D"); // Title sa top
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // para magclose pag pinindot yung X
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null); // para nasa center kapag nirun
        setContentPane(root); // dito madidisplay yung diff screens
        instance = this;
        showWelcome();
    }

    // Puts a fresh panel on screen under the given name, replacing the old one
    // para madisplay kung anong panel yung pinindot mo
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
    // the diff screens
    public static void showWelcome() {
        instance.display(welcomeText, new WelcomePanel());
    }

    public static void showLogin(String role) {
        instance.display(loginText, new LoginWindow(role));
    }

    public static void showRegister(String role) {
        instance.display(registerText, new RegisterWindow(role));
    }

    public static void showDashboard(String role) {
        if (role.equalsIgnoreCase("Landlord")) {
            instance.display(dashboardText, new LandlordDashboard());
        } else {
            instance.display(dashboardText, new TenantDashboard());
        }
    }

    private static ImageIcon LOGO;

    // left panel, logo side
    public static JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 10, 20, 10);
        gbcLeft.anchor = GridBagConstraints.CENTER;

        if (LOGO == null) {
            ImageIcon originalIcon = new ImageIcon(FindTheDUI.class.getResource("Logo.png"));
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
        // parent window
        Window owner = SwingUtilities.getWindowAncestor(parent);

        // new dialog, user cannot interact with others hanggat di naclose yung pop up
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

        // container na lalagyan 
        JPanel mainCanvas = new JPanel(new GridBagLayout());
        mainCanvas.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 30, 15, 30);
        JLabel messageLabel = new JLabel("Are you sure?");
        messageLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 44));
        messageLabel.setForeground(Color.WHITE);
        mainCanvas.add(messageLabel, gbc);

        // flow layout arranges horizontally
        gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; 
        gbc.insets = new Insets(0, 0, 15, 30);
        JPanel confirmationButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        confirmationButtons.setOpaque(false);

        JButton logoutYesButton = new JButton("Yes");
        logoutYesButton.setPreferredSize(new Dimension(80, 36));
        logoutYesButton.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        logoutYesButton.setBackground(Color.WHITE);
        logoutYesButton.setForeground(Color.WHITE);
        logoutYesButton.setBorder(new EmptyBorder(0,0,0,0));
        logoutYesButton.setFocusPainted(false);
        
        JButton noBtn = new JButton("No");
        noBtn.setPreferredSize(new Dimension(80, 36));
        noBtn.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        noBtn.setBackground(Color.WHITE);
        noBtn.setForeground(Color.WHITE);
        noBtn.setBorder(new EmptyBorder(0,0,0,0));
        noBtn.setFocusPainted(false);

        logoutYesButton.addActionListener(e -> {
            dialog.dispose();
            DormService.logout();
            showWelcome();
        });

        noBtn.addActionListener(e -> dialog.dispose());

        confirmationButtons.add(logoutYesButton);
        confirmationButtons.add(noBtn);
        mainCanvas.add(confirmationButtons, gbc);

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
