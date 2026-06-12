package ar.com.catgis.data.vector;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;

import ar.com.catgis.VectorLayer;
import ar.com.catgis.AppContext;
import ar.com.catgis.ReadOnlyVectorLayerSource;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.OpenAttributeTableAction;
import ar.com.catgis.MapPanel;
import ar.com.catgis.LayersPanel;
import ar.com.catgis.FieldConfig;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.CategorizedSymbology;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class VectorLayerUtils {

    private VectorLayerUtils() {
    }

    public static List<Layer> getVectorLayers() {
        List<Layer> layers = new ArrayList<>();
        if (AppContext.project() == null || AppContext.project().getLayers() == null) {
            return layers;
        }

        for (Layer layer : AppContext.project().getLayers()) {
            if (layer != null && !(layer instanceof RasterLayer)) {
                layers.add(layer);
            }
        }
        return layers;
    }

    public static boolean isReadOnlyVectorLayer(Layer layer) {
        return layer instanceof ReadOnlyVectorLayerSource
                && ((ReadOnlyVectorLayerSource) layer).isReadOnly();
    }

    public static String getReadOnlyVectorLayerReason(Layer layer) {
        if (!isReadOnlyVectorLayer(layer)) {
            return "";
        }
        String reason = ((ReadOnlyVectorLayerSource) layer).getReadOnlyReason();
        return reason != null ? reason.trim() : "";
    }

    public static ShapefileData ensureVectorData(Layer layer) {
        if (layer == null || layer instanceof RasterLayer) {
            return null;
        }
        return OpenAttributeTableAction.ensureLayerDataAvailable(layer);
    }

    public static String resolveGeometryFamily(Layer layer) {
        return resolveGeometryFamily(ensureVectorData(layer));
    }

    public static String resolveGeometryFamily(ShapefileData data) {
        if (data == null) {
            return "";
        }

        SimpleFeatureType schema = data.getSchema();
        if (schema != null && schema.getGeometryDescriptor() != null) {
            String byBinding = resolveGeometryFamily(schema.getGeometryDescriptor().getType().getBinding());
            if (!byBinding.isBlank()) {
                return byBinding;
            }
        }

        if (data.getFeatures() != null) {
            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                Object geomObj = feature.getDefaultGeometry();
                if (geomObj instanceof Geometry geometry) {
                    String byGeometry = resolveGeometryFamily(geometry.getClass());
                    if (!byGeometry.isBlank()) {
                        return byGeometry;
                    }
                }
            }
        }

        return "";
    }

    public static String resolveGeometryFamily(Class<?> geometryClass) {
        if (geometryClass == null) {
            return "";
        }
        if (Point.class.isAssignableFrom(geometryClass) || MultiPoint.class.isAssignableFrom(geometryClass)) {
            return "POINT";
        }
        if (LineString.class.isAssignableFrom(geometryClass) || MultiLineString.class.isAssignableFrom(geometryClass)) {
            return "LINE";
        }
        if (Polygon.class.isAssignableFrom(geometryClass) || MultiPolygon.class.isAssignableFrom(geometryClass)) {
            return "POLYGON";
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Geometry> resolveConcreteGeometryBinding(SimpleFeatureType schema,
                                                                          List<SimpleFeature> features) {
        Class<? extends Geometry> fallbackBinding = Geometry.class;
        if (schema != null && schema.getGeometryDescriptor() != null && schema.getGeometryDescriptor().getType() != null) {
            Class<?> schemaBinding = schema.getGeometryDescriptor().getType().getBinding();
            if (schemaBinding != null
                    && Geometry.class.isAssignableFrom(schemaBinding)
                    && !Geometry.class.equals(schemaBinding)) {
                fallbackBinding = (Class<? extends Geometry>) schemaBinding;
            }
        }
        return resolveConcreteGeometryBinding(extractGeometries(features), fallbackBinding);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Geometry> resolveConcreteGeometryBinding(List<Geometry> geometries,
                                                                          Class<? extends Geometry> fallbackBinding) {
        GeometryBindingAnalysis analysis = new GeometryBindingAnalysis();
        if (geometries != null) {
            for (Geometry geometry : geometries) {
                analyzeGeometryBinding(geometry, analysis);
                if (analysis.mixedFamilies) {
                    return Geometry.class;
                }
            }
        }

        if (analysis.family.isBlank()) {
            return fallbackBinding != null ? fallbackBinding : Geometry.class;
        }

        switch (analysis.family) {
            case "POINT":
                return analysis.requiresMulti ? MultiPoint.class : Point.class;
            case "LINE":
                return analysis.requiresMulti ? MultiLineString.class : LineString.class;
            case "POLYGON":
                return analysis.requiresMulti ? MultiPolygon.class : Polygon.class;
            default:
                return fallbackBinding != null ? fallbackBinding : Geometry.class;
        }
    }

    public static Geometry normalizeGeometryForBinding(Geometry geometry, Class<? extends Geometry> binding) {
        if (geometry == null || geometry.isEmpty() || binding == null) {
            return geometry;
        }
        if (binding.isInstance(geometry)) {
            return geometry;
        }

        GeometryFactory factory = geometry.getFactory() != null ? geometry.getFactory() : new GeometryFactory();

        if (Point.class.isAssignableFrom(binding)) {
            List<Point> points = new ArrayList<>();
            collectPoints(geometry, points);
            return points.size() == 1 ? (Geometry) points.get(0).copy() : null;
        }
        if (MultiPoint.class.isAssignableFrom(binding)) {
            List<Point> points = new ArrayList<>();
            collectPoints(geometry, points);
            if (points.isEmpty()) {
                return null;
            }
            Point[] pointArray = new Point[points.size()];
            for (int i = 0; i < points.size(); i++) {
                pointArray[i] = (Point) points.get(i).copy();
            }
            return factory.createMultiPoint(pointArray);
        }
        if (LineString.class.isAssignableFrom(binding)) {
            List<LineString> lines = new ArrayList<>();
            collectLines(geometry, lines);
            return lines.size() == 1 ? (Geometry) lines.get(0).copy() : null;
        }
        if (MultiLineString.class.isAssignableFrom(binding)) {
            List<LineString> lines = new ArrayList<>();
            collectLines(geometry, lines);
            if (lines.isEmpty()) {
                return null;
            }
            LineString[] lineArray = new LineString[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                lineArray[i] = (LineString) lines.get(i).copy();
            }
            return factory.createMultiLineString(lineArray);
        }
        if (Polygon.class.isAssignableFrom(binding)) {
            List<Polygon> polygons = new ArrayList<>();
            collectPolygons(geometry, polygons);
            return polygons.size() == 1 ? (Geometry) polygons.get(0).copy() : null;
        }
        if (MultiPolygon.class.isAssignableFrom(binding)) {
            List<Polygon> polygons = new ArrayList<>();
            collectPolygons(geometry, polygons);
            if (polygons.isEmpty()) {
                return null;
            }
            Polygon[] polygonArray = new Polygon[polygons.size()];
            for (int i = 0; i < polygons.size(); i++) {
                polygonArray[i] = (Polygon) polygons.get(i).copy();
            }
            return factory.createMultiPolygon(polygonArray);
        }

        return geometry;
    }

    public static String describeGeometryBinding(Class<?> binding) {
        if (binding == null) {
            return "Geometry";
        }
        if (Point.class.isAssignableFrom(binding)) {
            return "Point";
        }
        if (MultiPoint.class.isAssignableFrom(binding)) {
            return "MultiPoint";
        }
        if (LineString.class.isAssignableFrom(binding)) {
            return "LineString";
        }
        if (MultiLineString.class.isAssignableFrom(binding)) {
            return "MultiLineString";
        }
        if (Polygon.class.isAssignableFrom(binding)) {
            return "Polygon";
        }
        if (MultiPolygon.class.isAssignableFrom(binding)) {
            return "MultiPolygon";
        }
        return binding.getSimpleName();
    }

    public static Layer addResultLayer(String name,
                                       ShapefileData data,
                                       Layer styleSource,
                                       String sourceCrs,
                                       String path) {
        if (data == null) {
            return null;
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project("Proyecto actual"));
        }

        VectorLayer layer = new VectorLayer(name, path != null ? path : "");
        layer.setVisible(true);
        layer.setSourceName(data.getSourceName() != null ? data.getSourceName() : name);
        layer.setFeatureCount(data.getFeatureCount());
        layer.setSourceCRS(sourceCrs);
        copyLayerAppearance(styleSource, layer);
        populateFieldConfigs(layer, data.getSchema());

        AppContext.project().addLayer(layer);
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.addLayer(layer);
            CatgisDesktopApp.layersPanel.selectLayer(layer);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
            CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
            CatgisDesktopApp.mapPanel.repaint();
        }
        return layer;
    }

    public static void copyLayerAppearance(Layer source, Layer target) {
        if (source == null || target == null) {
            return;
        }

        target.setFillColor(copyColor(source.getFillColor()));
        target.setBorderColor(copyColor(source.getBorderColor()));
        target.setLineColor(copyColor(source.getLineColor()));
        target.setLineWidth(source.getLineWidth());
        target.setPointColor(copyColor(source.getPointColor()));
        target.setPointSize(source.getPointSize());
        target.setPointSymbolStyle(source.getPointSymbolStyle());
        target.setLineSymbolStyle(source.getLineSymbolStyle());
        target.setPolygonFillStyle(source.getPolygonFillStyle());
        copyCategorizedSymbology(source.getPointCategorizedSymbology(), target.getPointCategorizedSymbology());
        copyCategorizedSymbology(source.getLineCategorizedSymbology(), target.getLineCategorizedSymbology());
        copyCategorizedSymbology(source.getPolygonCategorizedSymbology(), target.getPolygonCategorizedSymbology());
        target.setLabelsVisible(source.isLabelsVisible());
        target.setLabelField(source.getLabelField());
        target.setLabelFontFamily(source.getLabelFontFamily());
        target.setLabelFontSize(source.getLabelFontSize());
        target.setLabelBold(source.isLabelBold());
        target.setLabelItalic(source.isLabelItalic());
        target.setLabelUnderline(source.isLabelUnderline());
        target.setLabelColor(copyColor(source.getLabelColor()));
        target.setLabelHaloEnabled(source.isLabelHaloEnabled());
        target.setLabelHaloColor(copyColor(source.getLabelHaloColor()));
        target.setLabelHaloWidth(source.getLabelHaloWidth());
        target.setLabelOffsetX(source.getLabelOffsetX());
        target.setLabelOffsetY(source.getLabelOffsetY());
        target.setLabelPlacement(source.getLabelPlacement());
        target.setLabelPlacementMode(source.getLabelPlacementMode());
        target.setLabelPriority(source.getLabelPriority());
        target.setLabelCollisionAvoid(source.isLabelCollisionAvoid());
        target.setLabelBackgroundEnabled(source.isLabelBackgroundEnabled());
        target.setLabelBackgroundColor(copyColor(source.getLabelBackgroundColor()));
        target.setLabelMinScale(source.getLabelMinScale());
        target.setLabelMaxScale(source.getLabelMaxScale());
        target.setSourceCRS(source.getSourceCRS());
        target.setCadOffsetX(source.getCadOffsetX());
        target.setCadOffsetY(source.getCadOffsetY());
        target.setCadScale(source.getCadScale());
        target.setCadRotationDegrees(source.getCadRotationDegrees());
        target.setCadGeoreferenceTransform(
                source.getCadGeoreferenceMethod(),
                source.getCadGeorefM00(),
                source.getCadGeorefM01(),
                source.getCadGeorefM02(),
                source.getCadGeorefM10(),
                source.getCadGeorefM11(),
                source.getCadGeorefM12()
        );
        target.setCadGeoreferenceDiagnostics(
                source.getCadGeorefResidualMean(),
                source.getCadGeorefResidualMax(),
                source.getCadGeorefReferenceCount(),
                source.getCadGeorefCheckCount()
        );
        target.setCadHiddenInternalLayers(source.getCadHiddenInternalLayers());

        for (FieldConfig sourceConfig : source.getFieldConfigs().values()) {
            if (sourceConfig == null) {
                continue;
            }
            FieldConfig targetConfig = target.getOrCreateFieldConfig(sourceConfig.getFieldName(), sourceConfig.getTypeName());
            targetConfig.setPublicName(sourceConfig.getPublicName());
            targetConfig.setVisible(sourceConfig.isVisible());
            targetConfig.setEditable(sourceConfig.isEditable());
            targetConfig.setLength(sourceConfig.getLength());
            targetConfig.setPrecision(sourceConfig.getPrecision());
        }
    }

    public static void populateFieldConfigs(Layer layer, SimpleFeatureType schema) {
        if (layer == null || schema == null) {
            return;
        }

        for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            String typeName = descriptor.getType() != null && descriptor.getType().getBinding() != null
                    ? descriptor.getType().getBinding().getSimpleName()
                    : "String";
            layer.getOrCreateFieldConfig(descriptor.getLocalName(), typeName);
        }
    }

    public static String pickLayerCrs(Layer layer, ShapefileData data) {
        if (layer != null && layer.getSourceCRS() != null && !layer.getSourceCRS().isBlank()) {
            return layer.getSourceCRS();
        }
        if (AppContext.project() != null && AppContext.project().getProjectCRS() != null) {
            return AppContext.project().getProjectCRS();
        }
        return "EPSG:4326";
    }

    private static Color copyColor(Color color) {
        if (color == null) {
            return null;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static void copyCategorizedSymbology(CategorizedSymbology source, CategorizedSymbology target) {
        if (source == null || target == null) {
            return;
        }
        target.setFieldName(source.getFieldName());
        target.setLegendTitle(source.getLegendTitle());
        target.setLegendSubtitle(source.getLegendSubtitle());
        target.clearRules();
        for (CategoryStyleRule sourceRule : source.getRules().values()) {
            if (sourceRule == null) {
                continue;
            }
            CategoryStyleRule targetRule = target.getOrCreateRule(sourceRule.getValue());
            targetRule.setPrimaryColor(copyColor(sourceRule.getPrimaryColor()));
            targetRule.setSecondaryColor(copyColor(sourceRule.getSecondaryColor()));
            targetRule.setLineStyle(sourceRule.getLineStyle());
            targetRule.setPointSymbolStyle(sourceRule.getPointSymbolStyle());
            targetRule.setPointSize(sourceRule.getPointSize());
            targetRule.setPolygonFillStyle(sourceRule.getPolygonFillStyle());
            targetRule.setLineWidth(sourceRule.getLineWidth());
        }
    }

    private static List<Geometry> extractGeometries(List<SimpleFeature> features) {
        List<Geometry> geometries = new ArrayList<>();
        if (features == null) {
            return geometries;
        }
        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object geometry = feature.getDefaultGeometry();
            if (geometry instanceof Geometry) {
                geometries.add((Geometry) geometry);
            }
        }
        return geometries;
    }

    private static void analyzeGeometryBinding(Geometry geometry, GeometryBindingAnalysis analysis) {
        if (geometry == null || geometry.isEmpty() || analysis == null || analysis.mixedFamilies) {
            return;
        }

        if (geometry instanceof Point) {
            analysis.acceptFamily("POINT", false);
            return;
        }
        if (geometry instanceof MultiPoint) {
            analysis.acceptFamily("POINT", geometry.getNumGeometries() > 1);
            return;
        }
        if (geometry instanceof LineString) {
            analysis.acceptFamily("LINE", false);
            return;
        }
        if (geometry instanceof MultiLineString) {
            analysis.acceptFamily("LINE", geometry.getNumGeometries() > 1);
            return;
        }
        if (geometry instanceof Polygon) {
            analysis.acceptFamily("POLYGON", false);
            return;
        }
        if (geometry instanceof MultiPolygon) {
            analysis.acceptFamily("POLYGON", geometry.getNumGeometries() > 1);
            return;
        }
        if (geometry instanceof GeometryCollection) {
            int beforePoints = analysis.pointParts;
            int beforeLines = analysis.lineParts;
            int beforePolygons = analysis.polygonParts;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                analyzeGeometryBinding(geometry.getGeometryN(i), analysis);
                if (analysis.mixedFamilies) {
                    return;
                }
            }
            int addedParts = (analysis.pointParts - beforePoints)
                    + (analysis.lineParts - beforeLines)
                    + (analysis.polygonParts - beforePolygons);
            if (addedParts > 1) {
                analysis.requiresMulti = true;
            }
        }
    }

    private static void collectPoints(Geometry geometry, List<Point> points) {
        if (geometry == null || geometry.isEmpty() || points == null) {
            return;
        }
        if (geometry instanceof Point) {
            points.add((Point) geometry);
            return;
        }
        if (geometry instanceof MultiPoint || geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectPoints(geometry.getGeometryN(i), points);
            }
        }
    }

    private static void collectLines(Geometry geometry, List<LineString> lines) {
        if (geometry == null || geometry.isEmpty() || lines == null) {
            return;
        }
        if (geometry instanceof LineString) {
            lines.add((LineString) geometry);
            return;
        }
        if (geometry instanceof MultiLineString || geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectLines(geometry.getGeometryN(i), lines);
            }
        }
    }

    private static void collectPolygons(Geometry geometry, List<Polygon> polygons) {
        if (geometry == null || geometry.isEmpty() || polygons == null) {
            return;
        }
        if (geometry instanceof Polygon) {
            polygons.add((Polygon) geometry);
            return;
        }
        if (geometry instanceof MultiPolygon || geometry instanceof GeometryCollection) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                collectPolygons(geometry.getGeometryN(i), polygons);
            }
        }
    }

    private static final class GeometryBindingAnalysis {
        private String family = "";
        private boolean requiresMulti = false;
        private boolean mixedFamilies = false;
        private int pointParts = 0;
        private int lineParts = 0;
        private int polygonParts = 0;

        private void acceptFamily(String candidateFamily, boolean multi) {
            if (candidateFamily == null || candidateFamily.isBlank()) {
                return;
            }
            if (family.isBlank()) {
                family = candidateFamily;
            } else if (!family.equals(candidateFamily)) {
                mixedFamilies = true;
                return;
            }
            requiresMulti = requiresMulti || multi;
            if ("POINT".equals(candidateFamily)) {
                pointParts++;
            } else if ("LINE".equals(candidateFamily)) {
                lineParts++;
            } else if ("POLYGON".equals(candidateFamily)) {
                polygonParts++;
            }
        }
    }
}
