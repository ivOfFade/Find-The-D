package ui.components;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
// ======================= One UI style (shared by every dropdown) =======================
// Soft rounded fields, a thin chevron, a floating rounded list with a gentle shadow, and pill-shaped
// row highlights. Change the colours here and every dropdown in the app follows.
public final class OneUi {
    private OneUi() { }

    public static final Color FIELD_BG    = new Color(0xF5, 0xF5, 0xF5);
    public static final Color FIELD_HOVER = new Color(0xEC, 0xEC, 0xEC);
    public static final Color FIELD_LINE  = new Color(0xD8, 0xD8, 0xD8);
    public static final Color POPUP_BG    = new Color(0xFC, 0xFC, 0xFC);
    public static final Color ROW_HOVER   = new Color(0xEE, 0xEE, 0xEE);
    public static final Color TEXT        = new Color(0x1C, 0x1C, 0x1C);
    public static final Color HINT        = new Color(0x80, 0x80, 0x80);
    public static final Color ACCENT      = new Color(0x3E, 0x91, 0xFF);   // One UI blue (ticks, chosen item)

    public static final int FIELD_ARC = 32;   // closed dropdown corner (diameter)
    public static final int POPUP_ARC = 36;   // open list corner (diameter)
    public static final int ROW_ARC   = 26;   // highlighted row corner (diameter)
    public static final int ROW_HEIGHT = 36;

    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }
}
