import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
