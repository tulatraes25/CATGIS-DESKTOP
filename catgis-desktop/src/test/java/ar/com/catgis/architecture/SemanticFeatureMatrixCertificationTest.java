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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticFeatureMatrixCertificationTest {

    private static final Path QUALITY = Path.of("..", "docs", "quality");
    private static final Path VISIBLE = QUALITY.resolve("VISIBLE_COMMAND_INVENTORY.tsv");
    private static final Path STATIC_MAP = QUALITY.resolve("STATIC_COMMAND_SEMANTIC_MAP.tsv");
    private static final Path DYNAMIC = QUALITY.resolve("DYNAMIC_COMMAND_INVENTORY.tsv");
    private static final Path MODULES = QUALITY.resolve("MODULE_ACTION_INVENTORY.tsv");
    private static final Path MODULE_MAP = QUALITY.resolve("MODULE_ACTION_SEMANTIC_MAP.tsv");
    private static final Path SURFACES = QUALITY.resolve("SOURCE_FEATURE_SURFACE_INVENTORY.tsv");
    private static final Path SURFACE_MAP = QUALITY.resolve("SURFACE_SEMANTIC_MAP.tsv");
    private static final Path MATRIX = QUALITY.resolve("SEMANTIC_FEATURE_MATRIX.tsv");

    private static final Set<String> VALID_TIERS =
            Set.of("CORE", "BETA", "EXPERIMENTAL", "EXTERNAL_DEPENDENCY");
    private static final Set<String> REQUIRED_SURFACE_CATEGORIES =
            Set.of("ACTION", "DIALOG_OR_WINDOW", "LOADER_OR_READER", "EXPORT_SURFACE");

    @Test
    void everyStaticCommandMapsExactlyOnceToSemanticFeature() throws Exception {
        Set<String> expected = new LinkedHashSet<>();
        for (String line : dataLines(VISIBLE)) {
            String[] p = line.split("\\t", -1);
            assertEquals(4, p.length, "Malformed visible command row: " + line);
            assertTrue(expected.add(p[0] + "\t" + p[1] + "\t" + p[2]),
                    "Duplicate visible command key: " + line);
        }

        Set<String> actual = new LinkedHashSet<>();
        Set<String> semanticIds = matrixIds();
        for (String line : dataLines(STATIC_MAP)) {
            String[] p = line.split("\\t", -1);
            assertEquals(5, p.length, "Malformed static semantic row: " + line);
            assertTrue(actual.add(p[0] + "\t" + p[1] + "\t" + p[2]),
                    "Duplicate static semantic mapping: " + line);
            assertTrue(semanticIds.contains(p[4]), "Static mapping points to missing semantic ID: " + p[4]);
        }
        assertEquals(expected, actual, "Static command semantic coverage drifted.");
    }

    @Test
    void everyActionBearingDynamicCommandExistsInMatrix() throws Exception {
        Set<String> semanticIds = matrixIds();
        for (String line : dataLines(DYNAMIC)) {
            String[] p = line.split("\\t", -1);
            assertEquals(6, p.length, "Malformed dynamic command row: " + line);
            String semanticId = p[5];
            if (!"NON_FEATURE".equals(semanticId)) {
                assertTrue(semanticIds.contains(semanticId),
                        "Dynamic action semantic ID missing from matrix: " + semanticId);
            }
        }
    }

    @Test
    void everyModuleRegistryActionMapsToSemanticFeature() throws Exception {
        Set<String> expected = new LinkedHashSet<>();
        for (String line : dataLines(MODULES)) {
            String[] p = line.split("\\t", -1);
            assertEquals(2, p.length, "Malformed module action row: " + line);
            expected.add(p[0] + "\t" + p[1]);
        }

        Set<String> actual = new LinkedHashSet<>();
        Set<String> semanticIds = matrixIds();
        for (String line : dataLines(MODULE_MAP)) {
            String[] p = line.split("\\t", -1);
            assertEquals(3, p.length, "Malformed module semantic row: " + line);
            actual.add(p[0] + "\t" + p[1]);
            assertTrue(semanticIds.contains(p[2]), "Module mapping points to missing semantic ID: " + p[2]);
        }
        assertEquals(expected, actual, "Module semantic coverage drifted.");
    }

    @Test
    void everyRequiredNonCommandSurfaceIsMappedAndTiered() throws Exception {
        Set<String> expected = new LinkedHashSet<>();
        for (String line : dataLines(SURFACES)) {
            String[] p = line.split("\\t", -1);
            assertEquals(2, p.length, "Malformed source surface row: " + line);
            if (REQUIRED_SURFACE_CATEGORIES.contains(p[0])) {
                expected.add(p[0] + "\t" + p[1]);
            }
        }

        Set<String> actual = new LinkedHashSet<>();
        Set<String> semanticIds = matrixIds();
        for (String line : dataLines(SURFACE_MAP)) {
            String[] p = line.split("\\t", -1);
            assertEquals(4, p.length, "Malformed surface semantic row: " + line);
            assertTrue(VALID_TIERS.contains(p[3]), "Invalid surface tier: " + p[3]);
            actual.add(p[0] + "\t" + p[1]);
            assertTrue(semanticIds.contains(p[2]), "Surface mapping points to missing semantic ID: " + p[2]);
        }
        assertEquals(expected, actual, "Required non-command semantic coverage drifted.");
    }

    @Test
    void semanticFeatureMatrixHasUniqueIdsValidTiersAndRuntimePendingStatus() throws Exception {
        Map<String, String> rows = new LinkedHashMap<>();
        for (String line : dataLines(MATRIX)) {
            String[] p = line.split("\\t", -1);
            assertEquals(7, p.length, "Malformed semantic feature row: " + line);
            assertTrue(p[0].matches("SEM-[A-Z0-9-]+"), "Malformed semantic ID: " + p[0]);
            assertFalse(p[1].isBlank(), "Blank domain: " + line);
            assertFalse(p[2].isBlank(), "Blank feature name: " + line);
            assertTrue(VALID_TIERS.contains(p[3]), "Invalid tier: " + p[3]);
            assertEquals("RUNTIME_PENDING", p[4],
                    "F1.2 must not claim runtime certification before A0.7: " + line);
            assertFalse(p[5].isBlank(), "Blank provenance: " + line);
            assertFalse(p[6].isBlank(), "Blank entry points: " + line);
            assertTrue(rows.putIfAbsent(p[0], line) == null, "Duplicate semantic ID: " + p[0]);
        }
        assertTrue(rows.size() >= 450, "Unexpected semantic feature-count regression: " + rows.size());
    }

    private static Set<String> matrixIds() throws Exception {
        Set<String> ids = new LinkedHashSet<>();
        for (String line : dataLines(MATRIX)) {
            String[] p = line.split("\\t", -1);
            assertTrue(p.length >= 1, "Malformed matrix row: " + line);
            assertTrue(ids.add(p[0]), "Duplicate semantic ID: " + p[0]);
        }
        return ids;
    }

    private static java.util.List<String> dataLines(Path path) throws Exception {
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .toList();
    }
}
