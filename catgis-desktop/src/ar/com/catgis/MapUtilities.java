package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineWmsLayer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapUtilities {

    private final MapPanel panel;
    public MapUtilities(MapPanel panel) {
        this.panel = panel;
    }

    // ========================================================================
    // 1. showCopiedMessage — toast message via EventBus
    // ========================================================================
    public void showCopiedMessage(String message) {
        if (CatgisDesktopApp.statusBar != null) {
            ar.com.catgis.service.EventBus.emit(
                    ar.com.catgis.service.EventBus.EventType.STATUS_MESSAGE, message);
        }
    }

    // ========================================================================
    // 2. copyToClipboard (static) — copy text to system clipboard
    // ========================================================================
    public static void copyToClipboard(String text) {
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(text), null);
    }

    // ========================================================================
    // 3. formatNumber (static) — format double to 6 decimal places
    // ========================================================================
    public static String formatNumber(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    // ========================================================================
    // 4. toDms (static) — decimal degrees to degrees/minutes/seconds
    // ========================================================================
    public static String toDms(double value, boolean isLat) {
        String hemi;
        if (isLat) {
            hemi = value >= 0 ? "N" : "S";
        } else {
            hemi = value >= 0 ? "E" : "O";
        }
        double abs = Math.abs(value);
        int degrees = (int) abs;
        double minFloat = (abs - degrees) * 60.0;
        int minutes = (int) minFloat;
        double secFloat = (minFloat - minutes) * 60.0;
        return String.format(Locale.US, "%d\u00B0 %d' %.2f\" %s", degrees, minutes, secFloat, hemi);
    }

    // ========================================================================
    // 5. getBaseName (static) — file name without extension
    // ========================================================================
    public static String getBaseName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    // ========================================================================
    // 6. writeWorldFile (static) — write ESRI world file for exported images
    // ========================================================================
    public static void writeWorldFile(File imageFile, double viewMinX, double viewMinY,
                                       double zoomFactor, int imgWidth, int imgHeight) throws Exception {
        String base = getBaseName(imageFile.getName());
        String ext = "";
        int dot = imageFile.getName().lastIndexOf('.');
        if (dot >= 0) {
            ext = imageFile.getName().substring(dot).toLowerCase(Locale.ROOT);
        }
        String worldExt;
        if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
            worldExt = ".jgw";
        } else if (".png".equals(ext)) {
            worldExt = ".pgw";
        } else if (".tif".equals(ext) || ".tiff".equals(ext)) {
            worldExt = ".tfw";
        } else if (".bmp".equals(ext)) {
            worldExt = ".bpw";
        } else if (".gif".equals(ext)) {
            worldExt = ".gfw";
        } else {
            worldExt = ".pgw";
        }
        File worldFile = new File(imageFile.getParentFile(), base + worldExt);
        double pixelWidth = 1.0 / zoomFactor;
        double pixelHeight = 1.0 / zoomFactor;
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(worldFile), StandardCharsets.UTF_8)) {
            writer.write(String.format(Locale.US, "%.15f%n", pixelWidth));
            writer.write("0.0\n");
            writer.write("0.0\n");
            writer.write(String.format(Locale.US, "%.15f%n", -pixelHeight));
            writer.write(String.format(Locale.US, "%.15f%n", viewMinX));
            writer.write(String.format(Locale.US, "%.15f%n", viewMinY + imgHeight * pixelHeight));
        }
    }

    // ========================================================================
    // 7. showCoordinateDialog — popup with coordinate info at screen point
    // ========================================================================
    public void showCoordinateDialog(int screenX, int screenY) {
        double worldX = panel.screenToWorldX(screenX);
        double worldY = panel.screenToWorldY(screenY);

        StringBuilder sb = new StringBuilder();
        sb.append("Coordenadas del punto\n\n");

        String projectCRS = (AppContext.project() != null)
                ? AppContext.project().getProjectCRS() : "";
        if (projectCRS != null && !projectCRS.isBlank()) {
            sb.append("CRS proyecto: ").append(projectCRS).append("\n");
        }
        sb.append("X: ").append(formatNumber(worldX)).append("\n");
        sb.append("Y: ").append(formatNumber(worldY)).append("\n");

        double[] geographic = reprojectPoint(worldX, worldY, projectCRS, "EPSG:4326");
        if (geographic != null) {
            sb.append("\nEPSG:4326\n");
            sb.append("Lon: ").append(formatNumber(geographic[0])).append("\n");
            sb.append("Lat: ").append(formatNumber(geographic[1])).append("\n");
            sb.append("Lon DMS: ").append(toDms(geographic[0], false)).append("\n");
            sb.append("Lat DMS: ").append(toDms(geographic[1], true)).append("\n");
        }
        NotificationManager.info(panel, "Visor de coordenadas", sb.toString());
    }

    // ========================================================================
    // 8. computeCircumcenter (static) — circumcenter of three points
    // ========================================================================
    public static Coordinate computeCircumcenter(double x1, double y1,
                                                  double x2, double y2,
                                                  double x3, double y3) {
        double d = 2.0 * (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2));
        if (Math.abs(d) < 1e-12) {
            double d12 = Math.hypot(x2 - x1, y2 - y1);
            double d23 = Math.hypot(x3 - x2, y3 - y2);
            double d13 = Math.hypot(x3 - x1, y3 - y1);
            if (d12 >= d23 && d12 >= d13) {
                return new Coordinate((x1 + x2) / 2.0, (y1 + y2) / 2.0);
            } else if (d23 >= d12 && d23 >= d13) {
                return new Coordinate((x2 + x3) / 2.0, (y2 + y3) / 2.0);
            } else {
                return new Coordinate((x1 + x3) / 2.0, (y1 + y3) / 2.0);
            }
        }
        double ux = ((x1 * x1 + y1 * y1) * (y2 - y3)
                + (x2 * x2 + y2 * y2) * (y3 - y1)
                + (x3 * x3 + y3 * y3) * (y1 - y2)) / d;
        double uy = ((x1 * x1 + y1 * y1) * (x3 - x2)
                + (x2 * x2 + y2 * y2) * (x1 - x3)
                + (x3 * x3 + y3 * y3) * (x2 - x1)) / d;
        return new Coordinate(ux, uy);
    }

    // ========================================================================
    // 9. reprojectEnvelopeIfNeeded — reproject envelope between CRS codes
    // ========================================================================
    public Envelope reprojectEnvelopeIfNeeded(Envelope env, String sourceCode, String targetCode) {
        if (env == null || env.isNull()) {
            return env;
        }
        try {
            if (sourceCode == null || sourceCode.isBlank()) return env;
            if (targetCode == null || targetCode.isBlank()) return env;
            if (sourceCode.equalsIgnoreCase(targetCode)) return env;

            Envelope manualEnv = CoordinateTransformSupport.reprojectEnvelope(env, sourceCode, targetCode);
            if (manualEnv != null && !manualEnv.isNull()) return manualEnv;

            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            return JTS.transform(env, null, transform, 10);
        } catch (Exception ex) {
            return env;
        }
    }

    // ========================================================================
    // 10. reprojectPoint — reproject a single point between CRS codes
    // ========================================================================
    static double[] reprojectPoint(double x, double y, String sourceCode, String targetCode) {
        try {
            if (sourceCode == null || sourceCode.isBlank()) return null;
            if (targetCode == null || targetCode.isBlank()) return null;
            if (sourceCode.equalsIgnoreCase(targetCode)) return new double[]{x, y};

            double[] manual = CoordinateTransformSupport.transformPoint(x, y, sourceCode, targetCode);
            if (manual != null) return manual;

            CoordinateReferenceSystem sourceCRS = CRSDefinitions.decode(sourceCode, true);
            CoordinateReferenceSystem targetCRS = CRSDefinitions.decode(targetCode, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            double[] src = new double[]{x, y};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            return dst;
        } catch (Exception ex) {
            return null;
        }
    }

    /** Public entry point for transformPoint used by MapPanel delegator. */
    public double[] transformPoint(double x, double y, String sourceCode, String targetCode) {
        return reprojectPoint(x, y, sourceCode, targetCode);
    }

    // ========================================================================
    // 11. getGlobalEnvelope — union envelope of all loaded layers
    // ========================================================================
    public Envelope getGlobalEnvelope() {
        Envelope global = null;

        for (Map.Entry<Layer, LocalRasterData> entry : panel.getRasterLayers().entrySet()) {
            Envelope env = panel.getRasterEnvelope(entry.getKey(), entry.getValue());
            if (env == null || env.isNull()) continue;
            if (global == null) global = new Envelope(env);
            else global.expandToInclude(env);
        }

        for (Map.Entry<Layer, ShapefileData> entry : panel.getShapefileLayers().entrySet()) {
            Layer layer = entry.getKey();
            ShapefileData data = entry.getValue();
            Envelope env = getLayerEnvelope(layer, data);
            if (env == null || env.isNull()) continue;
            if (global == null) global = new Envelope(env);
            else global.expandToInclude(env);
        }

        if (global == null) {
            for (Map.Entry<Layer, OnlineRasterSource> entry : panel.getOnlineTileLayers().entrySet()) {
                Envelope env = getOnlineLayerEnvelope(
                        entry.getKey() instanceof OnlineTileLayer ? (OnlineTileLayer) entry.getKey() : null);
                if (env == null || env.isNull()) continue;
                if (global == null) global = new Envelope(env);
                else global.expandToInclude(env);
            }
        }

        if (global == null) {
            for (Map.Entry<Layer, OnlineWmsLayer> entry : panel.getOnlineWmsLayers().entrySet()) {
                Envelope env = getOnlineWmsEnvelope(entry.getValue());
                if (env == null || env.isNull()) continue;
                if (global == null) global = new Envelope(env);
                else global.expandToInclude(env);
            }
        }

        return global;
    }

    // ========================================================================
    // 12. getLayerEnvelope — envelope of a single vector layer
    // ========================================================================
    public Envelope getLayerEnvelope(Layer layer, ShapefileData data) {
        if (data == null) return null;

        if (CadLayerSupport.isCadLayer(layer)) {
            Envelope cadDisplayEnvelope = buildCadDisplayEnvelope(layer, data);
            if (cadDisplayEnvelope != null && !cadDisplayEnvelope.isNull()) {
                return reprojectWithLayer(layer, cadDisplayEnvelope);
            }
        }

        Envelope env = null;
        if (data.getFeatureCollection() != null
                && !(CadLayerSupport.isCadLayer(layer) && layer.hasCadInternalLayerFilter())) {
            try {
                env = data.getFeatureCollection().getBounds();
            } catch (Exception ignored) {
                CatgisLogger.warn("Error al obtener bounds de FeatureCollection", ignored);
            }
        }

        if ((env == null || env.isNull()) && data.getEnvelope() != null) {
            env = new Envelope(data.getEnvelope());
        }

        if ((env == null || env.isNull()) && data.getFeatures() != null) {
            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null || !panel.isFeatureVisibleInLayer(layer, feature)) continue;
                Object geomObj = feature.getDefaultGeometry();
                if (geomObj instanceof Geometry geometry && !geometry.isEmpty()) {
                    if (env == null) env = new Envelope(geometry.getEnvelopeInternal());
                    else env.expandToInclude(geometry.getEnvelopeInternal());
                }
            }
        }

        return reprojectWithLayer(layer, env);
    }

    // ========================================================================
    // 13. buildCadDisplayEnvelope — trimmed envelope for CAD display
    // ========================================================================
    public Envelope buildCadDisplayEnvelope(Layer layer, ShapefileData data) {
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            return data != null && data.getEnvelope() != null
                    ? new Envelope(data.getEnvelope()) : null;
        }

        List<Envelope> featureEnvelopes = new ArrayList<>();
        List<Double> centerXs = new ArrayList<>();
        List<Double> centerYs = new ArrayList<>();
        Envelope fullEnvelope = null;

        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null || !panel.isFeatureVisibleInLayer(layer, feature)) continue;
            Object geomObj = feature.getDefaultGeometry();
            if (!(geomObj instanceof Geometry geometry) || geometry.isEmpty()) continue;

            Envelope fe = geometry.getEnvelopeInternal();
            if (fe == null || fe.isNull()) continue;

            Envelope copy = new Envelope(fe);
            featureEnvelopes.add(copy);
            centerXs.add((copy.getMinX() + copy.getMaxX()) / 2d);
            centerYs.add((copy.getMinY() + copy.getMaxY()) / 2d);
            if (fullEnvelope == null) fullEnvelope = new Envelope(copy);
            else fullEnvelope.expandToInclude(copy);
        }

        if (fullEnvelope == null || fullEnvelope.isNull() || featureEnvelopes.size() < 50) {
            return fullEnvelope != null ? fullEnvelope
                    : (data.getEnvelope() != null ? new Envelope(data.getEnvelope()) : null);
        }

        double q1x = percentile(centerXs, 0.25d);
        double q3x = percentile(centerXs, 0.75d);
        double q1y = percentile(centerYs, 0.25d);
        double q3y = percentile(centerYs, 0.75d);
        double iqrX = Math.max(1d, q3x - q1x);
        double iqrY = Math.max(1d, q3y - q1y);
        double minCenterX = q1x - (iqrX * 6d);
        double maxCenterX = q3x + (iqrX * 6d);
        double minCenterY = q1y - (iqrY * 6d);
        double maxCenterY = q3y + (iqrY * 6d);

        Envelope trimmedEnvelope = null;
        int keptCount = 0;
        for (Envelope featureEnvelope : featureEnvelopes) {
            double cx = (featureEnvelope.getMinX() + featureEnvelope.getMaxX()) / 2d;
            double cy = (featureEnvelope.getMinY() + featureEnvelope.getMaxY()) / 2d;
            if (cx < minCenterX || cx > maxCenterX || cy < minCenterY || cy > maxCenterY) continue;
            keptCount++;
            if (trimmedEnvelope == null) trimmedEnvelope = new Envelope(featureEnvelope);
            else trimmedEnvelope.expandToInclude(featureEnvelope);
        }

        if (trimmedEnvelope == null || trimmedEnvelope.isNull()) return fullEnvelope;

        int maxDiscarded = Math.max(5, (int) Math.ceil(featureEnvelopes.size() * 0.02d));
        boolean discardedOnlyMinority = (featureEnvelopes.size() - keptCount) <= maxDiscarded;
        boolean widthImproved = trimmedEnvelope.getWidth() > 0d
                && trimmedEnvelope.getWidth() < (fullEnvelope.getWidth() * 0.60d);
        boolean heightImproved = trimmedEnvelope.getHeight() > 0d
                && trimmedEnvelope.getHeight() < (fullEnvelope.getHeight() * 0.60d);

        if (discardedOnlyMinority && (widthImproved || heightImproved)) return trimmedEnvelope;
        return fullEnvelope;
    }

    // ========================================================================
    // Private helpers
    // ========================================================================

    private Envelope reprojectWithLayer(Layer layer, Envelope env) {
        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        String targetCode = (AppContext.project() != null)
                ? AppContext.project().getProjectCRS() : "";
        return CadPlacementSupport.applyPlacement(layer,
                reprojectEnvelopeIfNeeded(env, sourceCode, targetCode));
    }

    Envelope getOnlineLayerEnvelope(OnlineTileLayer layer) {
        if (layer == null) return null;
        Envelope world = new Envelope(OnlineMapUtils.WEB_MERCATOR_WORLD);
        return reprojectEnvelopeIfNeeded(world, "EPSG:3857",
                AppContext.project() != null
                        ? AppContext.project().getProjectCRS() : "");
    }

    Envelope getOnlineWmsEnvelope(OnlineWmsLayer layer) {
        if (layer == null) return null;
        if (Double.isNaN(layer.getExtentMinX()) || Double.isNaN(layer.getExtentMinY())
                || Double.isNaN(layer.getExtentMaxX()) || Double.isNaN(layer.getExtentMaxY())) {
            Envelope world = new Envelope(OnlineMapUtils.WEB_MERCATOR_WORLD);
            return reprojectEnvelopeIfNeeded(world, "EPSG:3857",
                    AppContext.project() != null
                            ? AppContext.project().getProjectCRS() : "");
        }
        Envelope env = new Envelope(layer.getExtentMinX(), layer.getExtentMaxX(),
                layer.getExtentMinY(), layer.getExtentMaxY());
        return reprojectEnvelopeIfNeeded(env, layer.getExtentCrs(),
                AppContext.project() != null
                        ? AppContext.project().getProjectCRS() : "");
    }

    static double percentile(List<Double> values, double quantile) {
        if (values == null || values.isEmpty()) return 0d;
        List<Double> sorted = new ArrayList<>(values);
        java.util.Collections.sort(sorted);
        if (sorted.size() == 1) return sorted.get(0);
        double clamped = Math.max(0d, Math.min(1d, quantile));
        double index = clamped * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) return sorted.get(lower);
        double fraction = index - lower;
        return sorted.get(lower) + ((sorted.get(upper) - sorted.get(lower)) * fraction);
    }

    // ========================================================================
    // 14. Scale/ruler utility methods (static)
    // ========================================================================

    static String formatScaleDenominator(double denominator) {
        if (denominator <= 0d) {
            return "";
        }
        return "1:" + new DecimalFormat("#,##0").format(Math.round(denominator));
    }

    static String buildScaleTooltip(double denominator) {
        if (denominator <= 0d) {
            return "Escala no disponible hasta que haya una vista cartografica valida.";
        }
        if (isGeographicProjectCrs()) {
            return "Escala actual aproximada para la vista. En CRS geograficos se estima segun la latitud central.";
        }
        return "Escala actual de la vista principal. Escribe 1:5000 o 5000 y presiona Enter para ajustarla.";
    }

    static Double parseScaleDenominator(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.isBlank()) {
            return null;
        }
        int colonIndex = text.indexOf(':');
        if (colonIndex >= 0 && colonIndex < text.length() - 1) {
            text = text.substring(colonIndex + 1);
        }
        text = text.replaceAll("[^0-9]", "");
        if (text.isBlank()) {
            return null;
        }
        try {
            double denominator = Double.parseDouble(text);
            return denominator > 0d ? denominator : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static int safeScreenDpi() {
        try {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            return dpi > 0 ? dpi : 96;
        } catch (Exception ex) {
            return 96;
        }
    }

    static boolean isGeographicProjectCrs() {
        String projectCrs = AppContext.project() != null
                ? CRSDefinitions.normalizeCode(AppContext.project().getProjectCRS())
                : "";
        return "EPSG:4326".equalsIgnoreCase(projectCrs)
                || "EPSG:4258".equalsIgnoreCase(projectCrs)
                || "EPSG:4269".equalsIgnoreCase(projectCrs)
                || "EPSG:4674".equalsIgnoreCase(projectCrs)
                || "EPSG:4190".equalsIgnoreCase(projectCrs)
                || "EPSG:4221".equalsIgnoreCase(projectCrs);
    }

    // ========================================================================
    // 15. Coordinate transform utility methods (static)
    // ========================================================================

    static Coordinate toSourceCoordinate(double projectX, double projectY, Layer layer) {
        String projectCRS = (AppContext.project() != null) ? AppContext.project().getProjectCRS() : "";
        String sourceCRS = layer != null ? layer.getSourceCRS() : "";

        if (projectCRS == null || projectCRS.isBlank() || sourceCRS == null || sourceCRS.isBlank()
                || projectCRS.equalsIgnoreCase(sourceCRS)) {
            return new Coordinate(projectX, projectY);
        }

        double[] source = reprojectPoint(projectX, projectY, projectCRS, sourceCRS);
        if (source == null || source.length < 2) {
            return new Coordinate(projectX, projectY);
        }
        return new Coordinate(source[0], source[1]);
    }

    static Coordinate toProjectCoordinate(Coordinate sourceCoordinate, Layer layer) {
        if (sourceCoordinate == null) {
            return null;
        }

        String projectCRS = (AppContext.project() != null) ? AppContext.project().getProjectCRS() : "";
        String sourceCRS = layer != null ? layer.getSourceCRS() : "";
        if (projectCRS == null || projectCRS.isBlank() || sourceCRS == null || sourceCRS.isBlank()
                || projectCRS.equalsIgnoreCase(sourceCRS)) {
            return new Coordinate(sourceCoordinate);
        }

        double[] projected = reprojectPoint(sourceCoordinate.x, sourceCoordinate.y, sourceCRS, projectCRS);
        if (projected == null || projected.length < 2) {
            return new Coordinate(sourceCoordinate);
        }
        return new Coordinate(projected[0], projected[1]);
    }

    static List<Coordinate> toSourceCoordinates(List<Coordinate> projectCoordinates, Layer layer) {
        List<Coordinate> out = new ArrayList<>();
        if (projectCoordinates == null) {
            return out;
        }
        for (Coordinate coordinate : projectCoordinates) {
            out.add(toSourceCoordinate(coordinate.x, coordinate.y, layer));
        }
        return out;
    }

    // ========================================================================
    // 16. Measurement/parsing utility methods (static)
    // ========================================================================

    static double parsePositiveDistance(String input) {
        if (input == null) {
            return Double.NaN;
        }
        String normalized = input.trim().replace(',', '.');
        if (normalized.isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    static String getPolygonSurfaceDistanceHint(Layer layer) {
        String sourceCode = layer != null ? layer.getSourceCRS() : "";
        if (sourceCode == null || sourceCode.isBlank()) {
            return "unidades de la capa";
        }
        String metricCode = MapMeasurementUtils.chooseMetricCRS(sourceCode);
        if (sourceCode.equalsIgnoreCase(metricCode)) {
            return "metros";
        }
        return "metros";
    }
}
