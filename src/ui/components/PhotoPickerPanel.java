import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

// Modern in-app photo picker: browse folders, see picture thumbnails, tick several, press Add Photos.
// Used instead of the plain system file chooser so it matches the rest of the system.
class PhotoPickerPanel extends JPanel {
    private static final Color PAGE_BG = Color.decode("#32568f");
    private static final int COLUMNS = 5;
    private static File lastDir;

    private final int maxPhotos;
    private final java.util.Set<File> selected = new java.util.LinkedHashSet<>();
    private final JTextField pathField = new JTextField();
    private final JPanel grid = new JPanel(new GridLayout(0, COLUMNS, 10, 10));
    private final JLabel emptyLabel = new JLabel(" ");
    private final JLabel countLabel = new JLabel();
    private final FlatButton upBtn = new FlatButton("Up");
    private final FlatButton addBtn = new FlatButton("Add Photos");
    private final JScrollPane scroll;
    private File currentDir;
    private SwingWorker<Void, PickerTile> loader;

    public PhotoPickerPanel(int maxPhotos, java.util.function.Consumer<java.util.List<File>> onDone, Runnable onCancel) {
        this.maxPhotos = maxPhotos;
        setLayout(new BorderLayout(0, 12));
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(22, 24, 18, 24));
        setPreferredSize(new Dimension(720, 540));

