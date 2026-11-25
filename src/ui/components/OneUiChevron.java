import javax.swing.*;
import java.awt.*;

// Thin rounded chevron (points down, or up while the list is open)
class OneUiChevron extends JComponent {
    private Color color;
    private boolean up;

    void setColor(Color color) {
        this.color = color;
        repaint();
    }

    OneUiChevron(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(30, 20));
    }

    void setUp(boolean up) {
        this.up = up;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double dy = up ? 2.5 : -2.5;
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        p.moveTo(cx - 5, cy + dy);
        p.lineTo(cx, cy - dy);
        p.lineTo(cx + 5, cy + dy);
        g2.draw(p);
        g2.dispose();
    }
}
