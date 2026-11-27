package ui.components;
import java.awt.*;
import javax.swing.*;

// Soft "floating" drop shadow under the bottom/right edge of a button. Wraps whatever border the
// button already has (outline, padding...) and adds room for the shadow next to it.
public class ButtonShadowBorder extends javax.swing.border.AbstractBorder {

  static final int SIZE = 8; // room reserved for the shadow (offset + blur)
  private final javax.swing.border.Border inner;

  public ButtonShadowBorder(javax.swing.border.Border inner) {
    this.inner = inner;
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return getBorderInsets(c, new Insets(0, 0, 0, 0));
  }

  @Override
  public Insets getBorderInsets(Component c, Insets insets) {
    Insets i =
      inner == null ? new Insets(0, 0, 0, 0) : inner.getBorderInsets(c);
    insets.set(i.top, i.left, i.bottom + SIZE, i.right + SIZE);
    return insets;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    paintShadow(c, g, x, y, w, h);
    if (inner != null) {
      inner.paintBorder(c, g, x, y, w - SIZE, h - SIZE);
    }
  }

  // Colour of whatever the button sits on, so the shadow strip can be cleared before drawing
  private static Color backdrop(Component c) {
    for (Component p = c.getParent(); p != null; p = p.getParent()) {
      if (
        p instanceof RoundedPanel ||
        p instanceof RoundedDialog.RoundedRoot ||
        p.isOpaque()
      ) {
        return p.getBackground();
      }
    }
    return Color.LIGHT_GRAY;
  }

  static void paintShadow(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    int fw = w - SIZE;
    int fh = h - SIZE;
    // only the strip to the right of / below the button face
    java.awt.geom.Area strips = new java.awt.geom.Area(
      new Rectangle(x, y, w, h)
    );
    strips.subtract(new java.awt.geom.Area(new Rectangle(x, y, fw, fh)));
    g2.clip(strips);
    g2.setColor(backdrop(c));
    g2.fillRect(x, y, w, h);
    g2.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON
    );
    boolean pressed =
      c instanceof AbstractButton &&
      ((AbstractButton) c).getModel().isPressed();
    final float offset = 2f; // how far the shadow is pushed down/right
    final float blur = 6f; // how far it fades out
    final float peak = pressed ? 0.14f : 0.30f; // darkest point, right next to the button
    // Stack many thin, translucent layers (outermost first) so the opacity falls off smoothly
    // with distance, like a real blurred shadow, instead of in visible steps.
    float covered = 0f;
    for (float e = blur; e >= 0f; e -= 0.5f) {
      float t = 1f - e / (blur + 0.5f);
      float target = peak * t * t; // smooth ease-in toward the button
      float layer = (target - covered) / (1f - covered); // alpha that brings the total to target
      covered = target;
      if (layer <= 0f) {
        continue;
      }
      g2.setColor(new Color(0f, 0f, 0f, Math.min(1f, layer)));
      g2.fill(
        new java.awt.geom.RoundRectangle2D.Float(
          x + offset - e,
          y + offset - e,
          fw + 2f * e,
          fh + 2f * e,
          3f + 2f * e,
          3f + 2f * e
        )
      );
    }
    g2.dispose();
  }
}
