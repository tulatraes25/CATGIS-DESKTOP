package ar.com.catgis;

import javax.swing.SwingWorker;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A0.3 adapter: perform potentially blocking executable discovery off the Swing EDT.
 * The result callback is delivered by SwingWorker.done() on the EDT.
 */
final class ToolAvailabilityProbe {

    private ToolAvailabilityProbe() {
    }

    static SwingWorker<String, Void> start(Supplier<String> resolver,
                                            Consumer<String> onResult) {
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(onResult, "onResult");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return resolver.get();
            }

            @Override
            protected void done() {
                String executable = null;
                try {
                    executable = get();
                } catch (CancellationException ignored) {
                    // Closing the dialog cannot turn a failed discovery into availability.
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException failure) {
                    CatgisLogger.warn("WhiteboxTools: fallo la deteccion del ejecutable", failure);
                }
                onResult.accept(executable);
            }
        };
        worker.execute();
        return worker;
    }
}