        // ----- Header: title, Up + path, quick places -----
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        
        JLabel title = new JLabel("Select Photos");
        title.setFont(new Font("YU Gothic UI Light", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(12));

        styleButton(upBtn, Color.WHITE, 60, 36, 13, new Color(0x9A, 0x9A, 0x9A), null);
        upBtn.addActionListener(e -> {
            if (currentDir != null && currentDir.getParentFile() != null) {
                navigate(currentDir.getParentFile());
            }
        }); 
        
        pathField.setBackground(InsetFieldBorder.FILL);
        pathField.setForeground(Color.BLACK);
        pathField.setCaretColor(Color.BLACK);
        pathField.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        pathField.setBorder(new InsetFieldBorder());
        pathField.setPreferredSize(new Dimension(100, 36));
        pathField.addActionListener(e -> {
            File typed = new File(pathField.getText().trim());
            if (typed.isDirectory()) {
                navigate(typed);
            } else {
                Toolkit.getDefaultToolkit().beep();
                pathField.setText(currentDir == null ? "" : currentDir.getAbsolutePath());
            }
        });
        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setOpaque(false);
        pathRow.add(upBtn, BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.add(pathRow);
        header.add(Box.createVerticalStrut(10));

        JPanel places = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        places.setOpaque(false);
        File home = new File(System.getProperty("user.home", "."));
        addPlace(places, "Pictures", new File(home, "Pictures"));
        addPlace(places, "Desktop", new File(home, "Desktop"));
        addPlace(places, "Downloads", new File(home, "Downloads"));
        addPlace(places, "Home", home);
        places.setAlignmentX(Component.LEFT_ALIGNMENT);
        places.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.add(places);
        add(header, BorderLayout.NORTH);

        // ----- Grid of folders + pictures -----
        emptyLabel.setFont(new Font("YU Gothic UI Light", Font.PLAIN, 13));
        emptyLabel.setForeground(Color.WHITE);
        emptyLabel.setBorder(new EmptyBorder(0, 2, 10, 0));
        emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyLabel.setVisible(false);
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(emptyLabel);
        stack.add(grid);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(PAGE_BG);
        holder.setBorder(new EmptyBorder(10, 10, 10, 10));
        holder.add(stack, BorderLayout.NORTH);

        scroll = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ModernScrollBarUI.apply(scroll);
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0x9D, 0x9D, 0x9D)));
        add(scroll, BorderLayout.CENTER);

        // ----- Footer: selection count + Cancel / Add Photos -----
        countLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);

        FlatButton cancelBtn = new FlatButton("Cancel");
        styleButton(cancelBtn, new Color(0xDF, 0xDF, 0xDF), 104, 42, 15,
            new Color(0x55, 0x55, 0x55), new Color(0x33, 0x33, 0x33));
        cancelBtn.addActionListener(e -> onCancel.run());
        styleButton(addBtn, Color.WHITE, 124, 42, 15, new Color(0x9A, 0x9A, 0x9A), null);
        addBtn.addActionListener(e -> onDone.accept(new java.util.ArrayList<>(selected)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(addBtn);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(countLabel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        updateFooter(null);
        File start = lastDir;
        if (start == null || !start.isDirectory()) {
            File pictures = new File(home, "Pictures");
            start = pictures.isDirectory() ? pictures : home;
        }
        navigate(start);
    }

    // Stop loading thumbnails when the picker is closed
    public void dispose() {
        if (loader != null) {
            loader.cancel(true);
        }
    }

    private void addPlace(JPanel row, String label, File dir) {
        if (!dir.isDirectory()) {
            return;
        }
        FlatButton b = new FlatButton(label);
        styleButton(b, new Color(0xD6, 0xD6, 0xD6), 84, 28, 12, new Color(0xA0, 0xA0, 0xA0), null);
        b.addActionListener(e -> navigate(dir));
        row.add(b);
    }

    private static void styleButton(FlatButton b, Color bg, int w, int h, int fontSize, Color shadow, Color outline) {
        b.setFont(new Font("Arial", Font.PLAIN, fontSize));
        b.setForeground(Color.BLACK);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(new DropShadowBorder(shadow, outline));
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static boolean isPicture(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
            || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    private void updateFooter(String message) {
        countLabel.setText(message != null ? message
            : (maxPhotos == Integer.MAX_VALUE ? selected.size() + " selected"
                                              : selected.size() + " of " + maxPhotos + " selected"));
        addBtn.setEnabled(!selected.isEmpty());
    }

    private void onTileClicked(PickerTile tile) {
        if (tile.folder) {
            navigate(tile.file);
            return;
        }
        if (selected.contains(tile.file)) {
            selected.remove(tile.file);
            tile.setSelected(false);
            updateFooter(null);
        } else if (selected.size() >= maxPhotos) {
            updateFooter("You can add up to " + maxPhotos + (maxPhotos == 1 ? " photo." : " photos."));
        } else {
            selected.add(tile.file);
            tile.setSelected(true);
            updateFooter(null);
        }
    }

    private void navigate(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        if (loader != null) {
            loader.cancel(true);
        }
        currentDir = dir;
        lastDir = dir;
        pathField.setText(dir.getAbsolutePath());
        upBtn.setEnabled(dir.getParentFile() != null);
        grid.removeAll();

        File[] items = dir.listFiles();
        java.util.List<File> folders = new java.util.ArrayList<>();
        java.util.List<File> pictures = new java.util.ArrayList<>();
        if (items != null) {
            for (File f : items) {
                if (f.isHidden() || f.getName().startsWith(".")) {
                    continue;
                }
                if (f.isDirectory()) {
                    folders.add(f);
                } else if (isPicture(f)) {
                    pictures.add(f);
                }
            }
        }
        java.util.Comparator<File> byName = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
        folders.sort(byName);
        pictures.sort(byName);

        java.util.List<PickerTile> pictureTiles = new java.util.ArrayList<>();
        for (File f : folders) {
            grid.add(new PickerTile(f, true, this::onTileClicked));
        }
        for (File f : pictures) {
            PickerTile t = new PickerTile(f, false, this::onTileClicked);
            t.setSelected(selected.contains(f));
            pictureTiles.add(t);
            grid.add(t);
        }
        if (items == null) {
            emptyLabel.setText("This folder can't be opened.");
        } else if (folders.isEmpty() && pictures.isEmpty()) {
            emptyLabel.setText("No folders or pictures here.");
        }
        emptyLabel.setVisible(items == null || (folders.isEmpty() && pictures.isEmpty()));
        grid.revalidate();
        grid.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));

        // Load thumbnails in the background so big folders do not freeze the window
        loader = new SwingWorker<Void, PickerTile>() {
            @Override
            protected Void doInBackground() {
                for (PickerTile t : pictureTiles) {
                    if (isCancelled()) {
                        break;
                    }
                    java.awt.image.BufferedImage img = readThumb(t.file, PickerTile.W);
                    if (img != null) {
                        t.setThumb(img);
                    } else {
                        t.markBroken();
                    }
                    publish(t);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<PickerTile> done) {
                for (PickerTile t : done) {
                    t.repaint();
                }
            }
        };
        loader.execute();
    }

    // Reads a reduced-size copy of a picture (sub-sampled while reading, so large photos load quickly)
    static java.awt.image.BufferedImage readThumb(File f, int target) {
        try (javax.imageio.stream.ImageInputStream in = javax.imageio.ImageIO.createImageInputStream(f)) {
            if (in == null) {
                return null;
            }
            java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                return null;
            }
            javax.imageio.ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
                int sub = Math.max(1, Math.min(w, h) / (target * 2));
                param.setSourceSubsampling(sub, sub, 0, 0);
                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        } catch (Exception | OutOfMemoryError ex) {
            return null;
        }
    }
}
