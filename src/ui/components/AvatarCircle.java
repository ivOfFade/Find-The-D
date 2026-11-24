import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Round avatar with the poster's initial
class AvatarCircle extends JComponent {
    private final String letter;

    public AvatarCircle(String letter, int size) {
        this.letter = letter;
        setPreferredSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0xEE, 0xEE, 0xEE));
        g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, getHeight() / 2));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(letter)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(letter, x, y);
        g2.dispose();
    }
}
