package ar.com.catgis.integration;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.GeoPackageLayer;
import ar.com.catgis.GeoPackageLoader;
import ar.com.catgis.OnlineMapCatalog;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LogicalRenderSmokeIntegrationTest {

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @Test
    void transformsRasterVectorAndBasemapEnvelopesIntoProjectViewCrs() throws Exception {
        String projectCrsCode = "EPSG:4326";
        CoordinateReferenceSystem projectCrs = CRSDefinitions.decode(projectCrsCode, true);

        Path dem = IntegrationFixtureFactory.ensureDemFixture();
        LocalRasterData raster = RasterImageLoader.loadReal(dem.toFile(), projectCrsCode, projectCrsCode);
        assertEquals(projectCrsCode, raster.getDisplayCRS());
        assertFiniteEnvelope(raster.getEnvelope());

        ShapefileData vector = IntegrationFixtureFactory.loadShapefileFixture();
        assertNotNull(vector.getSchema());
        CoordinateReferenceSystem vectorCrs = vector.getSchema().getCoordinateReferenceSystem();
        assertNotNull(vectorCrs);
        Envelope vectorEnvelope = vector.getEnvelope();
        assertFiniteEnvelope(vectorEnvelope);

        GeoPackageLayer gpkgLayer = IntegrationFixtureFactory.buildGeoPackageLayer(IntegrationFixtureFactory.ensureGeoPackageFixture());
        ShapefileData gpkg = GeoPackageLoader.loadLayerData(gpkgLayer);
        assertFiniteEnvelope(gpkg.getEnvelope());

        Envelope vectorProjectedEnvelope = CRS.equalsIgnoreMetadata(vectorCrs, projectCrs)
                ? new Envelope(vectorEnvelope)
                : JTS.transform(vectorEnvelope, CRS.findMathTransform(vectorCrs, projectCrs, true));
        ReferencedEnvelope vectorProjected = new ReferencedEnvelope(vectorProjectedEnvelope, projectCrs);
        Envelope rasterEnvelope = raster.getEnvelope();
        assertTrue(vectorProjected.intersects(rasterEnvelope),
                "Vector envelope no intersecta raster envelope. vector=" + vectorProjected + " raster=" + rasterEnvelope);

        CoordinateReferenceSystem webMercator = CRSDefinitions.decode("EPSG:3857", true);
        MathTransform projectToWeb = CRS.findMathTransform(projectCrs, webMercator, true);
        Envelope webEnvelope = JTS.transform(raster.getEnvelope(), projectToWeb);
        assertFiniteEnvelope(webEnvelope);

        MathTransform webToProject = CRS.findMathTransform(webMercator, projectCrs, true);
        Envelope roundTripEnvelope = JTS.transform(webEnvelope, webToProject);
        assertFiniteEnvelope(roundTripEnvelope);
        assertTrue(roundTripEnvelope.intersects(raster.getEnvelope()),
                "Envelope 3857->4326 no volvió a intersectar raster. roundTrip=" + roundTripEnvelope
                        + " raster=" + raster.getEnvelope());

        OnlineRasterSource osm = OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_OSM);
        assertNotNull(osm);
        assertEquals("EPSG:3857", osm.getSourceCRS());

        OnlineRasterSource esri = OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY);
        assertNotNull(esri);
        assertEquals("EPSG:3857", esri.getSourceCRS());
    }

    private static void assertFiniteEnvelope(Envelope envelope) {
        assertNotNull(envelope);
        assertFalse(envelope.isNull());
        assertTrue(Double.isFinite(envelope.getMinX()));
        assertTrue(Double.isFinite(envelope.getMaxX()));
        assertTrue(Double.isFinite(envelope.getMinY()));
        assertTrue(Double.isFinite(envelope.getMaxY()));
        assertFalse(Double.isNaN(envelope.getMinX()));
        assertFalse(Double.isNaN(envelope.getMaxX()));
        assertFalse(Double.isNaN(envelope.getMinY()));
        assertFalse(Double.isNaN(envelope.getMaxY()));
    }
}
