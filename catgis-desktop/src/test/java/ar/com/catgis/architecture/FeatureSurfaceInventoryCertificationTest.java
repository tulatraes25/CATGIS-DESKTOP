package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureSurfaceInventoryCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path INVENTORY = Path.of("..", "docs", "quality", "SOURCE_FEATURE_SURFACE_INVENTORY.tsv");

    private static final Set<String> COMMAND_SURFACES = Set.of(
            "MainMenuBar.java",
            "LayerContextMenuBuilder.java",
            "MapPopupMenuBuilder.java",
            "MapPopupHandler.java",
            "PinManager.java"
    );

    @Test
    void checkedInSurfaceInventoryMatchesProductionSourceExactly() throws Exception {
        Set<String> expected = readExpectedInventory();
        Set<String> actual = discoverInventory();

        assertFalse(expected.isEmpty(), "Checked-in feature-surface inventory must not be empty.");
        assertEquals(expected, actual,
                "User-facing/source capability surfaces changed. Update the certification inventory in the same PR.");
    }

    @Test
    void inventoryCoversEveryRequiredSurfaceCategory() throws Exception {
        Set<String> actual = discoverInventory();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String row : actual) {
            String category = row.substring(0, row.indexOf('\t'));
            counts.merge(category, 1, Integer::sum);
        }

        for (String category : Set.of(
                "DIALOG_OR_WINDOW",
                "ACTION",
                "LOADER_OR_READER",
                "SERVICE",
                "TOOLBAR",
                "EXPORT_SURFACE",
                "COMMAND_SURFACE")) {
            assertTrue(counts.getOrDefault(category, 0) > 0,
                    "Missing inventory category: " + category + "; counts=" + counts);
        }
    }

    private static Set<String> readExpectedInventory() throws Exception {
        assertTrue(Files.isRegularFile(INVENTORY), "Missing checked-in inventory: " + INVENTORY);
        Set<String> rows = new LinkedHashSet<>();
        for (String line : Files.readAllLines(INVENTORY, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            assertTrue(trimmed.contains("\t"), "Malformed inventory row: " + trimmed);
            assertTrue(rows.add(trimmed), "Duplicate inventory row: " + trimmed);
        }
        return rows;
    }

    private static Set<String> discoverInventory() throws Exception {
        Set<String> rows = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(PROD_ROOT)) {
            for (Path file : stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String relative = normalize(PROD_ROOT.relativize(file));
                String name = file.getFileName().toString();

                if (name.matches(".*(Dialog|Window)\\.java$")) {
                    rows.add("DIALOG_OR_WINDOW\t" + relative);
                }
                if (name.matches(".*Action\\.java$")) {
                    rows.add("ACTION\t" + relative);
                }
                if (name.matches(".*(Loader|Reader)\\.java$")) {
                    rows.add("LOADER_OR_READER\t" + relative);
                }
                if (name.matches(".*Service\\.java$")) {
                    rows.add("SERVICE\t" + relative);
                }
                if (name.matches(".*(Toolbar|ToolBar)\\.java$")) {
                    rows.add("TOOLBAR\t" + relative);
                }
                if (name.contains("Export") && name.endsWith(".java")) {
                    rows.add("EXPORT_SURFACE\t" + relative);
                }
                if (COMMAND_SURFACES.contains(name)) {
                    rows.add("COMMAND_SURFACE\t" + relative);
                }
            }
        }
        return rows;
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\\\', '/');
    }
}
