package ar.com.catgis.integration;

import ar.com.catgis.AppContext;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.DrainageExtractionService;
import ar.com.catgis.FloodScenarioService;
import ar.com.catgis.RasterImageLoader;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.RasterSidecarSupport;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FloodScenarioRoundTripIntegrationTest {

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearAppContext();
    }

    @Test
    void persistsFloodScenarioAcrossSaveReloadAndGeoTiffExport() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-integration-flood");
        Path demPath = tempDir.resolve("dem_flood_3857.tif");
        Path projectFile = tempDir.resolve("Proyecto inundacion 22182.catgis");
        Path exportPath = IntegrationFixtureFactory.outputsDir().resolve("flood-integration-export.tif");
        writeFloodDem3857(demPath);

        IntegrationTestSupport.runOnEdt(() -> {
            IntegrationTestSupport.initializeAppContext("Proyecto inundacion 22182");
            AppContext.project().setProjectCRS("EPSG:22182");
            AppContext.project().setProjectFile(projectFile.toFile());

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM inundacion 22182", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            AppContext.project().addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            AppContext.mapPanel().addOrUpdateRasterLayer(demLayer, demData);

            FloodScenarioService.FloodScenarioResult floodResult = FloodScenarioService.generateScenario(
                    new FloodScenarioService.FloodScenarioRequest(
                            demLayer,
                            "DEM inundacion 22182",
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ADVANCED,
                            4,
                            60d,
                            0.70d,
                            5d
                    )
            );

            AppContext.project().addLayer(floodResult.layer());
            CatgisDesktopApp.layersPanel.addLayer(floodResult.layer());
            AppContext.mapPanel().addOrUpdateRasterLayer(floodResult.layer(), floodResult.data());

            assertEquals("EPSG:22182", floodResult.layer().getSourceCRS());
            assertEquals("EPSG:22182", floodResult.data().getDisplayCRS());
            assertTrue(floodResult.affectedCells() > 0);

            FloodScenarioService.exportScenarioDepthGeoTiff(floodResult, exportPath.toFile());
            assertTrue(Files.exists(exportPath));

            assertTrue(IntegrationTestSupport.saveProject(projectFile.toFile(), false));
        });

        IntegrationTestSupport.runOnEdt(() -> {
            IntegrationTestSupport.initializeAppContext("Reload inundacion 22182");
            assertTrue(IntegrationTestSupport.loadProject(projectFile.toFile(), false));

            Layer floodLayer = findLayer("Inundacion preliminar - DEM inundacion 22182 - 60mm");
            assertNotNull(floodLayer);
            assertEquals("EPSG:22182", floodLayer.getSourceCRS());

            LocalRasterData floodData = AppContext.mapPanel().getRasterData(floodLayer);
            assertNotNull(floodData);
            assertEquals("EPSG:22182", floodData.getDisplayCRS());
        });

        LocalRasterData exported = RasterImageLoader.loadReal(exportPath.toFile(), "EPSG:22182", "EPSG:22182");
        assertEquals("EPSG:22182", RasterCoverageSupport.resolveOperationalRasterCrs(exported, "EPSG:22182"));
        assertEquals("EPSG:22182", exported.getDisplayCRS());
        assertTrue(exported.getWidth() > 0);
        assertTrue(exported.getHeight() > 0);
    }

    private static Layer findLayer(String name) {
        return AppContext.project().getLayers().stream()
                .filter(layer -> name.equals(layer.getName()))
                .findFirst()
                .orElse(null);
    }

    private static void writeFloodDem3857(Path file) throws Exception {
        int width = 20;
        int height = 20;
        float[] values = new float[width * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float basinShape = (float) (220d + (row * 2.5d) + (Math.abs(col - 10) * 1.7d));
                float depression = (row > 6 && row < 15 && col > 6 && col < 15) ? 18f : 0f;
                values[(row * width) + col] = basinShape - depression;
            }
        }
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(values, values.length);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665100, -7660900,
                -3881100, -3876900,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("flood-dem-3857", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, (org.geotools.api.parameter.GeneralParameterValue[]) null);
        } finally {
            writer.dispose();
        }
        RasterSidecarSupport.write(
                file.toFile(),
                new Envelope(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY()),
                "EPSG:3857"
        );
    }
}
