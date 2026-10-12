package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayerBoundaryImportRatchetTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Pattern LOCAL_IMPORT = Pattern.compile(
            "^\\s*import\\s+(?:static\\s+)?(ar\\.com\\.catgis\\.[A-Za-z0-9_.$*]+)\\s*;",
            Pattern.MULTILINE);

    @Test
    void coreModelCannotGainDependenciesOnUiOrApplicationServices() throws Exception {
        Set<String> reviewedDebt = Set.of(
                "geometry/SpatialIndex.java\tar.com.catgis.CatgisLogger",
                "model/DataDefinedOverrides.java\tar.com.catgis.renderer.labels.LabelExpressionEngine",
                "model/Layer.java\tar.com.catgis.CatgisLogger",
                "model/Layer.java\tar.com.catgis.ProportionalSymbols",
                "model/Layer.java\tar.com.catgis.PointSymbolCatalog",
                "model/Layer.java\tar.com.catgis.GraduatedSymbology",
                "model/Layer.java\tar.com.catgis.FieldConfig",
                "model/Layer.java\tar.com.catgis.CRSDefinitions",
                "model/Layer.java\tar.com.catgis.CategorizedSymbology",
                "model/Layer.java\tar.com.catgis.RuleBasedSymbology",
                "model/Layer.java\tar.com.catgis.CadLayerSupport",
                "model/Layer.java\tar.com.catgis.CadGeoreference",
                "model/Project.java\tar.com.catgis.CRSDefinitions",
                "model/Project.java\tar.com.catgis.CatmapLegendItem",
                "model/Project.java\tar.com.catgis.CatmapLayoutItem"
        );

        assertEquals(reviewedDebt, externalImports("core",
                        Set.of("ar.com.catgis.core.")),
                "Core outbound imports changed. Reduce legacy dependency debt or review the "
                        + "architecture decision explicitly; do not silently grow coupling.");
    }

    @Test
    void serviceLayerCannotGainUnreviewedDesktopUiDependencies() throws Exception {
        Set<String> reviewedDebt = Set.of(
                "EventBus.java\tar.com.catgis.CatgisLogger",
                "EventBusInitializer.java\tar.com.catgis.AppContext",
                "EventBusInitializer.java\tar.com.catgis.CatgisDesktopApp",
                "ToolboxAlgorithm.java\tar.com.catgis.ToolboxParameter",
                "ToolboxAlgorithm.java\tar.com.catgis.ToolboxOutputType",
                "ToolboxAlgorithm.java\tar.com.catgis.ToolboxInputType",
                "ToolboxRegistry.java\tar.com.catgis.analysis.vector.GeoprocessingAssistantDialog",
                "ToolboxRegistry.java\tar.com.catgis.ToolboxParameterType",
                "ToolboxRegistry.java\tar.com.catgis.ToolboxParameter",
                "ToolboxRegistry.java\tar.com.catgis.ToolboxOutputType",
                "ToolboxRegistry.java\tar.com.catgis.ToolboxInputType"
        );

        assertEquals(reviewedDebt, externalImports("service",
                        Set.of("ar.com.catgis.core.", "ar.com.catgis.service.")),
                "Service outbound imports changed. No unreviewed UI coupling may be introduced.");
    }


    // No fully-qualified local source calls may bypass the import-based baseline.
    @Test
    void fullyQualifiedNamesCannotBypassLayerDependencyGuard() throws Exception {
        Set<String> violations = new LinkedHashSet<>();
        Pattern qualified = Pattern.compile(
                "\\bar\\.com\\.catgis\\.[A-Za-z_$][A-Za-z0-9_.$]*");
        for (String folder : new String[]{"core", "service"}) {
            Path root = PROD_ROOT.resolve(folder);
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .toList()) {
                    String[] lines = Files.readString(file, StandardCharsets.UTF_8).split("\\R");
                    for (int index = 0; index < lines.length; index++) {
                        String trimmed = lines[index].trim();
                        if (trimmed.startsWith("package ") || trimmed.startsWith("import ")
                                || trimmed.startsWith("//") || trimmed.startsWith("*")) {
                            continue;
                        }
                        Matcher matcher = qualified.matcher(lines[index]);
                        while (matcher.find()) {
                            String relative = root.relativize(file).toString().replace('\\', '/');
                            violations.add(folder + "/" + relative + ":" + (index + 1)
                                    + "\\t" + matcher.group());
                        }
                    }
                }
            }
        }
        assertEquals(Set.of(), violations,
                "Fully qualified local dependencies bypass the import ratchet; use reviewed imports.");
    }

    private static Set<String> externalImports(String packageFolder, Set<String> allowedPrefixes)
            throws Exception {
        Path root = PROD_ROOT.resolve(packageFolder);
        Set<String> dependencies = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String source = Files.readString(file, StandardCharsets.UTF_8);
                Matcher matcher = LOCAL_IMPORT.matcher(source);
                while (matcher.find()) {
                    String symbol = matcher.group(1);
                    if (allowedPrefixes.stream().noneMatch(symbol::startsWith)) {
                        String relative = root.relativize(file).toString().replace('\\', '/');
                        dependencies.add(relative + "\t" + symbol);
                    }
                }
            }
        }
        return dependencies;
    }
}
