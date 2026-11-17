package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** REQ-BETA-4: all seven analysis workflows stay wired from CATMAP standalone. */
class AnalysisActionsContractTest {

    @Test
    void exposesEveryCertifiedAnalysisWorkflow() {
        List<String> expected = List.of(
                "openContours", "openDrainage", "openBasins", "openFlood",
                "openRisk", "openViewshed", "openProfile");
        for (String name : expected) {
            Method method = assertDoesNotThrow(() ->
                    AnalysisActions.class.getMethod(name));
            assertTrue(Modifier.isStatic(method.getModifiers()),
                    name + " must be a static entry point");
            assertTrue(Modifier.isPublic(method.getModifiers()),
                    name + " must be public");
        }
    }

    private static <T> T assertDoesNotThrow(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ReflectiveOperationException missing) {
            throw new AssertionError("missing analysis entry point", missing);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws ReflectiveOperationException;
    }
}
