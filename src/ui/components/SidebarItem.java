package ui.components;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Sidebar row: line icon + label. The open page gets a rounded highlight and bold text,
// and rows light up slightly on hover.
public class SidebarItem extends JLabel {
    // ----- Colours: edit the hex codes here -----
    // (dark theme: SIDEBAR_BG #0C1014, TEXT #F5F5F5, SELECTED_BG #262A31, HOVER_BG #181C22)
    public static final Color SIDEBAR_BG = Color.decode("#0B0D13");
    public static final Color TEXT = Color.decode("#F2F2FA");
    public static final Color SELECTED_BG = new Color(255, 255, 255, 38);
    public static final Color HOVER_BG = new Color(255, 255, 255, 20);
    static final int   RADIUS      = 10;

    public enum Glyph { DASHBOARD, BUILDING, BELL, LOGOUT, HOME, SEARCH }

    private boolean selected;
    private boolean hover;

    public SidebarItem(String text, Glyph glyph) {
        super(text);
        setIcon(new GlyphIcon(glyph));
        setIconTextGap(14);
        setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        setForeground(TEXT);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
        setFont(getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);   // stretch to the sidebar width
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (selected || hover) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? SELECTED_BG : HOVER_BG);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS * 2f, RADIUS * 2f));
            g2.dispose();
        }
        super.paintComponent(g);
    }

    // 22x22 line icons drawn with Java2D in the label's text colour
    private static class GlyphIcon implements Icon {
        private static final int SIZE = 22;
        private final Glyph glyph;

        GlyphIcon(Glyph glyph) {
            this.glyph = glyph;
        }

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
            switch (glyph) {
                case HOME:
                    p.moveTo(3, 11); p.lineTo(11, 4); p.lineTo(19, 11);
                    p.moveTo(5.5, 9.5); p.lineTo(5.5, 18); p.lineTo(16.5, 18); p.lineTo(16.5, 9.5);
                    p.moveTo(9, 18); p.lineTo(9, 13); p.lineTo(13, 13); p.lineTo(13, 18);
                    g2.draw(p);
                    break;
                case DASHBOARD:
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(3, 3, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(12.5f, 3, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(3, 12.5f, 6.5f, 6.5f, 2, 2));
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(12.5f, 12.5f, 6.5f, 6.5f, 2, 2));
                    break;
                case BUILDING:
                    g2.draw(new java.awt.geom.RoundRectangle2D.Float(4.5f, 3, 13, 16, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(8, 6.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(12, 6.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(8, 10.5f, 2, 2));
                    g2.fill(new java.awt.geom.Rectangle2D.Float(12, 10.5f, 2, 2));
                    p.moveTo(9.5f, 19); p.lineTo(9.5f, 15.5f); p.lineTo(12.5f, 15.5f); p.lineTo(12.5f, 19);
                    g2.draw(p);
                    break;
                case BELL:
                    p.moveTo(4.5f, 16); p.curveTo(6, 15, 6.5f, 13.5f, 6.5f, 10);
                    p.curveTo(6.5f, 6.5f, 8.5f, 4.5f, 11, 4.5f);
                    p.curveTo(13.5f, 4.5f, 15.5f, 6.5f, 15.5f, 10);
                    p.curveTo(15.5f, 13.5f, 16, 15, 17.5f, 16); p.closePath();
                    p.moveTo(9, 18.5f); p.curveTo(9.5f, 20, 12.5f, 20, 13, 18.5f);
                    g2.draw(p);
                    break;
                case LOGOUT:
                    p.moveTo(9, 4); p.lineTo(4.5f, 4); p.lineTo(4.5f, 18); p.lineTo(9, 18);
                    p.moveTo(8.5f, 11); p.lineTo(19, 11);
                    p.moveTo(15, 7); p.lineTo(19, 11); p.lineTo(15, 15);
                    g2.draw(p);
                    break;
                case SEARCH:
                    g2.draw(new java.awt.geom.Ellipse2D.Float(3.5f, 3.5f, 11.5f, 11.5f));
                    p.moveTo(13.5f, 13.5f); p.lineTo(18.5f, 18.5f);
                    g2.draw(p);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }
}
