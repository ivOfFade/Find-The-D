import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

// One tile in the photo picker: a folder, or a picture thumbnail with its file name.
// Selected pictures get a dark frame and a tick; the tick box also appears when hovering.
class PickerTile extends JComponent {
    static final int W = 120;
    static final int IMG_H = 100;
    static final int H = 126;

    final File file;
    final boolean folder;
    private volatile java.awt.image.BufferedImage thumb;
    private volatile boolean broken;
    private boolean selected;
    private boolean hover;

    PickerTile(File file, boolean folder, java.util.function.Consumer<PickerTile> onClick) {
        this.file = file;
        this.folder = folder;
        setPreferredSize(new Dimension(W, H));
        setMinimumSize(new Dimension(W, H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(file.getName());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.accept(PickerTile.this);
            }
        });
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    void setThumb(java.awt.image.BufferedImage thumb) {
        this.thumb = thumb;
    }

    void markBroken() {
        this.broken = true;
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && fm.stringWidth(cut + "...") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(InsetFieldBorder.FILL);
        g2.fillRect(0, 0, w, h);

        // Picture area
        Shape oldClip = g2.getClip();
        g2.clipRect(0, 0, w, IMG_H);   // narrows the clip (keeps the scroll area's own clip)
        java.awt.image.BufferedImage img = thumb;
        if (folder) {
            g2.setColor(new Color(0xD4, 0xD4, 0xD4));
            g2.fillRect(0, 0, w, IMG_H);
            int fw = 58;
            int fh = 40;
            int x = (w - fw) / 2;
            int y = (IMG_H - fh) / 2 + 4;
            g2.setColor(new Color(0x7A, 0x7A, 0x7A));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y - 7, 26, 14, 5, 5));   // tab
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y, fw, fh, 7, 7));       // back
            g2.setColor(new Color(0x92, 0x92, 0x92));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(x, y + 8, fw, fh - 8, 7, 7)); // front
        } else if (img != null) {
            double scale = Math.max((double) w / img.getWidth(), (double) IMG_H / img.getHeight());
            int dw = (int) Math.round(img.getWidth() * scale);
            int dh = (int) Math.round(img.getHeight() * scale);
            g2.drawImage(img, (w - dw) / 2, (IMG_H - dh) / 2, dw, dh, null);
        } else {
            g2.setColor(new Color(0xB4, 0xB4, 0xB4));
            g2.fillRect(0, 0, w, IMG_H);
            if (broken) {
                g2.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 11));
                g2.setColor(new Color(0x55, 0x55, 0x55));
                String msg = "No preview";
                g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, IMG_H / 2 + 4);
            }
        }
        g2.setClip(oldClip);

        // File / folder name
        g2.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 11));
        g2.setColor(new Color(0x1E, 0x1E, 0x1E));
        g2.drawString(fit(file.getName(), g2.getFontMetrics(), w - 16), 8, IMG_H + 17);

        // Frame + tick box
        if (selected) {
            g2.setColor(new Color(0x33, 0x33, 0x33));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(1, 1, w - 3, h - 3);
        } else if (hover) {
            g2.setColor(new Color(0x77, 0x77, 0x77));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(0, 0, w - 1, h - 1);
        }
        if (!folder && (selected || hover)) {
            SquareCheckIcon.paint(g2, w - 26, 8, selected);
        }
        g2.dispose();
    }
}
