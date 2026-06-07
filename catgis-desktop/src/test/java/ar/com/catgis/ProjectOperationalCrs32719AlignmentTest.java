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

class ProjectOperationalCrs32719AlignmentTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void keepsDerivedResultsAlignedWithDemOperationalCrs32719() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-project-crs-32719");
        Path demPath = tempDir.resolve("dem_3857_to_32719.tif");
        writeDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto 32719");
            CatgisDesktopApp.currentProject.setProjectCRS("EPSG:32719");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:32719", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM 32719", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:32719"));
            CatgisDesktopApp.currentProject.addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            DrainageExtractionService.GeneratedDrainageLayer drainage =
                    DrainageExtractionService.generateDrainage(
                            demLayer,
                            4,
                            "Escorrentias 32719",
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ROBUST,
                            0d,
                            DrainageExtractionService.CleanupLevel.BALANCED
                    );

            TerrainHydrologyAnalysisService.AnalysisResult analysis =
                    TerrainHydrologyAnalysisService.generateAnalysis(
                            new TerrainHydrologyAnalysisService.AnalysisRequest(
                                    demLayer,
                                    "Topo 32719",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                    4,
                                    8,
                                    false,
                                    true,
                                    false,
                                    false,
                                    true,
                                    false,
                                    true,
                                    true,
                                    false
                            )
                    );

            FloodScenarioService.FloodScenarioResult flood =
                    FloodScenarioService.generateScenario(
                            new FloodScenarioService.FloodScenarioRequest(
                                    demLayer,
                                    "Topo 32719",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                    4,
                                    50d,
                                    0.7d,
                                    15d
                            )
                    );

            TerrainHydrologyAnalysisService.GeneratedVectorLayer basins = analysis.vectorLayers().stream()
                    .filter(layer -> "basins".equalsIgnoreCase(layer.operation()))
                    .findFirst()
                    .orElseThrow();
            TerrainHydrologyAnalysisService.GeneratedRasterLayer slope = analysis.rasterLayers().stream()
                    .filter(layer -> TerrainHydrologyAnalysisService.OP_SLOPE.equalsIgnoreCase(layer.operation()))
                    .findFirst()
                    .orElseThrow();

            Envelope demEnvelope = demData.getEnvelope();
            assertNotNull(demEnvelope);

            assertEquals("EPSG:3857", demLayer.getSourceCRS());
            assertEquals("EPSG:32719", drainage.layer().getSourceCRS());
            assertEquals("EPSG:32719", basins.layer().getSourceCRS());
            assertEquals("EPSG:32719", slope.layer().getSourceCRS());
            assertEquals("EPSG:32719", flood.layer().getSourceCRS());
            assertEquals("EPSG:32719", demData.getDisplayCRS());

            assertTrue(drainage.data().getEnvelope().intersects(demEnvelope));
            assertTrue(basins.data().getEnvelope().intersects(demEnvelope));
            assertTrue(slope.data().getEnvelope().intersects(demEnvelope));
            assertTrue(flood.data().getEnvelope().intersects(demEnvelope));
        });
    }

    private static void writeDem3857(Path file) throws Exception {
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, 20, 20, 1);
        DataBufferFloat buffer = new DataBufferFloat(20 * 20);
        float[] values = buffer.getData();
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                values[row * 20 + col] = 320f + (row * 4f) - (Math.abs(col - 10) * 6f);
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7660200,
                -3881200, -3876200,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("dem-3857", raster, envelope);
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
