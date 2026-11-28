package ui.components;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// One photo box in the Post New Dorm form: empty grey square until a picture is uploaded.
// The picture fills the box (cropped to fit); clicking a filled box removes the picture.
public class PhotoThumb extends JPanel {
    private java.awt.image.BufferedImage image;
    private java.io.File file;
    private Runnable onRemoved;

    public PhotoThumb() {
        setBackground(InsetFieldBorder.FILL);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hasPhoto()) {
                    clearPhoto();
                    if (onRemoved != null) {
                        onRemoved.run();
                    }
                }
            }
        });
    }

    // Called after the user clicks a photo to remove it
    public void setOnRemoved(Runnable onRemoved) {
        this.onRemoved = onRemoved;
    }

    public boolean hasPhoto() {
        return image != null;
    }

    public java.io.File getFile() {
        return file;
    }

    // Loads the picture; returns false if the file is not a readable image
    public boolean setPhoto(java.io.File f) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) {
                return false;
            }
            // Keep only a small copy in memory (these are just thumbnails)
            int max = 400;
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
            image = img;
            file = f;
            setToolTipText("Click to remove this photo");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            repaint();
            return true;
        } catch (java.io.IOException | RuntimeException ex) {
            return false;
        }
    }

    public void clearPhoto() {
        image = null;
        file = null;
        setToolTipText(null);
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double scale = Math.max((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
        int dw = (int) Math.round(image.getWidth() * scale);
        int dh = (int) Math.round(image.getHeight() * scale);
        g2.drawImage(image, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
        g2.dispose();
    }
}
