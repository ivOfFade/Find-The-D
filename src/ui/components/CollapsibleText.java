package ui.components;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// see more/less
// One field of a post, e.g. "Location : Sampaloc, Manila". The name before the colon is drawn in bold.
public class CollapsibleText extends JComponent {

  private final int maxCollapsedLines;
  private static final int PAD_TOP = 3; // gap between two fields is PAD_TOP + PAD_BOTTOM (half of the old 10)
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
      addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            toggle();
          }
        }
      );
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
      label = text; // a heading without a value, e.g. "Nearby Establishment"
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
  private java.util.List<java.util.List<Word>> wrapWords(
    java.util.List<Word> words,
    int width
  ) {
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
    int room =
      wrapWidth - plainFm.stringWidth(ELLIPSIS) - boldFm.stringWidth(SEE_MORE);
    java.util.List<Word> last = new java.util.ArrayList<>(
      allLines.get(maxCollapsedLines - 1)
    );
    while (!last.isEmpty() && lineWidth(last) > room) {
      Word w = last.get(last.size() - 1);
      if (w.text.length() <= 1) {
        last.remove(last.size() - 1);
      } else {
        last.set(
          last.size() - 1,
          new Word(w.text.substring(0, w.text.length() - 1), w.bold)
        );
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
      lineCount = allLines.size() + 1; // + "See less" line
    } else {
      lineCount = maxCollapsedLines;
    }
    return new Dimension(
      PAD_LEFT + wrapWidth,
      PAD_TOP + PAD_BOTTOM + lineCount * plainFm.getHeight()
    );
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
    g2.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    );
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
