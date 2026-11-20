package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T4/#114: batch copy preserves complete dataset families and fails closed. */
class BatchDatasetCopierContractTest {

    @TempDir
    Path directory;

    @Test
    void shapefileFamilyIsCopiedWithStagedBasename() throws Exception {
        Path source = Files.createDirectory(directory.resolve("src"));
        write(source.resolve("survey.shp"), "SHP");
        write(source.resolve("survey.shx"), "SHX");
        write(source.resolve("survey.dbf"), "DBF");
        write(source.resolve("survey.prj"), "PRJ");
        write(source.resolve("survey.cpg"), "CPG");
        Path output = directory.resolve("stage").resolve("0-survey.shp");

        BatchDatasetCopier.copy(source.resolve("survey.shp").toFile(), output.toFile());

        assertEquals("SHP", Files.readString(output));
        assertEquals("SHX", Files.readString(output.resolveSibling("0-survey.shx")));
        assertEquals("DBF", Files.readString(output.resolveSibling("0-survey.dbf")));
        assertEquals("PRJ", Files.readString(output.resolveSibling("0-survey.prj")));
        assertEquals("CPG", Files.readString(output.resolveSibling("0-survey.cpg")));
    }

    @Test
    void missingRequiredDbfFailsBeforeWritingAnyFamilyMember() throws Exception {
        Path source = Files.createDirectory(directory.resolve("src"));
        write(source.resolve("broken.shp"), "SHP");
        write(source.resolve("broken.shx"), "SHX");
        Path output = directory.resolve("stage").resolve("0-broken.shp");

        assertThrows(IOException.class,
                () -> BatchDatasetCopier.copy(source.resolve("broken.shp").toFile(),
                        output.toFile()));
        assertFalse(Files.exists(output), "no partial shapefile family may be written");
        assertFalse(Files.exists(output.resolveSibling("0-broken.shx")));
    }

    @Test
    void caseInsensitiveMembersAreFound() throws Exception {
        Path source = Files.createDirectory(directory.resolve("src"));
        write(source.resolve("FIELD.SHP"), "SHP");
        write(source.resolve("FIELD.SHX"), "SHX");
        write(source.resolve("FIELD.DBF"), "DBF");
        Path output = directory.resolve("stage").resolve("1-FIELD.SHP");

        BatchDatasetCopier.copy(source.resolve("FIELD.SHP").toFile(), output.toFile());

        assertEquals("SHX", Files.readString(output.resolveSibling("1-FIELD.SHX")));
        assertEquals("DBF", Files.readString(output.resolveSibling("1-FIELD.DBF")));
    }

    @Test
    void rasterSidecarsAreCopiedAndUnrelatedFilesAreNot() throws Exception {
        Path source = Files.createDirectory(directory.resolve("src"));
        write(source.resolve("dem.tif"), "TIFF");
        write(source.resolve("dem.tif.aux.xml"), "AUX");
        write(source.resolve("dem.tfw"), "TFW");
        write(source.resolve("other.tif"), "OTHER");
        Path output = directory.resolve("stage").resolve("2-dem.tif");

        BatchDatasetCopier.copy(source.resolve("dem.tif").toFile(), output.toFile());

        assertEquals("TIFF", Files.readString(output));
        assertEquals("AUX", Files.readString(output.resolveSibling("2-dem.tif.aux.xml")));
        assertEquals("TFW", Files.readString(output.resolveSibling("2-dem.tfw")));
        assertFalse(Files.exists(output.resolveSibling("2-other.tif")));
    }

    @Test
    void singleFileFormatsCopyOnlyThePrimary() throws Exception {
        Path source = Files.createDirectory(directory.resolve("src"));
        write(source.resolve("points.geojson"), "{\"type\":\"FeatureCollection\"}");
        Path output = directory.resolve("stage").resolve("3-points.geojson");
        BatchDatasetCopier.copy(source.resolve("points.geojson").toFile(), output.toFile());
        assertTrue(Files.isRegularFile(output));
        assertEquals("{\"type\":\"FeatureCollection\"}", Files.readString(output));
        assertThrows(IOException.class, () -> BatchDatasetCopier.copy(
                source.resolve("missing.geojson").toFile(), output.toFile()));
    }

    private static void write(Path path, String text) throws IOException {
        Files.writeString(path, text, StandardCharsets.UTF_8);
    }
}
