package ar.com.catgis;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public final class CsvDataSourceAction {

    private CsvDataSourceAction() {
    }

    public static void openCsvDataSource() {
        JFileChooser chooser = FileChooserSupport.createChooser("table-open", "Origen de datos CSV");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));

        int result = chooser.showOpenDialog(CatgisDesktopApp.getMainFrameSafe());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        FileChooserSupport.rememberSelection("table-open", chooser);
        if (file == null) {
            return;
        }

        try {
            TablePointData data = CsvTableReader.read(file);
            CsvDataSourceDialog.open(file, data);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), "Error al abrir CSV: " + ex.getMessage(), "Origen de datos CSV", JOptionPane.ERROR_MESSAGE);
        }
    }
}
