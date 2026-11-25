package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Gives every JButton in the app the floating shadow as soon as it is shown, including buttons
// created later inside dialogs. Set a "noShadow" client property on a button to opt out.
public final class FloatingButtons {
    private FloatingButtons() {}

    public static void install() {
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (!(ev instanceof HierarchyEvent)) {
                return;
            }
            HierarchyEvent he = (HierarchyEvent) ev;
            if ((he.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                    && he.getComponent() instanceof JButton
                    && he.getComponent().isShowing()) {
                addShadow((JButton) he.getComponent());
            }
        }, AWTEvent.HIERARCHY_EVENT_MASK);
    }

    private static void addShadow(JButton b) {
        if (b.getClientProperty("noShadow") != null || b.getParent() instanceof JScrollBar) {
            return;
        }
        if (GlassButtons.isOnGradient(b)) {
            GlassButtons.apply(b);   // buttons on the gradient: translucent pill instead of a gray box with a shadow
            return;
        }
        if (!b.isOpaque() && !b.isContentAreaFilled()) {
            return;   // text-only / transparent buttons have no face to float
        }
        javax.swing.border.Border cur = b.getBorder();
        if (cur instanceof ButtonShadowBorder || cur instanceof DropShadowBorder) {
            return;
        }
        // older hard 2-3px matte "shadows" are replaced by the soft one
        if (cur instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder cb = (javax.swing.border.CompoundBorder) cur;
            if (cb.getOutsideBorder() instanceof javax.swing.border.MatteBorder) {
                Insets mi = cb.getOutsideBorder().getBorderInsets(b);
                if (mi.top == 0 && mi.left == 0 && mi.bottom > 0 && mi.bottom == mi.right) {
                    cur = cb.getInsideBorder();
                }
            }
        }
        boolean sized = b.isPreferredSizeSet();
        Dimension d = b.getPreferredSize();
        b.setBorder(new ButtonShadowBorder(cur));
        if (sized) {
            // keep the button face the size it was designed at
            b.setPreferredSize(new Dimension(d.width + ButtonShadowBorder.SIZE, d.height + ButtonShadowBorder.SIZE));
        }
        b.revalidate();
        b.repaint();
    }
}
