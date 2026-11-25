import java.awt.*;
import javax.swing.*;

// Button that always paints its own flat background colour (so the colour shows on every look-and-feel)
class FlatButton extends JButton {

  public FlatButton(String text) {
    super(text);
    setOpaque(true);
    setContentAreaFilled(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    super.paintComponent(g);
  }
}
