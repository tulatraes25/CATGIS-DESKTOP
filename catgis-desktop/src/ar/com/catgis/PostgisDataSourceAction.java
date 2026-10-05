package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

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
        Layer layer = AppContext.getSelectedLayer();
        if (layer == null) {
            NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null, I18n.t("Selecciona una capa vectorial para enviar a CATSERVER."));
            return;
        }
        PostgisExportDialog.open(CatgisDesktopApp.getMainFrameSafe(), layer);
    }

    public static void exportLayerToPostgis(Layer layer) {
        PostgisExportDialog.open(CatgisDesktopApp.getMainFrameSafe(), layer);
    }

    public static void savePostgisLayerChanges(PostgisLayer layer) {
        if (layer == null || AppContext.mapPanel() == null) {
            return;
        }
        ShapefileData data = AppContext.mapPanel().getShapefileData(layer);
        if (!ExportVectorLayerAction.hasExportableVectorData(data)) {
            NotificationManager.warn(CatgisDesktopApp.getMainFrameSafe(), null, "La capa no tiene datos disponibles para guardar en PostGIS.");
            return;
        }
        PostgisWriteService.saveLayerToCurrentPath(layer, data, CatgisDesktopApp.getMainFrameSafe(), true);
    }
}
