package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// Raised look: a drop shadow along the bottom and right, plus an optional thin outline
public class DropShadowBorder extends javax.swing.border.AbstractBorder {
    private final Color outline;

    public DropShadowBorder(Color shadow, Color outline) {
        this.outline = outline;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, ButtonShadowBorder.SIZE, ButtonShadowBorder.SIZE);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(1, 1, ButtonShadowBorder.SIZE, ButtonShadowBorder.SIZE);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        ButtonShadowBorder.paintShadow(c, g, x, y, w, h);
        if (outline != null) {
            g.setColor(outline);
            g.drawRect(x, y, w - ButtonShadowBorder.SIZE - 1, h - ButtonShadowBorder.SIZE - 1);
        }
    }
}
