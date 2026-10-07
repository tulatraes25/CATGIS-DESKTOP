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

class VisibleCommandInventoryCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path SURFACE_INVENTORY =
            Path.of("..", "docs", "quality", "SOURCE_FEATURE_SURFACE_INVENTORY.tsv");
    private static final Path COMMAND_INVENTORY =
            Path.of("..", "docs", "quality", "VISIBLE_COMMAND_INVENTORY.tsv");
    private static final Path GENERATED_EVIDENCE =
            Path.of("build", "certification", "visible-command-inventory.tsv");

    private static final int BASELINE_OCCURRENCES = 390;
    private static final int BASELINE_SOURCE_LABEL_PAIRS = 378;
    private static final int BASELINE_UNIQUE_LABELS = 313;

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
            Pattern.compile("createItem\\(\\s*\"([^\"]+)\""),
            Pattern.compile("new JMenuItem\\(\\s*\"([^\"]+)\""),
            Pattern.compile("createButton\\(\\s*I18n\\.t\\(\\s*\"([^\"]+)\""),
            Pattern.compile("createButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("new JButton\\(\\s*I18n\\.t\\(\\s*\"([^\"]+)\""),
            Pattern.compile("new JButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("new JToggleButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("flatButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("\\bflat\\(\\s*\"([^\"]+)\""),
            Pattern.compile("createActionButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("createToggleButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("LayoutToolbarFactory\\.createToolbarButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("\\.addButton\\(\\s*\"([^\"]+)\""),
            Pattern.compile("panel\\.createMenuItem\\(\\s*\"([^\"]+)\""),
            Pattern.compile("panel\\.createMenuItem\\(\\s*I18n\\.t\\(\\s*\"([^\"]+)\"")
    );

    @Test
    void allCommandAndToolbarSourceSurfacesAreInTheScanner() throws Exception {
        Set<String> expected = new LinkedHashSet<>();
        for (String line : Files.readAllLines(SURFACE_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.startsWith("COMMAND_SURFACE\t")) {
                expected.add(line.substring("COMMAND_SURFACE\t".length()));
            } else if (line.startsWith("TOOLBAR\t")) {
                expected.add(line.substring("TOOLBAR\t".length()));
            }
        }
        assertEquals(expected, COMMAND_SOURCES,
                "Command/toolbar source coverage drifted. Update the scanner and certification evidence.");
    }

    @Test
    void checkedInVisibleCommandInventoryMatchesSourceExactly() throws Exception {
        Map<String, Integer> expected = readExpectedCounts();
        Map<String, Integer> actual = discoverCounts();

        assertEquals(expected, actual,
                "Visible static commands changed. Review source and update VISIBLE_COMMAND_INVENTORY.tsv in the same PR.");

        int occurrences = actual.values().stream().mapToInt(Integer::intValue).sum();
        long uniqueLabels = actual.keySet().stream()
                .map(key -> key.substring(key.indexOf('\t') + 1))
                .distinct()
                .count();

        assertEquals(BASELINE_OCCURRENCES, occurrences,
                "F1.2 baseline command occurrence count changed; review before ratcheting.");
        assertEquals(BASELINE_SOURCE_LABEL_PAIRS, actual.size(),
                "F1.2 baseline source/label pair count changed; review before ratcheting.");
        assertEquals(BASELINE_UNIQUE_LABELS, uniqueLabels,
                "F1.2 baseline unique-label count changed; review before ratcheting.");

        Files.createDirectories(GENERATED_EVIDENCE.getParent());
        Files.writeString(GENERATED_EVIDENCE, renderEvidence(actual), StandardCharsets.UTF_8);
        assertTrue(Files.size(GENERATED_EVIDENCE) > 0, "Generated visible-command evidence is empty.");
    }

    @Test
    void featureIdsAreStableAndOneToOneWithStaticLabels() throws Exception {
        Map<String, String> labelToFeature = new LinkedHashMap<>();
        Set<String> featureIds = new LinkedHashSet<>();

        for (String line : Files.readAllLines(COMMAND_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            assertEquals(4, parts.length, "Malformed command inventory row: " + line);
            String label = parts[1];
            String featureId = parts[3];

            assertTrue(featureId.matches("CMD-[A-Z0-9-]+-[0-9A-F]{8}"),
                    "Malformed feature ID: " + featureId);
            String previous = labelToFeature.putIfAbsent(label, featureId);
            if (previous != null) {
                assertEquals(previous, featureId,
                        "Same visible label maps to multiple feature IDs: " + label);
            }
            featureIds.add(featureId);
        }

        assertEquals(BASELINE_UNIQUE_LABELS, labelToFeature.size(),
                "Static label-to-feature mapping count drifted.");
        assertEquals(labelToFeature.size(), featureIds.size(),
                "Two different static labels share one feature ID.");
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

    private static Map<String, Integer> discoverCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        List<String> sources = new ArrayList<>(COMMAND_SOURCES);
        sources.sort(String::compareTo);

        for (String source : sources) {
            Path file = PROD_ROOT.resolve(source);
            assertTrue(Files.isRegularFile(file), "Missing command source: " + file);
            String content = Files.readString(file, StandardCharsets.UTF_8);

            for (Pattern pattern : COMMAND_PATTERNS) {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String label = matcher.group(1);
                    assertFalse(label.isBlank(), "Blank static command label in " + source);
                    counts.merge(source + "\t" + label, 1, Integer::sum);
                }
            }
        }
        return sortCounts(counts);
    }

    private static Map<String, Integer> sortCounts(Map<String, Integer> source) {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    private static String renderEvidence(Map<String, Integer> counts) {
        StringBuilder out = new StringBuilder("# source\tlabel\tcount\n");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            out.append(entry.getKey())
                    .append('\t')
                    .append(entry.getValue())
                    .append('\n');
        }
        return out.toString();
    }
}
