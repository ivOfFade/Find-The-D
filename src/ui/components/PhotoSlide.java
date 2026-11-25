package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// One photo in the viewer: the whole picture, fitted inside the available space
public class PhotoSlide extends JComponent {
    private final java.awt.image.BufferedImage image;

    public PhotoSlide(java.awt.image.BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();
        double scale = Math.min((double) w / image.getWidth(), (double) h / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        g2.dispose();
    }
}

// ===== Gradient background =====
