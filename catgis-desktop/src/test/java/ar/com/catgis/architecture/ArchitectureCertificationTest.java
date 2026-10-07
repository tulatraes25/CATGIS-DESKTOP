package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureCertificationTest {

    private static final Path PROD_ROOT = Path.of("src", "ar", "com", "catgis");
    private static final Path TEST_ROOT = Path.of("src", "test", "java");

    @Test
    void coreDomainDoesNotDependOnGlobalAppContext() throws Exception {
        Set<String> offenders = filesContaining(PROD_ROOT.resolve("core"), "AppContext");
        assertTrue(offenders.isEmpty(), "core/ must not depend on global AppContext: " + offenders);
    }

    @Test
    void coreSwingDebtCannotGrow() throws Exception {
        Set<String> expected = Set.of("core/model/DataDefinedOverridesPanel.java");
        assertEquals(expected, filesContaining(PROD_ROOT.resolve("core"), "javax.swing"),
                "Swing usage inside core/ changed. Remove debt or update the certified architecture decision explicitly.");
    }

    @Test
    void serviceSwingAndGlobalContextDebtCannotGrow() throws Exception {
        Set<String> expected = Set.of("service/EventBusInitializer.java");
        assertEquals(expected, filesContaining(PROD_ROOT.resolve("service"), "javax.swing"),
                "Swing usage inside service/ changed.");
        assertEquals(expected, filesContaining(PROD_ROOT.resolve("service"), "AppContext"),
                "Global AppContext usage inside service/ changed.");
    }

    @Test
    void directProcessCreationCannotSpread() throws Exception {
        Set<String> expected = Set.of(
                "CartographyToolbar.java",
                "DwgImportSupport.java",
                "ExternalToolService.java",
                "Gdal2TilesService.java",
                "PostgisConnectionStore.java",
                "ProDatasetOpenService.java",
                "ProRasterMaterializationService.java",
                "RasterImageLoader.java",
                "RasterReprojectionService.java",
                "scripting/ScriptEngine.java"
        );
        assertEquals(expected, filesContaining(PROD_ROOT, "new ProcessBuilder("),
                "Direct process creation spread outside the certified legacy allowlist.");
    }

    @Test
    void directHttpConnectionsCannotSpread() throws Exception {
        Set<String> expected = Set.of(
                "OnlineWmsImageCache.java",
                "StacClient.java",
                "WcsClient.java",
                "WfsCapabilitiesService.java",
                "WmsCapabilitiesService.java",
                "data/online/OnlineTileCache.java"
        );
        assertEquals(expected, filesContaining(PROD_ROOT, "HttpURLConnection"),
                "Direct HTTP connection usage spread outside the certified legacy allowlist.");
    }

    @Test
    void threadSleepDebtCannotGrow() throws Exception {
        Set<String> expected = Set.of(
                "AnalysisConsoleDialog.java",
                "SplashScreenWindow.java",
                "TemporalController.java"
        );
        assertEquals(expected, filesContaining(PROD_ROOT, "Thread.sleep("),
                "Thread.sleep usage changed. Replace UI timing with event/timer primitives.");
    }

    @Test
    void productionDiagnosticsUseLoggerInsteadOfConsoleOrStackTrace() throws Exception {
        Set<String> offenders = new LinkedHashSet<>();
        offenders.addAll(filesContaining(PROD_ROOT, "System.out."));
        offenders.addAll(filesContaining(PROD_ROOT, "System.err."));
        offenders.addAll(filesContaining(PROD_ROOT, "printStackTrace("));
        assertTrue(offenders.isEmpty(), "Production diagnostics bypass CatgisLogger: " + offenders);
    }

    @Test
    void hotspotClassesCannotGrowPastCertifiedBaselineCeilings() throws Exception {
        Map<String, Integer> ceilings = new LinkedHashMap<>();
        ceilings.put("MapPanel.java", 3500);
        ceilings.put("MapLayoutComposerDialog.java", 5625);
        ceilings.put("LayersPanel.java", 2350);
        ceilings.put("MapEditingEngine.java", 2525);
        ceilings.put("ExportVectorLayerAction.java", 1900);
        ceilings.put("LayoutPreviewPanel.java", 1810);
        ceilings.put("MainMenuBar.java", 1370);
        ceilings.put("FloatingVectorEditToolbar.java", 1090);
        ceilings.put("LoadProjectAction.java", 995);

        for (Map.Entry<String, Integer> entry : ceilings.entrySet()) {
            Path file = PROD_ROOT.resolve(entry.getKey());
            long lines;
            try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
                lines = stream.count();
            }
            assertTrue(lines <= entry.getValue(),
                    entry.getKey() + " grew to " + lines + " lines; ceiling=" + entry.getValue()
                            + ". Extract responsibility instead of growing the hotspot.");
        }
    }

    @Test
    void junitDisabledAnnotationsAreForbidden() throws Exception {
        assertTrue(filesContaining(TEST_ROOT, "@Disabled").isEmpty(),
                "Do not hide certification gaps with @Disabled.");
    }

    @Test
    void conditionalAssumptionsRemainExplicitlyAllowlisted() throws Exception {
        Set<String> expected = Set.of(
                "ar/com/catgis/FlatGeobufRealTest.java",
                "ar/com/catgis/GeoPackageRealTest.java",
                "ar/com/catgis/ReleaseFormatCompatibilityTest.java",
                "ar/com/catgis/SpatiaLiteRealTest.java",
                "ar/com/catgis/integration/HeavyFormatsIntegrationTest.java"
        );
        Set<String> actual = filesContaining(TEST_ROOT, "Assumptions.assume");
        assertEquals(expected, actual,
                "Conditional test assumptions changed. Every environment skip must be reviewed explicitly.");
    }

    private static Set<String> filesContaining(Path root, String needle) throws IOException {
        Set<String> matches = new LinkedHashSet<>();
        if (!Files.isDirectory(root)) {
            return matches;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("ArchitectureCertificationTest.java"))
                    .toList()) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                if (content.contains(needle)) {
                    Path relativeBase = root.startsWith(PROD_ROOT) ? PROD_ROOT : TEST_ROOT;
                    matches.add(normalize(relativeBase.relativize(file)));
                }
            }
        }
        return matches;
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}
