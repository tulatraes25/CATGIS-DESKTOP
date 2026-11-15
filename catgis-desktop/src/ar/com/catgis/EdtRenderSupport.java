package ar.com.catgis;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Runs Swing-state-touching work on the Event Dispatch Thread (DC-3).
 *
 * <p>Map rendering for atlas pages, layout export and preview snapshots mutates
 * live {@code MapPanel} state (view offsets, zoom, temporary render order,
 * exporting flag) and calls {@code paintComponent}. Those calls can arrive from
 * background workers, so they must be serialized onto the EDT instead of racing
 * the normal paint cycle. Callers already on the EDT run inline to avoid
 * deadlocks and unnecessary round-trips.</p>
 */
public final class EdtRenderSupport {

    private EdtRenderSupport() {
    }

    public static <T> T call(Supplier<T> task, String description) {
        if (task == null) {
            throw new IllegalArgumentException("La tarea de render no puede ser nula.");
        }
        String label = description != null && !description.isBlank() ? description : "la operacion de render";
        if (SwingUtilities.isEventDispatchThread()) {
            return task.get();
        }

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result.set(task.get());
                } catch (Throwable error) {
                    failure.set(error);
                }
            });
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpio " + label + ".", interrupted);
        } catch (InvocationTargetException invocation) {
            failure.set(invocation.getCause() != null ? invocation.getCause() : invocation);
        }

        Throwable error = failure.get();
        if (error != null) {
            if (error instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (error instanceof Error fatal) {
                throw fatal;
            }
            throw new IllegalStateException("Fallo " + label + ": " + error.getMessage(), error);
        }
        return result.get();
    }
}
