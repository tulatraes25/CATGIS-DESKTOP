package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisiblePlaceholderActionCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path SOURCE_SURFACES =
            Path.of("..", "docs", "quality", "SOURCE_FEATURE_SURFACE_INVENTORY.tsv");
    private static final Path SUPPLEMENTAL_CARRIERS =
            Path.of("..", "docs", "quality", "UI_COMMAND_CARRIER_INVENTORY.tsv");
    private static final Path SUPPLEMENTAL_SEMANTICS =
            Path.of("..", "docs", "quality", "SUPPLEMENTAL_COMMAND_SEMANTIC_MAP.tsv");

    /*
     * F1.3 is about visible commands whose registered UI handler is empty. Do not
     * flag every empty lambda in a carrier: switch defaults and defensive callback
     * defaults are not themselves visible commands.
     */
    private static final Pattern EMPTY_REGISTERED_HANDLER = Pattern.compile(
            "(?:addActionListener|addItemListener|addChangeListener|addListSelectionListener)"
                    + "\\s*\\(\\s*(?:\\([^)]*\\)|[A-Za-z_$][A-Za-z0-9_$]*)"
                    + "\\s*->\\s*\\{\\s*\\}\\s*\\)",
            Pattern.MULTILINE
    );

    @Test
    void visibleCommandCarriersDoNotContainRegisteredEmptyHandlers() throws Exception {
        Set<String> carriers = commandCarriers();
        Set<String> findings = new LinkedHashSet<>();

        for (String relative : carriers) {
            Path file = PROD_ROOT.resolve(relative);
            if (!Files.isRegularFile(file)) {
                continue;
            }
            String content = Files.readString(file, StandardCharsets.UTF_8);
            Matcher matcher = EMPTY_REGISTERED_HANDLER.matcher(content);
            while (matcher.find()) {
                int line = 1;
                for (int i = 0; i < matcher.start(); i++) {
                    if (content.charAt(i) == '\n') {
                        line++;
                    }
                }
                String context = lineAt(content, line).trim();
                findings.add(relative + ":" + line + ":" + context);
            }
        }

        assertTrue(findings.isEmpty(),
                "Visible command carriers contain registered empty UI handlers. "
                        + "Implement or explicitly gate them: " + findings);
    }

    @Test
    void unsupportedVisibleCommandsRemainExplicitlyClassifiedAndGated() throws Exception {
        Set<String> expected = Set.of(
                "catmap/Main.java\tADDMENUITEM\tSincronizar capas visibles",
                "catmap/Main.java\tADDMENUITEM\tSincronizar etiquetas",
                "catmap/Main.java\tADDMENUITEM\tSincronizar simbología"
        );
        Set<String> actual = new LinkedHashSet<>();

        for (String line : Files.readAllLines(SUPPLEMENTAL_SEMANTICS, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\\t", -1);
            if (p.length >= 5 && "DISABLED_UNSUPPORTED".equals(p[3])) {
                actual.add(p[0] + "\t" + p[1] + "\t" + p[2]);
            }
        }
        assertEquals(expected, actual,
                "Visible unsupported commands changed; review product gating explicitly.");

        String catmap = Files.readString(PROD_ROOT.resolve("catmap/Main.java"), StandardCharsets.UTF_8);
        for (String label : Set.of(
                "Sincronizar capas visibles",
                "Sincronizar etiquetas",
                "Sincronizar simbología")) {
            assertTrue(catmap.contains("disableMenuItem(")
                            && catmap.contains("addMenuItem(menuMapa, \"" + label + "\""),
                    "Unsupported CATMAP command is no longer visibly gated: " + label);
        }
    }

    private static Set<String> commandCarriers() throws Exception {
        Set<String> carriers = new LinkedHashSet<>();
        for (String line : Files.readAllLines(SOURCE_SURFACES, StandardCharsets.UTF_8)) {
            if (line.startsWith("COMMAND_SURFACE\t") || line.startsWith("TOOLBAR\t")) {
                carriers.add(line.substring(line.indexOf('\t') + 1));
            }
        }
        for (String line : Files.readAllLines(SUPPLEMENTAL_CARRIERS, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                carriers.add(trimmed);
            }
        }
        return carriers;
    }

    private static String lineAt(String content, int lineNumber) {
        String[] lines = content.split("\\R", -1);
        return lineNumber >= 1 && lineNumber <= lines.length ? lines[lineNumber - 1] : "";
    }
}
