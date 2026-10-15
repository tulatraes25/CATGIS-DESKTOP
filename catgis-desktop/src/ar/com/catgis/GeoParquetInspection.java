package ar.com.catgis;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/** Existing metadata-only open flow, with file I/O off EDT and explicit cancellation. */
final class GeoParquetInspection {
    private static final ThreadPoolExecutor WORKERS = GeoParquetInspectionTask.newExecutor();
    private static final int TIMEOUT_MILLIS = 30000;

    private GeoParquetInspection() {}

    /** Always false: inspecting metadata is not loading a layer into the project. */
    static boolean open(File file, Component parent, boolean showDialogs) {
        if (file == null || !showDialogs || GraphicsEnvironment.isHeadless()) {
            // A silent layer-open request has no metadata consumer. Do not read a
            // file twice, create a dialog, or claim that a layer was imported.
            return false;
        }
        Request request = new Request(new File(file.getPath()), parent);
        if (SwingUtilities.isEventDispatchThread()) {
            request.showOnEdt();
        } else {
            try {
                SwingUtilities.invokeAndWait(request::showOnEdt);
            } catch (InterruptedException interrupted) {
                request.cancelFromAnyThread();
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException failed) {
                request.cancelFromAnyThread();
                throw new IllegalStateException("No se pudo mostrar la inspeccion GeoParquet.", failed.getCause());
            }
        }
        return false;
    }

    private static final class Request {
        private final File file;
        private final Component parent;
        private final AtomicBoolean dismissed = new AtomicBoolean();
        // Assigned, read and cleared exclusively on EDT.
        private Runnable cancelOnEdt;

        private Request(File file, Component parent) {
            this.file = file;
            this.parent = parent;
        }

        private void cancelFromAnyThread() {
            dismissed.set(true);
            SwingUtilities.invokeLater(() -> {
                if (cancelOnEdt != null) { cancelOnEdt.run(); }
            });
        }

        private void showOnEdt() {
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("La presentacion GeoParquet requiere EDT.");
            }
            Window owner = parent instanceof Window window ? window
                    : parent == null ? null : SwingUtilities.getWindowAncestor(parent);
            if (dismissed.get() || (owner != null && !owner.isDisplayable())) { return; }

            JProgressBar progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.getAccessibleContext().setAccessibleName(I18n.t("Inspeccion de metadatos GeoParquet"));
            String cancelLabel = I18n.t("Cancelar");
            JOptionPane pane = new JOptionPane(new Object[] {
                    I18n.t("Leyendo metadatos GeoParquet: ") + bounded(file.getName(), 160),
                    progress,
                    I18n.t("Solo inspeccion: no se agregara una capa ni se modificara el archivo.")
            }, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                    new Object[] {cancelLabel}, cancelLabel);
            JDialog dialog = pane.createDialog(parent, I18n.t("GeoParquet"));
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            GeoParquetInspectionTask.Outcome[] outcome = {null};
            boolean[] timedOut = {false};
            boolean[] refused = {false};
            GeoParquetInspectionTask task = new GeoParquetInspectionTask(
                    () -> GeoParquetReader.getSummary(file), result -> {
                        if (!dismissed.get()) { outcome[0] = result; }
                        dialog.dispose();
                    });
            Runnable cancel = () -> {
                // Invalidate presentation even if cancellation loses a race with
                // a completed computation whose done() callback is still queued.
                dismissed.set(true);
                task.cancel(true);
                dialog.dispose();
            };
            cancelOnEdt = cancel;
            pane.addPropertyChangeListener(event -> {
                if (JOptionPane.VALUE_PROPERTY.equals(event.getPropertyName())
                        && pane.getValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    cancel.run();
                }
            });
            dialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent event) { cancel.run(); }
                @Override public void windowClosed(WindowEvent event) {
                    if (!task.isDone()) { cancel.run(); }
                }
            });
            WindowAdapter ownerClosed = new WindowAdapter() {
                @Override public void windowClosed(WindowEvent event) { cancel.run(); }
            };
            if (owner != null) { owner.addWindowListener(ownerClosed); }
            Timer timeout = new Timer(TIMEOUT_MILLIS, event -> {
                if (!task.isDone()) {
                    timedOut[0] = true;
                    cancel.run();
                }
            });
            timeout.setRepeats(false);
            try {
                if (!dismissed.get()) {
                    // Execute FutureTask.run() in an explicitly bounded pool,
                    // never a default unbounded waiting queue or the EDT.
                    WORKERS.execute(task);
                    timeout.start();
                    // Modal secondary event loop keeps painting/cancel events
                    // alive and preserves the existing per-file batch ordering.
                    dialog.setVisible(true);
                }
            } catch (RejectedExecutionException busy) {
                refused[0] = true;
            } finally {
                timeout.stop();
                if (owner != null) { owner.removeWindowListener(ownerClosed); }
                if (!task.isDone()) {
                    dismissed.set(true);
                    task.cancel(true);
                }
                cancelOnEdt = null;
                dialog.dispose();
            }
            if (owner != null && !owner.isDisplayable()) { return; }
            if (refused[0]) {
                NotificationManager.warn(parent, "GeoParquet", I18n.t(
                        "Hay dos lecturas que siguen activas. Reintenta cuando una haya terminado."));
            } else if (timedOut[0]) {
                NotificationManager.warn(parent, "GeoParquet", I18n.t(
                        "La inspeccion excedio 30 segundos y se solicito su cancelacion. No se cargo ninguna capa."));
            } else if (!dismissed.get() && outcome[0] != null) {
                if (outcome[0].status() == GeoParquetInspectionTask.Status.INSPECTED) {
                    NotificationManager.info(parent, "GeoParquet", outcome[0].summary());
                } else if (outcome[0].status() == GeoParquetInspectionTask.Status.FAILED) {
                    Throwable failure = outcome[0].failure();
                    String detail = failure == null || failure.getMessage() == null
                            ? I18n.t("Error de inspeccion") : bounded(failure.getMessage(), 512);
                    NotificationManager.error(parent, "GeoParquet",
                            I18n.t("No se pudo inspeccionar el archivo. No se cargo ninguna capa.") + "\n" + detail);
                }
            }
        }
    }

    private static String bounded(String value, int limit) {
        String text = value.replace('\r', ' ').replace('\n', ' ');
        return text.length() <= limit ? text : text.substring(0, limit) + "...";
    }
}
