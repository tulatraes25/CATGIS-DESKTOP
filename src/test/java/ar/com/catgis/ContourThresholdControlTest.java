package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
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

class ContourThresholdControlTest {

    @AfterEach
    void tearDown() {
        ReleaseTestSupport.clearAppContext();
    }

    @Test
    void excludesLowCoastalElevationsWhenThresholdIsApplied() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-contours-threshold");
        Path demPath = tempDir.resolve("coastal_dem_3857.tif");
        writeCoastalDem3857(demPath);

        ReleaseTestSupport.runOnEdt(() -> {
            ReleaseTestSupport.initializeAppContext("Curvas umbral 22182");
            CatgisDesktopApp.currentProject.setProjectCRS("EPSG:22182");

            LocalRasterData demData = RasterImageLoader.loadReal(demPath.toFile(), "EPSG:22182", "EPSG:3857");
            RasterLayer demLayer = new RasterLayer("DEM costero 22182", demPath.toString());
            demLayer.setSourceName("DEM local");
            demLayer.setFeatureCount(1);
            demLayer.setSourceCRS(RasterCoverageSupport.resolveOperationalRasterCrs(demData, "EPSG:22182"));
            CatgisDesktopApp.currentProject.addLayer(demLayer);
            CatgisDesktopApp.layersPanel.addLayer(demLayer);
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(demLayer, demData);

            ContourGenerationService.GeneratedContourLayer allContours =
                    ContourGenerationService.generateContours(demLayer, 5d, 5, "Curvas sin filtro", true, false, null);
            ContourGenerationService.GeneratedContourLayer filteredContours =
                    ContourGenerationService.generateContours(demLayer, 5d, 5, "Curvas filtradas", true, false, 0d);

            assertNotNull(allContours.data());
            assertNotNull(filteredContours.data());
            assertEquals("EPSG:22182", filteredContours.layer().getSourceCRS());
            assertTrue(filteredContours.data().getFeatureCount() < allContours.data().getFeatureCount());

            for (SimpleFeature feature : filteredContours.data().getFeatures()) {
                Object value = feature.getAttribute("elevation_m");
                assertTrue(value instanceof Number);
                assertTrue(((Number) value).doubleValue() > 0d);
            }
        });
    }

    private static void writeCoastalDem3857(Path file) throws Exception {
        int width = 22;
        int height = 22;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float coastalGradient = -8f + (row * 2.4f);
                float relief = (float) (12d - (Math.abs(col - 11) * 1.3d));
                values[(row * width) + col] = coastalGradient + relief;
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665000, -7660600,
                -3881200, -3876800,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create("coastal-dem-3857", raster, envelope);
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
