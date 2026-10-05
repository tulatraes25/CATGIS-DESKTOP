package ar.com.catgis;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class OpenTablePointsAction {

    public static void openTablePoints() {
        JFileChooser chooser = FileChooserSupport.createChooser("table-open", "Cargar tabla externa");
        chooser.setAcceptAllFileFilterUsed(true);

        FileNameExtensionFilter allTables = new FileNameExtensionFilter(
                "Tablas soportadas (*.csv, *.xlsx, *.xls, *.dbf, *.ods)",
                "csv", "xlsx", "xls", "dbf", "ods"
        );
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV (*.csv)", "csv");
        FileNameExtensionFilter xlsxFilter = new FileNameExtensionFilter("Excel XLSX (*.xlsx)", "xlsx");
        FileNameExtensionFilter xlsFilter = new FileNameExtensionFilter("Excel XLS (*.xls)", "xls");
        FileNameExtensionFilter dbfFilter = new FileNameExtensionFilter("DBF (*.dbf)", "dbf");
        FileNameExtensionFilter odsFilter = new FileNameExtensionFilter("ODS (*.ods)", "ods");

        chooser.addChoosableFileFilter(allTables);
        chooser.addChoosableFileFilter(csvFilter);
        chooser.addChoosableFileFilter(xlsxFilter);
        chooser.addChoosableFileFilter(xlsFilter);
        chooser.addChoosableFileFilter(dbfFilter);
        chooser.addChoosableFileFilter(odsFilter);
        chooser.setFileFilter(allTables);

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
            TablePointData data = TableDataSupport.read(file, CatgisDesktopApp.getMainFrameSafe());
            if (data == null) {
                return;
            }
            CsvDataSourceDialog.open(file, data);
        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al abrir tabla externa " + file.getAbsolutePath(), ex);
            AppErrorSupport.showErrorDialog(
                    CatgisDesktopApp.getMainFrameSafe(),
                    "Tabla externa",
                    "Error al abrir tabla.",
                    ex
            );
        }
    }
}
