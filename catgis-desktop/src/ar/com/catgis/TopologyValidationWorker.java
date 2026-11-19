package ar.com.catgis;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * T5/#113: topology validation off the Swing EDT with cooperative cancellation.
 *
 * <p>The dialog must keep the UI responsive for large layers; the expensive JTS
 * evaluation runs in {@link #doInBackground()} and results are only applied on
 * the EDT via {@link #done()} by the dialog. Cancellation discards the result
 * instead of publishing a stale table.</p>
 */
final class TopologyValidationWorker
        extends SwingWorker<List<TopologyCheckResult>, Void> {

    @FunctionalInterface
    interface Validator {
        List<TopologyCheckResult> validate() throws Exception;
    }

    private final Validator validator;
    private final java.util.function.Consumer<List<TopologyCheckResult>> onSuccess;
    private final java.util.function.Consumer<Exception> failureHandler;

    TopologyValidationWorker(Validator validator,
                             java.util.function.Consumer<List<TopologyCheckResult>> onSuccess,
                             java.util.function.Consumer<Exception> failureHandler) {
        this.validator = validator;
        this.onSuccess = onSuccess;
        this.failureHandler = failureHandler;
    }

    @Override
    protected List<TopologyCheckResult> doInBackground() throws Exception {
        return validator.validate();
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            return;
        }
        try {
            List<TopologyCheckResult> results = get();
            if (!isCancelled() && onSuccess != null) {
                onSuccess.accept(results);
            }
        } catch (java.util.concurrent.CancellationException cancelled) {
            // Cancelled after the worker finished; do not publish.
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (java.util.concurrent.ExecutionException failure) {
            if (failureHandler != null) {
                failureHandler.accept(failure.getCause() instanceof Exception cause
                        ? cause : new IllegalStateException(failure.getCause()));
            }
        }
    }
}
