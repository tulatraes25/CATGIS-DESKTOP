package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Project;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectFloodOperationalCrsRoundTripTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void savesAndReloadsFloodScenarioInProjectCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-flood-project-22182");
        Path demPath = tempDir.resolve("dem_flood_3857.tif");
        Path projectFile = tempDir.resolve("Proyecto inundacion 22182.catgis");
        writeFloodDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto inundacion 22182");
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
            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload inundacion 22182");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertNotNull(AppContext.project());
            assertEquals("EPSG:22182", AppContext.project().getProjectCRS());

            Layer floodLayer = findLayer("Inundacion preliminar - DEM inundacion 22182 - 60mm");
            assertNotNull(floodLayer);
            assertEquals("EPSG:22182", floodLayer.getSourceCRS());

            LocalRasterData floodData = AppContext.mapPanel().getRasterData(floodLayer);
            assertNotNull(floodData);
            assertEquals("EPSG:22182", floodData.getDisplayCRS());
            assertTrue(floodLayer instanceof RasterLayer);
            assertTrue(((RasterLayer) floodLayer).isDerivedLayer());
            assertEquals(FloodScenarioService.OP_PRELIMINARY_FLOOD, ((RasterLayer) floodLayer).getDerivedOperation());
        });
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
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float basinShape = (float) (220d + (row * 2.5d) + (Math.abs(col - 10) * 1.7d));
                float depression = (row > 6 && row < 15 && col > 6 && col < 15) ? 18f : 0f;
                values[(row * width) + col] = basinShape - depression;
            }
        }
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
