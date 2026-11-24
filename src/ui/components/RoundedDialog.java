import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Gives the small message / confirmation dialogs rounded corners (radius 10).
// Call RoundedDialog.apply(dialog) after the dialog's content is added and before setVisible(true).
final class RoundedDialog {
    static final int RADIUS = 10;
    private static final Color HEADER_BG = new Color(0xBC, 0xBC, 0xBC);
    private static final Color OUTLINE = new Color(255, 255, 255, 60);

    // Modern dialog theme: solid black header over a nearly flat blue -> purple body
    static final Color DIALOG_HEADER = new Color(0x0B, 0x0B, 0x10);
    static final Color DIALOG_BLUE   = Color.decode("#32568f");
    static final Color DIALOG_PURPLE = Color.decode("#6c8ab9");
    static final Color DIALOG_LINE   = new Color(255, 255, 255, 60);

    private RoundedDialog() {}

    static void apply(JDialog d) {
        apply(d, DIALOG_BLUE, OUTLINE, true);
    }

    // Same, with a custom background and outline colour (e.g. the dark photo viewer)
    static void apply(JDialog d, Color background, Color outline) {
        apply(d, background, outline, false);
    }

    private static void apply(JDialog d, Color background, Color outline, boolean themed) {
        boolean wasDecorated = !d.isUndecorated();
        String title = d.getTitle();
        Container inner = d.getContentPane();
        d.setUndecorated(true);

        RoundedRoot root = new RoundedRoot(background, outline, themed);
        if (wasDecorated) {
            // rounded corners need an undecorated window, so draw the title bar ourselves
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(themed ? DIALOG_HEADER : HEADER_BG);
            header.setPreferredSize(new Dimension(480, 36));
            JLabel headerTitle = new JLabel("  " + (title == null ? "" : title));
            headerTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
            headerTitle.setForeground(themed ? Color.WHITE : Color.BLACK);
            header.add(headerTitle, BorderLayout.WEST);
            root.add(header, BorderLayout.NORTH);
        }
        if (themed) {
            styleComponentTree(inner);
        }
        root.add(inner, BorderLayout.CENTER);
        d.setContentPane(root);

        try {
            d.setBackground(new Color(0, 0, 0, 0)); // transparent window so the corners show through
        } catch (Exception ex) {
            // per-pixel transparency unavailable: clip the window to a rounded shape instead
            final Runnable clip = () -> {
                try {
                    d.setShape(new java.awt.geom.RoundRectangle2D.Float(
                        0, 0, d.getWidth(), d.getHeight(), RADIUS * 2f, RADIUS * 2f));
                } catch (Exception ignored) { }
            };
            d.addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { clip.run(); }
                @Override public void componentShown(ComponentEvent e) { clip.run(); }
            });
        }
    }

    // Replacement for JOptionPane.showMessageDialog with the same rounded look
    static void showMessage(Component parent, String title, String message) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 210);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 25, 15, 25);

        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel msgLabel = new JLabel("<html><div style='width: 400px;'>" + safe + "</div></html>");
        msgLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 18));
        msgLabel.setForeground(Color.WHITE);
        body.add(msgLabel, gbc);

        gbc.gridy = 1; gbc.weighty = 0; gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 15, 25);
        FlatButton okBtn = new FlatButton("OK");
        okBtn.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        okBtn.setForeground(Color.BLACK);
        okBtn.setBackground(new Color(0xDF, 0xDF, 0xDF));
        okBtn.setFocusPainted(false);
        okBtn.setMargin(new Insets(0, 0, 0, 0));
        okBtn.setBorder(new DropShadowBorder(new Color(0x9A, 0x9A, 0x9A), null));
        okBtn.setPreferredSize(new Dimension(92, 42));
        okBtn.setMinimumSize(new Dimension(92, 42));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());
        body.add(okBtn, gbc);

        dialog.add(body);
        dialog.getRootPane().setDefaultButton(okBtn);
        apply(dialog);
        dialog.setVisible(true);
    }

    // ----- recolours a dialog's existing content for the blue/purple theme -----
    private static boolean isLight(Color c) {
        return c != null && c.getAlpha() == 255 && (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 > 140;
    }

    // controls a component (ex. yesButton, yung lang maapektuhan)
    private static void styleComponent(Component c) {
        if (c instanceof JLabel) {

        } else if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            if (b instanceof JButton) {
                if (isLight(b.getBackground())) {
                    b.setBackground(new Color(0, 0, 0, 0));   // glass pill is drawn by GlassButtonUI
                }
            } else {
                b.setOpaque(false);
            }
        } else if (c instanceof JScrollPane) {
            ((JScrollPane) c).setOpaque(false);
            ((JScrollPane) c).getViewport().setOpaque(false);
        } else if (c instanceof RoundedPanel) {
            if (isLight(c.getBackground())) {
                c.setBackground(new Color(255, 255, 255, 34));
            }
        } else if (c instanceof JPanel) {
            Color bg = c.getBackground();
            if (HEADER_BG.equals(bg)) {
                c.setBackground(DIALOG_HEADER);            // old gray title bar -> solid black
            } else if (((JPanel) c).isOpaque() && isLight(bg)) {
                ((JPanel) c).setOpaque(false);             // let the blue/purple show through
            }
        }
    }

    // Themes a component and everything below it, and keeps theming rows added later
    // (the tenant lists are rebuilt after every approve / decline / remove)
    // controls components and its children
    private static void styleComponentTree(Component c) {
        if (c instanceof javax.swing.text.JTextComponent || c instanceof JComboBox
                || c instanceof JList || c instanceof JScrollBar) {
            return;
        }

        styleComponent(c);
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (jc.getClientProperty("dialogThemeWatch") == null) {
                jc.putClientProperty("dialogThemeWatch", Boolean.TRUE);
                jc.addContainerListener(new ContainerAdapter() {
                    @Override public void componentAdded(ContainerEvent e) {
                        styleComponentTree(e.getChild());
                    }
                });
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                styleComponentTree(child);
            }
        }
    }

    // Content pane that paints a white rounded background, clips its children to the
    // rounded shape and draws a rounded outline on top
    static class RoundedRoot extends JPanel {
        private final Color outline;
        final boolean themed;

        RoundedRoot(Color background, Color outline) {
            this(background, outline, false);
        }

        RoundedRoot(Color background, Color outline, boolean themed) {
            super(new BorderLayout());
            this.outline = outline;
            this.themed = themed;
            setOpaque(false);
            setBackground(background);
        }

        private java.awt.geom.RoundRectangle2D.Float shape(float inset) {
            return new java.awt.geom.RoundRectangle2D.Float(inset, inset,
                getWidth() - 2 * inset, getHeight() - 2 * inset, RADIUS * 2f, RADIUS * 2f);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (themed) {
                // almost solid: blue on one side, purple on the other, with only a short blend between
                float[] stops = {0f, 0.30f, 0.70f, 1f};
                Color[] cols = {DIALOG_BLUE, DIALOG_BLUE, DIALOG_PURPLE, DIALOG_PURPLE};
                g2.setPaint(new LinearGradientPaint(0, 0, Math.max(1, getWidth()), Math.max(1, getHeight()), stops, cols));
            } else {
                g2.setColor(getBackground());
            }
            g2.fill(shape(0));
            g2.dispose();
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.clip(shape(0));
            super.paintChildren(g2);
            g2.dispose();
        }

        @Override public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(outline);
            g2.draw(shape(0.5f));
            g2.dispose();
        }
    }
}
