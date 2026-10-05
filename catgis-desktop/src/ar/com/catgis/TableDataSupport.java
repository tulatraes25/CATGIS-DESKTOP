package ar.com.catgis;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.util.List;

public final class TableDataSupport {

    private TableDataSupport() {
    }

    public static TablePointData read(File file, Component parent) throws Exception {
        if (file == null) {
            throw new RuntimeException("No se selecciono ningun archivo tabular.");
        }
        String lower = file.getName().toLowerCase();
        if (lower.endsWith(".csv")) {
            return CsvTableReader.read(file);
        }
        if (lower.endsWith(".xlsx")) {
            Integer sheetIndex = chooseSheet(parent, "Seleccione la hoja de Excel", XlsxTableReader.getSheetNames(file));
            return sheetIndex != null ? XlsxTableReader.read(file, sheetIndex) : null;
        }
        if (lower.endsWith(".xls")) {
            Integer sheetIndex = chooseSheet(parent, "Seleccione la hoja de Excel XLS", XlsTableReader.getSheetNames(file));
            return sheetIndex != null ? XlsTableReader.read(file, sheetIndex) : null;
        }
        if (lower.endsWith(".ods")) {
            Integer sheetIndex = chooseSheet(parent, "Seleccione la hoja ODS", OdsTableReader.getSheetNames(file));
            return sheetIndex != null ? OdsTableReader.read(file, sheetIndex) : null;
        }
        if (lower.endsWith(".dbf")) {
            return DbfTableReader.read(file);
        }
        throw new RuntimeException("Formato tabular no soportado. Usa CSV, XLSX, XLS, DBF u ODS.");
    }

    public static boolean isSupportedTable(File file) {
        if (file == null) {
            return false;
        }
        String lower = file.getName().toLowerCase();
        return lower.endsWith(".csv")
                || lower.endsWith(".xlsx")
                || lower.endsWith(".xls")
                || lower.endsWith(".ods")
                || lower.endsWith(".dbf");
    }

    private static Integer chooseSheet(Component parent, String title, List<String> sheetNames) {
        if (sheetNames == null || sheetNames.isEmpty()) {
            throw new RuntimeException("No se encontraron hojas disponibles.");
        }
        if (sheetNames.size() == 1) {
            return 0;
        }

        Object selected = JOptionPane.showInputDialog(
                parent,
                "Seleccione la hoja a importar:",
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                sheetNames.toArray(),
                sheetNames.get(0)
        );

        if (selected == null) {
            return null;
        }

        String selectedName = selected.toString();
        for (int i = 0; i < sheetNames.size(); i++) {
            if (sheetNames.get(i).equals(selectedName)) {
                return i;
            }
        }
        return 0;
    }
}
