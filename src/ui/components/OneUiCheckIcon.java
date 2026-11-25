import javax.swing.*;
import java.awt.*;

// Round tick-box used by the check lists (blue circle with a white tick when ticked)
class OneUiCheckIcon implements Icon {
    private static final int SIZE = 20;

    @Override public int getIconWidth() { return SIZE; }
    @Override public int getIconHeight() { return SIZE; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        boolean on = c instanceof AbstractButton && ((AbstractButton) c).isSelected();
        Graphics2D g2 = OneUi.smooth(g);
        if (on) {
            g2.setColor(OneUi.ACCENT);
            g2.fillOval(x + 1, y + 1, SIZE - 2, SIZE - 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Double tick = new java.awt.geom.Path2D.Double();
            tick.moveTo(x + 5.6, y + 10.4);
            tick.lineTo(x + 8.7, y + 13.4);
            tick.lineTo(x + 14.4, y + 6.8);
            g2.draw(tick);
        } else {
            g2.setColor(new Color(0xB4, 0xB4, 0xB4));
            g2.setStroke(new BasicStroke(1.7f));
            g2.drawOval(x + 1, y + 1, SIZE - 2, SIZE - 2);
        }
        g2.dispose();
    }
}
