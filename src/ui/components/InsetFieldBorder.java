
import java.awt.*;

// Mockup-style field border: flat fill with a darker inset edge along the top and left
class InsetFieldBorder extends javax.swing.border.AbstractBorder {
    static final Color FILL = new Color(0xC6, 0xC6, 0xC6);

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(3, 10, 2, 8);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(3, 10, 2, 8);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        g.setColor(new Color(0x9D, 0x9D, 0x9D));
        g.fillRect(x, y, w, 3);       // darker top edge
        g.setColor(new Color(0xA7, 0xA7, 0xA7));
        g.fillRect(x, y, 3, h);       // darker left edge
    }
}
