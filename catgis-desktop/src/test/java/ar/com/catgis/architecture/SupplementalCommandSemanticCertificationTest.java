package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplementalCommandSemanticCertificationTest {

    private static final Path QUALITY = Path.of("..", "docs", "quality");
    private static final Path INVENTORY = QUALITY.resolve("SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv");
    private static final Path SEMANTIC_MAP = QUALITY.resolve("SUPPLEMENTAL_COMMAND_SEMANTIC_MAP.tsv");
    private static final Path MATRIX = QUALITY.resolve("SEMANTIC_FEATURE_MATRIX.tsv");

    private static final Set<String> CLASSIFICATIONS = Set.of(
            "ALIAS_EXISTING",
            "ACTION",
            "CONTROL",
            "DISPLAY_ONLY",
            "DISABLED_UNSUPPORTED"
    );

    @Test
    void everySupplementalCommandMapsExactlyOnce() throws Exception {
        Set<String> expected = new LinkedHashSet<>();
        for (String line : dataLines(INVENTORY)) {
            String[] p = line.split("\\t", -1);
            assertEquals(4, p.length, "Malformed supplemental inventory row: " + line);
            assertTrue(expected.add(key(p[0], p[1], p[2])),
                    "Duplicate supplemental inventory key: " + line);
        }

        Set<String> actual = new LinkedHashSet<>();
        for (String line : dataLines(SEMANTIC_MAP)) {
            String[] p = line.split("\\t", -1);
            assertEquals(5, p.length, "Malformed supplemental semantic row: " + line);
            assertTrue(actual.add(key(p[0], p[1], p[2])),
                    "Duplicate supplemental semantic key: " + line);
        }

        assertEquals(191, expected.size(), "Unexpected supplemental command baseline.");
        assertEquals(expected, actual, "Supplemental semantic coverage drifted.");
    }

    @Test
    void classificationsAreExplicitAndActionIdsExist() throws Exception {
        Map<String, String[]> matrix = matrixRows();
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String line : dataLines(SEMANTIC_MAP)) {
            String[] p = line.split("\\t", -1);
            String classification = p[3];
            String semanticId = p[4];

            assertTrue(CLASSIFICATIONS.contains(classification),
                    "Unsupported supplemental classification: " + classification);
            counts.merge(classification, 1, Integer::sum);

            if ("ACTION".equals(classification) || "ALIAS_EXISTING".equals(classification)) {
                assertTrue(semanticId.matches("SEM-[A-Z0-9-]+"),
                        "Action-bearing supplemental row needs semantic ID: " + line);
                assertTrue(matrix.containsKey(semanticId),
                        "Supplemental mapping points to missing semantic ID: " + semanticId);
                if ("ACTION".equals(classification)) {
                    assertEquals("SUPPLEMENTAL_COMMAND", matrix.get(semanticId)[5],
                            "New supplemental action must have SUPPLEMENTAL_COMMAND provenance: " + semanticId);
                }
            } else {
                assertEquals("NON_FEATURE", semanticId,
                        "Control/display/unsupported row must be NON_FEATURE: " + line);
            }
        }

        assertEquals(98, counts.getOrDefault("ALIAS_EXISTING", 0));
        assertEquals(74, counts.getOrDefault("ACTION", 0));
        assertEquals(14, counts.getOrDefault("CONTROL", 0));
        assertEquals(2, counts.getOrDefault("DISPLAY_ONLY", 0));
        assertEquals(3, counts.getOrDefault("DISABLED_UNSUPPORTED", 0));
    }

    @Test
    void disabledUnsupportedCommandsAreOnlyTheThreeGatedCatmapSyncEntries() throws Exception {
        Set<String> expected = Set.of(
                "catmap/Main.java\tADDMENUITEM\tSincronizar capas visibles",
                "catmap/Main.java\tADDMENUITEM\tSincronizar etiquetas",
                "catmap/Main.java\tADDMENUITEM\tSincronizar simbología"
        );
        Set<String> actual = new LinkedHashSet<>();
        for (String line : dataLines(SEMANTIC_MAP)) {
            String[] p = line.split("\\t", -1);
            if ("DISABLED_UNSUPPORTED".equals(p[3])) {
                actual.add(key(p[0], p[1], p[2]));
            }
        }
        assertEquals(expected, actual,
                "Unsupported visible commands changed; review gating and product behavior explicitly.");
    }

    @Test
    void supplementalActionRowsRemainRuntimePendingBeforeArchitectureClosure() throws Exception {
        Map<String, String[]> matrix = matrixRows();
        Set<String> checked = new LinkedHashSet<>();
        for (String line : dataLines(SEMANTIC_MAP)) {
            String[] p = line.split("\\t", -1);
            if ("ACTION".equals(p[3]) && checked.add(p[4])) {
                assertEquals("RUNTIME_PENDING", matrix.get(p[4])[4],
                        "Supplemental feature promoted before A0.7: " + p[4]);
            }
        }
        assertEquals(65, checked.size(), "Unexpected supplemental semantic feature count.");
    }

    private static Map<String, String[]> matrixRows() throws Exception {
        Map<String, String[]> rows = new LinkedHashMap<>();
        for (String line : dataLines(MATRIX)) {
            String[] p = line.split("\\t", -1);
            assertEquals(7, p.length, "Malformed semantic matrix row: " + line);
            assertTrue(rows.putIfAbsent(p[0], p) == null, "Duplicate semantic ID: " + p[0]);
        }
        return rows;
    }

    private static String key(String source, String kind, String label) {
        return source + "\t" + kind + "\t" + label;
    }

    private static java.util.List<String> dataLines(Path path) throws Exception {
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .toList();
    }
}
