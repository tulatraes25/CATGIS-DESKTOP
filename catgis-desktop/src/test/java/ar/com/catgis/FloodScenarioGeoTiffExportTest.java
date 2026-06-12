package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloodScenarioGeoTiffExportTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void exportsPreliminaryFloodRasterAsGeotiff() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-flood-export");
        Path demPath = tempDir.resolve("dem_export_3857.tif");
        Path exportPath = tempDir.resolve("inundacion_preliminar_50mm.tif");
        writeFloodDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Flood export 22182");
            AppContext.project().setProjectCRS("EPSG:22182");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM flood export", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            AppContext.project().addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            FloodScenarioService.FloodScenarioResult result = FloodScenarioService.generateScenario(
                    new FloodScenarioService.FloodScenarioRequest(
                            demLayer,
                            "DEM flood export",
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ADVANCED,
                            4,
                            50d,
                            0.70d,
                            5d
                    )
            );

            FloodScenarioService.exportScenarioDepthGeoTiff(result, exportPath.toFile());
            assertTrue(Files.exists(exportPath));

            LocalRasterData exported = RasterImageLoader.loadReal(exportPath.toFile(), "EPSG:22182", "EPSG:22182");
            assertEquals("EPSG:22182", RasterCoverageSupport.resolveOperationalRasterCrs(exported, "EPSG:22182"));
            assertEquals("EPSG:22182", exported.getDisplayCRS());
            assertTrue(exported.getWidth() > 0);
            assertTrue(exported.getHeight() > 0);
        });
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
        GridCoverage2D coverage = new GridCoverageFactory().create("flood-export-dem-3857", raster, envelope);
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
