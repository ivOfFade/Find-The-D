package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Small translucent pill (amenity tags on a post)
// g = graphics, g2 = graphics 2d
public class GlassChip extends JLabel {
    public GlassChip(String text) {
        super(text, SwingConstants.CENTER);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
