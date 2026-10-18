package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorSingleFilePublisherContractTest {

    @TempDir
    Path directory;

    @Test
    void successfulValidatedStageReplacesDestination() throws Exception {
        Path target = existingTarget();
        Path published = VectorSingleFilePublisher.publish(
                target.toFile(), ".geojson",
                stage -> Files.writeString(stage, "{\"type\":\"FeatureCollection\",\"features\":[]}",
                        StandardCharsets.UTF_8),
                stage -> assertTrue(Files.readString(stage).contains("FeatureCollection")));

        assertEquals(target.toRealPath(), published.toRealPath());
        assertTrue(Files.readString(target).contains("FeatureCollection"));
        assertNoStages();
    }

    @Test
    void partialWriterFailurePreservesPreviousBytes() throws Exception {
        Path target = existingTarget();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> VectorSingleFilePublisher.publish(
                target.toFile(), ".geojson",
                stage -> {
                    Files.writeString(stage, "partial", StandardCharsets.UTF_8);
                    throw new IOException("simulated writer failure");
                },
                stage -> {}));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void validatorFailurePreservesPreviousBytes() throws Exception {
        Path target = existingTarget();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> VectorSingleFilePublisher.publish(
                target.toFile(), ".kml",
                stage -> Files.writeString(stage, "<kml/>", StandardCharsets.UTF_8),
                stage -> { throw new IOException("simulated parser rejection"); }));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void unsupportedAtomicMovePreservesPreviousBytes() throws Exception {
        Path target = existingTarget();
        byte[] previous = Files.readAllBytes(target);

        assertThrows(IOException.class, () -> VectorSingleFilePublisher.publish(
                target.toFile(), ".gpx",
                stage -> Files.writeString(stage, "<gpx/>", StandardCharsets.UTF_8),
                stage -> {},
                (stage, destination) -> {
                    throw new AtomicMoveNotSupportedException(
                            stage.toString(), destination.toString(), "test");
                },
                4096));

        assertArrayEquals(previous, Files.readAllBytes(target));
        assertNoStages();
    }

    @Test
    void directoryTargetIsRejectedWithoutDestroyingIt() throws Exception {
        Path target = Files.createDirectory(directory.resolve("layer.kml"));
        assertThrows(IOException.class, () -> VectorSingleFilePublisher.publish(
                target.toFile(), ".kml",
                stage -> Files.writeString(stage, "<kml/>", StandardCharsets.UTF_8),
                stage -> {}));
        assertTrue(Files.isDirectory(target));
        assertNoStages();
    }

    @Test
    void stageKeepsRequestedExtensionForRealLoaderValidation() throws Exception {
        Path target = directory.resolve("layer.kmz");
        VectorSingleFilePublisher.publish(
                target.toFile(), ".kmz",
                stage -> {
                    assertTrue(stage.getFileName().toString().endsWith(".kmz"));
                    Files.write(stage, new byte[]{1, 2, 3});
                },
                stage -> assertTrue(stage.getFileName().toString().endsWith(".kmz")));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(target));
        assertNoStages();
    }

    private Path existingTarget() throws IOException {
        Path target = directory.resolve("existing.geojson");
        Files.writeString(target, "OLD_VALID_BYTES", StandardCharsets.UTF_8);
        return target;
    }

    private void assertNoStages() throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            List<Path> stages = paths
                    .filter(path -> path.getFileName().toString().startsWith(".catgis-vector-"))
                    .toList();
            assertTrue(stages.isEmpty(), "No owned staging file may remain: " + stages);
        }
    }
}
