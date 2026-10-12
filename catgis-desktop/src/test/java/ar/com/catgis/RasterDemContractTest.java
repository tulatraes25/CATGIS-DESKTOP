package ar.com.catgis;

import ar.com.catgis.data.raster.RasterCoverageSupport;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Test;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RasterDemContractTest {

    @Test
    void readsRealTinyGeoTiffWithCrsEnvelopeAndSamples() throws Exception {
        Path file = Files.createTempFile("catgis-f23-single-band-", ".tif");
        writeSingleBandGeoTiff(file, false);

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(file.toFile());

        assertNotNull(coverage);
        assertEquals("EPSG:4326", RasterCoverageSupport.resolveCoverageCrsCode(coverage, null));

        Raster raster = coverage.getRenderedImage().getData();
        assertEquals(6, raster.getWidth());
        assertEquals(5, raster.getHeight());
        assertEquals(1, raster.getNumBands());
        assertEquals(100d, raster.getSampleDouble(raster.getMinX(), raster.getMinY(), 0), 0.0001d);
        assertEquals(129d,
                raster.getSampleDouble(raster.getMinX() + 5, raster.getMinY() + 4, 0),
                0.0001d);

        ReferencedEnvelope envelope = new ReferencedEnvelope(coverage.getEnvelope2D());
        assertEquals(-58.60d, envelope.getMinX(), 0.000001d);
        assertEquals(-58.20d, envelope.getMaxX(), 0.000001d);
        assertEquals(-34.80d, envelope.getMinY(), 0.000001d);
        assertEquals(-34.40d, envelope.getMaxY(), 0.000001d);
    }

    @Test
    void preservesThreeBandsInRealTinyGeoTiff() throws Exception {
        Path file = Files.createTempFile("catgis-f23-three-band-", ".tif");
        writeThreeBandGeoTiff(file);

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(file.toFile());
        Raster raster = coverage.getRenderedImage().getData();

        assertEquals(3, raster.getNumBands());
        int x = raster.getMinX() + 2;
        int y = raster.getMinY() + 1;
        assertEquals(12d, raster.getSampleDouble(x, y, 0), 0.0001d);
        assertEquals(112d, raster.getSampleDouble(x, y, 1), 0.0001d);
        assertEquals(212d, raster.getSampleDouble(x, y, 2), 0.0001d);
    }

    @Test
    void preservesNanNoDataCellAndReprojectsRealCoverage() throws Exception {
        Path file = Files.createTempFile("catgis-f23-nodata-", ".tif");
        writeSingleBandGeoTiff(file, true);

        GridCoverage2D source = RasterCoverageSupport.readCoverage(file.toFile());
        Raster sourceRaster = source.getRenderedImage().getData();
        assertTrue(Double.isNaN(sourceRaster.getSampleDouble(
                sourceRaster.getMinX() + 2,
                sourceRaster.getMinY() + 2,
                0
        )), "La celda NoData/NaN del GeoTIFF real debe preservarse al leer la cobertura.");

        GridCoverage2D projected = RasterCoverageSupport.reprojectCoverage(
                source,
                "EPSG:4326",
                "EPSG:3857"
        );

        assertNotNull(projected);
        assertEquals("EPSG:3857", RasterCoverageSupport.resolveCoverageCrsCode(projected, null));
        assertNotNull(projected.getRenderedImage());

        ReferencedEnvelope envelope = new ReferencedEnvelope(projected.getEnvelope2D());
        assertTrue(envelope.getWidth() > 1000d);
        assertTrue(envelope.getHeight() > 1000d);
    }

    private static void writeSingleBandGeoTiff(Path file, boolean includeNan) throws Exception {
        int width = 6;
        int height = 5;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = 100f + (row * width) + col;
            }
        }
        if (includeNan) {
            values[(2 * width) + 2] = Float.NaN;
        }

        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        writeCoverage(file, "f23-single-band", raster);
    }

    private static void writeThreeBandGeoTiff(Path file) throws Exception {
        int width = 5;
        int height = 4;
        int bands = 3;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, bands);
        DataBufferFloat buffer = new DataBufferFloat(width * height, bands);

        for (int band = 0; band < bands; band++) {
            float[] values = buffer.getData(band);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    values[(row * width) + col] = (band * 100f) + (row * 10f) + col;
                }
            }
        }

        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        writeCoverage(file, "f23-three-band", raster);
    }

    private static void writeCoverage(Path file, String name, WritableRaster raster) throws Exception {
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -58.60d, -58.20d,
                -34.80d, -34.40d,
                CRSDefinitions.decode("EPSG:4326", true)
        );
        GridCoverage2D coverage = new GridCoverageFactory().create(name, raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }
    }
}
