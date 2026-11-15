package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real-file project-line roundtrip after in-memory vector reprojection. */
class LocalVectorCrsProjectRoundTripContractTest {
    @TempDir Path folder;

    @Test
    void realUtmShapefileKeepsPrjSourceAcrossProjectedSaveAndReopen() throws Exception {
        Path shp = utmShapefile();
        byte[] originalShp = Files.readAllBytes(shp);
        byte[] originalPrj = Files.readAllBytes(shp.resolveSibling("survey.prj"));
        ShapefileData data = ShapefileLoader.load(shp.toFile());
        assertEquals(1, data.getFeatureCount());
        assertEquals("EPSG:32719", ShapefileLoader.getCRSCode(shp.toFile()));

        Point sourcePoint = (Point) data.getFeatures().get(0).getDefaultGeometry();
        assertEquals(500000d, sourcePoint.getX(), 0.001d);
        assertEquals(4900000d, sourcePoint.getY(), 0.001d);
        Point projected = (Point) VectorCrsTransformSupport.reprojectGeometry(
                sourcePoint, "EPSG:32719", "EPSG:4326");
        assertTrue(projected.getX() > -70d && projected.getX() < -68d);
        assertTrue(projected.getY() > -48d && projected.getY() < -45d);

        Layer layer = new VectorLayer("Real UTM survey", shp.toString());
        layer.setSourceCRS(ShapefileLoader.getCRSCode(shp.toFile()));
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer, data);
        layer.setSourceCRS("EPSG:4326"); // actual CATGIS display projection state
        assertEquals("EPSG:32719", LocalVectorSourceCrsProvenance.crsForProjectFile(layer));
        assertProjectRoundTrip(layer, "EPSG:32719", "EPSG:4326");

        assertArrayEquals(originalShp, Files.readAllBytes(shp));
        assertArrayEquals(originalPrj, Files.readAllBytes(shp.resolveSibling("survey.prj")));
    }

    @Test
    void shapefileSchemaCrsIsCapturedWhenAuthorityTextIsMissing() throws Exception {
        Path shp = utmShapefile();
        ShapefileData data = ShapefileLoader.load(shp.toFile());
        Layer layer = new VectorLayer("WKT source", shp.toString());
        // A .prj schema can contain valid CRS even if lookupIdentifier is null.
        assertEquals("", layer.getSourceCRS());
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer, data);
        assertTrue(VectorCrsTransformSupport.equivalent(
                LocalVectorSourceCrsProvenance.crsForProjectFile(layer), "EPSG:32719"));
        layer.setSourceCRS("EPSG:4326");
        assertTrue(VectorCrsTransformSupport.equivalent(
                serializedSourceCrs(SaveProjectAction.buildLayerLine(layer)), "EPSG:32719"));
    }

    @Test
    void actualGeoJsonAndKmlFilesRemainWgs84AfterProjectedSaveAndReopen() throws Exception {
        Path geojson = folder.resolve("field sample.geojson");
        Files.writeString(geojson,
                "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\","
                        + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-70.0,-45.0]},"
                        + "\"properties\":{\"name\":\"Site A\"}}]}",
                StandardCharsets.UTF_8);
        Path kml = folder.resolve("field sample.kml");
        Files.writeString(kml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>"
                        + "<Placemark><name>Site A</name><Point>"
                        + "<coordinates>-70.0,-45.0,0</coordinates></Point></Placemark>"
                        + "</Document></kml>", StandardCharsets.UTF_8);

        for (Path file : List.of(geojson, kml)) {
            byte[] before = Files.readAllBytes(file);
            ShapefileData data = file.toString().endsWith(".kml")
                    ? KmlLoader.load(file.toFile()) : GeoJsonLoader.load(file.toFile());
            assertNotNull(data);
            assertTrue(data.getFeatureCount() > 0,
                    "A real document must produce features: " + file);
            Point point = (Point) data.getFeatures().get(0).getDefaultGeometry();
            assertEquals(-70d, point.getX(), 0.001d);
            assertEquals(-45d, point.getY(), 0.001d);

            Layer layer = new VectorLayer("WGS84 source", file.toString());
            layer.setSourceCRS("EPSG:4326"); // KML and RFC 7946 GeoJSON semantics
            LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer, data);
            layer.setSourceCRS("EPSG:32719");
            assertProjectRoundTrip(layer, "EPSG:4326", "EPSG:32719");
            assertArrayEquals(before, Files.readAllBytes(file));
        }
    }

    @Test
    void missingCrsDoesNotInventAuthorityAndChangedPathInvalidatesProvenance() {
        Layer shapefile = new VectorLayer("no-prj", folder.resolve("unknown.shp").toString());
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(shapefile, null);
        assertEquals("", LocalVectorSourceCrsProvenance.crsForProjectFile(shapefile));

        shapefile.setSourceCRS("EPSG:32719");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(shapefile);
        shapefile.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:32719", LocalVectorSourceCrsProvenance.crsForProjectFile(shapefile));
        shapefile.setPath(folder.resolve("different.shp").toString());
        assertEquals("EPSG:4326", LocalVectorSourceCrsProvenance.crsForProjectFile(shapefile));
        assertFalse(LocalVectorSourceCrsProvenance.crsForProjectFile(shapefile).isBlank());
    }

    private void assertProjectRoundTrip(Layer layer, String source, String displayed) {
        String line = SaveProjectAction.buildLayerLine(layer);
        assertEquals(source, serializedSourceCrs(line));
        Layer reopened = LoadProjectAction.parseLayer(line);
        assertNotNull(reopened);
        assertEquals(source, reopened.getSourceCRS());
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(reopened);
        reopened.setSourceCRS(displayed);
        assertEquals(source, serializedSourceCrs(SaveProjectAction.buildLayerLine(reopened)));
        assertEquals(displayed, reopened.getSourceCRS());
    }

    private static String serializedSourceCrs(String line) {
        return line.split("\\|", -1)[12];
    }

    private Path utmShapefile() throws Exception {
        Path shp = folder.resolve("survey.shp");
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("survey");
        tb.setCRS(CRS.decode("EPSG:32719", true));
        tb.add("the_geom", Point.class);
        tb.add("name", String.class);
        SimpleFeatureType type = tb.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        fb.add(new GeometryFactory().createPoint(new Coordinate(500000d, 4900000d)));
        fb.add("Sample");
        SimpleFeature point = fb.buildFeature("survey.1");
        ShapefileDataStore store = new ShapefileDataStore(shp.toUri().toURL());
        try {
            store.createSchema(type);
            SimpleFeatureStore featureStore =
                    (SimpleFeatureStore) store.getFeatureSource(store.getTypeNames()[0]);
            featureStore.addFeatures(new ListFeatureCollection(type, List.of(point)));
        } finally {
            store.dispose();
        }
        return shp;
    }
}
