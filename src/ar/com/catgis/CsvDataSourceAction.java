package ar.com.catgis;

public final class CsvDataSourceAction {

    private CsvDataSourceAction() {
    }

    public static void openCsvDataSource() {
        openTableDataSource();
    }

    public static void openTableDataSource() {
        OpenTablePointsAction.openTablePoints();
    }
}
