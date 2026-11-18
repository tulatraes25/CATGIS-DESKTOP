package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T1/#149: isolated, versioned and non-destructive recovery snapshots. */
class ProjectRecoveryJournalContractTest {

    private static final Path FIXTURE =
            Path.of("src", "test", "resources", "fixtures", "integration", "test_points.shp");

    @TempDir
    Path directory;

    private Project project;
    private Layer layer;
    private ShapefileData data;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty(ProjectRecoveryJournal.ROOT_PROPERTY,
                directory.resolve("recovery").toString());
        ar.com.catgis.catmap.app.CatmapAppContext.standalone().install();
        MapPanel mapPanel = new MapPanel();
        CatgisDesktopApp.mapPanel = mapPanel;
        AppContext.get().setMapPanel(mapPanel);
        project = new Project("Recuperable");
        project.setProjectCRS("EPSG:32719");
        layer = new Layer("Puntos", FIXTURE.toString(), "VECTOR");
        layer.setSourceCRS("EPSG:32719");
        project.addLayer(layer);
        AppContext.setCurrentProject(project);
        AppContext.get().setProject(project);
        data = ShapefileLoader.load(FIXTURE.toFile());
        mapPanel.addOrUpdateShapefileLayer(layer, data);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ProjectRecoveryJournal.ROOT_PROPERTY);
        AppContext.setCurrentProject(null);
        AppContext.get().setProject(null);
        AppContext.get().setMapPanel(null);
        CatgisDesktopApp.mapPanel = null;
    }

    private Map<Layer, ShapefileData> dataMap() {
        Map<Layer, ShapefileData> map = new LinkedHashMap<>();
        map.put(layer, data);
        return map;
    }

    @Test
    void snapshotAndRestoreRecoversInMemoryVectorData() throws Exception {
        int originalCount = layer.getFeatureCount();
        int inMemoryCount = data.getFeatureCount();
        Path snapshot = ProjectRecoveryJournal.snapshot(project, dataMap());

        assertTrue(Files.isRegularFile(snapshot), "snapshot project must exist");
        Path manifest = snapshot.getParent().resolve("manifest.properties");
        assertTrue(Files.isRegularFile(manifest), "manifest must exist");
        try (Stream<Path> files = Files.list(snapshot.getParent().resolve("data"))) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().endsWith(".shp")),
                    "unsaved vector data must be stored as a shapefile sidecar");
        }
        assertEquals(FIXTURE.toString(), layer.getPath(),
                "snapshot must not mutate the live layer path");
        assertTrue(Files.isRegularFile(FIXTURE), "original source must stay untouched");

        ProjectRecoveryJournal.RecoverableSnapshot recoverable =
                ProjectRecoveryJournal.latestRecoverable();
        assertNotNull(recoverable);
        Path restorable = ProjectRecoveryJournal.requireRestorable(recoverable);

        assertTrue(LoadProjectAction.loadProjectFile(restorable.toFile(), false),
                "snapshot must open through the normal project flow");
        assertEquals(1, AppContext.project().getLayers().size());
        Layer recovered = AppContext.project().getLayers().get(0);
        assertNotNull(CatgisDesktopApp.mapPanel.getShapefileData(recovered),
                "recovered layer data must be attached");
        assertEquals(inMemoryCount, recovered.getFeatureCount(),
                "recovered layer must keep the in-memory feature count");
        assertEquals(originalCount, layer.getFeatureCount(),
                "snapshot must not mutate the live layer count");
        assertTrue(recovered.getPath().contains("recovery"),
                "recovered vector path must point at the sidecar, not the user file");
    }

    @Test
    void retentionKeepsOnlyNewestSnapshots() throws Exception {
        for (int i = 0; i < ProjectRecoveryJournal.MAX_SNAPSHOTS + 2; i++) {
            ProjectRecoveryJournal.snapshot(project, dataMap());
        }
        Path identity = directory.resolve("recovery");
        long snapshots;
        try (Stream<Path> identities = Files.list(identity)) {
            snapshots = identities
                    .filter(Files::isDirectory)
                    .flatMap(dir -> {
                        try {
                            return Files.list(dir);
                        } catch (Exception failure) {
                            return Stream.empty();
                        }
                    })
                    .filter(path -> path.getFileName().toString().startsWith("snapshot-"))
                    .count();
        }
        assertTrue(snapshots <= ProjectRecoveryJournal.MAX_SNAPSHOTS,
                "retention must cap snapshots, found " + snapshots);
    }

    @Test
    void corruptSnapshotIsRejected() throws Exception {
        Path snapshot = ProjectRecoveryJournal.snapshot(project, dataMap());
        ProjectRecoveryJournal.RecoverableSnapshot recoverable =
                ProjectRecoveryJournal.latestRecoverable();
        assertNotNull(recoverable);
        Files.writeString(snapshot, "CATGIS_PROJECT\ncorrupto", StandardCharsets.UTF_8);

        assertNull(ProjectRecoveryJournal.latestRecoverable(),
                "a snapshot whose hash no longer matches must be ignored");
        assertThrows(java.io.IOException.class,
                () -> ProjectRecoveryJournal.requireRestorable(recoverable));
    }

    @Test
    void snapshotWithoutInMemoryDataKeepsOriginalPath() throws Exception {
        Path snapshot = ProjectRecoveryJournal.snapshot(project, Map.of());
        String text = Files.readString(snapshot, StandardCharsets.UTF_8);
        assertTrue(text.contains(FIXTURE.toString()),
                "layers without in-memory data keep their original path");
        assertTrue(LoadProjectAction.loadProjectFile(snapshot.toFile(), false));
        assertEquals(1, AppContext.project().getLayers().size());
    }

    @Test
    void snapshotRootIsOutsideProjectDirectory() throws Exception {
        ProjectRecoveryJournal.snapshot(project, dataMap());
        assertTrue(ProjectRecoveryJournal.root().endsWith("recovery"),
                "test override root must be the isolated recovery directory");
        assertEquals(directory.resolve("recovery").toAbsolutePath(),
                ProjectRecoveryJournal.root());
    }
}
