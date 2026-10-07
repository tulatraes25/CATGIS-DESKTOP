package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisibleCommandCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path SOURCE_SURFACE_INVENTORY =
            Path.of("..", "docs", "quality", "SOURCE_FEATURE_SURFACE_INVENTORY.tsv");
    private static final Path COMMAND_INVENTORY =
            Path.of("..", "docs", "quality", "VISIBLE_COMMAND_INVENTORY.tsv");
    private static final Path FEATURE_MATRIX =
            Path.of("..", "docs", "quality", "FEATURE_CERTIFICATION_MATRIX.md");

    private static final Set<String> COMMAND_SOURCES = Set.of(
            "MainMenuBar.java",
            "LayerContextMenuBuilder.java",
            "MapPopupMenuBuilder.java",
            "MapPopupHandler.java",
            "MainToolBar.java",
            "OnlineConnectionsToolbar.java",
            "CartographyToolbar.java",
            "TopographyToolbar.java",
            "FloatingVectorEditToolbar.java",
            "CatserverToolbar.java",
            "layout/LayoutToolBar.java",
            "ui/components/layout/LayoutPreviewToolbar.java",
            "ProInterpretationToolbar.java",
            "PinManager.java"
    );

    private static final List<Pattern> COMMAND_PATTERNS = List.of(
            Pattern.compile("createItem\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("new JMenuItem\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("createButton\\(\\s*I18n\\.t\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("createButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("new JButton\\(\\s*I18n\\.t\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("new JButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("new JToggleButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("flatButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("\\bflat\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("createActionButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("createToggleButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("LayoutToolbarFactory\\.createToolbarButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("\\.addButton\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("panel\\.createMenuItem\\(\\s*\\"([^\\"]+)\\""),
            Pattern.compile("panel\\.createMenuItem\\(\\s*I18n\\.t\\(\\s*\\"([^\\"]+)\\"")
    );

    private static final Set<String> VALID_DOMAINS = Set.of(
            "CATMAP", "ONLINE_INTEGRATION", "CAD", "HYDROLOGY", "TOPOGRAPHY",
            "RASTER_REMOTE_SENSING", "ENVIRONMENT", "TOPOLOGY", "EXTENSIBILITY",
            "CRS", "ATTRIBUTES_STYLE", "VECTOR_EDIT", "LAYER_IO", "MAP_NAVIGATION",
            "PROJECT", "HELP", "CROSS_CUTTING"
    );

    private static final Set<String> VALID_TIERS = Set.of(
            "CORE", "BETA", "EXPERIMENTAL", "EXTERNAL_DEPENDENCY"
    );

    @Test
    void everyCommandAndToolbarSurfaceIsCoveredByTheStaticInventoryScanner() throws Exception {
        Set<String> expectedSources = new LinkedHashSet<>();
        for (String line : Files.readAllLines(SOURCE_SURFACE_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.startsWith("COMMAND_SURFACE\t")) {
                expectedSources.add(line.substring("COMMAND_SURFACE\t".length()));
            } else if (line.startsWith("TOOLBAR\t")) {
                expectedSources.add(line.substring("TOOLBAR\t".length()));
            }
        }
        assertEquals(expectedSources, COMMAND_SOURCES,
                "Command/toolbar source coverage drifted. Update the executable command inventory scanner.");
    }

    @Test
    void checkedInVisibleCommandInventoryMatchesSourceExactly() throws Exception {
        Map<String, Integer> expected = readExpectedCounts();
        Map<String, Integer> actual = discoverCounts();
        assertFalse(expected.isEmpty(), "Visible command inventory must not be empty.");
        assertEquals(expected, actual,
                "Visible static command labels changed. Update VISIBLE_COMMAND_INVENTORY.tsv and the feature matrix in the same PR.");
    }

    @Test
    void everyVisibleCommandMapsToExactlyOneValidFeatureRow() throws Exception {
        Map<String, String> inventoryFeatureIds = readInventoryFeatureIds();
        Map<String, MatrixRow> matrix = readMatrix();

        assertFalse(matrix.isEmpty(), "Feature matrix must contain feature rows.");

        for (Map.Entry<String, String> entry : inventoryFeatureIds.entrySet()) {
            assertTrue(matrix.containsKey(entry.getValue()),
                    "Command inventory references missing feature ID " + entry.getValue() + " for " + entry.getKey());
        }

        Set<String> used = new LinkedHashSet<>(inventoryFeatureIds.values());
        assertEquals(matrix.keySet(), used,
                "Every static-command feature row must be referenced by at least one visible command occurrence.");

        for (MatrixRow row : matrix.values()) {
            assertTrue(VALID_DOMAINS.contains(row.domain()), "Invalid domain for " + row.id() + ": " + row.domain());
            assertTrue(VALID_TIERS.contains(row.tier()), "Invalid tier for " + row.id() + ": " + row.tier());
            assertFalse(row.feature().isBlank(), "Blank feature label for " + row.id());
            assertEquals("TEST_GAP", row.status(),
                    "F1.2 inventory rows cannot claim certification before F2 exact-SHA evidence: " + row.id());
            assertEquals("LOCKED_UNTIL_A0_7", row.windowsRuntime(),
                    "Windows runtime must remain locked until architecture certification: " + row.id());
        }
    }

    private static Map<String, Integer> discoverCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        List<String> sortedSources = new ArrayList<>(COMMAND_SOURCES);
        sortedSources.sort(String::compareTo);
        for (String source : sortedSources) {
            Path file = PROD_ROOT.resolve(source);
            assertTrue(Files.isRegularFile(file), "Missing command source: " + file);
            String content = Files.readString(file, StandardCharsets.UTF_8);
            for (Pattern pattern : COMMAND_PATTERNS) {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String key = source + "\t" + matcher.group(1);
                    counts.merge(key, 1, Integer::sum);
                }
            }
        }
        return sortCounts(counts);
    }

    private static Map<String, Integer> readExpectedCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String line : Files.readAllLines(COMMAND_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            assertEquals(4, parts.length, "Malformed command inventory row: " + line);
            String key = parts[0] + "\t" + parts[1];
            assertFalse(counts.containsKey(key), "Duplicate command inventory row: " + key);
            counts.put(key, Integer.parseInt(parts[2]));
        }
        return sortCounts(counts);
    }

    private static Map<String, String> readInventoryFeatureIds() throws Exception {
        Map<String, String> ids = new LinkedHashMap<>();
        for (String line : Files.readAllLines(COMMAND_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            String key = parts[0] + "\t" + parts[1];
            ids.put(key, parts[3]);
        }
        return ids;
    }

    private static Map<String, MatrixRow> readMatrix() throws Exception {
        Map<String, MatrixRow> rows = new LinkedHashMap<>();
        for (String line : Files.readAllLines(FEATURE_MATRIX, StandardCharsets.UTF_8)) {
            if (!line.startsWith("| CMD-")) {
                continue;
            }
            String[] raw = line.split("\\|", -1);
            List<String> cells = new ArrayList<>();
            for (int i = 1; i < raw.length - 1; i++) {
                cells.add(raw[i].trim());
            }
            assertEquals(13, cells.size(), "Unexpected feature matrix column count: " + line);
            MatrixRow row = new MatrixRow(
                    cells.get(0), cells.get(1), cells.get(3), cells.get(5),
                    cells.get(8), cells.get(11)
            );
            assertFalse(rows.containsKey(row.id()), "Duplicate feature ID: " + row.id());
            rows.put(row.id(), row);
        }
        return rows;
    }

    private static Map<String, Integer> sortCounts(Map<String, Integer> source) {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    private record MatrixRow(
            String id,
            String domain,
            String feature,
            String tier,
            String windowsRuntime,
            String status
    ) {
    }
}
