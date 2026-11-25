import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Dorm;
import service.DormService;

// Reusable Facebook-style dorm post (used in the tenant Browse feed and the landlord My Dorm feed).
// actionComponent is optional: pass an "Apply" button for tenants, or null for the landlord.
class DormPostCard extends RoundedPanel {

  private java.util.function.Consumer<Boolean> interestListener;

  /** Builds the post from a real Dorm. actionComponent is the tenant's Apply button, or the landlord's Delete. */
  public DormPostCard(
    Dorm dorm,
    JComponent actionComponent,
    boolean alreadyInterested
  ) {
    this(dorm, actionComponent, alreadyInterested, actionComponent != null);
  }

  /** canMarkInterest is false on the landlord's own posts: the count shows, the house does not click. */
  public DormPostCard(
    Dorm dorm,
    JComponent actionComponent,
    boolean alreadyInterested,
    boolean canMarkInterest
  ) {
    this(
      dorm.getOwner().getName(),
      DormService.dateText(dorm.getDatePosted()),
      dorm.getDormName(),
      dorm.getLocation(),
      dorm.getPlusCode().getCode(),
      dorm.getDescription(),
      dorm.getAmenities().toArray(new String[0]),
      dorm.getRules(),
      DormService.money(dorm.getRate()),
      DormService.slotsText(dorm),
      DormService.nearbyName(dorm),
      DormService.nearbyType(dorm),
      dorm.getOwner().getContactNum(),
      DormService.facebook(dorm.getOwner()),
      dorm.getInterestedCount(),
      actionComponent,
      alreadyInterested,
      canMarkInterest
    );
    java.util.List<File> files = new java.util.ArrayList<>();
    for (String path : dorm.getPhotos()) {
      files.add(new File(path));
    }
    if (!files.isEmpty()) {
      setPhotos(files);
    }
  }

  /** Called with true/false when a tenant toggles the interested house icon. */
  public void setInterestListener(
    java.util.function.Consumer<Boolean> listener
  ) {
    this.interestListener = listener;
  }

