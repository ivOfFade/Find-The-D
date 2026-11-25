package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// c = component, p = parent
public final class GlassButtons {
    private GlassButtons() { }

    public static boolean isOnGradient(Component c) {
        for (Component p = c.getParent(); p != null; p = p.getParent()) {
            if (p instanceof GradientPanel
                    || (p instanceof RoundedDialog.RoundedRoot && ((RoundedDialog.RoundedRoot) p).themed)) {
                return true;
            }
        }
        return false;
    }

    // Appy Button
    public static void apply(JButton applyButton) {
        if (applyButton.getUI() instanceof GlassButtonUI) {
            return;
        }
        if (!applyButton.isBorderPainted() && !applyButton.isContentAreaFilled()) {
            return;   // text-only buttons (like the three dots) stay as they are
        }
        applyButton.setUI(new GlassButtonUI());
        applyButton.setOpaque(false);
        applyButton.setContentAreaFilled(false);
        applyButton.setBorder(new EmptyBorder(0, 12, 0, 12));
        applyButton.setFocusPainted(false);
        applyButton.setRolloverEnabled(true);
        applyButton.revalidate();
        applyButton.repaint();
    }
}
