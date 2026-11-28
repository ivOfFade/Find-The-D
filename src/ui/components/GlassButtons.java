package ui.components;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// c = component, p = parent
final public class GlassButtons {
    private GlassButtons() { }

    static boolean isOnGradient(Component c) {
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
