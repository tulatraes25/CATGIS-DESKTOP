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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicCommandInventoryCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path DYNAMIC_INVENTORY =
            Path.of("..", "docs", "quality", "DYNAMIC_COMMAND_INVENTORY.tsv");
    private static final Path MODULE_INVENTORY =
            Path.of("..", "docs", "quality", "MODULE_ACTION_INVENTORY.tsv");
    private static final Path MODULE_REGISTRY = PROD_ROOT.resolve("ModuleRegistry.java");

    private static final Set<String> ALLOWED_CLASSIFICATIONS = Set.of(
            "STATE_VARIANT",
            "PARAMETERIZED_ACTION",
            "REGISTRY_ACTION",
            "COMPUTED_ACTION",
            "DISPLAY_ONLY",
            "INDIRECTION_FACTORY",
            "MENU_CONTAINER",
            "ALIAS_PASSTHROUGH"
    );

    private static final Set<String> NON_FEATURE_CLASSIFICATIONS = Set.of(
            "DISPLAY_ONLY",
            "INDIRECTION_FACTORY",
            "MENU_CONTAINER",
            "ALIAS_PASSTHROUGH"
    );

    private static final List<MenuToken> MENU_TOKENS = List.of(
            new MenuToken("JMENUITEM", "new JMenuItem("),
            new MenuToken("JMENUITEM", "new javax.swing.JMenuItem("),
            new MenuToken("CHECKBOX", "new JCheckBoxMenuItem("),
            new MenuToken("RADIO", "new JRadioButtonMenuItem("),
            new MenuToken("JMENU", "new JMenu("),
            new MenuToken("CREATE_MENU_ITEM", "createMenuItem(")
    );

    private static final Pattern STATIC_LITERAL =
            Pattern.compile("^\"(?:[^\"\\\\]|\\\\.)*\"$");
    private static final Pattern STATIC_I18N_LITERAL =
            Pattern.compile("^I18n\\.t\\(\\s*\"(?:[^\"\\\\]|\\\\.)*\"\\s*\\)$");
    private static final Pattern MODULE_ACTION =
            Pattern.compile("new\\s+CatgisModuleAction\\(\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"");

    @Test
    void checkedInDynamicMenuInventoryMatchesSourceExactly() throws Exception {
        Map<String, Integer> expected = readExpectedDynamicCounts();
        Map<String, Integer> actual = discoverDynamicMenuCounts();

        assertEquals(expected, actual,
                "Dynamic/computed menu command coverage changed. Review source and update "
                        + "DYNAMIC_COMMAND_INVENTORY.tsv in the same PR.");
    }

    @Test
    void everyDynamicRowHasAnExplicitSemanticClassification() throws Exception {
        Set<String> rowKeys = new LinkedHashSet<>();

        for (String line : Files.readAllLines(DYNAMIC_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertEquals(6, parts.length, "Malformed dynamic command row: " + line);

            String source = parts[0];
            String kind = parts[1];
            String expression = parts[2];
            int count = Integer.parseInt(parts[3]);
            String classification = parts[4];
            String semanticId = parts[5];

            assertTrue(Files.isRegularFile(PROD_ROOT.resolve(source)),
                    "Dynamic command source does not exist: " + source);
            assertTrue(MENU_TOKENS.stream().map(MenuToken::kind).anyMatch(kind::equals),
                    "Unsupported dynamic command kind: " + kind);
            assertFalse(expression.isBlank(), "Dynamic expression must not be blank: " + line);
            assertTrue(count > 0, "Dynamic command count must be positive: " + line);
            assertTrue(ALLOWED_CLASSIFICATIONS.contains(classification),
                    "Unsupported dynamic command classification: " + classification);

            if (NON_FEATURE_CLASSIFICATIONS.contains(classification)) {
                assertEquals("NON_FEATURE", semanticId,
                        "Display/factory/container/pass-through rows must be explicitly NON_FEATURE.");
            } else {
                assertTrue(semanticId.matches("SEM-[A-Z0-9-]+"),
                        "Action-bearing dynamic row requires a stable semantic feature ID: " + semanticId);
            }

            String key = source + "\t" + kind + "\t" + expression;
            assertTrue(rowKeys.add(key), "Duplicate dynamic command row: " + key);
        }
    }

    @Test
    void moduleActionRegistryIsExplicitlyLocked() throws Exception {
        Map<String, String> expected = readModuleInventory();
        Map<String, String> actual = discoverModuleActions();

        assertEquals(expected, actual,
                "Module action registry changed. Review and update MODULE_ACTION_INVENTORY.tsv in the same PR.");
        assertEquals(38, actual.size(), "Unexpected module action baseline count.");
    }

    private static Map<String, Integer> readExpectedDynamicCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String line : Files.readAllLines(DYNAMIC_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertEquals(6, parts.length, "Malformed dynamic command row: " + line);
            String key = parts[0] + "\t" + parts[1] + "\t" + parts[2];
            assertFalse(counts.containsKey(key), "Duplicate dynamic command row: " + key);
            counts.put(key, Integer.parseInt(parts[3]));
        }
        return sortCounts(counts);
    }

    private static Map<String, Integer> discoverDynamicMenuCounts() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();

        try (Stream<Path> stream = Files.walk(PROD_ROOT)) {
            for (Path file : stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                String source = normalize(PROD_ROOT.relativize(file));

                for (MenuToken token : MENU_TOKENS) {
                    int offset = 0;
                    while ((offset = content.indexOf(token.token(), offset)) >= 0) {
                        if (isCreateMenuItemDeclaration(content, offset, token)) {
                            offset += token.token().length();
                            continue;
                        }

                        String expression = firstArgument(content, offset + token.token().length());
                        if (!expression.isBlank() && !isStaticExpression(expression)) {
                            String normalizedExpression = normalizeWhitespace(expression);
                            counts.merge(source + "\t" + token.kind() + "\t" + normalizedExpression,
                                    1, Integer::sum);
                        }
                        offset += token.token().length();
                    }
                }
            }
        }

        return sortCounts(counts);
    }

    private static boolean isCreateMenuItemDeclaration(String content, int tokenOffset, MenuToken token) {
        if (!"CREATE_MENU_ITEM".equals(token.kind())) {
            return false;
        }
        int lineStart = content.lastIndexOf('\n', tokenOffset);
        lineStart = lineStart < 0 ? 0 : lineStart + 1;
        String prefix = content.substring(lineStart, tokenOffset).trim();
        return prefix.matches(".*\\bJMenuItem\\s*");
    }

    private static String firstArgument(String content, int start) {
        int nestedParentheses = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '\"') {
                    inString = false;
                }
                continue;
            }

            if (ch == '\"') {
                inString = true;
                continue;
            }
            if (ch == '(') {
                nestedParentheses++;
                continue;
            }
            if (ch == ')') {
                if (nestedParentheses == 0) {
                    return content.substring(start, i).trim();
                }
                nestedParentheses--;
                continue;
            }
            if (ch == ',' && nestedParentheses == 0) {
                return content.substring(start, i).trim();
            }
        }
        return "";
    }

    private static boolean isStaticExpression(String expression) {
        String normalized = normalizeWhitespace(expression);
        return STATIC_LITERAL.matcher(normalized).matches()
                || STATIC_I18N_LITERAL.matcher(normalized).matches();
    }

    private static String normalizeWhitespace(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private static Map<String, String> readModuleInventory() throws Exception {
        Map<String, String> actions = new LinkedHashMap<>();
        for (String line : Files.readAllLines(MODULE_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertEquals(2, parts.length, "Malformed module-action row: " + line);
            assertTrue(parts[0].matches("[a-z0-9-]+"), "Malformed module action ID: " + parts[0]);
            assertFalse(parts[1].isBlank(), "Blank module action name: " + line);
            assertFalse(actions.containsKey(parts[0]), "Duplicate module action ID: " + parts[0]);
            actions.put(parts[0], parts[1]);
        }
        return actions;
    }

    private static Map<String, String> discoverModuleActions() throws Exception {
        String content = Files.readString(MODULE_REGISTRY, StandardCharsets.UTF_8);
        Matcher matcher = MODULE_ACTION.matcher(content);
        Map<String, String> actions = new LinkedHashMap<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            String name = matcher.group(2);
            assertFalse(actions.containsKey(id), "Duplicate module action ID in source: " + id);
            actions.put(id, name);
        }
        return actions;
    }

    private static Map<String, Integer> sortCounts(Map<String, Integer> source) {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record MenuToken(String kind, String token) {}
}
