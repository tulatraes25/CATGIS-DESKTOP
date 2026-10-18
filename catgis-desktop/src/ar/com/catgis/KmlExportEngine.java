package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import org.geotools.api.feature.simple.SimpleFeature;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Legacy KML menu surface.
 *
 * <p>The actual writer is intentionally centralized in
 * {@link ExportVectorLayerAction}, which owns CRS-to-WGS84 conversion,
 * staging, reopen validation and atomic single-file publication.</p>
 */
public final class KmlExportEngine {

    private KmlExportEngine() {}

    public static void exportLayerWithDialog(Component parent, Layer layer) {
        if (layer == null) {
            return;
        }
        ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            NotificationManager.warn(parent, "KML Export", "La capa no tiene entidades para exportar.");
            return;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("vector-export", "Exportar KML");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("KML (*.kml)", "kml"));
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                "vector-export",
                new File(layer.getName().replaceAll("[^A-Za-z0-9]+", "_") + ".kml")
        ));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".kml")) {
            file = new File(file.getAbsolutePath() + ".kml");
        }
        if (file.exists() && !NotificationManager.confirm(
                parent,
                "KML Export",
                "El archivo ya existe.\n¿Querés reemplazarlo?\n\n" + file.getAbsolutePath())) {
            return;
        }

        if (exportLayerToFile(file, layer, data, parent)) {
            FileChooserSupport.rememberFile("vector-export", file);
            NotificationManager.info(
                    parent, "KML Export",
                    "Exportado a KML correctamente:\n" + file.getAbsolutePath());
        }
    }

    static boolean exportLayerToFile(
            File file, Layer layer, ShapefileData data, Component parent) {
        return ExportVectorLayerAction.saveLayerDataToFile(
                layer, data, file, parent, false);
    }

    /**
     * Unsafe legacy API retained only to fail closed.
     *
     * <p>A list of features has no source-CRS contract, therefore CATGIS cannot
     * prove the coordinates are WGS84 as required by KML. Callers must export
     * a Layer + ShapefileData through {@link #exportLayerToFile} instead.</p>
     */
    @Deprecated
    public static void exportToKml(
            File file, List<SimpleFeature> features, String layerName) throws IOException {
        throw new IOException(
                "Exportacion KML sin CRS deshabilitada: use una capa CATGIS con CRS de origen.");
    }
}
