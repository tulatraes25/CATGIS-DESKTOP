package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiCommandCarrierCoverageCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path SURFACE_INVENTORY =
            Path.of("..", "docs", "quality", "SOURCE_FEATURE_SURFACE_INVENTORY.tsv");
    private static final Path SUPPLEMENTAL_INVENTORY =
            Path.of("..", "docs", "quality", "UI_COMMAND_CARRIER_INVENTORY.tsv");

    private static final List<String> COMMAND_TOKENS = List.of(
            "new JMenuItem(",
            "new JButton(",
            "new JToggleButton(",
            "new JPopupMenu(",
            "addMenuItem(",
            "createMenuItem(",
            "createActionButton("
    );

    @Test
    void everyUiCommandCarrierIsClassifiedOrExplicitlySupplemented() throws Exception {
        Set<String> classified = readClassifiedSourceSurfaces();
        Set<String> discovered = discoverUiCommandCarriers();
        Set<String> expectedSupplemental = readSupplementalInventory();

        Set<String> actualSupplemental = new LinkedHashSet<>(discovered);
        actualSupplemental.removeAll(classified);

        assertEquals(expectedSupplemental, actualSupplemental,
                "UI command-carrier coverage changed. Classify the source surface or update "
                        + "UI_COMMAND_CARRIER_INVENTORY.tsv in the same PR.");
    }

    @Test
    void supplementalInventoryOnlyContainsRealCurrentCommandCarriers() throws Exception {
        Set<String> discovered = discoverUiCommandCarriers();
        for (String path : readSupplementalInventory()) {
            assertTrue(discovered.contains(path),
                    "Supplemental UI command carrier no longer contains a command/control surface: " + path);
        }
    }

    private static Set<String> readClassifiedSourceSurfaces() throws Exception {
        Set<String> paths = new LinkedHashSet<>();
        for (String line : Files.readAllLines(SURFACE_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertTrue(parts.length >= 2, "Malformed source-surface inventory row: " + line);
            paths.add(parts[1]);
        }
        return paths;
    }

    private static Set<String> readSupplementalInventory() throws Exception {
        assertTrue(Files.isRegularFile(SUPPLEMENTAL_INVENTORY),
                "Missing supplemental UI command-carrier inventory: " + SUPPLEMENTAL_INVENTORY);
        Set<String> paths = new LinkedHashSet<>();
        for (String line : Files.readAllLines(SUPPLEMENTAL_INVENTORY, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            assertTrue(paths.add(trimmed), "Duplicate supplemental UI command carrier: " + trimmed);
        }
        return paths;
    }

    private static Set<String> discoverUiCommandCarriers() throws Exception {
        Set<String> paths = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(PROD_ROOT)) {
            for (Path file : stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                if (COMMAND_TOKENS.stream().anyMatch(content::contains)) {
                    paths.add(normalize(PROD_ROOT.relativize(file)));
                }
            }
        }
        return paths;
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}
