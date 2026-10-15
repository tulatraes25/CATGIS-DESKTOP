package ar.com.catgis;

import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** Headless real Swing-EDT regression contracts; never instantiate a JDialog. */
class WcsRasterAsyncLoadContractTest {

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
}
