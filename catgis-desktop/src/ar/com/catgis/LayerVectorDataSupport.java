package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

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

        ShapefileData cached = AppContext.mapPanel() != null
                ? AppContext.mapPanel().getShapefileData(layer)
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
                File slFile = new File(slLayer.getPath());
                ValidationResult vr = SpatiaLiteLoader.validateFile(slFile);
                if (!vr.isValid()) {
                    CatgisLogger.warn("SpatiaLite validation failed: " + vr.message(), null);
                    return null;
                }
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
            File fgbFile = new File(path);
            ValidationResult vr = FlatGeobufLoader.validateFile(fgbFile);
            if (!vr.isValid()) {
                CatgisLogger.warn("FlatGeobuf validation failed: " + vr.message(), null);
                return null;
            }
            data = FlatGeobufLoader.load(path);
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS("EPSG:4326");
            }
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".las") || lowerPath.endsWith(".laz")) {
            data = loadLasFile(path, layer);
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS("EPSG:4326");
            }
            return projectAndAttach(layer, data);
        }

        return null;
    }

    private static ShapefileData loadLasFile(String path, Layer layer) throws Exception {
        File file = new File(path);
        LasReader.LasHeader header = LasReader.readHeader(file);
        List<LasReader.LasPoint> lasPoints = LasReader.readPoints(file, 50000);

        org.geotools.feature.simple.SimpleFeatureTypeBuilder tb =
                new org.geotools.feature.simple.SimpleFeatureTypeBuilder();
        tb.setName(layer.getName());
        tb.add("the_geom", org.locationtech.jts.geom.Point.class);
        tb.add("intensity", Double.class);
        tb.add("classification", Integer.class);

        var schema = tb.buildFeatureType();
        org.geotools.feature.simple.SimpleFeatureBuilder fb =
                new org.geotools.feature.simple.SimpleFeatureBuilder(schema);
        List<org.geotools.api.feature.simple.SimpleFeature> features = new java.util.ArrayList<>();
        org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();

        for (int i = 0; i < lasPoints.size(); i++) {
            LasReader.LasPoint lp = lasPoints.get(i);
            fb.reset();
            fb.set("the_geom", gf.createPoint(
                    new org.locationtech.jts.geom.Coordinate(lp.x(), lp.y())));
            fb.set("intensity", (double) lp.intensity());
            fb.set("classification", lp.classification());
            features.add(fb.buildFeature(String.valueOf(i)));
        }

        org.locationtech.jts.geom.Envelope envelope = new org.locationtech.jts.geom.Envelope(
                header.minX(), header.maxX(), header.minY(), header.maxY());

        return new ShapefileData(features, envelope, layer.getName(), features.size(),
                "LiDAR: " + features.size() + " puntos", schema);
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
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().addOrUpdateShapefileLayer(layer, projected);
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
            } catch (Exception ignored) { CatgisLogger.warn("LayerVectorDataSupport: operation failed", ignored); }
        }

        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName, File.class);
                Object result = method.invoke(null, file);
                if (result instanceof ShapefileData shapefileData) {
                    return shapefileData;
                }
            } catch (Exception ignored) { CatgisLogger.warn("LayerVectorDataSupport: operation failed", ignored); }
        }

        throw new RuntimeException(
                "No se encontro un loader compatible para: " + path + ". Verifica que la capa siga soportada."
        );
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
