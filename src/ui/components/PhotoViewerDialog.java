import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Instagram-style photo viewer: dark window, big photo, arrows (or drag / arrow keys) to swipe
// left and right, dots and a counter showing where you are.
class PhotoViewerDialog {
    // ----- Colours: edit the hex codes here -----
    static final Color BG      = Color.decode("#32568f");
    static final Color BAR     = Color.decode("#32568f");
    static final Color TEXT    = Color.decode("#FFFFFF");
    static final Color OUTLINE = Color.decode("#333333");

    static void show(Window owner, String title, java.util.List<? extends JComponent> slides, int start) {
        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        int w = 900;
        int h = 680;
        if (owner != null) {
            w = Math.max(420, Math.min(w, owner.getWidth() - 60));
            h = Math.max(360, Math.min(h, owner.getHeight() - 60));
        }
        d.setSize(w, h);
        d.setLocationRelativeTo(owner);

        PhotoSlidePane pane = new PhotoSlidePane(slides, start);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BAR);
        bar.setPreferredSize(new Dimension(100, 48));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("YU Gothic UI Light", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        titleLabel.setBorder(new EmptyBorder(0, 18, 0, 0));
        bar.add(titleLabel, BorderLayout.WEST);
        JLabel close = new JLabel("\u00D7   ", SwingConstants.RIGHT);
        close.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 28));
        close.setForeground(TEXT);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                d.dispose();
            }
        });
        bar.add(close, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        content.add(bar, BorderLayout.NORTH);
        content.add(pane, BorderLayout.CENTER);
        d.add(content);

        JRootPane root = d.getRootPane();
        root.registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> pane.previous(),
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> pane.next(),
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        RoundedDialog.apply(d, BG, OUTLINE);
        d.setVisible(true);
    }
}
