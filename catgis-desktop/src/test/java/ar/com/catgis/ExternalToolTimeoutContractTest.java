package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalToolTimeoutContractTest {

    @Test
    void aHangingOutputStreamCannotBypassTheProcessDeadline() throws Exception {
        HangingProcess process = new HangingProcess();
        ExternalToolService.ToolResult result = assertTimeoutPreemptively(
                Duration.ofSeconds(4),
                () -> ExternalToolService.collectProcess(process, 50L));

        assertFalse(result.success());
        assertTrue(result.error().contains("Timeout"));
        assertEquals(-1, result.exitCode());
        assertTrue(process.destroyed, "Timed-out processes must be forcibly terminated.");
    }

    @Test
    void successfulAndFailedExitsReturnCorrectOutputAndExitCode() {
        ExternalToolService.ToolResult success = ExternalToolService.collectProcess(
                new CompletedProcess("line one\nline two\n", 0), 1000L);
        assertTrue(success.success());
        assertEquals(0, success.exitCode());
        assertEquals("line one\nline two\n", success.output());

        ExternalToolService.ToolResult failure = ExternalToolService.collectProcess(
                new CompletedProcess("failure details\n", 8), 1000L);
        assertFalse(failure.success());
        assertEquals(8, failure.exitCode());
        assertEquals("failure details\n", failure.error());
    }

    @Test
    void veryLargeOutputIsFullyDrainedButStoredWithinMemoryBudget() {
        byte[] largeOutput = new byte[1_200_000];
        Arrays.fill(largeOutput, (byte) 'x');
        ExternalToolService.ToolResult result = ExternalToolService.collectProcess(
                new CompletedProcess(new ByteArrayInputStream(largeOutput), 0), 2000L);

        assertTrue(result.success());
        assertTrue(result.output().length() < 1_049_000);
        assertTrue(result.output().contains("[tool output truncated]"));
    }

    private static final class HangingProcess extends Process {
        private final PipedInputStream readable = new PipedInputStream();
        private final PipedOutputStream writable;
        private volatile boolean destroyed;

        private HangingProcess() throws IOException {
            writable = new PipedOutputStream(readable);
            writable.write("started\n".getBytes(StandardCharsets.UTF_8));
            writable.flush();
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public int waitFor() {
            return -1;
        }

        @Override
        public int exitValue() {
            if (!destroyed) {
                throw new IllegalThreadStateException("Still running");
            }
            return -1;
        }

        @Override
        public boolean isAlive() {
            return !destroyed;
        }

        @Override
        public void destroy() {
            destroyed = true;
            try {
                writable.close();
            } catch (IOException ignored) {
                // Synthetic child is being stopped.
            }
        }

        @Override
        public Process destroyForcibly() {
            destroy();
            return this;
        }

        @Override
        public InputStream getInputStream() {
            return readable;
        }

        @Override
        public InputStream getErrorStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }
    }

    private static final class CompletedProcess extends Process {
        private final InputStream stdout;
        private final int code;

        private CompletedProcess(String stdout, int code) {
            this(new ByteArrayInputStream(stdout.getBytes(StandardCharsets.UTF_8)), code);
        }

        private CompletedProcess(InputStream stdout, int code) {
            this.stdout = stdout;
            this.code = code;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public int waitFor() {
            return code;
        }

        @Override
        public int exitValue() {
            return code;
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public void destroy() {
        }

        @Override
        public InputStream getInputStream() {
            return stdout;
        }

        @Override
        public InputStream getErrorStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }
    }
}
