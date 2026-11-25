import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

// One choice in a list: rounded pill highlight on hover, optional blue "chosen" look
class OneUiItem extends JLabel {
    private final Color hoverColor;
    private boolean hovering;

    OneUiItem(String text, Runnable onPick) {
        this(text, OneUi.ROW_HOVER, onPick);
    }

    OneUiItem(String text, Color hoverColor, Runnable onPick) {
        super(text);
        this.hoverColor = hoverColor;
        setOpaque(false);
        setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        setForeground(OneUi.TEXT);
        setBorder(new EmptyBorder(0, 14, 0, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                hovering = false;
                repaint();
                if (onPick != null && contains(e.getPoint())) {
                    onPick.run();
                }
            }
        });
    }

    void setChosen(boolean chosen) {
        setFont(getFont().deriveFont(chosen ? Font.BOLD : Font.PLAIN));
        setForeground(chosen ? OneUi.ACCENT : OneUi.TEXT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(OneUi.ROW_HEIGHT, d.height));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovering) {
            Graphics2D g2 = OneUi.smooth(g);
            g2.setColor(hoverColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), OneUi.ROW_ARC, OneUi.ROW_ARC);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
