package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.raster.ContourProcess;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContourGenerationService {

    private static final String CONTOUR_INTERVAL_TOKEN = "interval=";
    private static final String CONTOUR_INDEX_TOKEN = "indexEvery=";
    private static final String CONTOUR_SIMPLIFY_TOKEN = "simplify=";
    private static final String CONTOUR_SMOOTH_TOKEN = "smooth=";
    private static final String CONTOUR_MIN_ELEVATION_TOKEN = "minElevation=";

    private ContourGenerationService() {
    }

    public static GeneratedContourLayer generateContours(Layer rasterLayer,
                                                         double interval,
                                                         int indexEvery,
                                                         String outputLayerName,
                                                         boolean simplify,
                                                         boolean smooth,
                                                         Double minimumElevation) throws Exception {
        if (!(rasterLayer instanceof RasterLayer)) {
            throw new IllegalArgumentException("La capa seleccionada no es raster.");
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("La equidistancia debe ser mayor a cero.");
        }
        if (indexEvery < 2) {
            throw new IllegalArgumentException("La frecuencia de curvas indice debe ser 2 o mayor.");
        }

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
        SimpleFeatureCollection contourCollection = ContourProcess.process(
                coverage,
                0,
                null,
                interval,
                simplify,
                smooth,
                null,
                null
        );
        if (contourCollection == null) {
            throw new IllegalStateException("El proceso de curvas no devolvio resultados.");
        }

        SimpleFeatureType sourceSchema = contourCollection.getSchema();
        SimpleFeatureType outputSchema = buildOutputSchema(sourceSchema, outputLayerName);
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(outputSchema);
        String elevationField = resolveElevationField(sourceSchema);
        int featureIndex = 1;
        try (FeatureIterator<SimpleFeature> iterator = contourCollection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                if (feature == null) {
                    continue;
                }
                Object geometryObject = feature.getDefaultGeometry();
                if (!(geometryObject instanceof Geometry geometry) || geometry.isEmpty()) {
                    continue;
                }
                double elevation = resolveElevationValue(feature, elevationField);
                if (minimumElevation != null && Double.isFinite(elevation) && elevation <= minimumElevation) {
                    continue;
                }
                String curveType = isIndexContour(elevation, interval, indexEvery) ? "Indice" : "Intermedia";
                featureBuilder.set("the_geom", geometry.copy());
                featureBuilder.set("elevation_m", elevation);
                featureBuilder.set("curve_type", curveType);
                features.add(featureBuilder.buildFeature(safeTypeName(outputLayerName) + "." + featureIndex++));
                featureBuilder.reset();
            }
        }
        if (features.isEmpty()) {
            if (minimumElevation != null) {
                throw new IllegalStateException("No se generaron curvas con esa equidistancia porque el filtro costero / cota minima esta dejando todo afuera. Prueba con un umbral menor o desactivalo.");
            }
            throw new IllegalStateException("No se generaron curvas de nivel para ese raster y esa equidistancia.");
        }

        ReferencedEnvelope bounds = contourCollection.getBounds();
        Envelope envelope = bounds != null && !bounds.isEmpty()
                ? new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY())
                : null;

        VectorLayer contourLayer = new VectorLayer(
                outputLayerName != null && !outputLayerName.isBlank() ? outputLayerName.trim() : "Curvas de nivel",
                ""
        );
        contourLayer.setVisible(true);
        contourLayer.setSourceName(buildPersistentSourceName(
                rasterLayer,
                interval,
                indexEvery,
                simplify,
                smooth,
                minimumElevation
        ));
        contourLayer.setFeatureCount(features.size());
        contourLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalAnalysisCrsCode(coverage, rasterLayer));
        contourLayer.setLineColor(new Color(142, 99, 61));
        contourLayer.setLineWidth(1.15f);
        contourLayer.setLineSymbolStyle(Layer.LineSymbolStyle.SOLID);
        VectorLayerUtils.populateFieldConfigs(contourLayer, outputSchema);
        contourLayer.setLabelField("elevation_m");
        applyTopographicSymbology(contourLayer);

        ShapefileData data = new ShapefileData(
                features,
                envelope,
                contourLayer.getSourceName(),
                features.size(),
                "Curvas generadas con equidistancia " + formatNumber(interval)
                        + " e indice cada " + indexEvery + " curvas."
                        + (minimumElevation != null ? " Se excluyeron curvas <= " + formatNumber(minimumElevation) + " m para limpieza costera / ruido de borde." : ""),
                outputSchema
        );
        return new GeneratedContourLayer(contourLayer, data, "elevation_m");
    }

    public static GeneratedContourLayer regenerateContours(Layer savedLayer, Layer rasterLayer) throws Exception {
        if (savedLayer == null) {
            throw new IllegalArgumentException("La capa de curvas guardada no es valida.");
        }
        RegenerationOptions options = RegenerationOptions.fromLayer(savedLayer);
        return generateContours(
                rasterLayer,
                options.interval(),
                options.indexEvery(),
                savedLayer.getName(),
                options.simplify(),
                options.smooth(),
                options.minimumElevation()
        );
    }

    private static SimpleFeatureType buildOutputSchema(SimpleFeatureType sourceSchema, String outputLayerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputLayerName));
        if (sourceSchema != null && sourceSchema.getCoordinateReferenceSystem() != null) {
            builder.setCRS(sourceSchema.getCoordinateReferenceSystem());
        }
        Class<?> geometryBinding = LineString.class;
        if (sourceSchema != null && sourceSchema.getGeometryDescriptor() != null
                && sourceSchema.getGeometryDescriptor().getType() != null
                && sourceSchema.getGeometryDescriptor().getType().getBinding() != null) {
            geometryBinding = sourceSchema.getGeometryDescriptor().getType().getBinding();
        }
        builder.add("the_geom", geometryBinding);
        builder.add("elevation_m", Double.class);
        builder.add("curve_type", String.class);
        return builder.buildFeatureType();
    }

    private static void applyTopographicSymbology(Layer contourLayer) {
        CategorizedSymbology symbology = contourLayer.getLineCategorizedSymbology();
        symbology.setFieldName("curve_type");
        symbology.setLegendTitle("Curvas de nivel");
        symbology.setLegendSubtitle("Indice e intermedia");
        symbology.clearRules();

        CategoryStyleRule indexRule = symbology.getOrCreateRule("Indice");
        indexRule.setPrimaryColor(new Color(102, 62, 32));
        indexRule.setSecondaryColor(new Color(102, 62, 32));
        indexRule.setLineStyle(Layer.LineSymbolStyle.SOLID);
        indexRule.setLineWidth(2.25f);

        CategoryStyleRule intermediateRule = symbology.getOrCreateRule("Intermedia");
        intermediateRule.setPrimaryColor(new Color(170, 126, 88));
        intermediateRule.setSecondaryColor(new Color(170, 126, 88));
        intermediateRule.setLineStyle(Layer.LineSymbolStyle.SOLID);
        intermediateRule.setLineWidth(1.05f);
    }

    private static String resolveElevationField(SimpleFeatureType schema) {
        if (schema == null) {
            return "";
        }
        for (var descriptor : schema.getAttributeDescriptors()) {
            if (descriptor == null) {
                continue;
            }
            String localName = descriptor.getLocalName();
            if ("the_geom".equalsIgnoreCase(localName) || "geom".equalsIgnoreCase(localName)) {
                continue;
            }
            if (localName != null && (localName.equalsIgnoreCase("value") || localName.toLowerCase().contains("elev"))) {
                return localName;
            }
        }
        for (var descriptor : schema.getAttributeDescriptors()) {
            if (descriptor == null) {
                continue;
            }
            String localName = descriptor.getLocalName();
            if (!"the_geom".equalsIgnoreCase(localName) && !"geom".equalsIgnoreCase(localName)) {
                return localName;
            }
        }
        return "";
    }

    private static double resolveElevationValue(SimpleFeature feature, String preferredField) {
        if (feature == null) {
            return Double.NaN;
        }
        if (preferredField != null && !preferredField.isBlank()) {
            Object value = FeatureAttributeResolver.resolveAttribute(feature, preferredField);
            Double parsed = toDouble(value);
            if (parsed != null) {
                return parsed;
            }
        }

        SimpleFeatureType type = feature.getFeatureType();
        if (type != null) {
            for (var descriptor : type.getAttributeDescriptors()) {
                if (descriptor == null) {
                    continue;
                }
                String localName = descriptor.getLocalName();
                if ("the_geom".equalsIgnoreCase(localName) || "geom".equalsIgnoreCase(localName)) {
                    continue;
                }
                Double parsed = toDouble(feature.getAttribute(localName));
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return Double.NaN;
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            double parsed = number.doubleValue();
            return Double.isFinite(parsed) ? parsed : null;
        }
        try {
            double parsed = Double.parseDouble(String.valueOf(value).trim().replace(",", "."));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean isIndexContour(double elevation, double interval, int indexEvery) {
        if (!Double.isFinite(elevation)) {
            return false;
        }
        double base = Math.abs(interval) * Math.max(2, indexEvery);
        if (base <= 0d) {
            return false;
        }
        double nearest = Math.rint(elevation / base) * base;
        double tolerance = Math.max(0.05d, Math.abs(interval) * 0.05d);
        return Math.abs(elevation - nearest) <= tolerance;
    }

    private static String safeTypeName(String text) {
        String base = text != null && !text.isBlank() ? text.trim() : "curvas_nivel";
        String normalized = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        if (normalized.isBlank()) {
            normalized = "curvas_nivel";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "c_" + normalized;
        }
        return normalized;
    }

    private static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static String buildPersistentSourceName(Layer rasterLayer,
                                                    double interval,
                                                    int indexEvery,
                                                    boolean simplify,
                                                    boolean smooth,
                                                    Double minimumElevation) {
        String rasterName = rasterLayer != null && rasterLayer.getName() != null && !rasterLayer.getName().isBlank()
                ? rasterLayer.getName().trim()
                : "DEM";
        StringBuilder sb = new StringBuilder("Curvas de nivel derivadas de ")
                .append(rasterName)
                .append("; ")
                .append(CONTOUR_INTERVAL_TOKEN).append(formatNumber(interval))
                .append("; ")
                .append(CONTOUR_INDEX_TOKEN).append(indexEvery)
                .append("; ")
                .append(CONTOUR_SIMPLIFY_TOKEN).append(simplify)
                .append("; ")
                .append(CONTOUR_SMOOTH_TOKEN).append(smooth);
        if (minimumElevation != null) {
            sb.append("; ")
                    .append(CONTOUR_MIN_ELEVATION_TOKEN)
                    .append(formatNumber(minimumElevation));
        }
        return sb.toString();
    }

    private static Double parseOptionalDoubleToken(String text, String token) {
        String value = parseToken(text, token);
        if (value == null || value.isBlank()) {
            return null;
        }
        return toDouble(value);
    }

    private static Integer parseOptionalIntegerToken(String text, String token) {
        String value = parseToken(text, token);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static Boolean parseOptionalBooleanToken(String text, String token) {
        String value = parseToken(text, token);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static String parseToken(String text, String token) {
        if (text == null || text.isBlank() || token == null || token.isBlank()) {
            return null;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerToken = token.toLowerCase(Locale.ROOT);
        int start = lowerText.indexOf(lowerToken);
        if (start < 0) {
            return null;
        }
        int valueStart = start + token.length();
        int end = text.indexOf(';', valueStart);
        String value = end >= 0 ? text.substring(valueStart, end) : text.substring(valueStart);
        return value != null ? value.trim() : null;
    }

    private record RegenerationOptions(double interval,
                                       int indexEvery,
                                       boolean simplify,
                                       boolean smooth,
                                       Double minimumElevation) {
        private static RegenerationOptions fromLayer(Layer layer) {
            String sourceName = layer != null ? layer.getSourceName() : "";
            Double parsedInterval = parseOptionalDoubleToken(sourceName, CONTOUR_INTERVAL_TOKEN);
            Integer parsedIndexEvery = parseOptionalIntegerToken(sourceName, CONTOUR_INDEX_TOKEN);
            Boolean parsedSimplify = parseOptionalBooleanToken(sourceName, CONTOUR_SIMPLIFY_TOKEN);
            Boolean parsedSmooth = parseOptionalBooleanToken(sourceName, CONTOUR_SMOOTH_TOKEN);
            Double parsedMinimumElevation = parseOptionalDoubleToken(sourceName, CONTOUR_MIN_ELEVATION_TOKEN);

            if (parsedInterval == null && layer != null && layer.getName() != null) {
                String name = layer.getName().trim().toLowerCase(Locale.ROOT);
                int start = name.indexOf("curvas ");
                int meterMarker = name.indexOf("m", Math.max(start, 0));
                if (start >= 0 && meterMarker > start + 7) {
                    String candidate = layer.getName().substring(start + 7, meterMarker).trim();
                    parsedInterval = toDouble(candidate);
                }
            }

            return new RegenerationOptions(
                    parsedInterval != null && parsedInterval > 0d ? parsedInterval : 10d,
                    parsedIndexEvery != null && parsedIndexEvery >= 2 ? parsedIndexEvery : 5,
                    parsedSimplify != null ? parsedSimplify : true,
                    parsedSmooth != null ? parsedSmooth : false,
                    parsedMinimumElevation
            );
        }
    }

    public record GeneratedContourLayer(Layer layer, ShapefileData data, String elevationField) {
    }
}
