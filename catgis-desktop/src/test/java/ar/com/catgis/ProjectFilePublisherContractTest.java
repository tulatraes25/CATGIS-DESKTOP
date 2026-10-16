package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectFilePublisherContractTest {

    @TempDir
    Path directory;

    @Test
    void successfulPublicationCreatesAndReplacesAValidProject() throws Exception {
        Path target = directory.resolve("proyecto.catgis");
        Files.writeString(target, "CATGIS_PROJECT\nOLD\n", StandardCharsets.UTF_8);

        Path published = ProjectFilePublisher.publish(target.toFile(),
                staging -> Files.writeString(staging,
                        "CATGIS_PROJECT\nPROJECT_CRS|EPSG:32719\n",
                        StandardCharsets.UTF_8));

        assertEquals(target.toRealPath(), published.toRealPath());
        assertEquals("CATGIS_PROJECT\nPROJECT_CRS|EPSG:32719\n",
                Files.readString(target, StandardCharsets.UTF_8));
        assertNoStages();
    }

    @Test
    void partialWriterFailurePreservesPreviousProjectByteForByte() throws Exception {
        Path target = existingProject();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(target.toFile(),
                staging -> {
                    Files.writeString(staging, "CATGIS_PROJECT\nPARTIAL",
                            StandardCharsets.UTF_8);
                    throw new IOException("simulated disk/write failure");
                }));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void unsupportedAtomicMovePreservesPreviousProjectByteForByte() throws Exception {
        Path target = existingProject();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(
                target.toFile(),
                staging -> Files.writeString(staging, "CATGIS_PROJECT\nNEW\n",
                        StandardCharsets.UTF_8),
                (staging, destination) -> {
                    throw new AtomicMoveNotSupportedException(
                            staging.toString(), destination.toString(), "test");
                },
                4096));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void invalidStagedHeaderCannotReplacePreviousProject() throws Exception {
        Path target = existingProject();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(target.toFile(),
                staging -> Files.writeString(staging, "NOT_A_PROJECT\n",
                        StandardCharsets.UTF_8)));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void boundedSizeIsCheckedBeforePublication() throws Exception {
        Path target = existingProject();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(
                target.toFile(),
                staging -> Files.writeString(staging,
                        "CATGIS_PROJECT\n" + "x".repeat(200),
                        StandardCharsets.UTF_8),
                (staging, destination) -> Files.move(staging, destination,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING),
                64));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void directoryTargetIsRejectedWithoutCreatingAStage() throws Exception {
        Path target = Files.createDirectory(directory.resolve("not-a-file.catgis"));

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(target.toFile(),
                staging -> Files.writeString(staging, "CATGIS_PROJECT\n",
                        StandardCharsets.UTF_8)));

        assertTrue(Files.isDirectory(target));
        assertNoStages();
    }

    @Test
    void missingParentIsRejectedWithoutCreatingDirectories() {
        Path parent = directory.resolve("missing");
        Path target = parent.resolve("project.catgis");

        assertThrows(IOException.class, () -> ProjectFilePublisher.publish(target.toFile(),
                staging -> Files.writeString(staging, "CATGIS_PROJECT\n",
                        StandardCharsets.UTF_8)));

        assertFalse(Files.exists(parent));
    }

    private Path existingProject() throws IOException {
        Path target = directory.resolve("existing.catgis");
        Files.writeString(target, "CATGIS_PROJECT\nPROJECT_CRS|EPSG:4326\n",
                StandardCharsets.UTF_8);
        return target;
    }

    private void assertNoStages() throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            List<Path> stages = paths
                    .filter(path -> path.getFileName().toString().startsWith(".catgis-project-"))
                    .toList();
            assertTrue(stages.isEmpty(), "No owned .part file may remain: " + stages);
        }
    }
}