  public DormPostCard(
    String posterName,
    String postDate,
    String dormName,
    String location,
    String fullPlusCode,
    String description,
    String[] amenities,
    String dormRules,
    String monthly,
    String availableSlot,
    String nearbyName,
    String nearbyType,
    String contactNumber,
    String facebookAccount,
    int initialLikes,
    JComponent actionComponent,
    boolean alreadyInterested,
    boolean canMarkInterest
  ) {
    setLayout(new BorderLayout(0, 0));
    setBackground(GradientBackground.GLASS);
    setOutline(GradientBackground.GLASS_LINE);
    setBorder(new EmptyBorder(15, 18, 12, 18));
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setMaximumSize(new Dimension(492, Integer.MAX_VALUE));

    // ----- Header: avatar + poster name + date -----
    JPanel header = new JPanel(new BorderLayout(21, 0));
    header.setOpaque(false);
    header.add(
      new AvatarCircle(posterName.substring(0, 1), 64),
      BorderLayout.WEST
    );

    JPanel nameBlock = new JPanel();
    nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
    nameBlock.setOpaque(false);
    nameBlock.setBorder(new EmptyBorder(12, 0, 0, 0));

    JLabel nameLabel = new JLabel(posterName);
    nameLabel.setFont(new Font("YU Gothic UI Light", Font.BOLD, 16));
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

    seeMoreLink.addMouseListener(
      new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          userExpanded = true;
          detailsPanel.setVisible(true);
          seeMoreLink.setVisible(false);
          relayoutWindow();
        }
      }
    );
    seeLessLink.addMouseListener(
      new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          userExpanded = false;
          detailsPanel.setVisible(false);
          seeMoreLink.setVisible(true);
          relayoutWindow();
        }
      }
    );
    body.add(seeMoreLink);
    body.add(detailsPanel);

    detailsPanel.add(createDetailField("Full Plus Code : " + fullPlusCode));
    detailsPanel.add(createDetailField("Description : " + description));

    JPanel amenitiesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    amenitiesRow.setOpaque(false);
    amenitiesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel amenitiesLabel = new JLabel("Amenities :");
    amenitiesLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 15));
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
    detailsPanel.add(
      createDetailField("Facebook Account : " + facebookAccount)
    );
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
    photoSlot.setMaximumSize(
      new Dimension(Integer.MAX_VALUE, NARROW_PHOTOS.height)
    );
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

    // Only the tenant's feed marks interest; the landlord sees the count only.
    final int[] likeCount = { initialLikes };
    final boolean[] liked = { alreadyInterested };
    HouseIcon thumb = new HouseIcon();
    thumb.setInteractive(canMarkInterest);
    thumb.setInterested(alreadyInterested);
    JLabel likeCountLabel = new JLabel(String.valueOf(likeCount[0]));
    likeCountLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
    likeCountLabel.setForeground(Color.BLACK);
    if (canMarkInterest) {
      thumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      thumb.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            liked[0] = !liked[0];
            likeCount[0] += liked[0] ? 1 : -1;
            thumb.setInterested(liked[0]);
            likeCountLabel.setText(String.valueOf(likeCount[0]));
            if (interestListener != null) {
              interestListener.accept(liked[0]);
            }
          }
        }
      );
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
    addComponentListener(
      new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          updatePhotoLayout();
        }
      }
    );
  }

  // Width from which the card puts text on the left and photos on the right
  private static final int WIDE_WIDTH = 820;

  private static final Dimension NARROW_PHOTOS = new Dimension(250, 250); // small layout: a square below the text
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
  private boolean userExpanded; // what the reader chose in the narrow layout

  // Photos take whatever is left beside the text (300px up to 460px wide), 5:4 ratio
  private String dormTitle = "";
  private java.util.List<java.awt.image.BufferedImage> photoImages; // null = placeholder pictures

  // Shows the dorm's real photos (any number). The grid shows the first 4, with "+N" on the last
  // one when there are more; clicking any photo opens the viewer with every photo in it.
  public void setPhotos(java.util.List<File> files) {
    java.util.List<java.awt.image.BufferedImage> loaded =
      new java.util.ArrayList<>();
    for (File f : files) {
      java.awt.image.BufferedImage img = PhotoTile.load(f);
      if (img != null) {
        loaded.add(img);
      }
    }
    photoImages = loaded.isEmpty() ? null : loaded;
    rebuildPhotoGrid();
  }

  private void rebuildPhotoGrid() {
    int total = photoImages == null ? 4 : photoImages.size();
    int shown = Math.min(4, total);
    photoGrid.removeAll();
    photoGrid.setLayout(
      total == 1
        ? new GridLayout(1, 1, 4, 4)
        : total == 2
          ? new GridLayout(1, 2, 4, 4)
          : new GridLayout(2, 2, 4, 4)
    );
    for (int i = 0; i < shown; i++) {
      final int start = i;
      JComponent tile =
        photoImages == null
          ? new ImagePlaceholder()
          : new PhotoTile(
              photoImages.get(i),
              (i == shown - 1 && total > shown) ? total - shown : 0
            );
      tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      tile.setToolTipText("Click to view bigger");
      tile.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            openPhotoViewer(start);
          }
        }
      );
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
    PhotoViewerDialog.show(
      SwingUtilities.getWindowAncestor(this),
      dormTitle,
      slides,
      start
    );
  }

  // Full screen: the photos are a perfect square, top-aligned with "Dorm Name". The side follows the
  // height of the text beside it (300 to 460px) and never takes room the text column needs.
  private void sizeWidePhotos() {
    int textHeight = Math.max(250, body.getPreferredSize().height - 4); // minus the 4px top padding
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
        sizeWidePhotos(); // keep the photos in step while the window is resized or the text grows
      }
      return;
    }
    wideMode = wide;
    if (wide) {
      // Full screen: text on the left, photos on the right, both starting at the same top line
      photoSlot.remove(photoGrid);
      photoGap.setVisible(false);
      detailsPanel.setVisible(true); // show all the details so the text fills the space beside the photos
      seeMoreLink.setVisible(false);
      seeLessLink.setVisible(false);
      photoWrap.add(photoGrid, BorderLayout.NORTH); // NORTH keeps it at its own square size
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
    return new CollapsibleText(
      text,
      new Font("YU Gothic UI Light", Font.PLAIN, 15),
      350,
      2
    );
  }

  // Lines inside the hidden section: shown in full once "See more" is clicked
  private JComponent createDetailField(String text) {
    return new CollapsibleText(
      text,
      new Font("YU Gothic UI Light", Font.PLAIN, 15),
      350,
      Integer.MAX_VALUE
    );
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
