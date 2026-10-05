package ar.com.catgis;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class OdsTableReader {

    public static TablePointData read(File file) throws Exception {
        return read(file, 0);
    }

    public static List<String> getSheetNames(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo ODS.");
        }

        List<String> sheetNames = new ArrayList<>();

        SpreadSheet spreadSheet = new SpreadSheet(file);
        List<Sheet> sheets = spreadSheet.getSheets();

        if (sheets == null || sheets.isEmpty()) {
            throw new RuntimeException("El ODS no contiene hojas.");
        }

        for (int i = 0; i < sheets.size(); i++) {
            Sheet sheet = sheets.get(i);
            String name = sheet.getName();
            if (name == null || name.isBlank()) {
                name = "Hoja " + (i + 1);
            }
            sheetNames.add(name);
        }

        return sheetNames;
    }

    public static TablePointData read(File file, int sheetIndex) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo ODS.");
        }

        TablePointData data = new TablePointData();

        SpreadSheet spreadSheet = new SpreadSheet(file);
        List<Sheet> sheets = spreadSheet.getSheets();

        if (sheets == null || sheets.isEmpty()) {
            throw new RuntimeException("El ODS no contiene hojas.");
        }

        if (sheetIndex < 0 || sheetIndex >= sheets.size()) {
            throw new RuntimeException("Índice de hoja ODS inválido.");
        }

        Sheet sheet = sheets.get(sheetIndex);
        Range range = sheet.getDataRange();

        int numRows = range.getNumRows();
        int numCols = range.getNumColumns();

        if (numRows <= 0 || numCols <= 0) {
            throw new RuntimeException("La hoja ODS está vacía.");
        }

        Object[][] values = range.getValues();

        for (int c = 0; c < numCols; c++) {
            String header = "";
            if (values[0][c] != null) {
                header = String.valueOf(values[0][c]).trim();
            }

            if (header.isBlank()) {
                header = "COL_" + (c + 1);
            }

            data.addColumn(header);
        }

        for (int r = 1; r < numRows; r++) {
            LinkedHashMap<String, String> row = new LinkedHashMap<>();
            boolean hasContent = false;

            List<String> cols = data.getColumns();
            for (int c = 0; c < cols.size(); c++) {
                Object value = values[r][c];
                String text = value != null ? String.valueOf(value).trim() : "";
                if (!text.isBlank()) {
                    hasContent = true;
                }
                row.put(cols.get(c), text);
            }

            if (hasContent) {
                data.addRow(row);
            }
        }

        return data;
    }
}