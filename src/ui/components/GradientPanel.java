package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Panel that paints the gradient as its background (drawn small and scaled up, so resizing stays fast).
public class GradientPanel extends JPanel {
    private static final int SCALE = 4;
    private java.awt.image.BufferedImage cache;

    public GradientPanel() {
        super();
        setOpaque(false);
    }

    public GradientPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int sw = Math.max(1, w / SCALE);
        int sh = Math.max(1, h / SCALE);
        if (cache == null || cache.getWidth() != sw || cache.getHeight() != sh) {
            cache = GradientBackground.render(sw, sh);
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(cache, 0, 0, w, h, null);
        g2.dispose();
    }
}
