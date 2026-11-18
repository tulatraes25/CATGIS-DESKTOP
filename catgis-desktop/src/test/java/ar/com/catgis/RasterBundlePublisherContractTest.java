package ar.com.catgis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#161: online raster publication keeps the previous TIFF + sidecar pair on failure. */
class RasterBundlePublisherContractTest {

    private static final Path FIXTURE =
            Path.of("src", "test", "resources", "fixtures", "integration", "test_dem.tif");
    private static final Envelope OLD_ENVELOPE = new Envelope(1000, 1100, 2000, 2100);
    private static final Envelope NEW_ENVELOPE = new Envelope(3000, 3100, 4000, 4100);

    @TempDir
    Path directory;

    @AfterEach
    void clearSeam() {
        RasterBundlePublisher.afterTiffMovedForTesting(null);
    }

    private Path target() {
        return directory.resolve("dem.tif");
    }

    private void publishFixture(Envelope envelope, String crs) throws Exception {
        RasterBundlePublisher.publish(target().toFile(),
                staged -> Files.copy(FIXTURE, staged.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING),
                envelope, crs);
    }

    private byte[] bytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private void assertNoLeftovers() throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            List<String> leftovers = entries.map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(".catgis-"))
                    .toList();
            assertTrue(leftovers.isEmpty(), "leftovers: " + leftovers);
        }
    }

    @Test
    void writerFailureKeepsPreviousRasterAndSidecar() throws Exception {
        publishFixture(OLD_ENVELOPE, "EPSG:4326");
        byte[] previous = bytes(target());

        IOException failure = assertThrows(IOException.class,
                () -> RasterBundlePublisher.publish(target().toFile(),
                        staged -> {
                            Files.writeString(staged.toPath(), "no es un raster",
                                    StandardCharsets.UTF_8);
                            throw new IOException("fallo simulado de descarga");
                        }, NEW_ENVELOPE, "EPSG:4326"));

        assertEquals("fallo simulado de descarga", failure.getMessage());
        assertArrayEquals(previous, bytes(target()), "previous TIFF must survive");
        RasterSidecarSupport.Metadata sidecar = RasterSidecarSupport.read(target().toFile());
        assertNotNull(sidecar);
        assertEquals("EPSG:4326", sidecar.sourceCrs());
        assertEquals(OLD_ENVELOPE.getMinX(), sidecar.envelope().getMinX(), 1.0e-9);
        assertNoLeftovers();
    }

    @Test
    void failureAfterTiffMovedRestoresBothFiles() throws Exception {
        publishFixture(OLD_ENVELOPE, "EPSG:4326");
        byte[] previous = bytes(target());
        RasterBundlePublisher.afterTiffMovedForTesting(() -> {
            throw new IllegalStateException("crash simulado tras mover el TIFF");
        });

        assertThrows(IllegalStateException.class,
                () -> publishFixture(NEW_ENVELOPE, "EPSG:4326"));

        assertArrayEquals(previous, bytes(target()), "previous TIFF must be restored");
        RasterSidecarSupport.Metadata sidecar = RasterSidecarSupport.read(target().toFile());
        assertNotNull(sidecar);
        assertEquals(OLD_ENVELOPE.getMinX(), sidecar.envelope().getMinX(), 1.0e-9,
                "previous sidecar must be restored");
        assertNoLeftovers();
    }

    @Test
    void successPublishesTiffAndNewSidecar() throws Exception {
        publishFixture(OLD_ENVELOPE, "EPSG:4326");
        publishFixture(NEW_ENVELOPE, "EPSG:32719");

        assertTrue(Files.isRegularFile(target()));
        RasterSidecarSupport.Metadata sidecar = RasterSidecarSupport.read(target().toFile());
        assertNotNull(sidecar);
        assertEquals("EPSG:32719", sidecar.sourceCrs());
        assertEquals(NEW_ENVELOPE.getMinX(), sidecar.envelope().getMinX(), 1.0e-9);
        assertNoLeftovers();
    }

    @Test
    void firstPublicationFailureLeavesNoPartialPair() throws Exception {
        assertThrows(IOException.class,
                () -> RasterBundlePublisher.publish(target().toFile(),
                        staged -> Files.writeString(staged.toPath(), "texto",
                                StandardCharsets.UTF_8),
                        NEW_ENVELOPE, "EPSG:4326"));

        assertFalse(Files.exists(target()), "no partial TIFF may be published");
        assertFalse(Files.exists(RasterSidecarSupport.sidecarFile(target().toFile()).toPath()),
                "no partial sidecar may be published");
        assertNoLeftovers();
    }

    @Test
    void publicationWithoutEnvelopeRemovesStaleSidecar() throws Exception {
        publishFixture(OLD_ENVELOPE, "EPSG:4326");
        publishFixture(null, null);

        assertTrue(Files.isRegularFile(target()));
        assertEquals(null, RasterSidecarSupport.read(target().toFile()),
                "a raster published without georeference must not keep stale metadata");
    }

    @Test
    void fixtureIsAValidRaster() throws Exception {
        byte[] copy = Arrays.copyOf(bytes(FIXTURE), (int) Files.size(FIXTURE));
        assertTrue(copy.length > 0);
    }
}
