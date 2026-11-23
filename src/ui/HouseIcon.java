import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Small house icon (the "\u2302" interest symbol); filled when the tenant has marked interest
class HouseIcon extends JComponent {
    private boolean interested = false;
    private boolean interactive = true;

    public HouseIcon() {
        setPreferredSize(new Dimension(22, 22));
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
        repaint();
    }

    // Landlords can see the icon but cannot click it (drawn in a lighter grey)
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        repaint();
    }

    public boolean isInteractive() {
        return interactive;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color c = Color.WHITE;

        // House outline: roof peak, eaves, walls
        java.awt.geom.Path2D.Double house = new java.awt.geom.Path2D.Double();
        house.moveTo(11, 3);     // roof peak
        house.lineTo(20, 11);    // right eave
        house.lineTo(17.5, 11);  // right wall top
        house.lineTo(17.5, 19);  // right wall bottom
        house.lineTo(4.5, 19);   // left wall bottom
        house.lineTo(4.5, 11);   // left wall top
        house.lineTo(2, 11);     // left eave
        house.closePath();

        g2.setColor(c);
        if (interested) {
            g2.fill(house);
        }
        g2.draw(house);
        g2.dispose();
    }
}
