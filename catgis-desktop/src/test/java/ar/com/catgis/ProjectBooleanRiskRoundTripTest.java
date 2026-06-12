package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

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

class ProjectBooleanRiskRoundTripTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void savesAndReloadsBooleanRiskLayersInProjectOperationalCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-boolean-risk-roundtrip");
        Path demPath = tempDir.resolve("dem_boolrisk_3857.tif");
        Path soilPath = tempDir.resolve("soil_boolrisk_3857.tif");
        Path projectFile = tempDir.resolve("Proyecto riesgo booleano 22182.catgis");
        writeDem3857(demPath);
        writeSoil3857(soilPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto riesgo booleano 22182");
            AppContext.project().setProjectCRS("EPSG:22182");
            AppContext.project().setProjectFile(projectFile.toFile());

            RasterLayer demLayer = addRasterLayer(demPath, "DEM riesgo booleano", "DEM local", "EPSG:22182");
            RasterLayer soilLayer = addRasterLayer(soilPath, "Arcilla SoilGrids", "SoilGrids clay 0-5 cm", "EPSG:22182");

            BooleanRiskService.RiskResult result = BooleanRiskService.generateRisk(
                    new BooleanRiskService.RiskRequest(
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
                    )
            );

            for (BooleanRiskService.GeneratedRasterLayer raster : result.rasterLayers()) {
                AppContext.project().addLayer(raster.layer());
                CatgisDesktopApp.layersPanel.addLayer(raster.layer());
                CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(raster.layer(), raster.data());
            }
            if (result.vectorLayer() != null) {
                AppContext.project().addLayer(result.vectorLayer().layer());
                CatgisDesktopApp.layersPanel.addLayer(result.vectorLayer().layer());
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(result.vectorLayer().layer(), result.vectorLayer().data());
            }

            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload riesgo booleano 22182");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));
            assertNotNull(AppContext.project());
            assertEquals("EPSG:22182", AppContext.project().getProjectCRS());

            Layer slopeMask = findLayer("Mascara pendiente - Riesgo 22182");
            Layer soilMask = findLayer("Mascara suelo - Riesgo 22182");
            Layer riskRaster = findLayer("Riesgo preliminar - Riesgo 22182");
            Layer riskVector = findLayer("Zonas riesgo preliminar - Riesgo 22182");

            assertNotNull(slopeMask);
            assertNotNull(soilMask);
            assertNotNull(riskRaster);
            assertNotNull(riskVector);

            assertEquals("EPSG:22182", slopeMask.getSourceCRS());
            assertEquals("EPSG:22182", soilMask.getSourceCRS());
            assertEquals("EPSG:22182", riskRaster.getSourceCRS());
            assertEquals("EPSG:22182", riskVector.getSourceCRS());

            LocalRasterData slopeMaskData = CatgisDesktopApp.mapPanel.getRasterData(slopeMask);
            LocalRasterData soilMaskData = CatgisDesktopApp.mapPanel.getRasterData(soilMask);
            LocalRasterData riskRasterData = CatgisDesktopApp.mapPanel.getRasterData(riskRaster);
            ShapefileData riskVectorData = CatgisDesktopApp.mapPanel.getShapefileData(riskVector);

            assertNotNull(slopeMaskData);
            assertNotNull(soilMaskData);
            assertNotNull(riskRasterData);
            assertNotNull(riskVectorData);

            assertEquals("EPSG:22182", slopeMaskData.getDisplayCRS());
            assertEquals("EPSG:22182", soilMaskData.getDisplayCRS());
            assertEquals("EPSG:22182", riskRasterData.getDisplayCRS());
            assertNotNull(riskVectorData.getSchema().getDescriptor("zone_name"));
            assertNotNull(riskVectorData.getSchema().getDescriptor("risk_val"));
            assertNotNull(riskVectorData.getSchema().getDescriptor("dem_name"));
            assertNotNull(riskVectorData.getSchema().getDescriptor("soil_name"));

            assertTrue(slopeMask instanceof RasterLayer);
            assertTrue(soilMask instanceof RasterLayer);
            assertTrue(riskRaster instanceof RasterLayer);
            assertEquals(BooleanRiskService.OP_SLOPE_BOOLEAN_MASK, ((RasterLayer) slopeMask).getDerivedOperation());
            assertEquals(BooleanRiskService.OP_SOIL_BOOLEAN_MASK, ((RasterLayer) soilMask).getDerivedOperation());
            assertEquals(BooleanRiskService.OP_PRELIMINARY_BOOLEAN_RISK, ((RasterLayer) riskRaster).getDerivedOperation());

            assertEnvelopeClose(slopeMaskData.getEnvelope(), soilMaskData.getEnvelope(), 1e-6);
            assertEnvelopeClose(slopeMaskData.getEnvelope(), riskRasterData.getEnvelope(), 1e-6);
            assertTrue(riskVectorData.getEnvelope().intersects(riskRasterData.getEnvelope()));
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
        CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, rasterData);
        return layer;
    }

    private static Layer findLayer(String name) {
        return AppContext.project().getLayers().stream()
                .filter(layer -> name.equals(layer.getName()))
                .findFirst()
                .orElse(null);
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
        writeRaster3857(file, width, height, values, "dem-boolrisk-3857");
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
        writeRaster3857(file, width, height, values, "soil-boolrisk-3857");
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
