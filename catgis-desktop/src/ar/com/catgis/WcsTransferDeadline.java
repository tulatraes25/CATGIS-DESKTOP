package ar.com.catgis;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One monotonic WCS transfer deadline across v2 and v1 attempts.
 * A small daemon watchdog disconnects blocked HTTP operations when the
 * deadline expires or the worker is interrupted. This does not certify
 * native TIFF decoder latency or filesystem durability.
 */
final class WcsTransferDeadline {

    static final String PROPERTY = "catgis.wcs.maxDownloadSeconds";
    private static final long DEFAULT_SECONDS = 600L;
    private static final long MAX_SECONDS = 7200L;
    private static final long WATCHDOG_POLL_MILLIS = 100L;
    private static final ScheduledExecutorService WATCHDOG =
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    private final long startedNanos;
    private final long budgetNanos;
    private final AtomicBoolean timedOut = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    private WcsTransferDeadline(long maxMillis) {
        this.startedNanos = System.nanoTime();
        this.budgetNanos = TimeUnit.MILLISECONDS.toNanos(maxMillis);
    }

    static WcsTransferDeadline configured() throws IOException {
        String requested = System.getProperty(PROPERTY);
        long seconds = DEFAULT_SECONDS;
        if (requested != null && !requested.isBlank()) {
            try {
                seconds = Long.parseLong(requested.trim());
            } catch (NumberFormatException invalid) {
                throw new IOException("WCS transfer deadline must be whole seconds.", invalid);
            }
        }
        if (seconds < 1L || seconds > MAX_SECONDS) {
            throw new IOException("WCS transfer deadline must be between 1 and 7200 seconds.");
        }
        return new WcsTransferDeadline(TimeUnit.SECONDS.toMillis(seconds));
    }

    /** Package-private test seam: allows subsecond deadlines without global properties. */
    static WcsTransferDeadline forTesting(long maxMillis) throws IOException {
        if (maxMillis < 1L || maxMillis > TimeUnit.SECONDS.toMillis(MAX_SECONDS)) {
            throw new IOException("WCS test deadline must be between 1 ms and 7200 seconds.");
        }
        return new WcsTransferDeadline(maxMillis);
    }

    void checkActive() throws InterruptedIOException {
        if (cancelled.get() || Thread.currentThread().isInterrupted()) {
            cancelled.set(true);
            throw new InterruptedIOException("WCS transfer cancelled; output was not published.");
        }
        if (timedOut.get() || System.nanoTime() - startedNanos >= budgetNanos) {
            timedOut.set(true);
            throw new InterruptedIOException("WCS transfer exceeded its total time budget.");
        }
    }

    int socketTimeoutMillis(int ceilingMillis) throws InterruptedIOException {
        checkActive();
        long remaining = budgetNanos - (System.nanoTime() - startedNanos);
        long millis = Math.max(1L, (remaining / 1_000_000L) + (remaining % 1_000_000L == 0 ? 0L : 1L));
        return (int) Math.max(1L, Math.min((long) ceilingMillis, millis));
    }

    /**
     * In addition to per-socket timeouts, forcibly disconnect active HTTP
     * connections. Without this, a response trickling data could evade every
     * individual read timeout. The flag survives IO exceptions and prevents
     * an unintended fallback to WCS 1.0 after deadline/cancellation.
     */
    Guard watch(HttpURLConnection connection) throws InterruptedIOException {
        checkActive();
        Thread owner = Thread.currentThread();
        ScheduledFuture<?> job = WATCHDOG.scheduleAtFixedRate(() -> {
            if (owner.isInterrupted()) {
                cancelled.set(true);
                connection.disconnect();
            } else if (System.nanoTime() - startedNanos >= budgetNanos) {
                timedOut.set(true);
                connection.disconnect();
            }
        }, WATCHDOG_POLL_MILLIS, WATCHDOG_POLL_MILLIS, TimeUnit.MILLISECONDS);
        return () -> job.cancel(false);
    }

    interface Guard extends AutoCloseable {
        @Override
        void close();
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable task) {
            Thread t = new Thread(task, "catgis-wcs-transfer-watchdog");
            t.setDaemon(true);
            return t;
        }
    }
}
