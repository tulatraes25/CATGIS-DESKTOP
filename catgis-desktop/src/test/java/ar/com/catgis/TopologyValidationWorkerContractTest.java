package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T5/#113: topology evaluation runs off the EDT and cancellation discards results. */
class TopologyValidationWorkerContractTest {

    @Test
    void validationRunsOffTheEventDispatchThread() throws Exception {
        AtomicBoolean ranOffEdt = new AtomicBoolean(false);
        CountDownLatch started = new CountDownLatch(1);
        TopologyValidationWorker worker = new TopologyValidationWorker(() -> {
            ranOffEdt.set(!SwingUtilities.isEventDispatchThread());
            started.countDown();
            return List.of();
        }, results -> { }, null);
        worker.execute();
        assertTrue(started.await(5, TimeUnit.SECONDS));
        worker.get(5, TimeUnit.SECONDS);
        assertTrue(ranOffEdt.get(), "JTS validation must never run on the EDT");
    }

    @Test
    void cancelledWorkerDoesNotPublishItsResult() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean donePublished = new AtomicBoolean(false);
        AtomicReference<Thread> taskThread = new AtomicReference<>();
        TopologyValidationWorker worker = new TopologyValidationWorker(() -> {
            taskThread.set(Thread.currentThread());
            started.countDown();
            try {
                Thread.sleep(30_000L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            return List.of();
        }, results -> donePublished.set(true), null);
        worker.execute();
        assertTrue(started.await(5, TimeUnit.SECONDS));
        worker.cancel(true);
        assertThrows(CancellationException.class, () -> worker.get(5, TimeUnit.SECONDS));
        Thread.sleep(150L);
        assertFalse(donePublished.get(), "a cancelled validation must not populate the table");
    }

    @Test
    void failureIsReportedToTheHandlerOffTheDonePath() throws Exception {
        AtomicReference<Exception> failure = new AtomicReference<>();
        CountDownLatch reported = new CountDownLatch(1);
        TopologyValidationWorker worker = new TopologyValidationWorker(() -> {
            throw new IllegalStateException("fallo simulado");
        }, results -> { }, error -> {
            failure.set(error);
            reported.countDown();
        });
        worker.execute();
        assertTrue(reported.await(5, TimeUnit.SECONDS));
        assertTrue(failure.get() instanceof IllegalStateException);
        assertTrue(failure.get().getMessage().contains("fallo simulado"));
    }
}
