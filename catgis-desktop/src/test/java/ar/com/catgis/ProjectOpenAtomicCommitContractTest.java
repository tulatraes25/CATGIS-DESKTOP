package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T1/#144: project open must stage layer data detached, commit atomically and
 * leave the live session untouched when a mandatory source cannot be rebuilt.
 */
class ProjectOpenAtomicCommitContractTest {

    @TempDir
    Path directory;

    private Project previousProject;
    private MapPanel mapPanel;

    @BeforeEach
    void setUp() {
        ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
        mapPanel = new MapPanel();
        CatgisDesktopApp.mapPanel = mapPanel;
        AppContext.get().setMapPanel(mapPanel);
        previousProject = new Project("Previo");
        previousProject.setProjectCRS("EPSG:32719");
        AppContext.setCurrentProject(previousProject);
        AppContext.get().setProject(previousProject);
    }

    @AfterEach
    void tearDown() {
        AppContext.setCurrentProject(null);
        AppContext.get().setProject(null);
        AppContext.get().setMapPanel(null);
        CatgisDesktopApp.mapPanel = null;
    }

    private Path writeProject(String name, Layer layer) throws Exception {
        StringBuilder text = new StringBuilder();
        text.append("CATGIS_PROJECT\n");
        text.append("PROJECT_CRS|EPSG:32719\n");
        text.append("VIEW|0.0|0.0|1.0\n");
        text.append(SaveProjectAction.buildLayerLine(layer)).append('\n');
        Path file = directory.resolve(name + ".catgis");
        Files.writeString(file, text.toString(), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void corruptMandatoryRasterLeavesPreviousSessionIntact() throws Exception {
        Path badRaster = directory.resolve("corrupto.tif");
        Files.writeString(badRaster, "this is not a GeoTIFF", StandardCharsets.UTF_8);
        Layer layer = new Layer("DEM corrupto", badRaster.toString(), "RASTER");
        layer.setSourceCRS("EPSG:32719");
        Path project = writeProject("corrupto", layer);

        boolean opened = LoadProjectAction.loadProjectFile(project.toFile(), false);

        assertFalse(opened, "corrupt mandatory source must fail the open");
        assertSame(previousProject, AppContext.project(),
                "the live project must remain the previous one");
        assertEquals("Previo", AppContext.project().getName());
        assertTrue(AppContext.project().getLayers() == null
                        || AppContext.project().getLayers().isEmpty(),
                "previous layers must not be cleared or replaced");
        assertTrue(mapPanel.getRasterData(layer) == null,
                "no staged layer may be attached after a failed open");
    }

    @Test
    void validProjectCommitsStagedRasterAtomically() throws Exception {
        Path fixture = Path.of("src", "test", "resources", "fixtures", "integration", "test_dem.tif");
        Path sidecar = Path.of(fixture + ".catgis-raster.properties");
        Path raster = directory.resolve("dem.tif");
        Files.copy(fixture, raster, StandardCopyOption.REPLACE_EXISTING);
        if (Files.exists(sidecar)) {
            Files.copy(sidecar, Path.of(raster + ".catgis-raster.properties"),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        Layer layer = new Layer("DEM", raster.toString(), "RASTER");
        layer.setSourceCRS("EPSG:32719");
        Path project = writeProject("valido", layer);

        boolean opened = LoadProjectAction.loadProjectFile(project.toFile(), false);

        assertTrue(opened, "valid staged project must open");
        assertEquals("valido", AppContext.project().getName());
        assertNotNull(AppContext.project().getLayers());
        assertEquals(1, AppContext.project().getLayers().size());
        Layer committed = AppContext.project().getLayers().get(0);
        assertNotNull(mapPanel.getRasterData(committed),
                "staged raster must be attached during the commit");
    }
}
