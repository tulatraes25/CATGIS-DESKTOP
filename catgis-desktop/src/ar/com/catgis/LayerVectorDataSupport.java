package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Method;

final class LayerVectorDataSupport {

    private LayerVectorDataSupport() {
    }

    static ShapefileData ensureDataLoaded(Layer layer,
                                          Component owner,
                                          String postgisPasswordPrompt,
                                          boolean promptForCadReference) throws Exception {
        if (layer == null) {
            return null;
        }

        ShapefileData cached = CatgisDesktopApp.mapPanel != null
                ? CatgisDesktopApp.mapPanel.getShapefileData(layer)
                : null;
        if (cached != null) {
            return cached;
        }

        ShapefileData data;
        if (layer instanceof PostgisLayer postgisLayer) {
            PostgisConnectionInfo info = PostgisConnectionStore.applyStoredPassword(postgisLayer.toConnectionInfo());
            if (info == null || info.getPassword().isBlank()) {
                info = PostgisConnectionStore.promptForPassword(
                        owner,
                        postgisLayer.toConnectionInfo(),
                        postgisPasswordPrompt
                );
                if (info == null) {
                    return null;
                }
            }
            data = PostgisLoader.loadLayerData(postgisLayer, info);
            return projectAndAttach(layer, data);
        }

        if (layer instanceof GeoPackageLayer geoPackageLayer) {
            data = GeoPackageLoader.loadLayerData(geoPackageLayer);
            return projectAndAttach(layer, data);
        }

        if (layer instanceof OnlineWfsLayer onlineWfsLayer) {
            data = WfsFeatureLoader.loadLayerData(onlineWfsLayer);
            return projectAndAttach(layer, data);
        }

        if (layer instanceof GpxLayer gpxLayer) {
            String path = layer.getPath();
            if (path == null || path.isBlank()) {
                return null;
            }
            data = GpxLoader.load(new File(path), gpxLayer.getContentKind());
            return projectAndAttach(layer, data);
        }

        if (layer instanceof SpatiaLiteLayer slLayer) {
            try {
                data = SpatiaLiteLoader.loadLayerData(slLayer);
            } catch (Exception e) {
                AppErrorSupport.logFailure("No se pudo cargar la capa SpatiaLite: " + slLayer.getTableName(), e);
                return null;
            }
            return projectAndAttach(layer, data);
        }

        String path = layer.getPath() != null ? layer.getPath().trim() : "";
        if (path.isBlank()) {
            return null;
        }

        if (TopographyWorkflowSupport.isTransientTopographyVector(layer)
                && !ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
            data = TopographyWorkflowSupport.tryRestoreTransientTopographyVectorLayer(layer);
            return projectAndAttach(layer, data);
        }

        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".shp")) {
            data = invokeLoader(ShapefileLoader.class, path,
                    new String[]{"load", "loadShapefile", "open", "openShapefile", "read", "readShapefile"});
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS(ShapefileLoader.getCRSCode(new File(path)));
            }
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".dxf")) {
            data = DxfLoader.load(new File(path));
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".dwg")) {
            DwgImportSupport.ResolvedCadReference resolvedCad = DwgImportSupport.resolveDwgReference(
                    new File(path),
                    owner,
                    promptForCadReference
            );
            if (resolvedCad == null || resolvedCad.dxfFile() == null) {
                return null;
            }
            layer.setSourceName(resolvedCad.resolutionMessage());
            data = DxfLoader.load(resolvedCad.dxfFile());
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".geojson") || lowerPath.endsWith(".json")) {
            data = invokeLoader(GeoJsonLoader.class, path,
                    new String[]{"load", "loadGeoJson", "open", "read"});
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS("EPSG:4326");
            }
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".kml") || lowerPath.endsWith(".kmz")) {
            data = invokeLoader(KmlLoader.class, path,
                    new String[]{"load", "loadKml", "loadKmz", "open", "read"});
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS("EPSG:4326");
            }
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".fgb")) {
            data = FlatGeobufLoader.load(path);
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS("EPSG:4326");
            }
            return projectAndAttach(layer, data);
        }

        return null;
    }

    static void showLoadFailure(Layer layer,
                                Component owner,
                                String title,
                                String genericIntro,
                                Exception ex) {
        String layerName = layer != null ? layer.getName() : "capa desconocida";
        String layerPath = layer != null ? layer.getPath() : "";
        AppErrorSupport.logFailure("No se pudo cargar la capa vectorial: " + layerName + " | " + layerPath, ex);
        if (layer instanceof PostgisLayer postgisLayer) {
            JOptionPane.showMessageDialog(
                    owner,
                    PostgisErrorSupport.toUserMessage(ex, postgisLayer.toConnectionInfo()),
                    "PostGIS",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        AppErrorSupport.showErrorDialog(owner, title, genericIntro, ex);
    }

    private static ShapefileData projectAndAttach(Layer layer, ShapefileData data) {
        if (layer == null || data == null) {
            return data;
        }
        ShapefileData projected = TopographyWorkflowSupport.projectVectorDataToCurrentProject(layer, data);
        layer.setSourceName(projected.getSourceName());
        layer.setFeatureCount(projected.getFeatureCount());
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, projected);
        }
        return projected;
    }

    private static ShapefileData invokeLoader(Class<?> clazz, String path, String[] methodNames) throws Exception {
        if (path == null || path.isBlank()) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + path);
        }

        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName, String.class);
                Object result = method.invoke(null, path);
                if (result instanceof ShapefileData shapefileData) {
                    return shapefileData;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName, File.class);
                Object result = method.invoke(null, file);
                if (result instanceof ShapefileData shapefileData) {
                    return shapefileData;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new RuntimeException(
                "No se encontro un loader compatible para: " + path + ". Verifica que la capa siga soportada."
        );
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
