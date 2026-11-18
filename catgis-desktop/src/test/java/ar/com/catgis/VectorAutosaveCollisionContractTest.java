package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T1/#260: transient vector auto-save outputs need a unique physical identity
 * per layer. Sanitized names collide ("A B" and "A_B" both become a_b.shp), so
 * the planner must reserve unique files, never overwrite another layer's file
 * and stay deterministic across saves.
 */
class VectorAutosaveCollisionContractTest {

    @TempDir
    Path directory;

    private static Layer layer(String name) {
        return new Layer(name, "", "VECTOR");
    }

    private static String fileName(File file) {
        return file.getName().toLowerCase(java.util.Locale.ROOT);
    }

    @Test
    void sanitizedNamesReceiveUniquePhysicalFiles() {
        File project = directory.resolve("proyecto.catgis").toFile();
        Set<String> reserved = new HashSet<>();
        File first = SaveProjectAction.buildAutoVectorFile(project, layer("A B"), ".shp", reserved);
        reserved.add(first.getAbsolutePath().toLowerCase(java.util.Locale.ROOT));
        File second = SaveProjectAction.buildAutoVectorFile(project, layer("A_B"), ".shp", reserved);
        reserved.add(second.getAbsolutePath().toLowerCase(java.util.Locale.ROOT));
        File third = SaveProjectAction.buildAutoVectorFile(project, layer("A B"), ".shp", reserved);

        assertEquals("a_b.shp", fileName(first));
        assertEquals("a_b_2.shp", fileName(second));
        assertEquals("a_b_3.shp", fileName(third));
        assertEquals(3, new LinkedHashSet<>(List.of(first.getAbsolutePath(),
                second.getAbsolutePath(), third.getAbsolutePath())).size());
        assertTrue(first.getParentFile().getName().equals("proyecto_capas"));
        assertFalse(first.exists(), "planning must not create files");
    }

    @Test
    void foreignExistingFileIsNeverOverwritten() throws Exception {
        File layersDir = directory.resolve("proyecto_capas").toFile();
        assertTrue(layersDir.mkdirs());
        Path foreign = layersDir.toPath().resolve("a_b.shp");
        Files.writeString(foreign, "foreign data");
        File project = directory.resolve("proyecto.catgis").toFile();
        File planned = SaveProjectAction.buildAutoVectorFile(project, layer("A B"), ".shp",
                new HashSet<>());
        assertEquals("a_b_2.shp", fileName(planned));
        assertEquals("foreign data", Files.readString(foreign),
                "pre-existing foreign file must remain untouched");
    }

    @Test
    void layerOwnFileCanBeReused() throws Exception {
        File layersDir = directory.resolve("proyecto_capas").toFile();
        assertTrue(layersDir.mkdirs());
        File own = layersDir.toPath().resolve("a_b.shp").toFile();
        Files.writeString(own.toPath(), "own data");
        Layer layer = layer("A B");
        layer.setPath(own.getAbsolutePath());
        File planned = SaveProjectAction.buildAutoVectorFile(
                directory.resolve("proyecto.catgis").toFile(), layer, ".shp", new HashSet<>());
        assertEquals(own.getAbsolutePath(), planned.getAbsolutePath(),
                "a layer may overwrite its own previous auto file");
    }

    @Test
    void sanitizationVariantsDoNotCollide() {
        File project = directory.resolve("proyecto.catgis").toFile();
        Set<String> reserved = new HashSet<>();
        Set<String> names = new LinkedHashSet<>();
        for (String variant : List.of("A B", "A_B", "A/B", "A:B", "A*B", "A?B", "A|B", "A<B")) {
            File planned = SaveProjectAction.buildAutoVectorFile(project, layer(variant),
                    ".shp", reserved);
            names.add(fileName(planned));
            reserved.add(planned.getAbsolutePath().toLowerCase(java.util.Locale.ROOT));
        }
        assertEquals(8, names.size(), "every layer needs its own physical file: " + names);
    }
}
