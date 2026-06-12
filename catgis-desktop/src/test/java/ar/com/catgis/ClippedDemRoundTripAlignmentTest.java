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
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClippedDemRoundTripAlignmentTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void keepsClippedDemAndDerivedLayersAlignedAfterReload() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-clipped-dem-roundtrip");
        Path demPath = tempDir.resolve("dem_3857.tif");
        Path clippedPath = tempDir.resolve("DEM_recortado.tif");
        Path projectFile = tempDir.resolve("Proyecto recortado 4326.catgis");
        Envelope[] clippedEnvelopeHolder = new Envelope[1];

        writeDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Proyecto recortado 4326");
            AppContext.project().setProjectCRS("EPSG:4326");
            AppContext.project().setProjectFile(projectFile.toFile());

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:4326", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM origen", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:4326"));
            addRasterLayer(demLayer, demData);

            Envelope sourceEnvelope = demData.getEnvelope();
            Envelope clipEnvelope = new Envelope(
                    sourceEnvelope.getMinX() + (sourceEnvelope.getWidth() * 0.20d),
                    sourceEnvelope.getMinX() + (sourceEnvelope.getWidth() * 0.68d),
                    sourceEnvelope.getMinY() + (sourceEnvelope.getHeight() * 0.18d),
                    sourceEnvelope.getMinY() + (sourceEnvelope.getHeight() * 0.82d)
            );

            DemClipService.ClipResult clipped = DemClipService.clipDem(
                    demLayer,
                    clipEnvelope,
                    null,
                    "DEM_recortado.tif",
                    clippedPath.toFile()
            );
            clippedEnvelopeHolder[0] = new Envelope(clipped.rasterData().getEnvelope());
            addRasterLayer(clipped.layer(), clipped.rasterData());

            DrainageExtractionService.GeneratedDrainageLayer drainage =
                    DrainageExtractionService.generateDrainage(
                            clipped.layer(),
                            4,
                            "Escorrentias - DEM_recortado.tif",
                            DrainageExtractionService.AnalysisDetail.BALANCED,
                            DrainageExtractionService.HydrologicConditioning.ADVANCED,
                            0d,
                            DrainageExtractionService.CleanupLevel.BALANCED
                    );
            ShapefileData projectedDrainage = TopographyWorkflowSupport.projectVectorDataToCurrentProject(
                    drainage.layer(),
                    drainage.data()
            );
            addVectorLayer(drainage.layer(), projectedDrainage);

            TerrainHydrologyAnalysisService.AnalysisResult analysis =
                    TerrainHydrologyAnalysisService.generateAnalysis(
                            new TerrainHydrologyAnalysisService.AnalysisRequest(
                                    clipped.layer(),
                                    "DEM_recortado.tif",
                                    DrainageExtractionService.AnalysisDetail.BALANCED,
                                    DrainageExtractionService.HydrologicConditioning.ADVANCED,
                                    4,
                                    8,
                                    false,
                                    true,
                                    true,
                                    true,
                                    true,
                                    false,
                                    true,
                                    true,
                                    false
                            )
                    );

            TerrainHydrologyAnalysisService.GeneratedRasterLayer slope = findRasterResult(
                    analysis,
                    TerrainHydrologyAnalysisService.OP_SLOPE
            );
            TerrainHydrologyAnalysisService.GeneratedRasterLayer accumulation = findRasterResult(
                    analysis,
                    TerrainHydrologyAnalysisService.OP_FLOW_ACCUMULATION
            );
            addRasterLayer(slope.layer(), slope.data());
            addRasterLayer(accumulation.layer(), accumulation.data());

            TerrainHydrologyAnalysisService.GeneratedVectorLayer basins = findVectorResult(analysis, "basins");
            TerrainHydrologyAnalysisService.GeneratedVectorLayer outlets = findVectorResult(analysis, "outlets");
            addVectorLayer(
                    basins.layer(),
                    TopographyWorkflowSupport.projectVectorDataToCurrentProject(basins.layer(), basins.data())
            );
            addVectorLayer(
                    outlets.layer(),
                    TopographyWorkflowSupport.projectVectorDataToCurrentProject(outlets.layer(), outlets.data())
            );

            assertTrue(SaveProjectAction.saveProjectToFile(projectFile.toFile(), false));
        });

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Reload recortado 4326");
            assertTrue(LoadProjectAction.loadProjectFile(projectFile.toFile(), false));

            Layer clippedLayer = findLayer("DEM_recortado.tif");
            Layer drainageLayer = findLayer("Escorrentias - DEM_recortado.tif");
            Layer basinsLayer = findLayer("Cuencas - DEM_recortado.tif");
            Layer outletsLayer = findLayer("Outlets - DEM_recortado.tif");
            Layer slopeLayer = findLayer("Pendiente - DEM_recortado.tif");
            Layer accumulationLayer = findLayer("Acumulacion de flujo - DEM_recortado.tif");

            assertNotNull(clippedLayer);
            assertNotNull(drainageLayer);
            assertNotNull(basinsLayer);
            assertNotNull(outletsLayer);
            assertNotNull(slopeLayer);
            assertNotNull(accumulationLayer);

            assertEquals("EPSG:4326", clippedLayer.getSourceCRS());
            assertEquals("EPSG:4326", drainageLayer.getSourceCRS());
            assertEquals("EPSG:4326", basinsLayer.getSourceCRS());
            assertEquals("EPSG:4326", outletsLayer.getSourceCRS());
            assertEquals("EPSG:4326", slopeLayer.getSourceCRS());
            assertEquals("EPSG:4326", accumulationLayer.getSourceCRS());

            LocalRasterData clippedData = CatgisDesktopApp.mapPanel.getRasterData(clippedLayer);
            LocalRasterData slopeData = CatgisDesktopApp.mapPanel.getRasterData(slopeLayer);
            LocalRasterData accumulationData = CatgisDesktopApp.mapPanel.getRasterData(accumulationLayer);
            ShapefileData drainageData = CatgisDesktopApp.mapPanel.getShapefileData(drainageLayer);
            ShapefileData basinsData = CatgisDesktopApp.mapPanel.getShapefileData(basinsLayer);
            ShapefileData outletsData = CatgisDesktopApp.mapPanel.getShapefileData(outletsLayer);

            assertNotNull(clippedData);
            assertNotNull(slopeData);
            assertNotNull(accumulationData);
            assertNotNull(drainageData);
            assertNotNull(basinsData);
            assertNotNull(outletsData);

            assertEnvelopeClose(clippedEnvelopeHolder[0], clippedData.getEnvelope(), 1e-9);
            assertEnvelopeClose(clippedData.getEnvelope(), slopeData.getEnvelope(), 1e-9);
            assertEnvelopeClose(clippedData.getEnvelope(), accumulationData.getEnvelope(), 1e-9);

            double tolerance = Math.max(
                    clippedData.getEnvelope().getWidth() / Math.max(1, clippedData.getWidth()),
                    clippedData.getEnvelope().getHeight() / Math.max(1, clippedData.getHeight())
            ) * 2d;

            assertEnvelopeWithin(clippedData.getEnvelope(), drainageData.getEnvelope(), tolerance);
            assertEnvelopeWithin(clippedData.getEnvelope(), basinsData.getEnvelope(), tolerance);
            assertEnvelopeWithin(clippedData.getEnvelope(), outletsData.getEnvelope(), tolerance);
            assertRasterHasVariation(clippedData.getImage());
        });
    }

    private static void addRasterLayer(RasterLayer layer, LocalRasterData data) {
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.layersPanel.addLayer(layer);
        CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(layer, data);
    }

    private static void addVectorLayer(Layer layer, ShapefileData data) {
        AppContext.project().addLayer(layer);
        CatgisDesktopApp.layersPanel.addLayer(layer);
        CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, data);
    }

    private static Layer findLayer(String name) {
        return AppContext.project().getLayers().stream()
                .filter(layer -> name.equals(layer.getName()))
                .findFirst()
                .orElse(null);
    }

    private static TerrainHydrologyAnalysisService.GeneratedRasterLayer findRasterResult(
            TerrainHydrologyAnalysisService.AnalysisResult analysis,
            String operation) {
        return analysis.rasterLayers().stream()
                .filter(layer -> operation.equalsIgnoreCase(layer.operation()))
                .findFirst()
                .orElseThrow();
    }

    private static TerrainHydrologyAnalysisService.GeneratedVectorLayer findVectorResult(
            TerrainHydrologyAnalysisService.AnalysisResult analysis,
            String operation) {
        return analysis.vectorLayers().stream()
                .filter(layer -> operation.equalsIgnoreCase(layer.operation()))
                .findFirst()
                .orElseThrow();
    }

    private static void assertEnvelopeClose(Envelope expected, Envelope actual, double tolerance) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(Math.abs(expected.getMinX() - actual.getMinX()) <= tolerance,
                "minX fuera de tolerancia: " + expected + " vs " + actual);
        assertTrue(Math.abs(expected.getMaxX() - actual.getMaxX()) <= tolerance,
                "maxX fuera de tolerancia: " + expected + " vs " + actual);
        assertTrue(Math.abs(expected.getMinY() - actual.getMinY()) <= tolerance,
                "minY fuera de tolerancia: " + expected + " vs " + actual);
        assertTrue(Math.abs(expected.getMaxY() - actual.getMaxY()) <= tolerance,
                "maxY fuera de tolerancia: " + expected + " vs " + actual);
    }

    private static void assertEnvelopeWithin(Envelope expectedContainer, Envelope actualEnvelope, double tolerance) {
        assertNotNull(expectedContainer);
        assertNotNull(actualEnvelope);
        Envelope expanded = new Envelope(expectedContainer);
        expanded.expandBy(tolerance);
        assertTrue(expanded.contains(actualEnvelope),
                "El derivado quedo fuera del DEM recortado: dem=" + expectedContainer + ", derivado=" + actualEnvelope);
    }

    private static void assertRasterHasVariation(BufferedImage image) {
        assertNotNull(image);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int sample = image.getRaster().getSample(x, y, 0);
                if (sample < min) {
                    min = sample;
                }
                if (sample > max) {
                    max = sample;
                }
            }
        }
        assertTrue(max > min, "La imagen raster recortada quedo sin variacion visual util.");
    }

    private static void writeDem3857(Path file) throws Exception {
        int width = 22;
        int height = 20;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = (float) (320d + (row * 7d) - (Math.abs(col - 11) * 5d));
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
