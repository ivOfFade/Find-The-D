import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Small dropdown (label + chevron) in the One UI style that opens a rounded list of choices underneath it.
// onSelect receives the index of the chosen item. Colours are passed in so they are easy to edit.
class CompactDropdown extends JPanel {
    private final JLabel textLabel = new JLabel();
    private final OneUiPopup popup = new OneUiPopup();
    private final OneUiChevron chevron;
    private final Color fill;
    private final Color line;
    private long closedAt = 0;
    private boolean hovering;

    CompactDropdown(String[] items, int width, int height, Color bg, Color border, Color text, Color hover,
                    java.util.function.IntConsumer onSelect) {
        super(new BorderLayout());
        this.fill = bg;
        this.line = new Color(border.getRed(), border.getGreen(), border.getBlue(), 110);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 14, 0, 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        
        textLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        textLabel.setForeground(text);
        textLabel.setText(items.length > 0 ? items[0] : "");
        add(textLabel, BorderLayout.CENTER);

        chevron = new OneUiChevron(text);
        chevron.setPreferredSize(new Dimension(26, height));
        add(chevron, BorderLayout.EAST);

        java.util.List<OneUiItem> rows = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            final int index = i;
            OneUiItem row = new OneUiItem(items[i], hover, () -> {
                textLabel.setText(items[index]);
                for (int r = 0; r < rows.size(); r++) {
                    rows.get(r).setChosen(r == index);
                }
                popup.setVisible(false);
                onSelect.accept(index);
            });
            row.setChosen(i == 0);
            rows.add(row);
            popup.add(row);
        }
        // clicking the field while the list is open should just close it
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                chevron.setUp(true);
                repaint();
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                closedAt = System.currentTimeMillis();
                chevron.setUp(false);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (System.currentTimeMillis() - closedAt < 250) {
                    return;
                }
                popup.showUnder(CompactDropdown.this);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = OneUi.smooth(g);
        int arc = Math.min(OneUi.FIELD_ARC, getHeight());
        if (hovering) {
            g2.setColor(new Color(Math.max(0, fill.getRed() - 10), Math.max(0, fill.getGreen() - 10), Math.max(0, fill.getBlue() - 10)));
        } else {
            g2.setColor(fill);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setColor(line);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
