package ar.com.catgis;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** One read-only inspection. No project state, dialogs, network or persistence. */
final class GeoParquetInspectionTask extends FutureTask<String> {
    enum Status { INSPECTED, FAILED, CANCELLED }

    record Outcome(Status status, String summary, Throwable failure) {}

    private final Consumer<Outcome> completion;

    GeoParquetInspectionTask(Callable<String> inspection, Consumer<Outcome> completion) {
        super(guarded(inspection));
        this.completion = Objects.requireNonNull(completion, "completion");
    }

    /**
     * No pending queue and at most two physically running tasks. In particular,
     * Future cancellation does not free a worker that still ignores interruption.
     */
    static ThreadPoolExecutor newExecutor() {
        AtomicInteger ids = new AtomicInteger();
        return new ThreadPoolExecutor(0, 2, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(), runnable -> {
                    Thread thread = new Thread(runnable, "catgis-geoparquet-inspection-" + ids.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    private static Callable<String> guarded(Callable<String> inspection) {
        Objects.requireNonNull(inspection, "inspection");
        return () -> {
            if (SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("La lectura GeoParquet no puede ejecutarse en el hilo de interfaz.");
            }
            checkActive();
            String summary = inspection.call();
            checkActive();
            if (summary == null || summary.isBlank()) {
                throw new IOException("La inspeccion no produjo metadatos verificables.");
            }
            return summary;
        };
    }

    private static void checkActive() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Inspeccion GeoParquet cancelada.");
        }
    }

    @Override
    protected void done() {
        Outcome outcome;
        try {
            outcome = new Outcome(Status.INSPECTED, get(), null);
        } catch (CancellationException cancelled) {
            outcome = new Outcome(Status.CANCELLED, null, null);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            outcome = new Outcome(Status.CANCELLED, null, null);
        } catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            boolean cancelled = cause instanceof InterruptedIOException
                    || cause instanceof InterruptedException || cause instanceof CancellationException;
            outcome = new Outcome(cancelled ? Status.CANCELLED : Status.FAILED, null, cause);
        }
        // FutureTask.done runs after terminal state, including pre-start cancel.
        // get() above cannot wait for a still-running computation. Deliver once
        // asynchronously on EDT even when cancellation itself was requested there.
        Outcome delivered = outcome;
        SwingUtilities.invokeLater(() -> completion.accept(delivered));
    }
}
