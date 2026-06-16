package ar.com.catgis;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;

public class DbfTableReader {

    public static TablePointData read(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo DBF.");
        }

        TablePointData data = new TablePointData();
        try (FileInputStream fis = new FileInputStream(file);
             DBFReader reader = new DBFReader(fis)) {

            int fieldCount = reader.getFieldCount();
            if (fieldCount <= 0) {
                throw new RuntimeException("El DBF no contiene campos.");
            }

            for (int i = 0; i < fieldCount; i++) {
                DBFField field = reader.getField(i);
                String name = field != null ? safe(field.getName()) : "";
                if (name.isBlank()) {
                    name = "COL_" + (i + 1);
                }
                data.addColumn(name);
            }

            Object[] rowValues;
            while ((rowValues = reader.nextRecord()) != null) {
                LinkedHashMap<String, String> row = new LinkedHashMap<>();
                boolean hasContent = false;

                List<String> cols = data.getColumns();
                for (int i = 0; i < cols.size(); i++) {
                    Object value = i < rowValues.length ? rowValues[i] : null;
                    String text = value != null ? String.valueOf(value).trim() : "";
                    if (!text.isBlank()) {
                        hasContent = true;
                    }
                    row.put(cols.get(i), text);
                }

                if (hasContent) {
                    data.addRow(row);
                }
            }

        }

        return data;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}