package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

class SupplementalVisibleCommandInventoryCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path CARRIER_INVENTORY =
            Path.of("..", "docs", "quality", "UI_COMMAND_CARRIER_INVENTORY.tsv");
    private static final Path COMMAND_INVENTORY =
            Path.of("..", "docs", "quality", "SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv");

    private static final int BASELINE_OCCURRENCES = 201;
    private static final int BASELINE_ROWS = 191;
    private static final int BASELINE_LABELS = 147;

    private static final List<CommandPattern> PATTERNS = List.of(
            new CommandPattern("JMENUITEM",
                    Pattern.compile("new\\s+(?:javax\\.swing\\.)?JMenuItem\\(\\s*(?:I18n\\.t\\(\\s*)?\"([^\"]+)\"")),
            new CommandPattern("JBUTTON",
                    Pattern.compile("new\\s+JButton\\(\\s*(?:I18n\\.t\\(\\s*)?\"([^\"]+)\"")),
            new CommandPattern("JTOGGLEBUTTON",
                    Pattern.compile("new\\s+JToggleButton\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("JCHECKBOXMENUITEM",
                    Pattern.compile("new\\s+JCheckBoxMenuItem\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("JRADIOBUTTONMENUITEM",
                    Pattern.compile("new\\s+JRadioButtonMenuItem\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("ADDMENUITEM",
                    Pattern.compile("addMenuItem\\(\\s*[^,\\n]+,\\s*\"([^\"]+)\"")),
            new CommandPattern("MENUITEM_HELPER",
                    Pattern.compile("\\bmenuItem\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("ITEM_HELPER",
                    Pattern.compile("\\bitem\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("MINIBTN_HELPER",
                    Pattern.compile("\\bminiBtn\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("ADDTOOLBUTTON",
                    Pattern.compile("\\baddToolButton\\(\\s*[^,\\n]+,\\s*\"([^\"]+)\"")),
            new CommandPattern("CREATETBTN",
                    Pattern.compile("\\bcreateTBtn\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("CREATETOOLBARBUTTON",
                    Pattern.compile("\\bcreateToolbarButton\\(\\s*\"([^\"]+)\"")),
            new CommandPattern("CREATEMENUITEM",
                    Pattern.compile("\\bcreateMenuItem\\(\\s*\"([^\"]+)\""))
    );

    @Test
    void checkedInSupplementalVisibleCommandInventoryMatchesSourceExactly() throws Exception {
        Map<String, Integer> expected = readExpectedCounts();
        Map<String, Integer> actual = discoverCounts();

        assertEquals(expected, actual,
                "Supplemental visible command coverage changed. Review source and update "
                        + "SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv in the same PR.");

        int occurrences = actual.values().stream().mapToInt(Integer::intValue).sum();
        long labels = actual.keySet().stream()
                .map(key -> key.substring(key.lastIndexOf('\t') + 1))
                .distinct()
                .count();

        assertEquals(BASELINE_OCCURRENCES, occurrences,
                "Supplemental visible-command occurrence baseline drifted.");
        assertEquals(BASELINE_ROWS, actual.size(),
                "Supplemental visible-command row baseline drifted.");
        assertEquals(BASELINE_LABELS, labels,
                "Supplemental visible-command label baseline drifted.");
    }

    @Test
    void activeMenuHelpersCannotUseEmptyActionLambdas() throws Exception {
        for (String source : carrierSources()) {
            String content = Files.readString(PROD_ROOT.resolve(source), StandardCharsets.UTF_8);
            Pattern emptyAddMenuItem = Pattern.compile(
                    "addMenuItem\\([^;]*?->\\s*\\{\\s*}\\s*\\);",
                    Pattern.DOTALL
            );
            Matcher matcher = emptyAddMenuItem.matcher(content);
            if (matcher.find()) {
                throw new AssertionError(
                        "Visible active menu command has an empty handler in " + source
                                + ": " + compact(matcher.group())
                );
            }
        }
    }

    private static Map<String, Integer> readExpectedCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String line : Files.readAllLines(COMMAND_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\\t", -1);
            assertEquals(4, p.length, "Malformed supplemental command row: " + line);
            String key = p[0] + "\t" + p[1] + "\t" + p[2];
            assertFalse(counts.containsKey(key), "Duplicate supplemental command row: " + key);
            counts.put(key, Integer.parseInt(p[3]));
        }
        return sort(counts);
    }

    private static Map<String, Integer> discoverCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String source : carrierSources()) {
            Path file = PROD_ROOT.resolve(source);
            assertTrue(Files.isRegularFile(file), "Missing UI command carrier: " + source);
            String content = Files.readString(file, StandardCharsets.UTF_8);

            for (CommandPattern commandPattern : PATTERNS) {
                Matcher matcher = commandPattern.pattern().matcher(content);
                while (matcher.find()) {
                    String label = matcher.group(1);
                    assertFalse(label.isBlank(), "Blank supplemental visible command in " + source);
                    String key = source + "\t" + commandPattern.kind() + "\t" + label;
                    counts.merge(key, 1, Integer::sum);
                }
            }
        }
        return sort(counts);
    }

    private static Set<String> carrierSources() throws Exception {
        Set<String> sources = new LinkedHashSet<>();
        for (String line : Files.readAllLines(CARRIER_INVENTORY, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                assertTrue(sources.add(trimmed), "Duplicate UI command carrier: " + trimmed);
            }
        }
        return sources;
    }

    private static Map<String, Integer> sort(Map<String, Integer> source) {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private record CommandPattern(String kind, Pattern pattern) {}
}
