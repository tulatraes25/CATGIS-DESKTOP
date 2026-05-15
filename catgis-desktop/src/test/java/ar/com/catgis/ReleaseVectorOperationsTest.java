package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseVectorOperationsTest {

    @Test
    void keepsSpatialJoinSummaryAndUnionOverlayUsable() throws Exception {
        Layer layerA = ReleaseTestSupport.buildVectorLayer("Lotes", null, "EPSG:4326");
        Layer layerB = ReleaseTestSupport.buildVectorLayer("Zonas", null, "EPSG:4326");
        Layer layerUnionA = ReleaseTestSupport.buildVectorLayer("UnionA", null, "EPSG:4326");
        Layer layerUnionB = ReleaseTestSupport.buildVectorLayer("UnionB", null, "EPSG:4326");

        ShapefileData spatialJoinLeft = buildSpatialJoinLeft();
        ShapefileData spatialJoinRight = buildSpatialJoinRight();
        ShapefileData unionLeft = buildUnionLeft();
        ShapefileData unionRight = buildUnionRight();

        GeoprocessingAssistantDialog dialog = allocateDialogInstance();
        Object joinOptionA = buildLayerOption(layerA, spatialJoinLeft, "POLYGON", "EPSG:4326");
        Object joinOptionB = buildLayerOption(layerB, spatialJoinRight, "POLYGON", "EPSG:4326");
        Object unionOptionA = buildLayerOption(layerUnionA, unionLeft, "POLYGON", "EPSG:4326");
        Object unionOptionB = buildLayerOption(layerUnionB, unionRight, "POLYGON", "EPSG:4326");

        Class<?> optionClass = Class.forName("ar.com.catgis.GeoprocessingAssistantDialog$LayerOption");
        Method spatialJoinMethod = GeoprocessingAssistantDialog.class.getDeclaredMethod(
                "spatialJoin",
                optionClass,
                optionClass,
                String.class,
                String.class
        );
        spatialJoinMethod.setAccessible(true);
        ShapefileData summaryJoin = (ShapefileData) spatialJoinMethod.invoke(dialog, joinOptionA, joinOptionB, "sj_summary", "Resumen util");
        ShapefileData firstJoin = (ShapefileData) spatialJoinMethod.invoke(dialog, joinOptionA, joinOptionB, "sj_first", "Primera coincidencia");

        Method unionMethod = GeoprocessingAssistantDialog.class.getDeclaredMethod(
                "unionLayers",
                optionClass,
                optionClass,
                String.class
        );
        unionMethod.setAccessible(true);
        ShapefileData union = (ShapefileData) unionMethod.invoke(dialog, unionOptionA, unionOptionB, "union_overlay");

        SimpleFeature summaryA1 = summaryJoin.getFeatures().get(0);
        SimpleFeature firstA1 = firstJoin.getFeatures().get(0);
        Set<String> overlayTypes = new LinkedHashSet<>();
        for (SimpleFeature feature : union.getFeatures()) {
            Object overlayType = feature.getAttribute("ov_type");
            if (overlayType != null) {
                overlayTypes.add(String.valueOf(overlayType));
            }
        }

        assertEquals(2, summaryJoin.getFeatureCount());
        assertEquals(2, ((Number) summaryA1.getAttribute("join_count")).intValue());
        assertEquals(15d, ((Number) summaryA1.getAttribute("sum_valor")).doubleValue(), 0.001d);
        assertEquals(7.5d, ((Number) summaryA1.getAttribute("avg_valor")).doubleValue(), 0.001d);
        assertEquals("FIRST", String.valueOf(firstA1.getAttribute("join_mode")));
        assertEquals(3, union.getFeatureCount());
        assertTrue(overlayTypes.contains("A"));
        assertTrue(overlayTypes.contains("A+B"));
        assertTrue(overlayTypes.contains("B"));
    }

    private static GeoprocessingAssistantDialog allocateDialogInstance() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        return (GeoprocessingAssistantDialog) unsafe.allocateInstance(GeoprocessingAssistantDialog.class);
    }

    private static Object buildLayerOption(Layer layer, ShapefileData data, String family, String crsCode) throws Exception {
        Class<?> optionClass = Class.forName("ar.com.catgis.GeoprocessingAssistantDialog$LayerOption");
        Constructor<?> constructor = optionClass.getDeclaredConstructor(Layer.class, ShapefileData.class, String.class, String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(layer, data, family, crsCode);
    }

    private static ShapefileData buildSpatialJoinLeft() throws Exception {
        SimpleFeatureType type = polygonType("spatial_join_left", "nombre", String.class);

        List<SimpleFeature> features = new ArrayList<>();
        features.add(ReleaseTestSupport.buildFeature(type, "left.1", square(0, 0, 2, 2), "A1"));
        features.add(ReleaseTestSupport.buildFeature(type, "left.2", square(3, 0, 5, 2), "A2"));

        return new ShapefileData(features, square(0, 0, 5, 2).getEnvelopeInternal(), "spatial_join_left", features.size(), "ok", type);
    }

    private static ShapefileData buildSpatialJoinRight() throws Exception {
        SimpleFeatureType type = polygonType(
                "spatial_join_right",
                "zona", String.class,
                "valor", Double.class
        );

        List<SimpleFeature> features = new ArrayList<>();
        features.add(ReleaseTestSupport.buildFeature(type, "right.1", square(1, 0, 4, 2), "Norte", 10d));
        features.add(ReleaseTestSupport.buildFeature(type, "right.2", square(0.5, 0.5, 1.5, 1.5), "Centro", 5d));

        return new ShapefileData(features, square(0.5, 0, 4, 2).getEnvelopeInternal(), "spatial_join_right", features.size(), "ok", type);
    }

    private static ShapefileData buildUnionLeft() throws Exception {
        SimpleFeatureType type = polygonType("union_left", "nombre_a", String.class);
        List<SimpleFeature> features = List.of(ReleaseTestSupport.buildFeature(type, "union_a.1", square(0, 0, 2, 2), "Lote A"));
        return new ShapefileData(features, square(0, 0, 2, 2).getEnvelopeInternal(), "union_left", features.size(), "ok", type);
    }

    private static ShapefileData buildUnionRight() throws Exception {
        SimpleFeatureType type = polygonType("union_right", "nombre_b", String.class);
        List<SimpleFeature> features = List.of(ReleaseTestSupport.buildFeature(type, "union_b.1", square(1, 1, 3, 3), "Zona B"));
        return new ShapefileData(features, square(1, 1, 3, 3).getEnvelopeInternal(), "union_right", features.size(), "ok", type);
    }

    private static SimpleFeatureType polygonType(String name, Object... attributes) throws Exception {
        Object[][] defs = new Object[attributes.length / 2][2];
        for (int i = 0; i < attributes.length; i += 2) {
            defs[i / 2][0] = attributes[i];
            defs[i / 2][1] = attributes[i + 1];
        }
        return ReleaseTestSupport.createFeatureType(name, "EPSG:4326", Polygon.class, defs);
    }

    private static Polygon square(double minX, double minY, double maxX, double maxY) {
        return ReleaseTestSupport.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(minX, minY),
                new Coordinate(maxX, minY),
                new Coordinate(maxX, maxY),
                new Coordinate(minX, maxY),
                new Coordinate(minX, minY)
        });
    }
}
