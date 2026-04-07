package ar.com.catgis;

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

    private ContourGenerationService() {
    }

    public static GeneratedContourLayer generateContours(Layer rasterLayer,
                                                         double interval,
                                                         int indexEvery,
                                                         String outputLayerName,
                                                         boolean simplify,
                                                         boolean smooth) throws Exception {
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
                String curveType = isIndexContour(elevation, interval, indexEvery) ? "Indice" : "Intermedia";
                featureBuilder.set("the_geom", geometry.copy());
                featureBuilder.set("elevation_m", elevation);
                featureBuilder.set("curve_type", curveType);
                features.add(featureBuilder.buildFeature(safeTypeName(outputLayerName) + "." + featureIndex++));
                featureBuilder.reset();
            }
        }
        if (features.isEmpty()) {
            throw new IllegalStateException("No se generaron curvas de nivel para ese raster y esa equidistancia.");
        }

        ReferencedEnvelope bounds = contourCollection.getBounds();
        Envelope envelope = bounds != null && !bounds.isEmpty()
                ? new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY())
                : null;

        Layer contourLayer = new Layer(
                outputLayerName != null && !outputLayerName.isBlank() ? outputLayerName.trim() : "Curvas de nivel",
                rasterLayer.getPath(),
                "LINE"
        );
        contourLayer.setSourceName("Curvas de nivel derivadas de " + rasterLayer.getName());
        contourLayer.setSourceCRS(RasterCoverageSupport.resolveCoverageCrsCode(coverage, rasterLayer));
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
                "Curvas generadas con equidistancia " + formatNumber(interval) + " e indice cada " + indexEvery + " curvas.",
                outputSchema
        );
        return new GeneratedContourLayer(contourLayer, data, "elevation_m");
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

    public record GeneratedContourLayer(Layer layer, ShapefileData data, String elevationField) {
    }
}
