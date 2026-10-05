package ar.com.catgis;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class XlsTableReader {

    public static TablePointData read(File file) throws Exception {
        return read(file, 0);
    }

    public static List<String> getSheetNames(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo XLS.");
        }

        List<String> sheetNames = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {

            int total = workbook.getNumberOfSheets();
            for (int i = 0; i < total; i++) {
                String name = workbook.getSheetName(i);
                if (name == null || name.isBlank()) {
                    name = "Hoja " + (i + 1);
                }
                sheetNames.add(name);
            }
        }

        return sheetNames;
    }

    public static TablePointData read(File file, int sheetIndex) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo XLS.");
        }

        TablePointData data = new TablePointData();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {

            if (workbook.getNumberOfSheets() <= 0) {
                throw new RuntimeException("No se encontró ninguna hoja en el archivo XLS.");
            }

            if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
                throw new RuntimeException("Índice de hoja XLS inválido.");
            }

            HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) {
                throw new RuntimeException("No se encontró la hoja seleccionada en el archivo XLS.");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new RuntimeException("La hoja XLS está vacía.");
            }

            int lastCell = headerRow.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String header = formatter.formatCellValue(cell).trim();
                if (header.isBlank()) {
                    header = "COL_" + (i + 1);
                }
                data.addColumn(header);
            }

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row rowObj = sheet.getRow(r);
                if (rowObj == null) {
                    continue;
                }

                LinkedHashMap<String, String> row = new LinkedHashMap<>();
                boolean hasContent = false;

                List<String> cols = data.getColumns();
                for (int c = 0; c < cols.size(); c++) {
                    Cell cell = rowObj.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = formatter.formatCellValue(cell).trim();
                    if (!value.isBlank()) {
                        hasContent = true;
                    }
                    row.put(cols.get(c), value);
                }

                if (hasContent) {
                    data.addRow(row);
                }
            }
        }

        return data;
    }
}