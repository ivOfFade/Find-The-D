package ui.components;
import java.awt.*;
import java.io.File;
import javax.swing.*;

// One photo in the post's grid: the picture cropped to fill the tile, with "+N" on top when
// the post has more photos than the grid can show
public class PhotoTile extends JComponent {
    private final java.awt.image.BufferedImage image;
    private final int more;

    public PhotoTile(java.awt.image.BufferedImage image, int more) {
        this.image = image;
        this.more = more;
        setPreferredSize(new Dimension(153, 123));
    }

    // Reads a picture, scaled down to at most 1600px on its longest side (keeps memory in check
    // when someone uploads lots of photos). Returns null if the file is not a readable image.
    static java.awt.image.BufferedImage load(File f) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) {
                return null;
            }
            int max = 1600;
            int w = img.getWidth();
            int h = img.getHeight();
            if (Math.max(w, h) > max) {
                double scale = (double) max / Math.max(w, h);
                int nw = Math.max(1, (int) Math.round(w * scale));
                int nh = Math.max(1, (int) Math.round(h * scale));
                java.awt.image.BufferedImage small =
                    new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = small.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(img, 0, 0, nw, nh, null);
                g2.dispose();
                img = small;
            }
            return img;
        } catch (java.io.IOException | RuntimeException ex) {
            return null;
        }
    }
     // Draws the picture cropped to fill the tile, with a dark overlay and "+N" on top when the post has more photos than the grid can show
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();
        g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 16, 16));
        double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        if (more > 0) {
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("YU Gothic UI Light", Font.BOLD, 28));
            String text = "+" + more;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
