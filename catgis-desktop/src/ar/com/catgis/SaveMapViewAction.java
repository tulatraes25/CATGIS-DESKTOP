package ar.com.catgis;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class SaveMapViewAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        saveCurrentView();
    }

    public static void saveCurrentView() {
        if (AppContext.mapPanel() == null) {
            NotificationManager.warn(null, null, "No hay mapa disponible para exportar.");
            return;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("export-image", "Salvar vista del mapa");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));

        int result = chooser.showSaveDialog(CatgisDesktopApp.getMainFrameSafe());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }
        FileChooserSupport.rememberSelection("export-image", chooser);

        String lower = file.getName().toLowerCase();
        String format = "png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            format = "jpg";
        } else if (!lower.endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
            format = "png";
        }

        try {
            int width = Math.max(AppContext.mapPanel().getWidth(), 1200);
            int height = Math.max(AppContext.mapPanel().getHeight(), 800);

            BufferedImage image = new BufferedImage(width, height,
                    "jpg".equalsIgnoreCase(format) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = image.createGraphics();
            if ("jpg".equalsIgnoreCase(format)) {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, width, height);
            }
            AppContext.mapPanel().paint(g2);
            g2.dispose();

            ImageIO.write(image, format, file);

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Vista guardada: " + file.getName());
            }

            NotificationManager.info(CatgisDesktopApp.getMainFrameSafe(),
                    "Salvar vista del mapa",
                    "Vista guardada correctamente:\n" + file.getAbsolutePath());

        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al guardar la vista del mapa en " + file.getAbsolutePath(), ex);
            AppErrorSupport.showErrorDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Salvar vista del mapa",
                    "Error al guardar la vista del mapa.",
                    ex
            );
        }
    }
}
