package ar.com.catgis;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class ProOpenBatchCoordinator {

    private ProOpenBatchCoordinator() {
    }

    static BatchResult prepareEntries(List<ProDatasetOpenService.Entry> entries,
                                      List<String> skipped,
                                      ProgressListener listener,
                                      CancellationToken cancellationToken) {
        List<OpenFileAction.PreparedProRaster> prepared = new ArrayList<>();
        List<BatchFailure> failures = new ArrayList<>();
        List<ProDatasetOpenService.Entry> safeEntries = entries != null ? entries : List.of();
        int total = safeEntries.size();

        for (int index = 0; index < total; index++) {
            ProDatasetOpenService.Entry entry = safeEntries.get(index);
            if (entry == null) {
                continue;
            }
            if (isCancellationRequested(cancellationToken)) {
                publish(listener, new ProgressSnapshot(prepared.size(), total, "Cancelando job Pro...", true));
                return new BatchResult(prepared, failures, skipped, true);
            }

            publish(listener, new ProgressSnapshot(
                    prepared.size(),
                    total,
                    "Preparando variable Pro " + (index + 1) + " de " + total + ": " + entry.variableLabel(),
                    false
            ));

            try {
                OpenFileAction.PreparedProRaster preparedRaster = OpenFileAction.prepareProRasterEntry(
                        entry,
                        new EntryMonitor(listener, cancellationToken, entry, prepared.size(), total)
                );
                prepared.add(preparedRaster);
                publish(listener, new ProgressSnapshot(
                        prepared.size(),
                        total,
                        "Variable Pro lista: " + entry.variableLabel(),
                        false
                ));
            } catch (InterruptedIOException ex) {
                if (cancellationToken != null) {
                    cancellationToken.requestCancel();
                }
                publish(listener, new ProgressSnapshot(prepared.size(), total, "Job Pro cancelado por el usuario.", true));
                return new BatchResult(prepared, failures, skipped, true);
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String reason = cause.getMessage() != null && !cause.getMessage().isBlank()
                        ? cause.getMessage()
                        : cause.getClass().getSimpleName();
                failures.add(new BatchFailure(entry.variableLabel(), reason));
                publish(listener, new ProgressSnapshot(
                        prepared.size(),
                        total,
                        "Error preparando " + entry.variableLabel() + ": " + reason,
                        false
                ));
            }
        }

        publish(listener, new ProgressSnapshot(
                prepared.size(),
                total,
                prepared.isEmpty()
                        ? "No quedaron variables Pro listas para incorporar."
                        : prepared.size() == 1
                        ? "1 variable Pro lista para incorporar al proyecto."
                        : prepared.size() + " variables Pro listas para incorporar al proyecto.",
                false
        ));
        return new BatchResult(prepared, failures, skipped, false);
    }

    private static boolean isCancellationRequested(CancellationToken cancellationToken) {
        return cancellationToken != null && cancellationToken.isCancellationRequested();
    }

    private static void publish(ProgressListener listener, ProgressSnapshot snapshot) {
        if (listener != null && snapshot != null) {
            listener.onProgress(snapshot);
        }
    }

    interface ProgressListener {
        void onProgress(ProgressSnapshot snapshot);
    }

    static final class CancellationToken {
        private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);

        void requestCancel() {
            cancellationRequested.set(true);
        }

        boolean isCancellationRequested() {
            return cancellationRequested.get();
        }
    }

    record ProgressSnapshot(int completed, int total, String message, boolean cancelRequested) {
    }

    record BatchFailure(String label, String reason) {
    }

    record BatchResult(List<OpenFileAction.PreparedProRaster> prepared,
                       List<BatchFailure> failures,
                       List<String> skipped,
                       boolean canceled) {
        BatchResult {
            prepared = prepared != null ? List.copyOf(prepared) : List.of();
            failures = failures != null ? List.copyOf(failures) : List.of();
            skipped = skipped != null ? List.copyOf(skipped) : List.of();
        }
    }

    private static final class EntryMonitor implements ProJobMonitor {
        private final ProgressListener listener;
        private final CancellationToken cancellationToken;
        private final ProDatasetOpenService.Entry entry;
        private final int completed;
        private final int total;

        private EntryMonitor(ProgressListener listener,
                             CancellationToken cancellationToken,
                             ProDatasetOpenService.Entry entry,
                             int completed,
                             int total) {
            this.listener = listener;
            this.cancellationToken = cancellationToken;
            this.entry = entry;
            this.completed = completed;
            this.total = total;
        }

        @Override
        public void report(String message) {
            publish(listener, new ProgressSnapshot(
                    completed,
                    total,
                    message != null && !message.isBlank()
                            ? message
                            : "Procesando variable Pro: " + (entry != null ? entry.variableLabel() : "-"),
                    isCancellationRequested()
            ));
        }

        @Override
        public boolean isCancellationRequested() {
            return cancellationToken != null && cancellationToken.isCancellationRequested();
        }
    }
}
