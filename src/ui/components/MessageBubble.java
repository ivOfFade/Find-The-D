package ui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MessageBubble extends RoundedPanel {

  // Colours: edit the hex codes here
  private static final Color BUBBLE_BG = new Color(255, 255, 255, 30);
  private static final Color TITLE_TEXT = Color.BLACK;
  private static final Color announcementDate = Color.decode("#323131");
  private static final Color DOTS = Color.decode("#C9C9DC");
  private static final Color DOTS_HOVER = Color.decode("#FFFFFF");
  private static final Color messageContentColor = Color.BLACK;
  private static final Color TIME_TEXT = Color.decode("#323131");

  private Runnable deleteAction;
  private String actionLabel = "\u2022\u2022\u2022";

  /** Same bubble, but the three-dot menu also removes the record behind it. */
  public MessageBubble(
    String title,
    String mainHeaderDate,
    String bottomTimestamp,
    boolean timeOnTop,
    String message,
    Runnable deleteAction
  ) {
    this(title, mainHeaderDate, bottomTimestamp, timeOnTop, message, deleteAction, "\u2022\u2022\u2022");
  }

  public MessageBubble(
    String title,
    String mainHeaderDate,
    String bottomTimestamp,
    boolean timeOnTop,
    String message,
    Runnable action,
    String actionLabel
  ) {
    this(title, mainHeaderDate, bottomTimestamp, timeOnTop, message);
    this.deleteAction = action;
    this.actionLabel = actionLabel;
    addActionButton();
  }

  public MessageBubble(
    String title,
    String mainHeaderDate,
    String bottomTimestamp,
    boolean timeOnTop,
    String message
  ) {
    setBackground(BUBBLE_BG);
    setBorder(new EmptyBorder(12, 16, 12, 16));
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    int verticalIndex = 0;

    // 1. Explicit Header Date Text (e.g., "11 Nov 2069" or "2d ago") on the very top left edge if provided and not timeOnTop
    if (!mainHeaderDate.isEmpty() && !timeOnTop) {
      gbc.gridy = verticalIndex++;
      gbc.insets = new Insets(0, 0, 4, 0);
      JLabel dateLbl = new JLabel(mainHeaderDate);
      dateLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 11));
      dateLbl.setForeground(announcementDate);
      add(dateLbl, gbc);
    }

    // 1b. Time on Top scenario (e.g. 11:11 pm above the box contents)
    if (timeOnTop && !bottomTimestamp.isEmpty()) {
      gbc.gridy = verticalIndex++;
      gbc.insets = new Insets(0, 0, 4, 0);
      JLabel topTimeLbl = new JLabel(bottomTimestamp);
      topTimeLbl.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 11));
      topTimeLbl.setForeground(announcementDate);
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

    // Kept as a placeholder for the action control added by the action constructor.
    JButton dotsButton = new JButton("\u2022\u2022\u2022");
    dotsButton.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 12));
    dotsButton.setForeground(DOTS);
    dotsButton.setContentAreaFilled(false);
    dotsButton.setBorderPainted(false);
    dotsButton.setFocusPainted(false);
    dotsButton.setMargin(new Insets(0, 4, 0, 4));
    dotsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    dotsButton.setToolTipText("Message options");
    dotsButton.addMouseListener(
      new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          dotsButton.setForeground(DOTS_HOVER);
        }

        @Override
        public void mouseExited(MouseEvent e) {
          dotsButton.setForeground(DOTS);
        }
      }
    );
    dotsButton.addActionListener(e -> {
      if (showDeleteConfirmationDialog("Remove")) deleteSelf();
    });
    titleRow.add(dotsButton, BorderLayout.EAST);
    add(titleRow, gbc);

    // 3. Main Text Area matching user dimensions dynamically
    gbc.gridy = verticalIndex++;
    gbc.insets = new Insets(0, 0, 4, 0);
    JTextArea messageContentArea = new JTextArea(message);
    messageContentArea.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 13));
    messageContentArea.setForeground(messageContentColor);
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

  private void addActionButton() {
    Component[] children = getComponents();
    for (Component child : children) {
      if (child instanceof JPanel) {
        for (Component rowChild : ((JPanel) child).getComponents()) {
          if (rowChild instanceof JButton) {
            JButton button = (JButton) rowChild;
            button.setText(actionLabel);
            button.setForeground(Color.WHITE);
            button.setToolTipText("Resolve".equals(actionLabel) ? "Resolve complaint" : "Remove message");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            if ("Resolve".equals(actionLabel)) {
              button.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
              button.setUI(new GlassButtonUI());
              button.setOpaque(false);
              button.setContentAreaFilled(false);
              button.setBorder(new EmptyBorder(0, 12, 0, 12));
              button.setPreferredSize(new Dimension(88, 32));
            } else {
              button.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 12));
              button.setBorderPainted(false);
              button.setBorder(new EmptyBorder(0, 4, 0, 4));
            }
            button.setContentAreaFilled(false);
            button.setRolloverEnabled(true);
            for (ActionListener listener : button.getActionListeners()) button.removeActionListener(listener);
            button.addActionListener(e -> {
              boolean resolving = "Resolve".equals(actionLabel);
              String question = resolving ? "Resolve this complaint?" : "Are you sure?";
              if (showDeleteConfirmationDialog(resolving ? "Resolve" : "Remove", question)) deleteSelf();
            });
            return;
          }
        }
      }
    }
  }

  // Removes this message bubble and, when a delete action was given, the record behind it
  private void deleteSelf() {
    if (deleteAction != null) {
      deleteAction.run();
    }
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
  private boolean showDeleteConfirmationDialog(String action) {
    return showDeleteConfirmationDialog(action, "Are you sure?");
  }

  private boolean showDeleteConfirmationDialog(String action, String question) {
    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog = new JDialog(
      owner,
      action,
      Dialog.ModalityType.APPLICATION_MODAL
    );
    dialog.setSize(480, 190);
    dialog.setResizable(false);
    dialog.setLocationRelativeTo(owner);

    final boolean[] confirmed = { false };

    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.setBackground(new Color(0xF2, 0xF2, 0xF2));
    contentPanel.setBorder(new EmptyBorder(14, 26, 14, 26));

    String safeQuestion = question.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    JLabel questionLabel = new JLabel(
      "<html><div style='width: 390px;'>" + safeQuestion + "</div></html>",
      SwingConstants.LEFT
    );
    questionLabel.setFont(new Font("Yu Gothic UI Light", Font.BOLD, 32));
    questionLabel.setForeground(Color.WHITE);
    contentPanel.add(questionLabel, BorderLayout.CENTER);

    JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    actionWrapper.setOpaque(false);

    // "Yes" - flat light gray button
    JButton yesButton = new JButton("Yes");
    yesButton.setPreferredSize(new Dimension(86, 36));
    yesButton.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
    yesButton.setBackground(new Color(0xE0, 0xE0, 0xE0));
    yesButton.setForeground(Color.WHITE);
    yesButton.setOpaque(true);
    yesButton.setFocusPainted(false);
    yesButton.setBorder(
      BorderFactory.createCompoundBorder(
        new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1),
        new EmptyBorder(0, 0, 0, 0)
      )
    );
    yesButton.addActionListener(ev -> {
      confirmed[0] = true;
      dialog.dispose();
    });

    // "No" - outlined button with a darker border and a soft drop shadow
    JButton noBtn = new JButton("No");
    noBtn.setPreferredSize(new Dimension(86, 36));
    noBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
    noBtn.setBackground(new Color(0xF2, 0xF2, 0xF2));
    noBtn.setForeground(Color.WHITE);
    noBtn.setOpaque(true);
    noBtn.setFocusPainted(false);
    noBtn.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(
          0,
          0,
          3,
          3,
          new Color(0xB0, 0xB0, 0xB0)
        ),
        new LineBorder(new Color(0x55, 0x55, 0x55), 1)
      )
    );
    noBtn.addActionListener(ev -> dialog.dispose());

    actionWrapper.add(yesButton);
    actionWrapper.add(noBtn);
    contentPanel.add(actionWrapper, BorderLayout.SOUTH);

    dialog.add(contentPanel);
    dialog.getRootPane().setDefaultButton(noBtn); // Enter = "No" (safe default)
    RoundedDialog.apply(dialog);
    dialog.setVisible(true); // blocks until closed
    return confirmed[0];
  }
}
