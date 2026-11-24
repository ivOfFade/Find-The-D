import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

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
