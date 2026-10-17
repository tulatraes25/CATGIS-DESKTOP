package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import com.google.flatbuffers.FlatBufferBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A real framed FlatGeobuf header must carry its provenance into schema. */
class FlatGeobufCrsImportContractTest {
    @TempDir Path folder;

    @Test
    void actualFramedProjectedHeaderCarriesProjectedCrsIntoGeoToolsSchema() throws Exception {
        ShapefileData loaded = FlatGeobufLoader.load(makeFile("projected", 32719).toFile());
        assertEquals("EPSG:32719",
                FlatGeobufCrsSupport.schemaCode(loaded.getSchema()));
        assertEquals(0, loaded.getFeatureCount());
    }

    @Test
    void absentCrsIsUnknownAndDirectHeadlessImportCannotClaimSuccess() throws Exception {
        Path missing = makeFile("unknown", 0);
        ShapefileData loaded = FlatGeobufLoader.load(missing.toFile());
        assertTrue(FlatGeobufCrsSupport.schemaCode(loaded.getSchema()).isBlank());
        assertFalse(OpenFileAction.openSelectedFile(
                missing.toFile(), "FlatGeobuf", null, false),
                "Noninteractive unknown-CRS import must not attach invented WGS84");
    }

    private Path makeFile(String name, int srid) throws Exception {
        HeaderMeta original = new HeaderMeta();
        original.name = name;
        original.geometryType = (byte) GeometryType.Point;
        original.srid = srid;
        original.featuresCount = 0;
        original.indexNodeSize = 0;
        original.columns = List.of();
        ByteArrayOutputStream framed = new ByteArrayOutputStream();
        framed.write(org.wololo.flatgeobuf.Constants.MAGIC_BYTES);
        HeaderMeta.write(original, framed, new FlatBufferBuilder(128));
        Path file = folder.resolve(name + ".fgb");
        Files.write(file, framed.toByteArray());
        assertTrue(FlatGeobufLoader.validateFile(file.toFile()).isValid());
        return file;
    }
}
