import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import exception.DormAppException;
import exception.ServiceOperationException;
import model.Announcement;
import model.Application;
import model.Dorm;
import model.DormDetails;
import model.Landlord;
import model.Tenant;
import service.DormService;

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
    private final Landlord landlord = DormService.currentLandlord();
    private JPanel myFeedPanel;
    private JPanel notificationCard;

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
        notificationCard = createNotificationCardView();
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
                refreshNotification();
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
        refreshOverview();
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
        List<Dorm> dorms = DormService.myDorms(landlord);
        totalDormsValue.setText(DormService.twoDigits(dorms.size()));
        totalTenantsValue.setText(DormService.twoDigits(DormService.totalTenants(landlord)));
        complaintsValue.setText(DormService.twoDigits(DormService.totalComplaints(landlord)));

        dormRowsPanel.removeAll();
        for (Dorm dorm : dorms) {
            dormRowsPanel.add(createDormRow(dorm));
            dormRowsPanel.add(Box.createVerticalStrut(4));
        }
        dormRowsPanel.revalidate();
        dormRowsPanel.repaint();
    }

    // One row of the Dorm List: name | availability | monthly rate | interested | Manage
    private JPanel createDormRow(Dorm dorm) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(100, 40));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // rows inside ng tenant list
        JLabel nameCell = createTableCell(dorm.getDormName(), SwingConstants.LEFT);
        JLabel availabilityCell = createTableCell(
            DormService.twoDigits(dorm.getAvailableSlots()) + "/" + DormService.twoDigits(dorm.getTotalSlots()), SwingConstants.CENTER);
        JLabel rateCell = createTableCell(String.format("Php %.2f", dorm.getRate()), SwingConstants.CENTER);
        JLabel interestedCell = createTableCell(
            DormService.twoDigits(dorm.getInterestedCount()), SwingConstants.CENTER);
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

    // Rebuilds the landlord's own posts from the real dorms; also called after posting a new dorm
    private void populateMyFeed() {
        myFeedPanel.removeAll();
        boolean first = true;
        for (Dorm dorm : DormService.myDorms(landlord)) {
            if (!first) {
                myFeedPanel.add(Box.createVerticalStrut(14));
            }
            DormPostCard card = new DormPostCard(dorm, deleteButton(dorm), false, false);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            myFeedPanel.add(card);
            first = false;
        }
        if (first) {
            JLabel empty = new JLabel("You have not posted a dorm yet.");
            empty.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
            empty.setForeground(GradientBackground.TEXT_SOFT);
            empty.setBorder(new EmptyBorder(10, 4, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            myFeedPanel.add(empty);
        }
        myFeedPanel.revalidate();
        myFeedPanel.repaint();
    }

    // Delete control for one of the landlord's own posts
    private JButton deleteButton(Dorm dorm) {
        JButton delete = new JButton("Delete");
        delete.setPreferredSize(new Dimension(104, 26));
        delete.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        delete.setBackground(new Color(0xD6, 0xD6, 0xD6));
        delete.setForeground(Color.BLACK);
        delete.setOpaque(true);
        delete.setFocusPainted(false);
        delete.setBorder(new LineBorder(new Color(0x55, 0x55, 0x55), 1));
        delete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delete.addActionListener(e -> confirmDeleteDorm(dorm));
        return delete;
    }

    private void confirmDeleteDorm(Dorm dorm) {
        showActionConfirmationDialog(null, "Delete Dorm", () -> {
            try {
                DormService.removeDorm(landlord, dorm);
            } catch (DormAppException ex) {
                RoundedDialog.showMessage(this, "Message", ex.getMessage());
                return;
            }
            refreshOverview();
            populateMyFeed();
            refreshNotification();
        });
    }


    private JPanel createMyDormCardView() {
        JPanel container = new GradientPanel(new GridBagLayout());
        container.setBackground(FindTheDUI.LIGHT_GRAY_BG);
        container.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        // Feed of the landlord's own dorm posts
        myFeedPanel = new JPanel();
        myFeedPanel.setLayout(new BoxLayout(myFeedPanel, BoxLayout.Y_AXIS));
        myFeedPanel.setOpaque(false);
        populateMyFeed();

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
        approveTenantBtn.setForeground(Color.WHITE);
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
        postNewDormBtn.setForeground(Color.WHITE);
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
    private void showApproveTenantDialog(Dorm onlyDorm) {
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
        titleLbl.setForeground(Color.WHITE);
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
    private void populateManageTenants(JDialog dialog, JPanel body, Dorm onlyDorm, Runnable refresh) {
        body.removeAll();

        final boolean manage = onlyDorm != null;

        // ---- Part 1: APPROVE/DECLINE ----
        if (manage) {
            addManageSectionHeader(body, "Approve/Decline");
        }
        boolean hasApplicants = false;
        for (Dorm dorm : DormService.myDorms(landlord)) {
            if ((onlyDorm != null && dorm.getDormId() != onlyDorm.getDormId()) || DormService.pending(dorm).isEmpty()) {
                continue;
            }
            hasApplicants = true;
            JPanel group = new JPanel();
            group.setOpaque(false);
            group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
            group.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!manage) {
                approveTenantPanel(group, dorm.getDormName());
            }
            for (Application applicant : DormService.pending(dorm)) {
                acceptTenant(dialog, group, refresh, dorm, applicant);
            }
            body.add(group);
        }
        if (!hasApplicants) {
            addManageEmptyLabel(body, onlyDorm != null
                ? "No pending applications for " + onlyDorm.getDormName() + "."
                : "No pending applications.");
        }

        // ---- Part 2: TENANT LIST (Manage dialog only) ----
        if (manage) {
            body.add(Box.createVerticalStrut(10));
            addManageSectionHeader(body, "Tenant List");
            boolean hasTenants = false;
            for (Dorm dorm : DormService.myDorms(landlord)) {
                if (dorm.getDormId() != onlyDorm.getDormId() || !dorm.hasTenants()) {
                    continue;
                }
                hasTenants = true;
                JPanel group = new JPanel();
                group.setOpaque(false);
                group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
                group.setAlignmentX(Component.LEFT_ALIGNMENT);
                for (Tenant tenant : dorm.getTenants()) {
                    removeCurrentTenant(dialog, group, refresh, dorm, tenant);
                }
                body.add(group);
            }
            if (!hasTenants) {
                addManageEmptyLabel(body, "No current tenants in " + onlyDorm.getDormName() + ".");
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
        lbl.setForeground(Color.WHITE);
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

    private void approveTenantPanel(JPanel container, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lbl);
        container.add(Box.createVerticalStrut(5));

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.WHITE);
        separator.setBackground(Color.WHITE);
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(separator);
        container.add(Box.createVerticalStrut(5));
    }

    // Pending applicant: name + Approve / Decline
    private void acceptTenant(JDialog parentDialog, JPanel group, Runnable refresh, Dorm dorm, Application application) {
        final String name = application.getTenant().getName();
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
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
            if (!dorm.hasAvailableSlots()) {
                RoundedDialog.showMessage(parentDialog, "Message", dorm.getDormName() + " is already full.");
                return;
            }
            showActionConfirmationDialog(parentDialog, "Approve/Decline", () -> {
                try {
                    DormService.approve(application);   // tenant takes a slot, Total Tenants goes up
                } catch (DormAppException ex) {
                    RoundedDialog.showMessage(parentDialog, "Message", ex.getMessage());
                }
                refresh.run();
            });
        });
        decline.addActionListener(e -> showActionConfirmationDialog(parentDialog, "Approve/Decline", () -> {
            DormService.decline(application);
            refresh.run();
        }));

        btnRow.add(approve);
        btnRow.add(decline);
        row.add(btnRow, BorderLayout.EAST);

        group.add(row);
        group.add(Box.createVerticalStrut(12));
    }

    // Current tenant: name + Remove
    private void removeCurrentTenant(JDialog parentDialog, JPanel group, Runnable refresh, Dorm dorm, Tenant tenant) {
        final String name = tenant.getName();
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tenant names once you click manage
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        nameLbl.setForeground(Color.BLACK);
        row.add(nameLbl, BorderLayout.WEST);

        // Remove button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton remove = new JButton("Remove");
        remove.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        styleRowButton(remove);
        remove.addActionListener(e -> showActionConfirmationDialog(parentDialog, "Remove Tenant", () -> {
            DormService.removeTenant(dorm, tenant);     // frees the slot, Total Tenants goes down
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

        // title ng pop ups
        JLabel title = new JLabel("  " + dialogTitle);
        title.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);
        
        JLabel msg = new JLabel("Are you sure?");
        msg.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 44));
        msg.setForeground(Color.WHITE);
        body.add(msg, gbc);

        gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(0, 0, 15, 25);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton yes = new JButton("Yes");
        yes.setPreferredSize(new Dimension(75, 34));
        yes.setBackground(Color.WHITE);
        yes.setForeground(Color.WHITE);
        yes.setBorder(new EmptyBorder(0,0,0,0));
        yes.setFocusPainted(false);

        JButton no = new JButton("No");
        no.setPreferredSize(new Dimension(75, 34));
        no.setBackground(Color.WHITE);
        no.setForeground(Color.WHITE);
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
        for (String amenity : new String[]{"Wi-Fi", "Laundry", "Kitchen", "Parking", "Security", "Air Conditioning", "Study Area", "Water Supply"}) {
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
            if (plusCodeField.getText().isBlank()) {
                RoundedDialog.showMessage(dialog, "Message", "Full Plus Code is required.");
                return;
            }
            try {
                List<String> amenities = new ArrayList<>();
                for (JCheckBox cb : amenityChecks) {
                    if (cb.isSelected()) {
                        amenities.add(cb.getText());
                    }
                }
                List<String> photos = new ArrayList<>();
                for (PhotoThumb t : photoThumbs) {
                    if (t.hasPhoto()) {
                        photos.add(t.getFile().getPath());
                    }
                }
                DormDetails details = new DormDetails(
                    nameField.getText(),
                    locationField.getText(),
                    plusCodeField.getText(),
                    descriptionField.getText(),
                    DormService.parseWhole(slotField.getText(), "Available Slot"),
                    DormService.parseMoney(rateField.getText()),
                    amenities,
                    rulesField.getText(),
                    photos
                );
                DormService.postDorm(landlord, details);
            } catch (RuntimeException ex) {
                RoundedDialog.showMessage(dialog, "Message", ex.getMessage());
                return;
            } catch (Exception ex) {
                RoundedDialog.showMessage(dialog, "Message",
                    ex.getMessage() == null ? "Could not post dorm." : ex.getMessage());
                return;
            }
            dialog.dispose();
            refreshOverview();
            populateMyFeed();
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
        label.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 15));
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
        mainPanel.add(createAnnouncementLabel("Dorm"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0);
        DropdownField dormCombo = new DropdownField("Select a Dorm...", 0, 38);
        for (Dorm dorm : DormService.myDorms(landlord)) {
            dormCombo.addChoice(dorm.getDormName());
        }
        mainPanel.add(dormCombo, gbc);

        // Message
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
        mainPanel.add(createAnnouncementLabel("Message"), gbc);

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
            try {
                Dorm target = null;
                for (Dorm d : DormService.myDorms(landlord)) {
                    if (d.getDormName().equals(dormCombo.getValue())) {
                        target = d;
                    }
                }
                if (target == null) {
                    throw new ServiceOperationException("Please select a dorm.", null);
                }
                DormService.publishAnnouncement(landlord, target, msgArea.getText());
            } catch (RuntimeException ex) {
                RoundedDialog.showMessage(dialog, "Message", ex.getMessage());
                return;
            }
            dialog.dispose();
            refreshNotification();
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
        statusMsgLabel.setForeground(Color.WHITE);
        contentPanel.add(statusMsgLabel, BorderLayout.CENTER);

        JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionWrapper.setOpaque(false);
        
        // Close button sa pop up after you post an announcement
        JButton closeNotificationBtn = new JButton("Close");
        closeNotificationBtn.setPreferredSize(new Dimension(90, 35));
        closeNotificationBtn.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        closeNotificationBtn.setBackground(new Color(225, 225, 225));
        closeNotificationBtn.setForeground(Color.WHITE);
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
    private static final Color notifTitleColor      = Color.WHITE;
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
        JPanel complaintsCardsPanel = new JPanel(filterLayout);
        complaintsCardsPanel.setOpaque(false);

        // Complaints section
        // Header: title on the left, dorm filter dropdown in the upper right corner
        JPanel compHeaderPanel = new JPanel(new BorderLayout());
        compHeaderPanel.setOpaque(false);
        JLabel compTitle = new JLabel("Complaints");
        compTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        compTitle.setForeground(notifTitleColor);
        compHeaderPanel.add(compTitle, BorderLayout.WEST);

        java.util.List<Dorm> myDorms = DormService.myDorms(landlord);
        // Dorm selectedDorm = null;
        String[] filterDorms = new String[myDorms.size()];
        String[] filterCards = new String[myDorms.size()];
        for (int i = 0; i < myDorms.size(); i++) {
            Dorm dorm = myDorms.get(i);
            filterDorms[i] = dorm.getDormName();
            filterCards[i] = "D" + i;
            JPanel content = new JPanel(new GridBagLayout());
            content.setOpaque(false);
            GridBagConstraints cg = new GridBagConstraints();
            cg.gridx = 0; cg.weightx = 1.0;
            cg.fill = GridBagConstraints.HORIZONTAL; cg.anchor = GridBagConstraints.NORTHWEST;
            cg.insets = new Insets(10, 0, 0, 0);
            java.util.List<Map<String, String>> list = DormService.complaints(landlord, dorm);
            if (list.isEmpty()) {
                JLabel none = new JLabel("No complaints for " + dorm.getDormName() + ".");
                none.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 13));
                none.setForeground(Color.BLACK);
                cg.gridy = 0;
                content.add(none, cg);
            }
            int row = 0;
            for (int c = 0; c < list.size(); c++) {      // already newest first
                Map<String, String> cp = list.get(c);
                LocalDateTime postedAt = LocalDateTime.parse(cp.get("datePosted"));
                int complaintId = Integer.parseInt(cp.get("complaintId"));
                cg.gridy = row++;
                content.add(new MessageBubble(dorm.getDormName() + " - Anonymous",
                    DormService.dateText(postedAt), DormService.timeText(postedAt),
                    false, cp.get("message"),
                    () -> {
                        DormService.removeComplaint(landlord, complaintId);
                        refreshNotification();
                    }), cg);
            }
            JScrollPane complaintsScroll = new JScrollPane(content);
            ModernScrollBarUI.apply(complaintsScroll);
            complaintsScroll.setBorder(null);
            complaintsScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );
            GradientBackground.makeTransparent(complaintsScroll);
            complaintsScroll.setPreferredSize(new Dimension(0, 0));
            complaintsScroll.setMinimumSize(new Dimension(0, 0));
            complaintsScroll.getVerticalScrollBar().setUnitIncrement(16);

            complaintsCardsPanel.add(complaintsScroll, filterCards[i]);
            // selectedDorm = dorm;
        }
        CompactDropdown dormFilter = new CompactDropdown(filterDorms, 170, 28,
            NOTIF_DROPDOWN_BG, NOTIF_DROPDOWN_BORDER, NOTIF_DROPDOWN_TEXT, NOTIF_DROPDOWN_HOVER,
            index -> filterLayout.show(complaintsCardsPanel, filterCards[index]));
        compHeaderPanel.add(dormFilter, BorderLayout.EAST);

        complaintsOuterBox.add(compHeaderPanel, BorderLayout.NORTH);

        complaintsOuterBox.add(complaintsCardsPanel, BorderLayout.CENTER);
        container.add(complaintsOuterBox, gbc);

        // Announcement Container
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        RoundedPanel announcementOuterBox = new RoundedPanel(new BorderLayout());
        announcementOuterBox.setBackground(NOTIF_BOX_BG);
        announcementOuterBox.setOutline(NOTIF_BOX_OUTLINE);
        announcementOuterBox.setBorder(new EmptyBorder(13, 16, 13, 16));

        // title "Announcement"
        JPanel annHeaderPanel = new JPanel(new BorderLayout());
        annHeaderPanel.setOpaque(false);
        JLabel annTitle = new JLabel("Announcement");
        annTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        annTitle.setForeground(notifTitleColor);
        annHeaderPanel.add(annTitle, BorderLayout.WEST);

        // Post Announcement button
        JButton postAnnouncementButton = new JButton("Post Announcement");
        postAnnouncementButton.setPreferredSize(new Dimension(160, 32));
        postAnnouncementButton.setBackground(NOTIF_BUTTON_BG);
        postAnnouncementButton.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
        postAnnouncementButton.setForeground(Color.WHITE);
        postAnnouncementButton.setBorder(new LineBorder(NOTIF_BUTTON_BORDER));
        postAnnouncementButton.setFocusPainted(false);
        postAnnouncementButton.addActionListener(e -> showPostAnnouncementDialog());
        annHeaderPanel.add(postAnnouncementButton, BorderLayout.EAST);

        announcementOuterBox.add(annHeaderPanel, BorderLayout.NORTH);
        
        JPanel annBodyPanel = new JPanel(new GridBagLayout());
        annBodyPanel.setOpaque(false);
        GridBagConstraints annBodyGbc = new GridBagConstraints();
        annBodyGbc.gridx = 0; annBodyGbc.weightx = 1.0;
        annBodyGbc.fill = GridBagConstraints.HORIZONTAL; annBodyGbc.anchor = GridBagConstraints.NORTHWEST;
        
        java.util.List<Announcement> posted = DormService.landlordAnnouncements(landlord);
        for (int n = 0; n < posted.size(); n++) {
            Announcement an = posted.get(n);
            annBodyGbc.gridy = n;
            annBodyGbc.insets = new Insets(n == 0 ? 10 : 0, 0, n == posted.size() - 1 ? 0 : 10, 0);
            String dormName = an.getDorm().getDormName();
            Runnable remove = () -> {
                DormService.removeAnnouncement(landlord, an);
                refreshNotification();
            };

            MessageBubble bubble = DormService.isToday(an.getDatePosted())
                ? new MessageBubble(dormName, "", DormService.timeText(an.getDatePosted()), true, an.getMessage(), remove)
                : new MessageBubble(dormName, DormService.stamp(an.getDatePosted()), "", false, an.getMessage(), remove);
            annBodyPanel.add(bubble, annBodyGbc);
        }
        JScrollPane announcementsScroll = new JScrollPane(annBodyPanel);
            ModernScrollBarUI.apply(announcementsScroll);
            announcementsScroll.setBorder(null);
            announcementsScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );
            GradientBackground.makeTransparent(announcementsScroll);
            announcementsScroll.setPreferredSize(new Dimension(0, 0));
            announcementsScroll.setMinimumSize(new Dimension(0, 0));
            announcementsScroll.getVerticalScrollBar().setUnitIncrement(16);

            announcementOuterBox.add(announcementsScroll, BorderLayout.CENTER);

        gbc.gridy = 1;
        container.add(announcementOuterBox, gbc);

        return container;
    }

    // Rebuilds the Notification page from the real complaints and announcements
    private void refreshNotification() {
        boolean wasVisible = notificationCard.isShowing();
        cardsContainer.remove(notificationCard);
        notificationCard = createNotificationCardView();
        cardsContainer.add(notificationCard, "Notification");
        if (wasVisible) {
            cardLayout.show(cardsContainer, "Notification");
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JLabel createAnnouncementLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
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

