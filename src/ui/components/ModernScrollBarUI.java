import javax.swing.*;
import java.awt.*;

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
