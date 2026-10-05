package ar.com.catgis;

public enum PostgisTableWriteMode {
    CREATE_NEW("Crear nueva tabla", true),
    REPLACE_TABLE("Reemplazar tabla existente", true),
    APPEND_RECORDS("Anexar registros", true),
    OVERWRITE_CONTENT("Sobrescribir contenido actual", false);

    private final String label;
    private final boolean visibleInExportDialog;

    PostgisTableWriteMode(String label, boolean visibleInExportDialog) {
        this.label = label;
        this.visibleInExportDialog = visibleInExportDialog;
    }

    public boolean isVisibleInExportDialog() {
        return visibleInExportDialog;
    }

    @Override
    public String toString() {
        return label;
    }
}
