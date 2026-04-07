package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TopographyWorkflowSupport {

    private TopographyWorkflowSupport() {
    }

    public static List<Layer> getAvailableRasterLayers() {
        List<Layer> rasters = new ArrayList<>();
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.currentProject.getLayers() == null) {
            return rasters;
        }
        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
            if (isRasterLayer(layer)) {
                rasters.add(layer);
            }
        }
        return rasters;
    }

    public static Layer resolvePreferredRasterLayer() {
        Layer selected = CatgisDesktopApp.layersPanel != null ? CatgisDesktopApp.layersPanel.getSelectedLayer() : null;
        if (isRasterLayer(selected)) {
            return selected;
        }
        List<Layer> rasters = getAvailableRasterLayers();
        for (Layer layer : rasters) {
            if (isDemLikeRaster(layer)) {
                return layer;
            }
        }
        return rasters.isEmpty() ? null : rasters.get(0);
    }

    public static SelectedProfileLine resolveSelectedProfileLine() {
        MapPanel mapPanel = CatgisDesktopApp.mapPanel;
        if (mapPanel == null) {
            return null;
        }

        Layer layer = mapPanel.getSelectedLayerRef();
        SimpleFeature feature = mapPanel.getSelectedFeatureRef();
        if (layer == null || feature == null) {
            return null;
        }

        Object geometryObject = feature.getDefaultGeometry();
        if (!(geometryObject instanceof Geometry geometry)) {
            return null;
        }
        if (!(geometry instanceof LineString) && !(geometry instanceof MultiLineString)) {
            return null;
        }

        String sourceCrs = layer.getSourceCRS();
        if (sourceCrs == null || sourceCrs.isBlank()) {
            sourceCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        }
        String description = layer.getName() + " | " + feature.getID();
        return new SelectedProfileLine((Geometry) geometry.copy(), sourceCrs, description);
    }

    public static void showNoRasterMessage() {
        JOptionPane.showMessageDialog(
                CatgisDesktopApp.getMainFrameSafe(),
                I18n.t("No hay capas raster disponibles para trabajar con DEM, curvas o perfiles.")
        );
    }

    private static boolean isRasterLayer(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (layer instanceof RasterLayer) {
            return true;
        }
        String type = layer.getType() != null ? layer.getType().trim().toUpperCase(Locale.ROOT) : "";
        return type.contains("RASTER") || type.contains("IMAGE") || type.contains("IMAGEN");
    }

    public static boolean isDemLikeRaster(Layer layer) {
        if (!isRasterLayer(layer)) {
            return false;
        }
        String sourceName = layer.getSourceName() != null ? layer.getSourceName().toLowerCase(Locale.ROOT) : "";
        String layerName = layer.getName() != null ? layer.getName().toLowerCase(Locale.ROOT) : "";
        String path = layer.getPath() != null ? layer.getPath().toLowerCase(Locale.ROOT) : "";
        return sourceName.contains("dem")
                || layerName.contains("dem")
                || sourceName.contains("terrain")
                || sourceName.contains("elevacion")
                || sourceName.contains("elevation")
                || path.endsWith(".asc");
    }

    public record SelectedProfileLine(Geometry geometry, String sourceCrs, String description) {
    }
}
