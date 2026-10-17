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
            // Inspect verified file CRS even when saved layer metadata exists:
            // stale EPSG:4326 defaults must not silently override true UTM data.
            FlatGeobufCrsSupport.Resolution headerCrs =
                    FlatGeobufLoader.inspectSourceCrs(fgbFile);
            String discovered = headerCrs.sourceCode();
            if (isBlank(layer.getSourceCRS())) {
                requireDeclaredFgbSourceCrs(discovered);
            } else {
                FlatGeobufCrsSupport.requireConsistentWithAssignedCrs(
                        headerCrs, layer.getSourceCRS());
            }
            data = FlatGeobufLoader.load(path);
            String schemaCrs = FlatGeobufCrsSupport.schemaCode(data.getSchema());
            if (!discovered.equals(schemaCrs)) {
                throw new java.io.IOException(
                        "FlatGeobuf: los metadatos CRS cambiaron durante la lectura.");
            }
            if (isBlank(layer.getSourceCRS())) {
                layer.setSourceCRS(schemaCrs);
            }
            return projectAndAttach(layer, data);
        }
        if (lowerPath.endsWith(".las") || lowerPath.endsWith(".laz")) {
            // LAS/LAZ CRS VLR/EVLR metadata is not yet parsed by the reader.
            // Reject before reading potentially large data, never invent WGS84.
            requireDeclaredLasSourceCrs(layer.getSourceCRS());
            data = loadLasFile(path, layer);
            return projectAndAttach(layer, data);
        }

        return null;
    }

    static void requireDeclaredLasSourceCrs(String sourceCrs) {
        if (isBlank(sourceCrs)) {
            throw new IllegalStateException(
                    "LiDAR sin CRS de origen declarado: asigne explícitamente el CRS correcto antes de cargar. "
                    + "CATGIS no supondrá EPSG:4326.");
        }
    }

    static void requireDeclaredFgbSourceCrs(String sourceCrs) {
        if (isBlank(sourceCrs)) {
            throw new IllegalStateException(
                    "FlatGeobuf sin CRS verificado: asigne explícitamente el CRS antes de cargar. "
                    + "CATGIS no supondrá EPSG:4326.");
        }
    }

    static final int MAX_COMPLETE_LAS_FEATURES = 50_000;

    // The current vector model has no typed PREVIEW status and exports/analyses
    // would treat a prefix as complete. Reject larger sources, never lie.
    static void requireCompleteLasDataset(long declaredCount) throws java.io.IOException {
        if (declaredCount < 0 || declaredCount > MAX_COMPLETE_LAS_FEATURES) {
            throw new java.io.IOException("LiDAR: el archivo declara " + declaredCount
                    + " puntos y supera los " + MAX_COMPLETE_LAS_FEATURES
                    + " admitidos para carga íntegra. CATGIS no presentará una"
                    + " muestra parcial como datos completos. Se requiere lector indexado.");
        }
    }

    private static ShapefileData loadLasFile(String path, Layer layer) throws Exception {
        File file = new File(path);
        LasReader.LasHeader header = LasReader.readHeader(file);
        requireCompleteLasDataset(header.pointCount());
        List<LasReader.LasPoint> lasPoints =
                LasReader.readPoints(file, MAX_COMPLETE_LAS_FEATURES, header);
        if (lasPoints.size() != header.pointCount()) {
            throw new java.io.IOException("LiDAR: lectura incompleta de puntos declarados.");
        }

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
                "LiDAR verificado: " + features.size() + "/" + header.pointCount() + " puntos completos", schema);
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
            NotificationManager.error(
                    owner,
                    "PostGIS",
                    PostgisErrorSupport.toUserMessage(ex, postgisLayer.toConnectionInfo())
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
