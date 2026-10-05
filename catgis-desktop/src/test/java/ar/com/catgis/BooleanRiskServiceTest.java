package ar.com.catgis;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanRiskServiceTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void generatesBooleanRiskMasksAndVectorInProjectOperationalCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-boolean-risk");
        Path demPath = tempDir.resolve("dem_riesgo_3857.tif");
        Path soilPath = tempDir.resolve("soil_arcilla_3857.tif");
        writeDem3857(demPath);
        writeSoil3857(soilPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto riesgo 22182");
            AppContext.project().setProjectCRS("EPSG:22182");

            RasterLayer demLayer = addRasterLayer(demPath, "DEM riesgo base", "DEM local", "EPSG:22182");
            RasterLayer soilLayer = addRasterLayer(soilPath, "Arcilla SoilGrids", "SoilGrids clay 0-5 cm", "EPSG:22182");

            BooleanRiskService.RiskRequest andRequest = new BooleanRiskService.RiskRequest(
                    demLayer,
                    soilLayer,
                    "Riesgo 22182",
                    DrainageExtractionService.AnalysisDetail.BALANCED,
                    DrainageExtractionService.HydrologicConditioning.ROBUST,
                    BooleanRiskService.RiskRule.from(BooleanRiskService.ComparisonMode.GREATER_THAN, 1d, 1d),
                    BooleanRiskService.RiskRule.from(BooleanRiskService.ComparisonMode.GREATER_THAN, 300d, 300d),
                    BooleanRiskService.LogicMode.AND,
                    true,
                    true,
                    true
            );

            BooleanRiskService.RiskResult andResult = BooleanRiskService.generateRisk(andRequest);
            assertTrue(andResult.positiveCellCount() > 0);
            assertTrue(andResult.positiveSlopeCellCount() > 0);
            assertTrue(andResult.positiveSoilCellCount() > 0);
            assertTrue(andResult.vectorFeatureCount() > 0);
            assertEquals(3, andResult.rasterLayers().size());
            assertNotNull(andResult.vectorLayer());
            assertTrue(andResult.intersectingSoilSamples() > 0);
            assertTrue(Double.isFinite(andResult.positiveAreaHectares()));

            Envelope demEnvelope = AppContext.mapPanel().getRasterData(demLayer).getEnvelope();
            for (BooleanRiskService.GeneratedRasterLayer generated : andResult.rasterLayers()) {
                assertEquals("EPSG:22182", generated.layer().getSourceCRS());
                assertEquals("EPSG:22182", generated.data().getDisplayCRS());
                assertEnvelopeClose(demEnvelope, generated.data().getEnvelope(), 1e-6);
            }

            assertEquals("EPSG:22182", andResult.vectorLayer().layer().getSourceCRS());
            assertNotNull(andResult.vectorLayer().data());
            assertTrue(andResult.vectorLayer().data().getEnvelope().intersects(demEnvelope));
            assertTrue(Files.exists(Path.of(andResult.vectorLayer().layer().getPath())));
            assertNotNull(andResult.vectorLayer().data().getSchema().getDescriptor("zone_name"));
            assertNotNull(andResult.vectorLayer().data().getSchema().getDescriptor("risk_val"));
            assertNotNull(andResult.vectorLayer().data().getSchema().getDescriptor("dem_name"));
            assertNotNull(andResult.vectorLayer().data().getSchema().getDescriptor("soil_name"));

            BooleanRiskService.RiskRequest orRequest = new BooleanRiskService.RiskRequest(
                    demLayer,
                    soilLayer,
                    "Riesgo 22182 OR",
                    DrainageExtractionService.AnalysisDetail.BALANCED,
                    DrainageExtractionService.HydrologicConditioning.ROBUST,
                    BooleanRiskService.RiskRule.from(BooleanRiskService.ComparisonMode.GREATER_THAN, 1d, 1d),
                    BooleanRiskService.RiskRule.from(BooleanRiskService.ComparisonMode.GREATER_THAN, 300d, 300d),
                    BooleanRiskService.LogicMode.OR,
                    false,
                    false,
                    false
            );

            BooleanRiskService.RiskResult orResult = BooleanRiskService.generateRisk(orRequest);
            assertTrue(orResult.positiveCellCount() >= andResult.positiveCellCount());
            BooleanRiskService.GeneratedRasterLayer riskRaster = orResult.rasterLayers().stream()
                    .filter(layer -> BooleanRiskService.OP_PRELIMINARY_BOOLEAN_RISK.equalsIgnoreCase(layer.operation()))
                    .findFirst()
                    .orElseThrow();
            assertEquals("EPSG:22182", riskRaster.layer().getSourceCRS());
            assertEquals("EPSG:22182", riskRaster.data().getDisplayCRS());
            assertEnvelopeClose(demEnvelope, riskRaster.data().getEnvelope(), 1e-6);
        });
    }

    private static RasterLayer addRasterLayer(Path rasterPath, String layerName, String sourceName, String projectCrs) throws Exception {
        LocalRasterData rasterData = RasterImageLoader.loadReal(rasterPath.toFile(), projectCrs, "EPSG:3857");
        RasterLayer layer = new RasterLayer(layerName, rasterPath.toString());
        layer.setSourceName(sourceName);
        layer.setFeatureCount(1);
        layer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(rasterData, projectCrs));
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.layersPanel.addLayer(layer);
        AppContext.mapPanel().addOrUpdateRasterLayer(layer, rasterData);
        return layer;
    }

    private static void assertEnvelopeClose(Envelope expected, Envelope actual, double tolerance) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(Math.abs(expected.getMinX() - actual.getMinX()) <= tolerance);
        assertTrue(Math.abs(expected.getMaxX() - actual.getMaxX()) <= tolerance);
        assertTrue(Math.abs(expected.getMinY() - actual.getMinY()) <= tolerance);
        assertTrue(Math.abs(expected.getMaxY() - actual.getMaxY()) <= tolerance);
    }

    private static void writeDem3857(Path file) throws Exception {
        int width = 24;
        int height = 24;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float ridge = (float) (320d + (row * 3.5d) + (Math.abs(col - 12) * 6.5d));
                float channel = (row > 5 && row < 19 && col > 9 && col < 15) ? 24f : 0f;
                values[(row * width) + col] = ridge - channel;
            }
        }
        writeRaster3857(file, width, height, values, "dem-riesgo-3857");
    }

    private static void writeSoil3857(Path file) throws Exception {
        int width = 24;
        int height = 24;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = (float) (180d + (col * 11d) + (row * 4d));
            }
        }
        writeRaster3857(file, width, height, values, "soil-riesgo-3857");
    }

    private static void writeRaster3857(Path file, int width, int height, float[] values, String name) throws Exception {
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(values, values.length);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7660400,
                -3881400, -3876600,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create(name, raster, envelope);
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
