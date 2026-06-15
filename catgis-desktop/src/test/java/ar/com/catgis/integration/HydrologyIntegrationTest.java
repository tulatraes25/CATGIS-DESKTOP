package ar.com.catgis.integration;

import ar.com.catgis.DrainageExtractionService;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.TerrainHydrologyAnalysisService;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HydrologyIntegrationTest {

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearAppContext();
    }

    @Test
    void computesHydrologyGridAndDrainageFromSyntheticDem() throws Exception {
        Path demPath = IntegrationFixtureFactory.ensureHydrologyDemFixture();

        IntegrationTestSupport.runOnEdt(() -> {
            IntegrationTestSupport.initializeAppContext("Integration hydrology");
            var project = ar.com.catgis.AppContext.project();
            project.setProjectCRS("EPSG:4326");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:4326", "EPSG:4326");
            RasterLayer demLayer = new RasterLayer("Hydrology DEM", demPath.toString());
            demLayer.setSourceName("test_hydro_dem.tif");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:4326"));
            project.addLayer(demLayer);
            ar.com.catgis.CatgisDesktopApp.layersPanel.addLayer(demLayer);
            ar.com.catgis.AppContext.mapPanel().addOrUpdateRasterLayer(demLayer, demData);

            TerrainHydrologyAnalysisService.AnalysisResult analysis =
                    TerrainHydrologyAnalysisService.generateAnalysis(
                            new TerrainHydrologyAnalysisService.AnalysisRequest(
                                    demLayer,
                                    "Hydrology Integration",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ROBUST,
                                    6,
                                    16,
                                    false,
                                    false,
                                    false,
                                    true,
                                    true,
                                    true,
                                    false,
                                    false,
                                    false
                            )
                    );

            assertNotNull(analysis);
            DrainageExtractionService.HydrologyGrid grid = analysis.grid();
            assertNotNull(grid);
            assertEquals(32, grid.width());
            assertEquals(32, grid.height());
            assertEquals("EPSG:4326", grid.sourceCrsCode());

            long maxAccumulation = Long.MIN_VALUE;
            int maxIndex = -1;
            for (int row = 0; row < grid.height(); row++) {
                for (int col = 0; col < grid.width(); col++) {
                    if (!grid.isValidCell(row, col)) {
                        continue;
                    }
                    assertTrue(Double.isFinite(grid.sourceElevations()[row][col]));
                    assertTrue(Double.isFinite(grid.conditionedElevations()[row][col]));
                    long acc = grid.accumulation()[grid.linearIndex(row, col)];
                    assertTrue(acc >= 1L);
                    if (acc > maxAccumulation) {
                        maxAccumulation = acc;
                        maxIndex = grid.linearIndex(row, col);
                    }
                }
            }
            assertTrue(maxAccumulation > 10L, "Acumulación máxima demasiado baja.");
            int outletRow = maxIndex / grid.width();
            assertTrue(outletRow >= grid.height() - 3,
                    "La acumulación máxima no cayó hacia la salida esperada. row=" + outletRow);

            assertEquals(2, analysis.rasterLayers().size(), "Se esperaban flow direction y flow accumulation.");
            assertEquals(1, analysis.vectorLayers().size(), "Se esperaba una capa de drenaje.");

            ShapefileData drainage = analysis.vectorLayers().get(0).data();
            assertNotNull(drainage);
            assertTrue(drainage.getFeatureCount() > 0);

            Map<String, Integer> endpointUsage = new HashMap<>();
            for (SimpleFeature feature : drainage.getFeatures()) {
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                assertNotNull(geometry);
                assertFalse(geometry.isEmpty());
                assertTrue(geometry instanceof LineString || geometry instanceof MultiLineString);
                if (geometry instanceof LineString line) {
                    registerEndpoint(endpointUsage, line.getCoordinateN(0).x, line.getCoordinateN(0).y);
                    registerEndpoint(endpointUsage, line.getCoordinateN(line.getNumPoints() - 1).x, line.getCoordinateN(line.getNumPoints() - 1).y);
                }
            }
            boolean hasConnectivity = drainage.getFeatureCount() == 1
                    || endpointUsage.values().stream().anyMatch(count -> count >= 2);
            assertTrue(hasConnectivity, "La red drenaje no mostró conectividad básica.");
        });
    }

    private static void registerEndpoint(Map<String, Integer> counts, double x, double y) {
        String key = Math.round(x * 1_000_000d) + ":" + Math.round(y * 1_000_000d);
        counts.merge(key, 1, Integer::sum);
    }
}
