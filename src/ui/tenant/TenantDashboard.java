package ui.tenant;

import ui.components.*;
import exception.ServiceOperationException;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.Announcement;
import model.Application;
import model.Dorm;
import model.Tenant;
import enums.ApplicationStatus;
import service.DormService;
import ui.FindTheDUI;

// Tenant Page
public class TenantDashboard extends JPanel {
    private Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private final Tenant tenant = DormService.currentTenant();
    private java.util.Set<Integer> interestedDormIds = new java.util.HashSet<>();
    private Application currentApplication;
    private JPanel browseCard;
    private JPanel myDormCard;

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private JLabel linkMyDorm; // hidden until the tenant applies to a dorm
    private Component myDormGap; // spacing under the My Dorm link (hidden with it)

    // Browse feed width: share of the available space, and the narrowest it will
    // ever get
    private static final int FEED_WIDTH_PERCENT = 95;
    private static final int FEED_MIN_WIDTH = 492;

    public TenantDashboard() {
        setLayout(new BorderLayout());

        // Keep the tenant's single current application so every listing button
        // can reflect its state without querying the database for each card.
        loadCurrentApplication();

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
        // "My Dorm" only appears once an owner has accepted the tenant
        linkMyDorm.setVisible(tenant.hasDorm());
        myDormGap.setVisible(tenant.hasDorm());
        sidebarPanel.add(linkBrowse);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(linkLogOut);

        // ---------- Center cards ----------
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        browseCard = createBrowseCardView();
        myDormCard = createMyDormCardView();
        cardsContainer.add(browseCard, "Browse");
        cardsContainer.add(myDormCard, "MyDorm");

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

        String initialCard = tenant.hasDorm() ? "MyDorm" : "Browse";
        cardLayout.show(cardsContainer, initialCard);
        setUnderlined(linkMyDorm, tenant.hasDorm());
        setUnderlined(linkBrowse, !tenant.hasDorm());
    }

    private void loadCurrentApplication() {
        try {
            java.util.List<Application> applications = DormService.getApplications(tenant);
            currentApplication = applications.isEmpty() ? null : applications.get(0);
        } catch (ServiceOperationException ex) {
            currentApplication = null;
            RoundedDialog.showMessage(owner(), "Message", ex.getMessage());
        }
    }

