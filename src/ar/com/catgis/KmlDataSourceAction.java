package ar.com.catgis;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public final class KmlDataSourceAction {

    private KmlDataSourceAction() {
    }

    public static void openKmlDataSource() {
        JFileChooser chooser = FileChooserSupport.createChooser("open-layer-data", "Origen de datos KML");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));

        int result = chooser.showOpenDialog(CatgisDesktopApp.getMainFrameSafe());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        FileChooserSupport.rememberSelection("open-layer-data", chooser);
        if (file == null) {
            return;
        }

        OpenFileAction.openSelectedFile(file, "KML", CatgisDesktopApp.getMainFrameSafe());
    }
}
