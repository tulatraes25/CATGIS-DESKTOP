package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DC-1 contract: a failed vector export must never destroy the previously saved
 * dataset, and a successful one must replace the whole set without leaving
 * staging or backup files behind.
 */
class VectorExportPublisherContractTest {

    @TempDir
    Path tempDir;

    @Test
    void singleFileWriteFailureKeepsThePreviousExport() throws Exception {
        Path target = tempDir.resolve("capa.geojson");
        write(target, "dataset-anterior");

        IOException failure = assertThrows(IOException.class, () ->
                VectorExportPublisher.publishSingleFile(target.toFile(), staging -> {
                    write(staging.toPath(), "dataset-nuevo-incompleto");
                    throw new IOException("fallo simulado de exportacion");
                }));

        assertEquals("fallo simulado de exportacion", failure.getMessage());
        assertEquals("dataset-anterior", read(target),
                "El archivo anterior debe sobrevivir a una exportacion fallida.");
        assertNoExportLeftovers();
    }

    @Test
    void singleFileWriteSuccessReplacesTheExport() throws Exception {
        Path target = tempDir.resolve("capa.geojson");
        write(target, "dataset-anterior");

        VectorExportPublisher.publishSingleFile(target.toFile(), staging -> write(staging.toPath(), "dataset-nuevo"));

        assertEquals("dataset-nuevo", read(target));
        assertNoExportLeftovers();
    }

    @Test
    void singleFileWriteSuccessCreatesAMissingExport() throws Exception {
        Path target = tempDir.resolve("nueva.geojson");

        VectorExportPublisher.publishSingleFile(target.toFile(), staging -> write(staging.toPath(), "primera-version"));

        assertEquals("primera-version", read(target));
        assertNoExportLeftovers();
    }

    @Test
    void shapefileWriteFailureKeepsThePreviousDataset() throws Exception {
        createPreviousDataset("pozos");

        IOException failure = assertThrows(IOException.class, () ->
                VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(), staging -> {
                    write(staging.toPath(), "SHP-NUEVO-PARCIAL");
                    throw new IOException("fallo simulado de exportacion");
                }));

        assertEquals("fallo simulado de exportacion", failure.getMessage());
        assertPreviousDatasetIntact("pozos");
        assertNoExportLeftovers();
    }

    @Test
    void shapefilePublishesTheWholeSetAndRemovesStaleParts() throws Exception {
        createPreviousDataset("pozos");

        VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(), staging -> {
            Path directory = staging.toPath().getParent();
            assertEquals("pozos.shp", staging.getName(), "El escritor debe recibir un destino .shp en staging.");
            write(directory.resolve("pozos.shp"), "SHP-NUEVO");
            write(directory.resolve("pozos.shx"), "SHX-NUEVO");
            write(directory.resolve("pozos.dbf"), "DBF-NUEVO");
            write(directory.resolve("pozos.prj"), "PRJ-NUEVO");
        });

        assertEquals("SHP-NUEVO", read(tempDir.resolve("pozos.shp")));
        assertEquals("SHX-NUEVO", read(tempDir.resolve("pozos.shx")));
        assertEquals("DBF-NUEVO", read(tempDir.resolve("pozos.dbf")));
        assertEquals("PRJ-NUEVO", read(tempDir.resolve("pozos.prj")));
        assertFalse(Files.exists(tempDir.resolve("pozos.qix")),
                "Los sidecars obsoletos del dataset anterior no deben sobrevivir.");
        assertNoExportLeftovers();
    }

    @Test
    void shapefileMissingMandatoryPartsIsRejectedWithoutTouchingThePreviousDataset() throws Exception {
        createPreviousDataset("pozos");

        IOException failure = assertThrows(IOException.class, () ->
                VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(), staging -> {
                    Path directory = staging.toPath().getParent();
                    write(directory.resolve("pozos.shp"), "SHP-NUEVO");
                    // falta .shx
                }));

        assertTrue(failure.getMessage().contains("requerido"),
                "Se esperaba un error de dataset incompleto: " + failure.getMessage());
        assertPreviousDatasetIntact("pozos");
        assertNoExportLeftovers();
    }

    @Test
    void shapefilePublishesANewDatasetWhenNoneExists() throws Exception {
        VectorExportPublisher.publishShapefile(tempDir.resolve("nuevos.shp").toFile(), staging -> {
            Path directory = staging.toPath().getParent();
            write(directory.resolve("nuevos.shp"), "SHP");
            write(directory.resolve("nuevos.shx"), "SHX");
            write(directory.resolve("nuevos.dbf"), "DBF");
        });

        assertEquals("SHP", read(tempDir.resolve("nuevos.shp")));
        assertEquals("SHX", read(tempDir.resolve("nuevos.shx")));
        assertEquals("DBF", read(tempDir.resolve("nuevos.dbf")));
        assertNoExportLeftovers();
    }

    private void createPreviousDataset(String baseName) throws IOException {
        write(tempDir.resolve(baseName + ".shp"), "SHP-VIEJO");
        write(tempDir.resolve(baseName + ".shx"), "SHX-VIEJO");
        write(tempDir.resolve(baseName + ".dbf"), "DBF-VIEJO");
        write(tempDir.resolve(baseName + ".prj"), "PRJ-VIEJO");
        write(tempDir.resolve(baseName + ".qix"), "QIX-VIEJO");
    }

    private void assertPreviousDatasetIntact(String baseName) throws IOException {
        assertEquals("SHP-VIEJO", read(tempDir.resolve(baseName + ".shp")));
        assertEquals("SHX-VIEJO", read(tempDir.resolve(baseName + ".shx")));
        assertEquals("DBF-VIEJO", read(tempDir.resolve(baseName + ".dbf")));
        assertEquals("PRJ-VIEJO", read(tempDir.resolve(baseName + ".prj")));
        assertEquals("QIX-VIEJO", read(tempDir.resolve(baseName + ".qix")));
    }

    private void assertNoExportLeftovers() throws IOException {
        try (Stream<Path> entries = Files.list(tempDir)) {
            List<String> leftovers = entries
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(".catgis-") || name.contains(".catgis-old-"))
                    .toList();
            assertTrue(leftovers.isEmpty(), "Quedaron temporales de exportacion: " + leftovers);
        }
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
