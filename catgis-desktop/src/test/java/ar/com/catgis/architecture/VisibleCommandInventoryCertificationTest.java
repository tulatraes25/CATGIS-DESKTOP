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
    private static final Path GENERATED_EVIDENCE =
            Path.of("build", "certification", "visible-command-inventory.tsv");

    private static final int EXPECTED_OCCURRENCES = 390;
    private static final int EXPECTED_SOURCE_LABEL_PAIRS = 378;
    private static final int EXPECTED_UNIQUE_LABELS = 313;
    private static final String EXPECTED_FNV64 = "bad0905a00836b51";

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
    void visibleStaticCommandInventoryHasNoUnreviewedDrift() throws Exception {
        Map<String, Integer> counts = discoverCounts();
        int occurrences = counts.values().stream().mapToInt(Integer::intValue).sum();
        long uniqueLabels = counts.keySet().stream()
                .map(key -> key.substring(key.indexOf('\t') + 1))
                .distinct()
                .count();

        assertEquals(EXPECTED_OCCURRENCES, occurrences, "Visible command occurrence count changed.");
        assertEquals(EXPECTED_SOURCE_LABEL_PAIRS, counts.size(), "Visible source/label pair count changed.");
        assertEquals(EXPECTED_UNIQUE_LABELS, uniqueLabels, "Visible unique-label count changed.");

        String evidence = renderEvidence(counts);
        assertEquals(EXPECTED_FNV64, fnv1a64(evidence),
                "Visible command inventory changed. Review the exact generated evidence and update the F1.2 gate.");

        Files.createDirectories(GENERATED_EVIDENCE.getParent());
        Files.writeString(GENERATED_EVIDENCE, evidence, StandardCharsets.UTF_8);
        assertTrue(Files.size(GENERATED_EVIDENCE) > 0, "Generated visible-command evidence is empty.");
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

        Map<String, Integer> sorted = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    private static String renderEvidence(Map<String, Integer> counts) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            out.append(entry.getKey())
                    .append('\t')
                    .append(entry.getValue())
                    .append('\n');
        }
        return out.toString();
    }

    private static String fnv1a64(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return String.format("%016x", hash);
    }
}
