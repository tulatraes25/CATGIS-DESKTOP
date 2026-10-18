package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVisibleTextEncodingContractTest {

    @Test
    void sourceTreeContainsNoKnownMojibakeSentinels() throws Exception {
        Path sourceRoot = resolveModulePath("src");
        List<String> offenders = new ArrayList<>();
        List<String> sentinels = List.of(
                "\u00C3",
                "\u00C2",
                "\u00E2\u20AC"
        );

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(UserVisibleTextEncodingContractTest::isTextSource)
                    .forEach(path -> inspect(path, sentinels, offenders));
        }

        assertTrue(offenders.isEmpty(),
                "Mojibake detected in source/resources:\n" + String.join("\n", offenders));
    }

    @Test
    void representativeUserFacingStringsRemainCorrectUnicode() throws Exception {
        assertContains("src/ar/com/catgis/MapPanel.java",
                "Primero seleccioná una capa en el panel de capas.");
        assertContains("src/ar/com/catgis/MapEditingEngine.java",
                "Modo mover vértice activo.");
        assertContains("src/ar/com/catgis/LayoutPreviewPanel.java",
                "\"Tamaño\"");
        assertContains("src/ar/com/catgis/MapLayoutComposerDialog.java",
                "Técnica · Leyenda derecha");
        assertContains("src/ar/com/catgis/layout/LayoutPageRenderer.java",
                "Escala: —");
    }

    private static boolean isTextSource(Path path) {
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".java")
                || name.endsWith(".properties")
                || name.endsWith(".xml")
                || name.endsWith(".json");
    }

    private static void inspect(Path path, List<String> sentinels, List<String> offenders) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            for (String sentinel : sentinels) {
                if (content.contains(sentinel)) {
                    offenders.add(path + " contains U+"
                            + Integer.toHexString(sentinel.codePointAt(0)).toUpperCase());
                    return;
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot inspect " + path, ex);
        }
    }

    private static void assertContains(String relativePath, String expected) throws IOException {
        Path path = resolveModulePath(relativePath);
        String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.contains(expected),
                () -> path + " must contain exact Unicode text: " + expected);
    }

    private static Path resolveModulePath(String relative) {
        Path local = Path.of(relative);
        if (Files.exists(local)) {
            return local;
        }
        Path nested = Path.of("catgis-desktop").resolve(relative);
        if (Files.exists(nested)) {
            return nested;
        }
        throw new IllegalStateException("CATGIS module path not found: " + relative);
    }
}
