package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** T30 guard: no duplicated visible commands in the CATMAP menu bar. */
class CatmapCommandUniquenessTest {

    private static final Path MAIN_SOURCE = Path.of("src/ar/com/catgis/catmap/Main.java");

    @Test
    void everyVisibleCatmapCommandHasASingleMenuEntry() throws Exception {
        String source = Files.readString(MAIN_SOURCE, StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile(
                "addMenuItem\\(\\s*(menu\\w+)\\s*,\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(source);
        Map<String, List<String>> menusByLabel = new LinkedHashMap<>();
        int count = 0;
        while (matcher.find()) {
            count++;
            menusByLabel.computeIfAbsent(matcher.group(2), key -> new ArrayList<>())
                    .add(matcher.group(1));
        }
        assertTrue(count >= 30, "expected the full CATMAP command surface, got " + count);
        List<String> duplicated = menusByLabel.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                .toList();
        assertTrue(duplicated.isEmpty(),
                "duplicate visible commands (one path per export): " + duplicated);
    }
}
