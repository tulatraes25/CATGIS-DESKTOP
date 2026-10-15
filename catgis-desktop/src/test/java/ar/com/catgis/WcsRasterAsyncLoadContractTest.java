package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.file.Path;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** Headless real Swing-EDT regression contracts; never instantiate a JDialog. */
class WcsRasterAsyncLoadContractTest {

    @TempDir
    Path tempDir;

    @Test
    void slowRasterDecodingRunsOffEdtWhileUiEventsRemainResponsive() throws Exception {
        CountDownLatch began = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch completion = new CountDownLatch(1);
        AtomicBoolean loadWasOffEdt = new AtomicBoolean();
        AtomicBoolean completionWasOnEdt = new AtomicBoolean();
        AtomicReference<String> committed = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        WcsRasterAsyncLoad<String> worker = new WcsRasterAsyncLoad<>(
                () -> {
                    loadWasOffEdt.set(!SwingUtilities.isEventDispatchThread());
                    began.countDown();
                    assertTrue(release.await(10L, TimeUnit.SECONDS));
                    return "real-raster-ready";
                },
                value -> {
                    completionWasOnEdt.set(SwingUtilities.isEventDispatchThread());
                    committed.set(value);
                    completion.countDown();
                },
                problem -> {
                    error.set(problem);
                    completion.countDown();
                },
                completion::countDown,
                value -> {});
        worker.execute();
        try {
            assertTrue(began.await(5L, TimeUnit.SECONDS));
            AtomicBoolean edtReceivedEvent = new AtomicBoolean();
            assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                    SwingUtilities.invokeAndWait(() -> edtReceivedEvent.set(true)));
            assertTrue(edtReceivedEvent.get(),
                    "The EDT must process input while raster decoding is blocked.");
        } finally {
            release.countDown();
        }
        assertTrue(completion.await(10L, TimeUnit.SECONDS));
        assertTrue(loadWasOffEdt.get());
        assertTrue(completionWasOnEdt.get());
        assertEquals("real-raster-ready", committed.get());
        assertEquals(null, error.get());
    }

    @Test
    void cancellingAfterWorkerStartedReleasesUnpublishedRasterExactlyOnce() throws Exception {
        CountDownLatch began = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch discarded = new CountDownLatch(1);
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger disposals = new AtomicInteger();
        WcsRasterAsyncLoad<String> worker = new WcsRasterAsyncLoad<>(
                () -> {
                    began.countDown();
                    boolean released = false;
                    while (!released) {
                        try {
                            released = release.await(1L, TimeUnit.SECONDS);
                        } catch (InterruptedException ignored) {
                            // Mimics a decoder that ignores cancellation.
                        }
                    }
                    return "raster-resource";
                },
                value -> commits.incrementAndGet(),
                failed -> commits.incrementAndGet(),
                () -> {},
                value -> {
                    disposals.incrementAndGet();
                    discarded.countDown();
                });
        worker.execute();
        try {
            assertTrue(began.await(5L, TimeUnit.SECONDS));
            worker.cancel(true);
        } finally {
            release.countDown();
        }
        assertTrue(discarded.await(10L, TimeUnit.SECONDS));
        assertEquals(0, commits.get());
        assertEquals(1, disposals.get());
    }

    @Test
    void callbackFailureDiscardsRasterInsteadOfPublishingProjectState() throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicInteger releases = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();
        WcsRasterAsyncLoad<String> worker = new WcsRasterAsyncLoad<>(
                () -> "unpublished-raster",
                value -> { throw new IllegalStateException("simulated map publish failure"); },
                problem -> {
                    error.set(problem);
                    finished.countDown();
                },
                finished::countDown,
                value -> releases.incrementAndGet());
        worker.execute();
        assertTrue(finished.await(5L, TimeUnit.SECONDS));
        assertTrue(error.get() instanceof IllegalStateException);
        assertEquals(1, releases.get());
    }

    @Test
    void closingResolutionDialogNeverStartsRasterImport() {
        assertFalse(WcsDialog.wantsRasterImport(-1));
        assertFalse(WcsDialog.wantsRasterImport(2));
        assertTrue(WcsDialog.wantsRasterImport(0));
        assertTrue(WcsDialog.wantsRasterImport(1));
    }

    @Test
    void actualSmallGeotiffPreviewAndFullDecodeOnWorkerWithoutBlockingEdt() throws Exception {
        Path file = tempDir.resolve("WCS coverage with spaces.tif");
        int width = 16;
        int height = 16;
        BandedSampleModel samples = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        DataBufferFloat buffer = new DataBufferFloat(width * height);
        float[] values = buffer.getData();
        for (int i = 0; i < values.length; i++) {
            values[i] = 20.0f + i;
        }
        WritableRaster raster = Raster.createWritableRaster(samples, buffer, null);
        ReferencedEnvelope envelope = new ReferencedEnvelope(-70, -69, -46, -45,
                CRSDefinitions.decode("EPSG:4326", true));
        GridCoverage2D coverage = new GridCoverageFactory().create(
                "tiny-real-wcs", raster, envelope);
        GeoTiffWriter writer = new GeoTiffWriter(file.toFile());
        try {
            writer.write(coverage, null);
        } finally {
            writer.dispose();
        }

        for (boolean full : Arrays.asList(false, true)) {
            CountDownLatch done = new CountDownLatch(1);
            AtomicReference<Throwable> error = new AtomicReference<>();
            AtomicBoolean decodedOffEdt = new AtomicBoolean();
            AtomicBoolean committedOnEdt = new AtomicBoolean();
            WcsRasterAsyncLoad<ar.com.catgis.data.raster.LocalRasterData> worker =
                    new WcsRasterAsyncLoad<>(
                            () -> {
                                decodedOffEdt.set(!SwingUtilities.isEventDispatchThread());
                                return full ? RasterImageLoader.loadReal(file.toFile(), "EPSG:4326", null)
                                        : RasterImageLoader.loadPreview(file.toFile(), "EPSG:4326", null);
                            },
                            result -> {
                                try {
                                    committedOnEdt.set(SwingUtilities.isEventDispatchThread());
                                    assertNotNull(result.getImage());
                                    assertTrue(result.getWidth() > 0 && result.getHeight() > 0);
                                    assertTrue(result.getBandCount() > 0);
                                    assertTrue(result.isGeoreferenced());
                                    assertNotNull(result.getEnvelope());
                                    assertTrue(result.getEnvelope().getWidth() > 0);
                                    assertTrue(result.getEnvelope().getHeight() > 0);
                                    assertNotNull(result.getSourceCRS());
                                } catch (Throwable failed) {
                                    error.set(failed);
                                } finally {
                                    result.dispose();
                                    done.countDown();
                                }
                            },
                            problem -> {
                                error.set(problem);
                                done.countDown();
                            },
                            done::countDown,
                            result -> {
                                if (result != null) {
                                    result.dispose();
                                }
                            });
            worker.execute();
            assertTrue(done.await(30L, TimeUnit.SECONDS),
                    "GeoTIFF Preview/Full decode must terminate.");
            assertTrue(decodedOffEdt.get());
            assertTrue(committedOnEdt.get());
            assertEquals(null, error.get());
        }
    }
}
