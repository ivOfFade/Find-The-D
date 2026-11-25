import javax.swing.*;
import java.awt.*;

// Small outlined down-pointing triangle (the dropdown arrow in the mockup)
class OutlineTriangle extends JComponent {
    public OutlineTriangle() {
        setPreferredSize(new Dimension(40, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(new Color(0x44, 0x44, 0x44));
        int triW = 20, triH = 11;
        int x = (getWidth() - triW) / 2;
        int y = (getHeight() - triH) / 2;
        g2.drawPolygon(new int[] {x, x + triW, x + triW / 2}, new int[] {y, y, y + triH}, 3);
        g2.dispose();
    }
}
