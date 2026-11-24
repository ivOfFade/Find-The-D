import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Dark rounded square with a simple "picture" icon (sun + mountains)
class ImagePlaceholder extends JComponent {
    public ImagePlaceholder() {
        setPreferredSize(new Dimension(153, 123));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0x55, 0x55, 0x55));
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        g2.setColor(new Color(0xEE, 0xEE, 0xEE));
        int sun = (int) (w * 0.16);
        g2.fillOval((int) (w * 0.27), (int) (h * 0.20), sun, sun);

        // small hill (left)
        g2.fillPolygon(
            new int[] {(int) (w * 0.08), (int) (w * 0.20), (int) (w * 0.34), (int) (w * 0.20)},
            new int[] {(int) (h * 0.88), (int) (h * 0.66), (int) (h * 0.66), (int) (h * 0.88)}, 4);

        // big mountain (right)
        g2.fillPolygon(
            new int[] {(int) (w * 0.26), (int) (w * 0.56), (int) (w * 0.68), (int) (w * 0.94), (int) (w * 0.94)},
            new int[] {(int) (h * 0.88), (int) (h * 0.46), (int) (h * 0.46), (int) (h * 0.74), (int) (h * 0.88)}, 5);
        g2.dispose();
    }
}
