import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// The floating list: a rounded white card with a soft shadow that opens right under its field
class OneUiPopup extends JPopupMenu {
    private static final int SIDE = 6, TOP = 4, BOTTOM = 10;   // room around the card for the shadow
    private static final int PAD = 6;                          // space between the card edge and its rows

    OneUiPopup() {
        setOpaque(false);
        setBorder(new EmptyBorder(TOP + PAD, SIDE + PAD, BOTTOM + PAD, SIDE + PAD));
        setLightWeightPopupEnabled(true);
        addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                SwingUtilities.invokeLater(OneUiPopup.this::makeSeeThrough);
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });
    }

    // The window/panel that carries the popup is opaque by default, which would show square corners
    private void makeSeeThrough() {
        Container parent = getParent();
        if (parent instanceof JComponent) {
            ((JComponent) parent).setOpaque(false);
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JWindow) {
            try {
                w.setBackground(new Color(0, 0, 0, 0));
            } catch (RuntimeException ignored) {
                // this platform cannot do see-through windows: the corners stay square, nothing else breaks
            }
        }
    }

    // Opens the list under the given field, as wide as the field
    void showUnder(JComponent field) {
        setPreferredSize(null);
        int h = getPreferredSize().height;
        setPreferredSize(new Dimension(field.getWidth() + 2 * SIDE, h));
        show(field, -SIDE, field.getHeight() + 4 - TOP);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        int x = SIDE, y = TOP, w = getWidth() - 2 * SIDE, h = getHeight() - TOP - BOTTOM;
        for (int i = 1; i <= 6; i++) {
            g2.setColor(new Color(0, 0, 0, 6));
            g2.fillRoundRect(x - i, y - i + 3, w + 2 * i, h + 2 * i, OneUi.POPUP_ARC + 2 * i, OneUi.POPUP_ARC + 2 * i);
        }
        g2.setColor(OneUi.POPUP_BG);
        g2.fillRoundRect(x, y, w, h, OneUi.POPUP_ARC, OneUi.POPUP_ARC);
        g2.setColor(new Color(0, 0, 0, 22));
        g2.drawRoundRect(x, y, w - 1, h - 1, OneUi.POPUP_ARC, OneUi.POPUP_ARC);
        g2.dispose();
    }
}
