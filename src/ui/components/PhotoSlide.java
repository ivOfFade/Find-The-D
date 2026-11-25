import javax.swing.*;
import java.awt.*;

// One photo in the viewer: the whole picture, fitted inside the available space
class PhotoSlide extends JComponent {
    private final java.awt.image.BufferedImage image;

    PhotoSlide(java.awt.image.BufferedImage image) {
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
