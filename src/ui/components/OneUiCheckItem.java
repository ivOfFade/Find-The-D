package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// A check box row for the multi-pick lists (stays open so several can be ticked)
public class OneUiCheckItem extends JCheckBox {
    private boolean hovering;

    public OneUiCheckItem(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 13));
        setForeground(OneUi.TEXT);
        setIcon(new OneUiCheckIcon());
        setSelectedIcon(new OneUiCheckIcon());
        setIconTextGap(12);
        setBorder(new EmptyBorder(0, 14, 0, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
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
            g2.setColor(OneUi.ROW_HOVER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), OneUi.ROW_ARC, OneUi.ROW_ARC);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
