package ar.com.catgis;

import ar.com.catgis.scripting.ScriptEngine;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T4/#132: real-Python lifecycle evidence on every desktop platform.
 *
 * <p>Repeated syntax-error executions must return a bounded, non-empty diagnostic
 * (the original Windows anomaly was an empty output after the 30 s timeout), and
 * a very large stdout must stay bounded without hanging. This test intentionally
 * has no assumption/skip: Certification verifies it executed.</p>
 */
class ScriptNativeLifecycleContractTest {

    @Test
    void realPythonReturnsBoundedDiagnosticsAndBoundedOutput() {
        assertTrue(ScriptEngine.isPythonAvailable(),
                "Python is required for the native scripting lifecycle evidence");
        ScriptEngine.ScriptResult ok = ScriptEngine.executeCode("print('lifecycle-ok')", 15);
        assertNotNull(ok);
        assertTrue(ok.success(), "plain python code must succeed: " + ok.error());
        assertTrue(ok.output().contains("lifecycle-ok"));

        long maxSyntaxMillis = 0L;
        for (int attempt = 1; attempt <= 3; attempt++) {
            long began = System.nanoTime();
            ScriptEngine.ScriptResult syntax = ScriptEngine.executeCode("def bad(", 15);
            long elapsed = (System.nanoTime() - began) / 1_000_000L;
            maxSyntaxMillis = Math.max(maxSyntaxMillis, elapsed);
            assertFalse(syntax.success(), "syntax error must fail, attempt " + attempt);
            assertFalse(syntax.error() == null || syntax.error().isBlank(),
                    "syntax error must retain a diagnostic, attempt " + attempt
                            + " (elapsed=" + elapsed + "ms)");
            assertTrue(elapsed < 25_000L,
                    "syntax-error run exceeded the bounded deadline: " + elapsed + "ms");
        }

        ScriptEngine.ScriptResult large = ScriptEngine.executeCode(
                "print('x' * 300000)", 20);
        assertTrue(large.success(), "large-output run failed: " + large.error());
        assertTrue(large.output().length() <= (1024 * 1024) + 1,
                "stdout must stay within the reviewed byte budget: "
                        + large.output().length());

        // Bounded stdin: a real child receives the selected input and oversized
        // input fails closed before launching an unbounded child.
        try {
            java.nio.file.Path stdinScript = java.nio.file.Files.createTempFile(
                    "catgis_stdin_", ".py");
            try {
                java.nio.file.Files.writeString(stdinScript,
                        "import sys\ndata=sys.stdin.read()\nprint(len(data))",
                        java.nio.charset.StandardCharsets.UTF_8);
                ScriptEngine.ScriptResult stdin = ScriptEngine.executeScript(
                        stdinScript.toFile(), 15, "x".repeat(10_000));
                assertTrue(stdin.success(), "bounded stdin run failed: " + stdin.error());
                assertTrue(stdin.output().contains("10000"), stdin.output());
                String tooLarge = "y".repeat(1024 * 1024 + 1);
                ScriptEngine.ScriptResult rejected = ScriptEngine.executeScript(
                        stdinScript.toFile(), 15, tooLarge);
                assertFalse(rejected.success(), "oversized stdin must fail closed");
                assertFalse(rejected.error() == null || rejected.error().isBlank(),
                        "oversized stdin must return a diagnostic");
            } finally {
                java.nio.file.Files.deleteIfExists(stdinScript);
            }
        } catch (java.io.IOException io) {
            throw new AssertionError("stdin fixture failed", io);
        }

        System.out.println("SCRIPT_NATIVE_LIFECYCLE_OK os="
                + System.getProperty("os.name", "").toLowerCase(Locale.ROOT)
                + " syntaxIterations=3 maxSyntaxMillis=" + maxSyntaxMillis
                + " boundedOutputBytes=" + large.output().length());
    }
}
