package ar.com.catgis;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Test;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RasterCoverageSupportReprojectionOrientationTest {

    @Test
    void keepsNorthSouthOrientationWhenReprojectingCoverage() throws Exception {
        GridCoverage2D coverage = buildGradientCoverage3857();

        GridCoverage2D reprojected = RasterCoverageSupport.reprojectCoverage(coverage, "EPSG:3857", "EPSG:4326");

        assertNotNull(reprojected);
        assertNotNull(reprojected.getRenderedImage());

        Raster raster = reprojected.getRenderedImage().getData();
        int sampleCol = raster.getMinX() + (Math.max(1, raster.getWidth()) / 2);
        double topSample = raster.getSampleDouble(sampleCol, raster.getMinY(), 0);
        double bottomSample = raster.getSampleDouble(sampleCol, raster.getMinY() + raster.getHeight() - 1, 0);

        assertTrue(topSample < bottomSample,
                "La reproyeccion raster esta invirtiendo norte/sur: top=" + topSample + ", bottom=" + bottomSample);
    }

    private static GridCoverage2D buildGradientCoverage3857() throws Exception {
        int width = 12;
        int height = 10;
        BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[(row * width) + col] = (float) (100d + (row * 25d));
            }
        }
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(
                -7665200, -7661200,
                -3881200, -3877200,
                CRSDefinitions.decode("EPSG:3857", true)
        );
        return new GridCoverageFactory().create("reprojection-orientation", raster, envelope);
    }
}
