import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
    setMaximumSize(
      new Dimension(width > 0 ? width : Integer.MAX_VALUE, height)
    );
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // remember when the list closed, so clicking the field while it is open just closes it
    popup.addPopupMenuListener(
      new javax.swing.event.PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeVisible(
          javax.swing.event.PopupMenuEvent e
        ) {
          chevron.setUp(true);
          repaint();
        }

        @Override
        public void popupMenuWillBecomeInvisible(
          javax.swing.event.PopupMenuEvent e
        ) {
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
      }
    );
    addMouseListener(
      new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          hovering = true;
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
          hovering = false;
          repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
          if (System.currentTimeMillis() - closedAt < 250) {
            return;
          }
          popup.showUnder(DropdownField.this);
        }
      }
    );
    setValue(null);
  }

  public String getValue() {
    return currentValue;
  }

  public JPopupMenu getPopup() {
    return popup;
  }

  // A simple pick-one choice: shows it in the field and closes the list
  public void addChoice(String text) {
    popup.add(
      new OneUiItem(text, () -> {
        setValue(text);
        popup.setVisible(false);
      })
    );
  }

  // null shows the grey placeholder, anything else shows the chosen value
  public void setValue(String value) {
    currentValue = value;
    textLabel.setText(value == null ? placeholder : value);
    if (glass) {
      textLabel.setForeground(
        value == null ? GradientBackground.TEXT_SOFT : GradientBackground.TEXT
      );
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
    g2.fillRoundRect(
      0,
      0,
      getWidth(),
      getHeight(),
      OneUi.FIELD_ARC,
      OneUi.FIELD_ARC
    );
    g2.setColor(glass ? new Color(255, 255, 255, 70) : OneUi.FIELD_LINE);
    g2.drawRoundRect(
      0,
      0,
      getWidth() - 1,
      getHeight() - 1,
      OneUi.FIELD_ARC,
      OneUi.FIELD_ARC
    );
    g2.dispose();
    super.paintComponent(g);
  }
}
