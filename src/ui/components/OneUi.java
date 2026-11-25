import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// ======================= One UI style (shared by every dropdown) =======================
// Soft rounded fields, a thin chevron, a floating rounded list with a gentle shadow, and pill-shaped
// row highlights. Change the colours here and every dropdown in the app follows.
final class OneUi {
    private OneUi() { }

    static final Color FIELD_BG    = new Color(0xF5, 0xF5, 0xF5);
    static final Color FIELD_HOVER = new Color(0xEC, 0xEC, 0xEC);
    static final Color FIELD_LINE  = new Color(0xD8, 0xD8, 0xD8);
    static final Color POPUP_BG    = new Color(0xFC, 0xFC, 0xFC);
    static final Color ROW_HOVER   = new Color(0xEE, 0xEE, 0xEE);
    static final Color TEXT        = new Color(0x1C, 0x1C, 0x1C);
    static final Color HINT        = new Color(0x80, 0x80, 0x80);
    static final Color ACCENT      = new Color(0x3E, 0x91, 0xFF);   // One UI blue (ticks, chosen item)

    static final int FIELD_ARC = 32;   // closed dropdown corner (diameter)
    static final int POPUP_ARC = 36;   // open list corner (diameter)
    static final int ROW_ARC   = 26;   // highlighted row corner (diameter)
    static final int ROW_HEIGHT = 36;

    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }
}
