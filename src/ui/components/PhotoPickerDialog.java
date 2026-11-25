import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.color.*;

// Shows the photo picker in a modal window and returns the pictures the user ticked (empty if cancelled)
class PhotoPickerDialog {
    static java.util.List<File> choose(Window owner, int maxPhotos) {
        JDialog d = new JDialog(owner, "Select Photos", Dialog.ModalityType.APPLICATION_MODAL);
        java.util.List<File> result = new java.util.ArrayList<>();
        PhotoPickerPanel panel = new PhotoPickerPanel(maxPhotos, files -> {
            result.addAll(files);
            d.dispose();
        }, d::dispose);
        d.setContentPane(panel);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(owner);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setVisible(true);
        panel.dispose();
        return result;
    }
}
