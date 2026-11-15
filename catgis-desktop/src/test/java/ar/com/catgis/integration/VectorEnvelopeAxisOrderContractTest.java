package ar.com.catgis.integration;

import ar.com.catgis.GeoPackageLayer;
import ar.com.catgis.GeoPackageLoader;
import ar.com.catgis.ShapefileLoader;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DC-2 contract: a layer envelope is the (x, y) bounds of its stored geometries.
 * A CRS whose declared axis order is latitude-first must never swap it, because
 * that would make the layer envelope disagree with the features that are drawn
 * and reprojected (zoom-to-layer, intersection checks, global envelope).
 */
class VectorEnvelopeAxisOrderContractTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final Coordinate STORED_COORDINATE = new Coordinate(-58.45, -34.62);
    private static final double DELTA = 1e-9;

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @Test
    void latitudeFirstCrsDoesNotSwapTheStoredEnvelope() throws Exception {
        CoordinateReferenceSystem latitudeFirst = CRS.decode("EPSG:4326");
        assertLatitudeFirst(latitudeFirst);

        ShapefileData data = buildPointData(latitudeFirst);

        assertEnvelopeIsStoredCoordinates(data);
    }

    @Test
    void longitudeFirstCrsKeepsTheStoredEnvelope() throws Exception {
        CoordinateReferenceSystem longitudeFirst = CRS.decode("EPSG:4326", true);
        assertEquals("EAST", longitudeFirst.getCoordinateSystem().getAxis(0).getDirection().name());

        ShapefileData data = buildPointData(longitudeFirst);

        assertEnvelopeIsStoredCoordinates(data);
    }

    @Test
    void collectionConstructorKeepsRawBoundsForLatitudeFirstCrs() throws Exception {
        ShapefileData data = buildPointData(CRS.decode("EPSG:4326"));
        SimpleFeature feature = data.getFeatures().get(0);
        SimpleFeatureCollection collection = new ListFeatureCollection(data.getSchema(), List.of(feature));

        ShapefileData rebuilt = new ShapefileData(collection, "latlon", 1);

        assertEnvelopeIsStoredCoordinates(rebuilt);
    }

    @Test
    void realShapefileFixtureEnvelopeMatchesItsGeometryBounds() throws Exception {
        Path shp = IntegrationFixtureFactory.ensureShapefileFixture();
        ShapefileData data = ShapefileLoader.load(shp.toFile());

        assertEnvelopeMatchesGeometryBounds(data);
        assertEquals(-58.45d, data.getEnvelope().getMinX(), DELTA,
                "La longitud -58.45 debe quedar en X.");
        assertEquals(-34.62d, data.getEnvelope().getMinY(), DELTA,
                "La latitud -34.62 debe quedar en Y.");
        assertEquals(-58.37d, data.getEnvelope().getMaxX(), DELTA);
        assertEquals(-34.57d, data.getEnvelope().getMaxY(), DELTA);
    }

    @Test
    void realGeoPackageFixtureEnvelopeMatchesItsGeometryBounds() throws Exception {
        Path gpkg = IntegrationFixtureFactory.ensureGeoPackageFixture();
        GeoPackageLayer layer = IntegrationFixtureFactory.buildGeoPackageLayer(gpkg);
        ShapefileData data = GeoPackageLoader.loadLayerData(layer);

        assertEnvelopeMatchesGeometryBounds(data);
        assertEquals(-58.45d, data.getEnvelope().getMinX(), DELTA,
                "La longitud -58.45 debe quedar en X.");
        assertEquals(-34.62d, data.getEnvelope().getMinY(), DELTA,
                "La latitud -34.62 debe quedar en Y.");
        assertEquals(-58.37d, data.getEnvelope().getMaxX(), DELTA);
        assertEquals(-34.57d, data.getEnvelope().getMaxY(), DELTA);
    }

    private static ShapefileData buildPointData(CoordinateReferenceSystem crs) throws Exception {
        SimpleFeatureType type = buildPointType(crs);
        SimpleFeature feature = SimpleFeatureBuilder.build(
                type,
                new Object[] {GEOMETRY_FACTORY.createPoint(STORED_COORDINATE), "Point A"},
                "axis.1"
        );
        Envelope rawEnvelope = new Envelope(STORED_COORDINATE);
        return new ShapefileData(List.of(feature), rawEnvelope, "axis-fixture", 1, "ok", type);
    }

    private static SimpleFeatureType buildPointType(CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("axis_points");
        builder.setCRS(crs);
        builder.add("the_geom", Point.class);
        builder.add("name", String.class);
        return builder.buildFeatureType();
    }

    private static void assertLatitudeFirst(CoordinateReferenceSystem crs) {
        assertEquals("NORTH", crs.getCoordinateSystem().getAxis(0).getDirection().name(),
                "El fixture del contrato debe declarar el eje de latitud primero.");
    }

    private static void assertEnvelopeIsStoredCoordinates(ShapefileData data) {
        assertEnvelopeMatchesGeometryBounds(data);
        assertEquals(STORED_COORDINATE.x, data.getEnvelope().getMinX(), DELTA,
                "minX debe ser la longitud almacenada, no la latitud.");
        assertEquals(STORED_COORDINATE.x, data.getEnvelope().getMaxX(), DELTA);
        assertEquals(STORED_COORDINATE.y, data.getEnvelope().getMinY(), DELTA,
                "minY debe ser la latitud almacenada, no la longitud.");
        assertEquals(STORED_COORDINATE.y, data.getEnvelope().getMaxY(), DELTA);
    }

    private static void assertEnvelopeMatchesGeometryBounds(ShapefileData data) {
        assertNotNull(data.getEnvelope(), "La capa debe exponer una envolvente.");
        Envelope rawBounds = data.getFeatureCollection().getBounds();
        assertNotNull(rawBounds, "La coleccion debe exponer bounds.");
        assertEquals(rawBounds.getMinX(), data.getEnvelope().getMinX(), DELTA,
                "La envolvente de capa debe coincidir con los bounds de sus geometrias.");
        assertEquals(rawBounds.getMinY(), data.getEnvelope().getMinY(), DELTA);
        assertEquals(rawBounds.getMaxX(), data.getEnvelope().getMaxX(), DELTA);
        assertEquals(rawBounds.getMaxY(), data.getEnvelope().getMaxY(), DELTA);
    }
}
