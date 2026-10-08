package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticCommandAliasCertificationTest {

    private static final Path COMMAND_INVENTORY =
            Path.of("..", "docs", "quality", "VISIBLE_COMMAND_INVENTORY.tsv");
    private static final Path ALIAS_MAP =
            Path.of("..", "docs", "quality", "STATIC_COMMAND_ALIAS_MAP.tsv");

    @Test
    void everyNormalizationCollisionIsExplicitlyDeclared() throws Exception {
        Set<String> labels = readVisibleLabels();
        Map<String, Set<String>> collisionGroups = collisionGroups(labels);
        Map<String, AliasGroup> declared = readDeclaredGroups();

        assertEquals(collisionGroups.keySet(), declared.keySet(),
                "Static visible-command alias groups changed. Review and update STATIC_COMMAND_ALIAS_MAP.tsv.");

        for (Map.Entry<String, Set<String>> entry : collisionGroups.entrySet()) {
            AliasGroup group = declared.get(entry.getKey());
            assertEquals(entry.getValue(), group.aliases(),
                    "Alias membership drifted for normalized key: " + entry.getKey());
            assertFalse(group.semanticId().isBlank(), "Semantic ID must not be blank.");
            assertTrue(group.semanticId().matches("SEM-[A-Z0-9-]+"),
                    "Malformed semantic ID: " + group.semanticId());
            assertTrue(group.aliases().contains(group.canonicalLabel()),
                    "Canonical label must be one of the current visible aliases: " + group.canonicalLabel());
        }
    }

    @Test
    void semanticIdsAreUniqueAcrossAliasGroups() throws Exception {
        Map<String, AliasGroup> declared = readDeclaredGroups();
        Set<String> semanticIds = new LinkedHashSet<>();
        for (AliasGroup group : declared.values()) {
            assertTrue(semanticIds.add(group.semanticId()),
                    "Semantic ID reused across unrelated alias groups: " + group.semanticId());
        }
    }

    private static Set<String> readVisibleLabels() throws Exception {
        Set<String> labels = new LinkedHashSet<>();
        for (String line : Files.readAllLines(COMMAND_INVENTORY, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertEquals(4, parts.length, "Malformed visible-command row: " + line);
            labels.add(parts[1]);
        }
        return labels;
    }

    private static Map<String, Set<String>> collisionGroups(Set<String> labels) {
        Map<String, Set<String>> all = new LinkedHashMap<>();
        for (String label : labels) {
            all.computeIfAbsent(normalize(label), ignored -> new LinkedHashSet<>()).add(label);
        }
        Map<String, Set<String>> collisions = new LinkedHashMap<>();
        all.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> collisions.put(entry.getKey(), entry.getValue()));
        return collisions;
    }

    private static Map<String, AliasGroup> readDeclaredGroups() throws Exception {
        Map<String, AliasGroupBuilder> builders = new LinkedHashMap<>();
        for (String line : Files.readAllLines(ALIAS_MAP, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            assertEquals(3, parts.length, "Malformed alias-map row: " + line);
            String semanticId = parts[0];
            String canonical = parts[1];
            String alias = parts[2];
            String key = normalize(alias);

            AliasGroupBuilder builder = builders.computeIfAbsent(
                    key, ignored -> new AliasGroupBuilder(semanticId, canonical));
            assertEquals(builder.semanticId, semanticId,
                    "One normalized alias group maps to multiple semantic IDs: " + key);
            assertEquals(builder.canonicalLabel, canonical,
                    "One normalized alias group has multiple canonical labels: " + key);
            assertTrue(builder.aliases.add(alias), "Duplicate alias-map row: " + line);
        }

        Map<String, AliasGroup> groups = new LinkedHashMap<>();
        builders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> groups.put(entry.getKey(),
                        new AliasGroup(entry.getValue().semanticId,
                                entry.getValue().canonicalLabel,
                                Set.copyOf(entry.getValue().aliases))));
        return groups;
    }

    private static String normalize(String value) {
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return ascii.toLowerCase(Locale.ROOT)
                .replaceAll("\\.\\.\\.$", "")
                .replace('—', ' ')
                .replace('–', ' ')
                .replace('/', ' ')
                .replaceAll("[()]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record AliasGroup(String semanticId, String canonicalLabel, Set<String> aliases) {}

    private static final class AliasGroupBuilder {
        private final String semanticId;
        private final String canonicalLabel;
        private final Set<String> aliases = new LinkedHashSet<>();

        private AliasGroupBuilder(String semanticId, String canonicalLabel) {
            this.semanticId = semanticId;
            this.canonicalLabel = canonicalLabel;
        }
    }
}
