package ar.com.catgis.integration;

import ar.com.catgis.ContourGenerationService;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContourIntegrationTest {

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearAppContext();
    }

    @Test
    void generatesValidContoursFromSyntheticDem() throws Exception {
        Path demPath = IntegrationFixtureFactory.ensureDemFixture();

        IntegrationTestSupport.runOnEdt(() -> {
            IntegrationTestSupport.initializeAppContext("Integration contours");
            var project = ar.com.catgis.AppContext.project();
            project.setProjectCRS("EPSG:4326");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:4326", "EPSG:4326");
            RasterLayer demLayer = new RasterLayer("Integration DEM", demPath.toString());
            demLayer.setSourceName("test_dem.tif");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:4326"));
            project.addLayer(demLayer);
            ar.com.catgis.CatgisDesktopApp.layersPanel.addLayer(demLayer);
            ar.com.catgis.AppContext.mapPanel().addOrUpdateRasterLayer(demLayer, demData);

            ContourGenerationService.GeneratedContourLayer result =
                    ContourGenerationService.generateContours(demLayer, 10d, 5, "Integration Contours", false, false, null);

            assertNotNull(result);
            assertNotNull(result.layer());
            ShapefileData data = result.data();
            assertNotNull(data);
            assertTrue(data.getFeatureCount() > 0, "No se generaron curvas.");
            assertNotNull(data.getSchema());
            assertNotNull(data.getSchema().getCoordinateReferenceSystem());
            assertEquals("EPSG:4326", result.layer().getSourceCRS());

            Envelope envelope = data.getEnvelope();
            assertNotNull(envelope);
            assertFalse(envelope.isNull());
            assertTrue(Double.isFinite(envelope.getMinX()));
            assertTrue(Double.isFinite(envelope.getMaxX()));
            assertTrue(Double.isFinite(envelope.getMinY()));
            assertTrue(Double.isFinite(envelope.getMaxY()));

            List<SimpleFeature> features = data.getFeatures();
            double minElevation = Double.POSITIVE_INFINITY;
            double maxElevation = Double.NEGATIVE_INFINITY;
            for (SimpleFeature feature : features) {
                Object geomObj = feature.getDefaultGeometry();
                assertTrue(geomObj instanceof LineString || geomObj instanceof MultiLineString,
                        "Geometría inesperada: " + (geomObj == null ? "null" : geomObj.getClass().getName()));
                Geometry geometry = (Geometry) geomObj;
                assertFalse(geometry.isEmpty(), "Curva vacía detectada.");
                double elevation = ((Number) feature.getAttribute("elevation_m")).doubleValue();
                minElevation = Math.min(minElevation, elevation);
                maxElevation = Math.max(maxElevation, elevation);
                double remainder = Math.abs(elevation % 10d);
                assertTrue(remainder < 1e-6 || Math.abs(remainder - 10d) < 1e-6,
                        "Cota fuera de intervalo: " + elevation);
            }

            assertTrue(minElevation >= 100d, "Cota mínima inesperada: " + minElevation);
            assertTrue(maxElevation <= 200d, "Cota máxima inesperada: " + maxElevation);
        });
    }
}
