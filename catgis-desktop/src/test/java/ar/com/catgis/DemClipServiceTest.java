package ar.com.catgis;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemClipServiceTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void clipsDemByPolygonMaskAndKeepsOperationalCrs() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-dem-clip-22182");
        Path demPath = tempDir.resolve("dem_3857.tif");
        Path outputPath = tempDir.resolve("dem_clip_22182.tif");
        writeDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("DEM clip 22182");
            CatgisDesktopApp.currentProject.setProjectCRS("EPSG:22182");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM clip 22182", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            CatgisDesktopApp.currentProject.addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            Envelope sourceEnvelope = demData.getEnvelope();
            double width = sourceEnvelope.getWidth();
            double height = sourceEnvelope.getHeight();
            Geometry polygonMask = ReleaseTestSupport.GEOMETRY_FACTORY.toGeometry(new Envelope(
                    sourceEnvelope.getMinX() + (width * 0.20d),
                    sourceEnvelope.getMinX() + (width * 0.70d),
                    sourceEnvelope.getMinY() + (height * 0.15d),
                    sourceEnvelope.getMinY() + (height * 0.75d)
            ));

            DemClipService.ClipResult result = DemClipService.clipDem(
                    demLayer,
                    sourceEnvelope,
                    polygonMask,
                    "DEM recortado 22182",
                    outputPath.toFile()
            );

            assertNotNull(result);
            assertTrue(Files.exists(outputPath));
            assertNotNull(result.rasterData());
            assertEquals("EPSG:22182", result.layer().getSourceCRS());
            assertEquals("EPSG:22182", result.rasterData().getDisplayCRS());
            assertTrue(result.validPixels() > 0);
            assertEquals(demData.getWidth(), result.width());
            assertEquals(demData.getHeight(), result.height());
            assertTrue(result.validPixels() < (result.width() * result.height()));
        });
    }

    private static void writeDem3857(Path file) throws Exception {
        int width = 24;
        int height = 24;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = 180f + (row * 4f) - (Math.abs(col - 12) * 6f);
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7660200,
                -3881400, -3876400,
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
