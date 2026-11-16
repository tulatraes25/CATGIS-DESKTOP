package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T1c guard: the layout model must not compile-depend on the CATMAP serializer.
 * The only allowed mentions of {@code ar.com.catgis.catmap} are the reflective
 * class names inside {@code LayoutCodecs} (runtime bridge).
 */
class LayoutPackageDependencyGuardTest {

    private static final Path LAYOUT_SOURCES = Path.of("src/ar/com/catgis/layout");

    @Test
    void layoutSourcesDoNotImportTheCatmapPackage() throws Exception {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.list(LAYOUT_SOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                if (file.getFileName().toString().equals("LayoutCodecs.java")) {
                    continue;
                }
                if (text.contains("ar.com.catgis.catmap")) {
                    violations.add(file.getFileName().toString());
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "layout/ must not reference catmap except through LayoutCodecs: " + violations);
    }

    @Test
    void reflectiveBridgeOnlyMentionsClassNamesInsideStringLiterals() throws Exception {
        String bridge = Files.readString(LAYOUT_SOURCES.resolve("LayoutCodecs.java"),
                StandardCharsets.UTF_8);
        assertFalse(bridge.contains("import ar.com.catgis.catmap"),
                "LayoutCodecs must not import catmap: reflection only");
        for (String line : bridge.split("\n")) {
            String trimmed = line.trim();
            if (!line.contains("ar.com.catgis.catmap")) {
                continue;
            }
            boolean comment = trimmed.startsWith("*") || trimmed.startsWith("//")
                    || trimmed.startsWith("/*") || trimmed.contains("{@code");
            assertTrue(comment || line.contains("Class.forName(") || line.contains("requires "),
                    "unexpected direct catmap reference: " + trimmed);
        }
    }

    @Test
    void snapshotCallSitesUseTheCodecRegistry() throws Exception {
        String model = Files.readString(LAYOUT_SOURCES.resolve("LayoutModel.java"),
                StandardCharsets.UTF_8);
        String templates = Files.readString(LAYOUT_SOURCES.resolve("LayoutTemplateManager.java"),
                StandardCharsets.UTF_8);
        assertTrue(model.contains("LayoutCodecs.elements()"));
        assertTrue(templates.contains("LayoutCodecs.documents()"));
        assertFalse(model.contains("CatmapSerializer"));
        assertFalse(templates.contains("CatmapDocumentIO"));
    }
}
