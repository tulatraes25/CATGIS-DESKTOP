package ar.com.catgis;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.List;

public class OpenTablePointsAction {

    public static void openTablePoints() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir tabla de coordenadas");
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

        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }

        try {
            String lower = file.getName().toLowerCase();
            TablePointData data;

            if (lower.endsWith(".csv")) {
                data = CsvTableReader.read(file);

            } else if (lower.endsWith(".xlsx")) {
                List<String> sheetNames = XlsxTableReader.getSheetNames(file);
                int selectedIndex = chooseSheet("Seleccione la hoja de Excel", sheetNames);
                if (selectedIndex < 0) {
                    return;
                }
                data = XlsxTableReader.read(file, selectedIndex);

            } else if (lower.endsWith(".xls")) {
                List<String> sheetNames = XlsTableReader.getSheetNames(file);
                int selectedIndex = chooseSheet("Seleccione la hoja de Excel XLS", sheetNames);
                if (selectedIndex < 0) {
                    return;
                }
                data = XlsTableReader.read(file, selectedIndex);

            } else if (lower.endsWith(".dbf")) {
                data = DbfTableReader.read(file);

            } else if (lower.endsWith(".ods")) {
                List<String> sheetNames = OdsTableReader.getSheetNames(file);
                int selectedIndex = chooseSheet("Seleccione la hoja ODS", sheetNames);
                if (selectedIndex < 0) {
                    return;
                }
                data = OdsTableReader.read(file, selectedIndex);

            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Formato no soportado todavía.\nUse CSV, XLSX, XLS, DBF u ODS."
                );
                return;
            }

            TablePointImportDialog.open(file, data);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al abrir tabla: " + ex.getMessage());
        }
    }

    private static int chooseSheet(String title, List<String> sheetNames) {
        if (sheetNames == null || sheetNames.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron hojas disponibles.");
            return -1;
        }

        Object selected = JOptionPane.showInputDialog(
                null,
                "Seleccione la hoja a importar:",
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                sheetNames.toArray(),
                sheetNames.get(0)
        );

        if (selected == null) {
            return -1;
        }

        String selectedName = selected.toString();
        for (int i = 0; i < sheetNames.size(); i++) {
            if (sheetNames.get(i).equals(selectedName)) {
                return i;
            }
        }

        return -1;
    }
}