    // Rebuilds Browse and My Dorm from the current data (slots, accepted
    // applications, announcements)
    private void refreshTenantViews() {
        cardsContainer.remove(browseCard);
        cardsContainer.remove(myDormCard);
        feedEntries.clear();
        browseCard = createBrowseCardView();
        myDormCard = createMyDormCardView();
        cardsContainer.add(browseCard, "Browse");
        cardsContainer.add(myDormCard, "MyDorm");
        linkMyDorm.setVisible(tenant.hasDorm());
        myDormGap.setVisible(tenant.hasDorm());
        Container sidebar = linkMyDorm.getParent();
        if (sidebar != null) {
            sidebar.revalidate();
            sidebar.repaint();
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    // ---------- Sidebar helpers ---------- // ---------- Sidebar helpers
    // ----------
    private JLabel createSidebarLink(String text, SidebarItem.Glyph glyph) {
        return new SidebarItem(text, glyph);
    }

    private void setUnderlined(JLabel label, boolean underlined) {
        if (label instanceof SidebarItem) {
            ((SidebarItem) label).setSelected(underlined);
            return;
        }
        java.util.Map<java.awt.font.TextAttribute, Object> attrs = new java.util.HashMap<java.awt.font.TextAttribute, Object>(
                label.getFont().getAttributes());
        attrs.put(java.awt.font.TextAttribute.UNDERLINE,
                underlined ? java.awt.font.TextAttribute.UNDERLINE_ON : Integer.valueOf(-1));
        label.setFont(label.getFont().deriveFont(attrs));
    }

    // ---------- Browse view (dorm feed + filters) ----------
    private JPanel createBrowseCardView() {
        interestedDormIds = DormService.getInterestedDormIds(tenant);

        JPanel browsePanel = new GradientPanel(new BorderLayout());

        // Feed: a vertical list of dorm posts inside a scroll pane
        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setOpaque(false);

        Dorm currentDorm = tenant.getCurrentDorm();
        boolean firstPost = true;
        for (Dorm dorm : DormService.listings()) {
            if (currentDorm != null && currentDorm.getDormId() == dorm.getDormId()) {
                continue;
            }
            if (!firstPost) {
                feedPanel.add(Box.createVerticalStrut(14));
            }
            feedPanel.add(createDormPostCard(dorm));
            firstPost = false;
        }
        if (firstPost) {
            JLabel empty = new JLabel("Dorm listings will appear here.");
            empty.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
            empty.setForeground(GradientBackground.TEXT);
            empty.setBorder(new EmptyBorder(10, 4, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            feedPanel.add(empty);
        }

        JPanel feedHolder = new JPanel(new BorderLayout());
        feedHolder.setOpaque(false);
        feedHolder.add(feedPanel, BorderLayout.NORTH);
        // The posts grow with the window (the full screen layout needs the room) and
        // stay centred
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
    private JPanel createDormPostCard(Dorm dorm) {
        JButton applyBtn = new JButton("Apply");
        applyBtn.setPreferredSize(new Dimension(104, 26));
        applyBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        applyBtn.setBackground(new Color(0xD6, 0xD6, 0xD6));
        applyBtn.setForeground(Color.BLACK);
        applyBtn.setOpaque(true);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorder(new LineBorder(new Color(0x55, 0x55, 0x55), 1));
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (dorm.equals(tenant.getCurrentDorm())) {
            applyBtn.setText("Current");
            applyBtn.setEnabled(false);
        } else if (isApplicationFor(dorm)) {
            applyBtn.setText("Applied");
            applyBtn.setEnabled(false);
        }
        applyBtn.addActionListener(e -> {
            try {
                boolean hasPreviousApplication = currentApplication != null;
                boolean confirmation = showApplyConfirmDialog(
                        owner(),
                        hasPreviousApplication ? "Replace Previous Application" : "Apply",
                        hasPreviousApplication
                                ? "Submitting a new application will remove your previous application. Continue?"
                                : "Submit an application for this dorm?");
                if (confirmation) {
                    boolean confirmTransfer = false;

                    if (tenant.hasDorm()
                            && tenant.getCurrentDorm().getDormId() != dorm.getDormId()) {
                        boolean choice = showApplyConfirmDialog(
                                owner(),
                                "Confirm Transfer",
                                "If accepted, you will transfer from your current dorm. Continue?");

                        if (!choice) {
                            return;

                        }

                        confirmTransfer = true;
                    }
                    applyBtn.setEnabled(false);
                    currentApplication = DormService.apply(tenant, dorm, confirmTransfer);
                    refreshTenantViews();
                }
            } catch (ServiceOperationException ex) {
                applyBtn.setEnabled(true);
                RoundedDialog.showMessage(owner(), "Message", ex.getMessage());
            }
        });

        DormPostCard postCard = new DormPostCard(dorm, applyBtn, interestedDormIds.contains(dorm.getDormId()));
        postCard.setInterestListener(on -> {
            try {
                DormService.setInterested(tenant, dorm, on);
            } catch (ServiceOperationException ex) {
                RoundedDialog.showMessage(owner(), "Message", ex.getMessage());
            }
        });
        postCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // let it stretch with the feed
        feedEntries.add(new FeedEntry(dorm, postCard));
        return postCard;
    }

    private boolean isApplicationFor(Dorm dorm) {
        return currentApplication != null
                && currentApplication.getStatus() == ApplicationStatus.PENDING
                && currentApplication.getDorm().getDormId() == dorm.getDormId();
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

        // Dorm details box
        Dorm myDorm = tenant.getCurrentDorm();
        RoundedPanel detailsBox = new RoundedPanel();
        detailsBox.setLayout(new BoxLayout(detailsBox, BoxLayout.Y_AXIS));
        detailsBox.setBackground(GradientBackground.GLASS);
        detailsBox.setOutline(GradientBackground.GLASS_LINE);
        detailsBox.setBorder(new EmptyBorder(14, 24, 14, 24));
        detailsBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (myDorm != null) {
            String[][] rows = {
                    { "Location", myDorm.getLocation() },
                    { "Monthly Rate", String.format("%,.0f", myDorm.getRate()) },
                    { "Owner", myDorm.getOwner().getName() },
                    { "Contact", myDorm.getOwner().getContactNum() }
            };
            for (String[] row : rows) {
                JLabel line = new JLabel("<html><b>" + row[0] + "</b> : " + row[1] + "</html>");
                line.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
                line.setForeground(Color.BLACK);
                line.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailsBox.add(line);
                detailsBox.add(Box.createVerticalStrut(3));
            }
        }

        // The one dorm name above the box
        JLabel dormNameLabel = new JLabel(myDorm == null ? "No dorm yet" : myDorm.getDormName());
        dormNameLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 18));
        dormNameLabel.setForeground(GradientBackground.TEXT);
        dormNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel annHeading = new JLabel("Announcements");
        annHeading.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        annHeading.setForeground(GradientBackground.TEXT);
        annHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pageStack = new JPanel();
        pageStack.setLayout(new BoxLayout(pageStack, BoxLayout.Y_AXIS));
        pageStack.setOpaque(false);
        pageStack.add(dormNameLabel);
        pageStack.add(Box.createVerticalStrut(8));
        pageStack.add(detailsBox);
        pageStack.add(Box.createVerticalStrut(14));
        pageStack.add(annHeading);
        pageStack.add(Box.createVerticalStrut(8));
        pageStack.add(outerBox);

        java.util.List<Announcement> mine = tenant.viewAnnouncements();
        for (int n = mine.size() - 1; n >= 0; n--) { // newest first
            if (n < mine.size() - 1) {
                outerBox.add(Box.createVerticalStrut(8));
            }
            outerBox.add(createAnnouncementEntry(mine.get(n)));
        }
        if (mine.isEmpty()) {
            JLabel none = new JLabel("No announcements yet.");
            none.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 14));
            none.setForeground(Color.BLACK);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            outerBox.add(none);
        }

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(20, 45, 0, 55));
        topWrapper.add(pageStack, BorderLayout.NORTH);
        page.add(topWrapper, BorderLayout.CENTER);

        // "Post Complain" button, bottom right
        JButton postComplainBtn = new JButton("Post Complain");
        postComplainBtn.setPreferredSize(new Dimension(152, 40));
        postComplainBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        postComplainBtn.setBackground(new Color(0xCC, 0xCC, 0xCC));
        postComplainBtn.setForeground(Color.WHITE);
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

    // One announcement: title above, gray text box with shadow, timestamp below on
    // the right
    private JPanel createAnnouncementEntry(Announcement announcement) {
        String timestamp = announcement.getDatePosted().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
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

        JTextArea messageArea = new JTextArea(announcement.getMessage());
        messageArea.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 13));
        messageArea.setForeground(Color.BLACK);
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
        JCheckBox anonymousBox = new JCheckBox("Post anonymously");
        anonymousBox.setOpaque(false);
        anonymousBox.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        anonymousBox.setFocusPainted(false);
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
            if (showComplaintConfirmDialog(dialog, "Post Complain", "Post this complaint?")) {
                try {
                    DormService.submitComplaint(tenant, complaintArea.getText(), anonymousBox.isSelected());
                    dialog.dispose();
                } catch (ServiceOperationException ex) {
                    showCustomWarningDialog(dialog, ex.getMessage());
                }
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
        actions.add(anonymousBox);
        content.add(actions, BorderLayout.SOUTH);

        dialog.add(content);
        RoundedDialog.apply(dialog);
        dialog.setVisible(true);
    }

