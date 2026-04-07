package ar.com.catgis;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

public class DemLocalLoadAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        openDialog();
    }

    public static void openDialog() {
        openDialog(CatgisDesktopApp.getMainFrameSafe());
    }

    public static void openDialog(Component parent) {
        JFileChooser chooser = FileChooserSupport.createChooser("open-dem-data", I18n.t("Cargar datos DEM"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                I18n.t("Modelos de elevacion compatibles (*.tif, *.tiff, *.img, *.asc)"),
                "tif", "tiff", "img", "asc"
        ));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                I18n.t("GeoTIFF / TIFF (*.tif, *.tiff)"),
                "tif", "tiff"
        ));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                I18n.t("ERDAS IMG (*.img)"),
                "img"
        ));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                I18n.t("Arc/Info ASCII Grid (*.asc)"),
                "asc"
        ));

        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        if (files == null || files.length == 0) {
            File file = chooser.getSelectedFile();
            files = file != null ? new File[]{file} : new File[0];
        }
        if (files.length == 0) {
            return;
        }

        FileChooserSupport.rememberSelection("open-dem-data", chooser);
        for (File file : files) {
            OpenFileAction.openDemRasterFile(file, parent);
        }
    }
}
