package ar.com.catgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.List;

public class CsvTableReader {

    public static TablePointData read(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new RuntimeException("No existe el archivo CSV.");
        }

        TablePointData data = new TablePointData();

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new RuntimeException("El CSV está vacío.");
            }

            String delimiter = detectDelimiter(headerLine);
            String[] headers = splitCsv(headerLine, delimiter);

            for (int i = 0; i < headers.length; i++) {
                headers[i] = clean(headers[i]);
                if (headers[i].isBlank()) {
                    headers[i] = "COL_" + (i + 1);
                }
                data.addColumn(headers[i]);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = splitCsv(line, delimiter);
                LinkedHashMap<String, String> row = new LinkedHashMap<>();

                List<String> cols = data.getColumns();
                for (int i = 0; i < cols.size(); i++) {
                    String value = i < values.length ? clean(values[i]) : "";
                    row.put(cols.get(i), value);
                }

                data.addRow(row);
            }
        }

        return data;
    }

    private static String detectDelimiter(String line) {
        int commas = count(line, ',');
        int semicolons = count(line, ';');
        int tabs = count(line, '\t');

        if (semicolons >= commas && semicolons >= tabs) {
            return ";";
        }
        if (tabs >= commas && tabs >= semicolons) {
            return "\t";
        }
        return ",";
    }

    private static int count(String text, char c) {
        int total = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                total++;
            }
        }
        return total;
    }

    private static String[] splitCsv(String line, String delimiter) {
        return line.split(java.util.regex.Pattern.quote(delimiter), -1);
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1);
        }
        return v.trim();
    }
}