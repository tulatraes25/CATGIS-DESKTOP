package ar.com.catgis;

public final class PostgisDataSourceAction {

    private PostgisDataSourceAction() {
    }

    public static void openPostgisBrowser() {
        PostgisBrowserDialog.open(CatgisDesktopApp.getMainFrameSafe());
    }

    public static void openCatserverBrowser() {
        PostgisBrowserDialog.openCatserver(CatgisDesktopApp.getMainFrameSafe());
    }

    public static void exportSelectedLayerToPostgis() {
        Layer layer = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (layer == null) {
            javax.swing.JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("Selecciona una capa vectorial para enviar a CATSERVER."));
            return;
        }
        PostgisExportDialog.open(CatgisDesktopApp.getMainFrameSafe(), layer);
    }

    public static void exportLayerToPostgis(Layer layer) {
        PostgisExportDialog.open(CatgisDesktopApp.getMainFrameSafe(), layer);
    }

    public static void savePostgisLayerChanges(PostgisLayer layer) {
        if (layer == null || CatgisDesktopApp.mapPanel == null) {
            return;
        }
        ShapefileData data = CatgisDesktopApp.mapPanel.getShapefileData(layer);
        if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
            javax.swing.JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), "La capa no tiene datos disponibles para guardar en PostGIS.");
            return;
        }
        PostgisWriteService.saveLayerToCurrentPath(layer, data, CatgisDesktopApp.getMainFrameSafe(), true);
    }
}
