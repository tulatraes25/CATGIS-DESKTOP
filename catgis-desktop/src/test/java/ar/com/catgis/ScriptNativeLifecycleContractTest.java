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

        System.out.println("SCRIPT_NATIVE_LIFECYCLE_OK os="
                + System.getProperty("os.name", "").toLowerCase(Locale.ROOT)
                + " syntaxIterations=3 maxSyntaxMillis=" + maxSyntaxMillis
                + " boundedOutputBytes=" + large.output().length());
    }
}
