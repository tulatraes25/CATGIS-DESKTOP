package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T1/#148: versioned .catgis codec is lossless for text and paths. */
class ProjectCodecRoundTripContractTest {

    private static final String TRICKY_TEXT =
            "Capa | borde\nLínea 2 100% Ñandú 中文 \\ruta\\carpeta";

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
        MapPanel mapPanel = new MapPanel();
        CatgisDesktopApp.mapPanel = mapPanel;
        AppContext.get().setMapPanel(mapPanel);
    }

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
        AppContext.get().setProject(null);
        AppContext.get().setMapPanel(null);
        CatgisDesktopApp.mapPanel = null;
    }

    @Test
    void codecRoundTripsStructuralCharacters() {
        String escaped = ProjectRecordCodec.escape(TRICKY_TEXT);
        assertFalse(escaped.contains("|"), "pipe must be escaped");
        assertFalse(escaped.contains("\n"), "newline must be escaped");
        assertFalse(escaped.contains("\r"), "CR must be escaped");
        assertTrue(escaped.contains("中文"), "Unicode must stay verbatim");
        assertEquals(TRICKY_TEXT, ProjectRecordCodec.unescape(escaped));
        assertEquals("", ProjectRecordCodec.escape(null));
        assertThrows(IllegalArgumentException.class, () -> ProjectRecordCodec.unescape("%"));
        assertThrows(IllegalArgumentException.class, () -> ProjectRecordCodec.unescape("%ZZ"));
    }

    @Test
    void canonicalProjectPreservesPipesNewlinesUnicodeAndWindowsPaths() throws Exception {
        Project project = new Project("Proyecto");
        project.setProjectCRS("EPSG:32719");
        project.setStudyName("Estudio | Ñandú\nLínea 2 100% 中文");
        // Real local source: path has spaces, Unicode and '%' (codec must keep it
        // verbatim); the layer name carries the structural pipe/newline.
        Path source = directory.resolve("capa con espacios Ñ 100%.geojson");
        Files.writeString(source, "{}", StandardCharsets.UTF_8);
        Layer layer = new Layer("Capa | borde\n2", source.toString(), "VECTOR");
        layer.setLabelField("campo|raro");
        project.addLayer(layer);
        AppContext.setCurrentProject(project);
        AppContext.get().setProject(project);

        File file = directory.resolve("codificado.catgis").toFile();
        Method save = Class.forName("ar.com.catgis.SaveProjectAction")
                .getDeclaredMethod("saveProjectToFile", File.class, boolean.class);
        save.setAccessible(true);
        assertTrue((boolean) save.invoke(null, file, false));

        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains(ProjectRecordCodec.FORMAT_LINE), "v2 marker must be written");
        assertTrue(text.contains("PROJECT_META|STUDY_NAME|"), "metadata must stay one line");
        ProjectOpenService.ProjectInspection inspection = ProjectOpenService.inspect(file);
        assertEquals("Estudio | Ñandú\nLínea 2 100% 中文", inspection.project().getStudyName());
        assertEquals(1, inspection.project().getLayers().size());
        Layer reopened = inspection.project().getLayers().get(0);
        assertEquals("Capa | borde\n2", reopened.getName());
        assertEquals(source.toString(), reopened.getPath());
        assertEquals("campo|raro", reopened.getLabelField());
    }

    @Test
    void v1ProjectWithoutMarkerStillOpens() throws Exception {
        Path file = directory.resolve("v1.catgis");
        Files.writeString(file, "CATGIS_PROJECT\n"
                + "PROJECT_CRS|EPSG:32719\n"
                + "PROJECT_META|STUDY_NAME|Proyecto legado\n"
                + "VIEW|0.0|0.0|1.0\n", StandardCharsets.UTF_8);
        ProjectOpenService.ProjectInspection inspection = ProjectOpenService.inspect(file.toFile());
        assertEquals("Proyecto legado", inspection.project().getStudyName());
        assertEquals("EPSG:32719", inspection.project().getProjectCRS());
    }

    @Test
    void legacyProjectSerializerIsLosslessToo() throws Exception {
        Project project = new Project("Legacy");
        Layer layer = new Layer("Capa | Ñ\n2", "C:\\ruta\\datos_Ñ geojson.geojson", "VECTOR");
        project.addLayer(layer);
        File file = directory.resolve("legacy.catgis").toFile();
        ProjectSerializer.serialize(project, file);
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains("# CATGIS Project v2"));
        ProjectDeserializer.Result result = ProjectDeserializer.deserialize(file);
        assertEquals(1, result.project.getLayers().size());
        Layer reopened = result.project.getLayers().get(0);
        assertEquals("Capa | Ñ\n2", reopened.getName());
        assertEquals(reopenPathForComparison("C:\\ruta\\datos_Ñ geojson.geojson"),
                reopenPathForComparison(reopened.getPath()));
    }

    private static String reopenPathForComparison(String path) {
        return path == null ? null : path.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
    }
}
