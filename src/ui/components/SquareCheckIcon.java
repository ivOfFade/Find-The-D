import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Minimalist square check box icon: outlined rounded square, dark fill + white tick when selected
class SquareCheckIcon implements Icon {
    private static final int SIZE = 16;

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        paint(g2, x, y, c instanceof AbstractButton && ((AbstractButton) c).isSelected());
        g2.dispose();
    }

    // Draws the square (used by check boxes and by the photo picker tiles)
    static void paint(Graphics2D g2, int x, int y, boolean on) {
        Object oldAa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Stroke oldStroke = g2.getStroke();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.geom.RoundRectangle2D box =
            new java.awt.geom.RoundRectangle2D.Double(x + 0.75, y + 0.75, SIZE - 1.5, SIZE - 1.5, 4, 4);
        if (on) {
            g2.setColor(new Color(0x33, 0x33, 0x33));
            g2.fill(box);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Double tick = new java.awt.geom.Path2D.Double();
            tick.moveTo(x + 4.2, y + 8.4);
            tick.lineTo(x + 7.0, y + 11.2);
            tick.lineTo(x + 11.8, y + 4.9);
            g2.draw(tick);
        } else {
            g2.setColor(new Color(0xE2, 0xE2, 0xE2));
            g2.fill(box);
            g2.setColor(new Color(0x66, 0x66, 0x66));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(box);
        }
        g2.setStroke(oldStroke);
        if (oldAa != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }
}