    // warning for complaints if wala naaklagay and ipopost na
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
        headerTitle.setForeground(Color.WHITE);
        headerBar.add(headerTitle, BorderLayout.WEST);

        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);

        JLabel msgLabel = new JLabel("<html><div style='width: 380px;'>" + message + "</div></html>");
        msgLabel.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 22));
        msgLabel.setForeground(Color.BLACK);
        bodyPanel.add(msgLabel, gbc);

        // "OK" button
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(25, 0, 0, 25);
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

    private boolean showApplyConfirmDialog(
            Window owner, String title, String message) {
        return showComplaintConfirmDialog(owner, title, message);
    }

    private boolean showComplaintConfirmDialog(
            Window owner, String title, String message) {
        JDialog dialog = new JDialog(
                owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 190);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        final boolean[] confirmed = { false };

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(15, 40, 12, 20));

        JLabel messageLabel = new JLabel(
                "<html>" + message + "</html>",
                SwingConstants.LEFT);
        messageLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 20));
        messageLabel.setForeground(Color.WHITE);
        content.add(messageLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        JButton yesButton = new JButton("Yes");
        yesButton.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        yesButton.setForeground(Color.WHITE);
        yesButton.setPreferredSize(new Dimension(86, 36));
        yesButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        JButton noButton = new JButton("No");
        noButton.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 14));
        noButton.setForeground(Color.WHITE);
        noButton.setPreferredSize(new Dimension(86, 36));
        noButton.addActionListener(e -> dialog.dispose());

        buttons.add(yesButton);
        buttons.add(noButton);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.add(content);
        RoundedDialog.apply(dialog);

        JPanel header = (JPanel) ((BorderLayout) dialog.getContentPane()
                .getLayout()).getLayoutComponent(BorderLayout.NORTH);
        JLabel headerTitle = (JLabel) header.getComponent(0);

        headerTitle.setFont(new Font("Arial", Font.PLAIN, 15));
        headerTitle.setForeground(Color.WHITE);

        dialog.setVisible(true);

        return confirmed[0];

    }

    // ---------- Filters (Amenities + Budget) ----------
    private static final String[] BUDGET_LABELS = {
            "Under 3,000", "3,000 - 5,000", "5,000 - 8,000", "8,000 - 10,000", "10,000+" };
    private static final int[] BUDGET_MIN = { 0, 3000, 5000, 8000, 10000 };
    private static final int[] BUDGET_MAX = { 2999, 5000, 8000, 10000, Integer.MAX_VALUE };

    private final java.util.List<FeedEntry> feedEntries = new java.util.ArrayList<>();

    // One dorm post in the feed together with the listing the filters test
    private static class FeedEntry {
        final Dorm dorm;
        final JPanel card;

        FeedEntry(Dorm dorm, JPanel card) {
            this.dorm = dorm;
            this.card = card;
        }
    }

    // What the two dropdowns currently ask for, turned into the dorm filter the
    // domain expects
    private boolean matchesFilters(Dorm dorm, java.util.List<String> wanted, int budget) {
        return dorm.getAmenities().containsAll(wanted)
                && (budget < 0
                        || (dorm.getRate() >= BUDGET_MIN[budget]
                                && dorm.getRate() <= BUDGET_MAX[budget]));
    }

    // Two slim dropdown fields (same style as the landlord's Post New Dorm form).
    // The feed updates right away.
    private JPanel createFilterPanel(JPanel feedPanel) {
        final int fieldW = 190;
        final String[] sortChoice = { "Recent posts" };
        final int[] budget = { -1 }; // -1 = any budget
        java.util.List<JCheckBox> amenityChecks = new java.util.ArrayList<>();
        java.util.List<JLabel> budgetRows = new java.util.ArrayList<>();

        DropdownField amenitiesField = new DropdownField("Amenities", fieldW);
        DropdownField budgetField = new DropdownField("Budget", fieldW);
        DropdownField sortField = new DropdownField("Sort by", fieldW);
        amenitiesField.setGlass(true);
        budgetField.setGlass(true);
        sortField.setGlass(true);

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
                budgetRows.get(i).setFont(new Font("Yu Gothic UI Light",
                        i - 1 == budget[0] && budget[0] >= 0 ? Font.BOLD : Font.PLAIN, 13));
            }
            sortField.setValue(sortChoice[0]);

            clearLink.setVisible(!picked.isEmpty() || budget[0] >= 0);
            applyFeedFilters(feedPanel, picked, budget[0], resultsLabel);
        };

        // Amenities: list of check boxes, stays open so several can be ticked
        JPopupMenu amenityPopup = amenitiesField.getPopup();
        java.util.Set<String> amenities = new java.util.TreeSet<>();
        for (FeedEntry entry : feedEntries) {
            amenities.addAll(entry.dorm.getAmenities());
        }
        for (String amenity : amenities) {
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

        JPopupMenu sortPopup = sortField.getPopup();
        String[] sortTexts = { "Recent posts", "Most popular" };
        for (String text : sortTexts) {
            OneUiItem row = new OneUiItem(text, () -> {
                sortPopup.setVisible(false);
                try {
                    java.util.List<Integer> dormIds = DormService.listingOrder("Most popular".equals(text));
                    java.util.Map<Integer, Integer> rank = new java.util.HashMap<>();
                    for (int i = 0; i < dormIds.size(); i++) {
                        rank.put(dormIds.get(i), i);
                    }
                    feedEntries.sort(java.util.Comparator.comparingInt(
                            entry -> rank.getOrDefault(entry.dorm.getDormId(), Integer.MAX_VALUE)));
                    sortChoice[0] = text;
                    refresh.run();
                } catch (ServiceOperationException ex) {
                    RoundedDialog.showMessage(owner(), "Message", ex.getMessage());
                }
            });
            sortPopup.add(row);
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
        sortField.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(amenitiesField);
        stack.add(Box.createVerticalStrut(10));
        stack.add(budgetField);
        stack.add(Box.createVerticalStrut(10));
        stack.add(sortField);
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
        // The tenant searches the listings; the cards that survive are the ones we put
        // back on screen
        int shown = 0;
        for (FeedEntry entry : feedEntries) {
            if (!matchesFilters(entry.dorm, wanted, budget)) {
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
