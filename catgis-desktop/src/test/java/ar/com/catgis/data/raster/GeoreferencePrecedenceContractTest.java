package ar.com.catgis.data.raster;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.RasterSidecarSupport;
import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.catmap.TerrainRasterAlgorithms;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#128: embedded GeoKeys win over sidecar; no invented CRS; pixels unchanged. */
class GeoreferencePrecedenceContractTest {

    private Path directory;

    @BeforeEach
    void createDirectory() throws Exception {
        directory = Files.createTempDirectory("catgis-georef-");
    }

    @AfterEach
    void deleteDirectory() {
        // Best effort: GeoTools may keep a transient Windows handle on the TIFF
        // until GC; a leaked temp dir must not fail the test suite.
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // transient handle
                }
            });
        } catch (Exception ignored) {
            // best effort
        }
    }

    private File embeddedGeoTiff() throws Exception {
        float[] values = new float[20 * 20];
        for (int index = 0; index < values.length; index++) {
            values[index] = index;
        }
        AnalysisRaster raster = new AnalysisRaster("dem", "m", 20, 20, values,
                TerrainRasterAlgorithms.NO_DATA, 0, 0, 20, 20, "EPSG:32719");
        File file = directory.resolve("embebido.tif").toFile();
        raster.exportGeoTiff(file);
        return file;
    }

    private static int[] pixels(GridCoverage2D coverage) {
        var image = coverage.getRenderedImage();
        return image.getData().getPixels(0, 0, image.getWidth(), image.getHeight(), (int[]) null);
    }

    @Test
    void conflictingSidecarIsVetoedAndEmbeddedGeoreferenceWins() throws Exception {
        File file = embeddedGeoTiff();
        RasterSidecarSupport.write(file, new Envelope(-10, 10, -20, 20), "EPSG:4326");

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(file);
        var decision = RasterCoverageSupport.decideGeoreference(file, coverage);

        assertTrue(decision.conflict(), "conflicting sidecar must be reported");
        assertEquals(RasterCoverageSupport.GeoreferenceSource.CONFLICT_KEPT_EMBEDDED,
                decision.source());
        assertEquals("EPSG:32719", decision.crsCode());
        assertNotNull(coverage.getCoordinateReferenceSystem());
        assertEquals("EPSG:32719", org.geotools.referencing.CRS.lookupIdentifier(
                        coverage.getCoordinateReferenceSystem(), true),
                "embedded CRS must be preserved: " + coverage.getCoordinateReferenceSystem().getName());
        assertEquals(0.0, coverage.getEnvelope2D().getMinX(), 1.0e-6);

        // Pixel/checksum oracle: vetoing the sidecar must not move or alter pixels.
        GeoTiffReader reader = new GeoTiffReader(
                new java.io.ByteArrayInputStream(Files.readAllBytes(file.toPath())));
        try {
            GridCoverage2D direct = reader.read(
                    (org.geotools.api.parameter.GeneralParameterValue[]) null);
            try {
                assertTrue(Arrays.equals(pixels(direct), pixels(coverage)),
                        "pixels must be identical to the embedded GeoTIFF");
            } finally {
                direct.dispose(true);
            }
        } finally {
            reader.dispose();
        }
    }

    @Test
    void sidecarAppliesOnlyWhenTiffHasNoEmbeddedKeys() throws Exception {
        Path plain = directory.resolve("sin-geokeys.tif");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY);
        assertTrue(ImageIO.write(image, "tif", plain.toFile()));
        File plainFile = plain.toFile();
        RasterSidecarSupport.write(plainFile, new Envelope(500, 540, 600, 640), "EPSG:32719");

        GridCoverage2D synthetic = new GridCoverageFactory().create("sintetica",
                image.getRaster(), new ReferencedEnvelope(0, 4, 0, 4,
                        CRSDefinitions.decode("EPSG:4326", true)));
        var decision = RasterCoverageSupport.decideGeoreference(plainFile, synthetic);
        assertEquals(RasterCoverageSupport.GeoreferenceSource.SIDECAR, decision.source());
        assertEquals("EPSG:32719", decision.crsCode());
        assertEquals(500.0, decision.envelope().getMinX(), 1.0e-9);

        GridCoverage2D applied = RasterCoverageSupport.applySidecarMetadataIfPresent(
                plainFile, synthetic);
        assertEquals(500.0, applied.getEnvelope2D().getMinX(), 1.0e-9);
        assertEquals("EPSG:32719", org.geotools.referencing.CRS.lookupIdentifier(
                applied.getCoordinateReferenceSystem(), true));
    }

    @Test
    void missingCrsIsNeverInvented() throws Exception {
        Path plain = directory.resolve("sin-metadata.tif");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY);
        assertTrue(ImageIO.write(image, "tif", plain.toFile()));
        GridCoverage2D synthetic = new GridCoverageFactory().create("sintetica",
                image.getRaster(), new ReferencedEnvelope(0, 4, 0, 4,
                        CRSDefinitions.decode("EPSG:4326", true)));

        var decision = RasterCoverageSupport.decideGeoreference(plain.toFile(), synthetic);
        assertEquals(RasterCoverageSupport.GeoreferenceSource.NONE, decision.source());
        assertEquals("", decision.crsCode());
        assertFalse(decision.hasGeoreference());
        assertSame(synthetic, RasterCoverageSupport.applySidecarMetadataIfPresent(
                plain.toFile(), synthetic), "coverage without georeference must stay untouched");
    }

    @Test
    void matchingSidecarDoesNotOverrideEmbedded() throws Exception {
        File file = embeddedGeoTiff();
        RasterSidecarSupport.write(file, new Envelope(0, 20, 0, 20), "EPSG:32719");

        GridCoverage2D coverage = RasterCoverageSupport.readCoverage(file);
        var decision = RasterCoverageSupport.decideGeoreference(file, coverage);
        assertEquals(RasterCoverageSupport.GeoreferenceSource.EMBEDDED_GEOTIFF,
                decision.source());
        assertFalse(decision.conflict());
        assertEquals("EPSG:32719", decision.crsCode());
    }
}
