package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Actual Swing event dispatch and workers, not mocked execution or GUI certification. */
class GeoParquetInspectionTaskContractTest {
    @TempDir Path temp;

    @Test
    void readsOnceOffEdtAndDeliversOnEdtWithoutStoppingEventDispatch() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> {
                assertFalse(SwingUtilities.isEventDispatchThread());
                reads.incrementAndGet();
                entered.countDown();
                await(release);
                return "declared metadata";
            });
            try {
                SwingUtilities.invokeAndWait(() -> workers.pool.execute(probe.task));
                await(entered);
                CountDownLatch uiEvent = new CountDownLatch(1);
                SwingUtilities.invokeLater(uiEvent::countDown);
                await(uiEvent);
                assertFalse(probe.task.isDone(), "The EDT event must run while the file operation is blocked.");
            } finally {
                release.countDown();
            }
            assertEquals(GeoParquetInspectionTask.Status.INSPECTED, probe.result().status());
            assertEquals("declared metadata", probe.outcome.get().summary());
            assertEquals(1, reads.get());
        }
    }

    @Test
    void genuineGeoParquetSummaryPreservesBytesAndReleasesItsHandle() throws Exception {
        Path file = GeoParquetTestFixtures.empty(temp.resolve("datos con espacios y \u00f1.parquet"));
        byte[] before = Files.readAllBytes(file);
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> GeoParquetReader.getSummary(file.toFile()));
            workers.pool.execute(probe.task);
            var result = probe.result();
            assertEquals(GeoParquetInspectionTask.Status.INSPECTED, result.status());
            assertTrue(result.summary().contains("Filas declaradas: 0"));
            assertTrue(result.summary().contains("No se cargo una capa"));
            assertTrue(result.summary().contains("OGC:CRS84"));
            assertArrayEquals(before, Files.readAllBytes(file));
            Path moved = temp.resolve("moved.parquet");
            Files.move(file, moved);
            Files.delete(moved);
        }
    }

    @Test
    void corruptRealFileCannotProduceASuccessOrChangeSourceBytes() throws Exception {
        Path file = temp.resolve("corrupt.parquet");
        byte[] bytes = {'P', 'A', 'R', '1'};
        Files.write(file, bytes);
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> GeoParquetReader.getSummary(file.toFile()));
            workers.pool.execute(probe.task);
            var result = probe.result();
            assertEquals(GeoParquetInspectionTask.Status.FAILED, result.status());
            assertInstanceOf(IOException.class, result.failure());
            assertNull(result.summary());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            Files.delete(file);
        }
    }

    @Test
    void cancellationBeforeExecutionNeverCallsTheReader() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> { calls.incrementAndGet(); return "must not run"; });
            SwingUtilities.invokeAndWait(() -> assertTrue(probe.task.cancel(true)));
            workers.pool.execute(probe.task);
            assertEquals(GeoParquetInspectionTask.Status.CANCELLED, probe.result().status());
            workers.pool.shutdown();
            assertTrue(workers.pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(0, calls.get());
            probe.assertSingleEdtDelivery();
        }
    }

    @Test
    void cancellationInterruptsBlockedReaderAndNeverReportsFailureOrSuccess() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> {
                entered.countDown();
                try {
                    release.await();
                    return "not expected";
                } catch (InterruptedException cancelled) {
                    interrupted.set(true);
                    throw cancelled;
                } finally {
                    exited.countDown();
                }
            });
            try {
                workers.pool.execute(probe.task);
                await(entered);
                SwingUtilities.invokeAndWait(() -> assertTrue(probe.task.cancel(true)));
                assertEquals(GeoParquetInspectionTask.Status.CANCELLED, probe.result().status());
                await(exited);
                assertTrue(interrupted.get());
            } finally {
                release.countDown();
            }
            probe.assertSingleEdtDelivery();
        }
    }

    @Test
    void aReaderIgnoringInterruptionCannotPublishALateSuccessfulResult() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> {
                entered.countDown();
                ignoreInterruptsUntil(release);
                exited.countDown();
                return "late success must be discarded";
            });
            try {
                workers.pool.execute(probe.task);
                await(entered);
                SwingUtilities.invokeAndWait(() -> assertTrue(probe.task.cancel(true)));
                assertEquals(GeoParquetInspectionTask.Status.CANCELLED, probe.result().status());
                assertNull(probe.outcome.get().summary());
            } finally {
                release.countDown();
            }
            await(exited);
            workers.pool.shutdown();
            assertTrue(workers.pool.awaitTermination(10, TimeUnit.SECONDS));
            probe.assertSingleEdtDelivery();
            assertEquals(GeoParquetInspectionTask.Status.CANCELLED, probe.outcome.get().status());
        }
    }

    @Test
    void twoPhysicalReadersAreTheLimitEvenWhenTheirFuturesHaveBeenCancelled() throws Exception {
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maximum = new AtomicInteger();
        AtomicBoolean daemon = new AtomicBoolean(true);
        AtomicInteger unexpected = new AtomicInteger();
        try (Workers workers = new Workers()) {
            Callable<String> read = () -> {
                daemon.compareAndSet(true, Thread.currentThread().isDaemon());
                maximum.accumulateAndGet(active.incrementAndGet(), Math::max);
                entered.countDown();
                try { ignoreInterruptsUntil(release); return "cancelled"; }
                finally { active.decrementAndGet(); }
            };
            Probe first = new Probe(read);
            Probe second = new Probe(read);
            Probe rejected = new Probe(() -> { unexpected.incrementAndGet(); return "not allowed"; });
            try {
                workers.pool.execute(first.task);
                workers.pool.execute(second.task);
                await(entered);
                assertThrows(RejectedExecutionException.class, () -> workers.pool.execute(rejected.task));
                assertEquals(0, workers.pool.getQueue().size());
                SwingUtilities.invokeAndWait(() -> {
                    first.task.cancel(true);
                    second.task.cancel(true);
                });
                assertEquals(GeoParquetInspectionTask.Status.CANCELLED, first.result().status());
                assertEquals(GeoParquetInspectionTask.Status.CANCELLED, second.result().status());
                assertEquals(2, active.get(), "Cancelled Future is not proof that physical I/O stopped.");
                assertThrows(RejectedExecutionException.class, () -> workers.pool.execute(rejected.task));
                assertEquals(2, maximum.get());
                assertTrue(daemon.get());
                assertEquals(0, unexpected.get());
                rejected.task.cancel(true);
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void checkedAndUncheckedReaderFailuresKeepTheirCauseAndNeverLookInspected() throws Exception {
        for (Exception failure : List.of(new IOException("broken footer"), new IllegalStateException("decoder"))) {
            try (Workers workers = new Workers()) {
                Probe probe = new Probe(() -> { throw failure; });
                workers.pool.execute(probe.task);
                var result = probe.result();
                assertEquals(GeoParquetInspectionTask.Status.FAILED, result.status());
                assertSame(failure, result.failure());
                assertNull(result.summary());
            }
        }
    }

    @Test
    void interruptedIoIsAnExplicitCancelledOutcomeNotAnErrorPopup() throws Exception {
        try (Workers workers = new Workers()) {
            Probe probe = new Probe(() -> { throw new InterruptedIOException("cancelled read"); });
            workers.pool.execute(probe.task);
            assertEquals(GeoParquetInspectionTask.Status.CANCELLED, probe.result().status());
            assertNull(probe.outcome.get().summary());
        }
    }

    @Test
    void missingOrBlankSummaryIsAFailureRatherThanSuccessfulInspection() throws Exception {
        for (int i = 0; i < 2; i++) {
            String summary = i == 0 ? null : " \n\t";
            try (Workers workers = new Workers()) {
                Probe probe = new Probe(() -> summary);
                workers.pool.execute(probe.task);
                assertEquals(GeoParquetInspectionTask.Status.FAILED, probe.result().status());
                assertInstanceOf(IOException.class, probe.outcome.get().failure());
                assertNull(probe.outcome.get().summary());
            }
        }
    }

    @Test
    void accidentalDirectExecutionOnEdtFailsBeforeCallingTheReader() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        Probe probe = new Probe(() -> { reads.incrementAndGet(); return "bad threading"; });
        SwingUtilities.invokeAndWait(probe.task::run);
        assertEquals(GeoParquetInspectionTask.Status.FAILED, probe.result().status());
        assertInstanceOf(IllegalStateException.class, probe.outcome.get().failure());
        assertEquals(0, reads.get());
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for a deterministic event.");
    }

    private static void ignoreInterruptsUntil(CountDownLatch release) {
        boolean done = false;
        while (!done) {
            try {
                done = release.await(10, TimeUnit.SECONDS);
                if (!done) { throw new AssertionError("Uncooperative reader was not released by the test."); }
            } catch (InterruptedException ignored) {
                // Deliberate test of an uncooperative I/O provider, NOT production guidance.
            }
        }
    }

    private static final class Probe {
        private final CountDownLatch delivered = new CountDownLatch(1);
        private final AtomicInteger callbacks = new AtomicInteger();
        private final AtomicBoolean onEdt = new AtomicBoolean(true);
        private final AtomicReference<GeoParquetInspectionTask.Outcome> outcome = new AtomicReference<>();
        private final GeoParquetInspectionTask task;

        private Probe(Callable<String> read) {
            task = new GeoParquetInspectionTask(read, result -> {
                onEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread());
                outcome.set(result);
                callbacks.incrementAndGet();
                delivered.countDown();
            });
        }

        private GeoParquetInspectionTask.Outcome result() throws Exception {
            await(delivered);
            assertSingleEdtDelivery();
            return outcome.get();
        }

        private void assertSingleEdtDelivery() throws Exception {
            SwingUtilities.invokeAndWait(() -> {});
            assertTrue(onEdt.get(), "Result delivery must happen on EDT.");
            assertEquals(1, callbacks.get(), "One request must deliver exactly one outcome.");
        }
    }

    private static final class Workers implements AutoCloseable {
        private final ThreadPoolExecutor pool = GeoParquetInspectionTask.newExecutor();
        @Override public void close() throws InterruptedException {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "Test left a physical reader running.");
        }
    }
}
