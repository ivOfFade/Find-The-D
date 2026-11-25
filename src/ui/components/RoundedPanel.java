package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Panel with 8px rounded corners: paints its own background (and optional outline) so
// any box-style container can use it in place of a plain JPanel
public class RoundedPanel extends JPanel {
    public static final int RADIUS = 8;
    private Color outline;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public void setOutline(Color outline) {
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
