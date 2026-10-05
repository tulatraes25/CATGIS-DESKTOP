package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TablePointData {

    private final List<String> columns = new ArrayList<>();
    private final List<Map<String, String>> rows = new ArrayList<>();

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void addColumn(String column) {
        if (column != null && !column.isBlank() && !columns.contains(column)) {
            columns.add(column);
        }
    }

    public void addRow(Map<String, String> row) {
        if (row != null) {
            rows.add(row);
        }
    }

    public boolean isEmpty() {
        return rows.isEmpty() || columns.isEmpty();
    }
}