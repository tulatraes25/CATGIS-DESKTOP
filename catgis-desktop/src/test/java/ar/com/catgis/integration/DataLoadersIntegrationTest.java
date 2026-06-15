package ar.com.catgis.integration;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.GeoPackageLayer;
import ar.com.catgis.GeoPackageLoader;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.ShapefileLoader;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DataLoadersIntegrationTest {

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @Test
    void loadsSyntheticGeoTiffFixture() throws Exception {
        Path dem = IntegrationFixtureFactory.ensureDemFixture();
        LocalRasterData data = RasterImageLoader.loadReal(dem.toFile(), "EPSG:4326", "EPSG:4326");

        assertNotNull(data);
        assertTrue(data.isGeoreferenced());
        assertEquals("EPSG:4326", data.getSourceCRS());
        assertEquals("EPSG:4326", data.getDisplayCRS());
        assertTrue(data.getWidth() > 0);
        assertTrue(data.getHeight() > 0);
        assertNotNull(data.getEnvelope());
        assertTrue(Double.isFinite(data.getEnvelope().getMinX()));
    }

    @Test
    void loadsSyntheticShapefileFixture() throws Exception {
        Path shp = IntegrationFixtureFactory.ensureShapefileFixture();
        ShapefileData data = ShapefileLoader.load(shp.toFile());

        assertNotNull(data);
        assertEquals(2, data.getFeatureCount());
        assertNotNull(data.getSchema());
        assertNotNull(data.getSchema().getCoordinateReferenceSystem());
        assertEquals("EPSG:4326",
                CRSDefinitions.normalizeCode(org.geotools.referencing.CRS.toSRS(data.getSchema().getCoordinateReferenceSystem(), true)));
        assertEquals("Point A", data.getFeatures().get(0).getAttribute("name"));
        Geometry geometry = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertTrue(geometry instanceof Point);
        assertFalse(geometry.isEmpty());
        assertNotNull(data.getEnvelope());
    }

    @Test
    void loadsSyntheticGeoPackageFixture() throws Exception {
        Path gpkg = IntegrationFixtureFactory.ensureGeoPackageFixture();
        GeoPackageLayer layer = IntegrationFixtureFactory.buildGeoPackageLayer(gpkg);
        ShapefileData data = GeoPackageLoader.loadLayerData(layer);

        assertNotNull(data);
        assertEquals(2, data.getFeatureCount());
        assertNotNull(data.getSchema());
        assertEquals("EPSG:4326", layer.getSourceCRS());
        assertEquals("Point A", data.getFeatures().get(0).getAttribute("name"));
        Geometry geometry = (Geometry) data.getFeatures().get(0).getDefaultGeometry();
        assertTrue(geometry instanceof Point);
        assertFalse(geometry.isEmpty());
        assertNotNull(data.getEnvelope());
    }
}
