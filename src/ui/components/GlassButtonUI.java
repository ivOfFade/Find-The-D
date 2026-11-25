package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Translucent rounded button look, used for every button that sits on the gradient
public class GlassButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
    private static final int ARC = 14;

    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        ButtonModel model = button.getModel();
        Graphics2D g2 = OneUi.smooth(graphics);

        int width = component.getWidth();
        int height = component.getHeight();
        boolean enabled = button.isEnabled();

        int alpha = !enabled ? 14 : model.isPressed() ? 74
                : model.isRollover() ? 54 : 34;

        g2.setColor(new Color(255, 255, 255, alpha));
        g2.fillRoundRect(0, 0, width, height, ARC, ARC);

        g2.setColor(new Color(255, 255, 255, enabled ? 75 : 35));
        g2.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);

        String text = button.getText();

        if (text != null && !text.isEmpty()) {
            FontMetrics metrics = g2.getFontMetrics(button.getFont());

            g2.setFont(button.getFont());
            g2.setColor(enabled ? button.getForeground()
                    : new Color(255, 255, 255, 120));

            g2.drawString(text,
                    (width - metrics.stringWidth(text)) / 2,
                    (height - metrics.getHeight()) / 2 + metrics.getAscent());
        }

        g2.dispose();
    }
}
