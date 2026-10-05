package ar.com.catgis;

import java.io.InterruptedIOException;

interface ProJobMonitor {

    void report(String message);

    boolean isCancellationRequested();

    default void checkCanceled() throws InterruptedIOException {
        if (isCancellationRequested()) {
            throw new InterruptedIOException("El job Pro fue cancelado por el usuario.");
        }
    }

    static ProJobMonitor noop() {
        return NoopMonitor.INSTANCE;
    }

    final class NoopMonitor implements ProJobMonitor {
        private static final NoopMonitor INSTANCE = new NoopMonitor();

        private NoopMonitor() {
        }

        @Override
        public void report(String message) {
        }

        @Override
        public boolean isCancellationRequested() {
            return false;
        }
    }
}
