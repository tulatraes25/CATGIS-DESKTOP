package ar.com.catgis;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolAvailabilityProbeTest {

    @Test
    void slowExternalDiscoveryNeverBlocksSwingEdtAndCallbackRunsOnEdt() throws Exception {
        CountDownLatch resolverEntered = new CountDownLatch(1);
        CountDownLatch allowFinish = new CountDownLatch(1);
        CountDownLatch callbackReceived = new CountDownLatch(1);
        AtomicBoolean resolverOnEdt = new AtomicBoolean(true);
        AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
        AtomicReference<String> result = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> ToolAvailabilityProbe.start(() -> {
            resolverOnEdt.set(SwingUtilities.isEventDispatchThread());
            resolverEntered.countDown();
            try {
                if (!allowFinish.await(8, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Test resolver did not unblock.");
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return null;
            }
            return "C:/Program Files/WhiteboxTools/whitebox_tools.exe";
        }, executable -> {
            callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
            result.set(executable);
            callbackReceived.countDown();
        }));

        try {
            assertTrue(resolverEntered.await(6, TimeUnit.SECONDS),
                    "The worker must start independently of EDT.");
            // If the resolver were on EDT, invokeAndWait would already have blocked.
            assertFalse(resolverOnEdt.get());
        } finally {
            allowFinish.countDown();
        }

        assertTrue(callbackReceived.await(8, TimeUnit.SECONDS));
        assertTrue(callbackOnEdt.get());
        assertEquals("C:/Program Files/WhiteboxTools/whitebox_tools.exe", result.get());
    }

    @Test
    void detectionFailureIsReportedAsUnavailableOnEdt() throws Exception {
        CountDownLatch callbackReceived = new CountDownLatch(1);
        AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
        AtomicReference<String> value = new AtomicReference<>("unset");

        SwingUtilities.invokeAndWait(() -> ToolAvailabilityProbe.start(
                () -> { throw new IllegalStateException("Unavailable dependency"); },
                result -> {
                    callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
                    value.set(result);
                    callbackReceived.countDown();
                }));

        assertTrue(callbackReceived.await(8, TimeUnit.SECONDS));
        assertTrue(callbackOnEdt.get());
        assertNull(value.get());
    }
}
