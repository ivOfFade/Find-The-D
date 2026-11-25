package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Every GradientPanel uses this renderer, so the same background appears throughout the app.
public final class GradientBackground {
    private GradientBackground() { }

    // Deep blue/navy base with soft purple and magenta light blooms.
    public static final Color BASE   = new Color(0x9BAFD9);
    public static final Color WHITE = new Color(0x9BAFD9);
    public static final Color BLUE   = new Color(0x103783);

    public static final Color GLASS      = new Color(0x9BAFD9);
    public static final Color GLASS_LINE = new Color(0x9BAFD9);

    public static final Color TEXT      = new Color(0xF7, 0xF2, 0xFF);
    public static final Color TEXT_SOFT = new Color(0xDC, 0xD0, 0xF5);

    // Smooth blend: a long diagonal sweep of deep navy -> royal blue -> violet -> navy, with large
    // Gaussian-faded colour pools on top. There are no hard-edged shapes, so nothing shows a seam.
    public static java.awt.image.BufferedImage render(int w, int h) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float[] stops = {0.00f, 0.25f, 0.50f, 0.75f, 1.00f};
        Color[] colors = {
            new Color(0x2A, 0x1F, 0x8C),
            new Color(0x0B, 0x33, 0x96),
            new Color(0x3C, 0x1E, 0x98),
            new Color(0x08, 0x27, 0x78),
            new Color(0x00, 0x12, 0x3C)
        };
        g2.setPaint(new LinearGradientPaint(0, 0, w * 0.60f, Math.max(1, h), stops, colors));
        g2.fillRect(0, 0, w, h);

        // Very wide, very soft colour pools (strength is the centre opacity).
        blob(g2, w, h, 0.05f, 0.02f, 0.95f, new Color(0x103783), 0.55f);
        blob(g2, w, h, 0.88f, 0.08f, 0.80f, new Color(0x9BAFD9), 0.45f);
        blob(g2, w, h, 0.04f, 0.62f, 0.80f, new Color(0x9BAFD9), 0.45f);
        blob(g2, w, h, 0.28f, 1.00f, 0.80f, new Color(0x9BAFD9), 0.80f);
        blob(g2, w, h, 0.62f, 0.30f, 0.70f, new Color(0x103783), 0.22f);
        blob(g2, w, h, 1.00f, 0.95f, 0.75f, new Color(0x103783), 0.65f);

        g2.dispose();
        dither(img);
        return img;
    }

    // Adds +/-1 of noise so the long, gentle fades do not show 8-bit colour bands.
    private static void dither(java.awt.image.BufferedImage img) {
        java.util.Random rnd = new java.util.Random(7);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int n = rnd.nextInt(3) - 1;
                int r = Math.max(0, Math.min(255, ((rgb >> 16) & 0xFF) + n));
                int g = Math.max(0, Math.min(255, ((rgb >> 8) & 0xFF) + n));
                int b = Math.max(0, Math.min(255, (rgb & 0xFF) + n));
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
    }

    // Radial pool whose opacity follows a Gaussian curve and reaches exactly 0 at the edge.
    private static void blob(Graphics2D g2, int w, int h, float cx, float cy,
                             float radius, Color c, float strength) {
        float r = Math.max(1f, Math.max(w, h) * radius);
        final int n = 14;
        final double k = 3.2;
        double edge = Math.exp(-k);
        float[] stops = new float[n];
        Color[] colors = new Color[n];
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            double a = strength * (Math.exp(-k * t * t) - edge) / (1.0 - edge);
            stops[i] = (float) t;
            colors[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(),
                Math.max(0, Math.min(255, (int) Math.round(255 * a))));
        }
        g2.setPaint(new RadialGradientPaint(w * cx, h * cy, r, stops, colors));
        g2.fillRect(0, 0, w, h);
    }

    // Lets the gradient behind a scroll pane show through it.
    public static void makeTransparent(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    }
}
