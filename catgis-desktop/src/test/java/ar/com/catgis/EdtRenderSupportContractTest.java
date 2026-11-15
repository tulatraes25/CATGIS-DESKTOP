package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DC-3 contract: Swing-state-touching render work requested from a background
 * thread must execute on the Event Dispatch Thread; work already on the EDT must
 * run inline, and task failures must surface unchanged to the caller.
 */
class EdtRenderSupportContractTest {

    @Test
    void backgroundCallerIsMarshalledToTheEdt() throws Exception {
        AtomicBoolean ranOnEdt = new AtomicBoolean(false);

        String result = EdtRenderSupport.call(
                () -> {
                    ranOnEdt.set(SwingUtilities.isEventDispatchThread());
                    return "render-ok";
                },
                "prueba de render");

        assertEquals("render-ok", result);
        assertTrue(ranOnEdt.get(), "La tarea debe ejecutarse en el Event Dispatch Thread.");
    }

    @Test
    void edtCallerRunsInlineWithoutDeadlock() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            AtomicBoolean ranOnEdt = new AtomicBoolean(false);
            String result = EdtRenderSupport.call(
                    () -> {
                        ranOnEdt.set(SwingUtilities.isEventDispatchThread());
                        return "inline";
                    },
                    "prueba de render");
            assertEquals("inline", result);
            assertTrue(ranOnEdt.get());
        });
    }

    @Test
    void taskFailureReachesTheBackgroundCallerUnchanged() throws Exception {
        IllegalStateException failure = new IllegalStateException("fallo de render");
        Throwable[] captured = new Throwable[1];
        Thread worker = new Thread(() -> {
            try {
                EdtRenderSupport.call(() -> {
                    throw failure;
                }, "prueba de render");
            } catch (Throwable thrown) {
                captured[0] = thrown;
            }
        }, "dc3-worker");
        worker.start();
        worker.join(30_000L);

        assertSame(failure, captured[0], "La excepcion original debe propagarse sin envolver.");
    }

    @Test
    void nullTaskIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> EdtRenderSupport.call(null, "prueba"));
    }
}
