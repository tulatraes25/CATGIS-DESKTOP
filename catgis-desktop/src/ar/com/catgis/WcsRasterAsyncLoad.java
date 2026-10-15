package ar.com.catgis;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Headless-testable raster decode worker. The caller commits project and
 * map state only from the EDT success callback, never from doInBackground.
 * Ownership of a loaded raster is released on cancellation/failure.
 */
final class WcsRasterAsyncLoad<T> extends SwingWorker<T, Void> {

    @FunctionalInterface
    interface Decode<T> {
        T load() throws Exception;
    }

    private final Decode<T> decode;
    private final Consumer<T> onLoaded;
    private final Consumer<Throwable> onFailure;
    private final Runnable onCancelled;
    private final Consumer<T> discard;
    private final AtomicReference<T> owned = new AtomicReference<>();

    WcsRasterAsyncLoad(Decode<T> decode, Consumer<T> onLoaded,
                       Consumer<Throwable> onFailure, Runnable onCancelled,
                       Consumer<T> discard) {
        this.decode = decode;
        this.onLoaded = onLoaded;
        this.onFailure = onFailure;
        this.onCancelled = onCancelled;
        this.discard = discard;
    }

    @Override
    protected T doInBackground() throws Exception {
        T data = decode.load();
        owned.set(data);
        if (isCancelled()) {
            discardOwned();
        }
        return data;
    }

    private void discardOwned() {
        T data = owned.getAndSet(null);
        if (data != null) {
            discard.accept(data);
        }
    }

    @Override
    protected void done() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Raster completion must run on Swing EDT.");
        }
        if (isCancelled()) {
            discardOwned();
            onCancelled.run();
            return;
        }
        try {
            get();
            if (isCancelled()) {
                discardOwned();
                onCancelled.run();
                return;
            }
            T data = owned.getAndSet(null);
            try {
                onLoaded.accept(data);
            } catch (RuntimeException failure) {
                if (data != null) {
                    discard.accept(data);
                }
                onFailure.accept(failure);
            }
        } catch (CancellationException cancelled) {
            discardOwned();
            onCancelled.run();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            discardOwned();
            onFailure.accept(interrupted);
        } catch (ExecutionException failed) {
            discardOwned();
            onFailure.accept(failed.getCause() == null ? failed : failed.getCause());
        }
    }
}
