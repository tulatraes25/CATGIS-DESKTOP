package ar.com.catgis;

import com.google.flatbuffers.FlatBufferBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Header;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlatGeobufCrsSupportTest {
    @TempDir Path tempDir;


    @Test
    void missingOrganizationWithNumericCodeMeansEpsgPerFlatGeobufSpec() {
        FlatGeobufCrsSupport.Resolution result =
                FlatGeobufCrsSupport.resolveMetadata(null, 3857, "", "");

        assertEquals("EPSG:3857", result.sourceCode());
        assertEquals("AUTHORITY", result.provenance());
        assertFalse(result.coordinateReferenceSystem() == null);
    }

    @Test
    void absentMetadataRemainsUnknownAndNeverBecomes4326() {
        FlatGeobufCrsSupport.Resolution result =
                FlatGeobufCrsSupport.resolveMetadata(null, 0, "", "");

        assertTrue(result.sourceCode().isBlank());
        assertEquals("UNKNOWN", result.provenance());
        assertTrue(result.coordinateReferenceSystem() == null);
    }

    @Test
    void actualFlatbufferHeaderCrsIsReadRatherThanInvented() {
        FlatBufferBuilder builder = new FlatBufferBuilder(256);
        int org = builder.createString("EPSG");
        Crs.startCrs(builder);
        Crs.addOrg(builder, org);
        Crs.addCode(builder, 32719);
        int crs = Crs.endCrs(builder);

        Header.startHeader(builder);
        Header.addCrs(builder, crs);
        int header = Header.endHeader(builder);
        builder.finish(header);

        ByteBuffer buffer = builder.dataBuffer();
        FlatGeobufCrsSupport.Resolution result = FlatGeobufCrsSupport.resolve(buffer);

        assertEquals("EPSG:32719", result.sourceCode());
        assertEquals("AUTHORITY", result.provenance());
    }

    @Test
    void realFramedFileHeaderIsVerifiedBeforeFeatureImport() throws Exception {
        FlatBufferBuilder builder = new FlatBufferBuilder(256);
        int authorityOrg = builder.createString("EPSG");
        Crs.startCrs(builder);
        Crs.addOrg(builder, authorityOrg);
        Crs.addCode(builder, 32719);
        int crs = Crs.endCrs(builder);
        Header.startHeader(builder);
        Header.addCrs(builder, crs);
        int header = Header.endHeader(builder);
        builder.finish(header);

        ByteBuffer raw = builder.dataBuffer();
        ByteBuffer framed = ByteBuffer.allocate(
                org.wololo.flatgeobuf.Constants.MAGIC_BYTES.length
                        + Integer.BYTES + raw.remaining()).order(ByteOrder.LITTLE_ENDIAN);
        framed.put(org.wololo.flatgeobuf.Constants.MAGIC_BYTES);
        framed.putInt(raw.remaining());
        framed.put(raw);
        Path source = tempDir.resolve("metadata.fgb");
        Files.write(source, framed.array());

        FlatGeobufCrsSupport.Resolution detected =
                FlatGeobufLoader.inspectSourceCrs(source.toFile());
        assertEquals("EPSG:32719", detected.sourceCode());
        assertEquals("AUTHORITY", detected.provenance());
    }

    @Test
    void cachedWgs84MustNotOverrideVerifiedUtmCoordinates() {
        FlatGeobufCrsSupport.Resolution embedded =
                FlatGeobufCrsSupport.resolveMetadata("EPSG", 32719, "", "");
        assertThrows(IllegalArgumentException.class,
                () -> FlatGeobufCrsSupport.requireConsistentWithAssignedCrs(
                        embedded, "EPSG:4326"));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> FlatGeobufCrsSupport.requireConsistentWithAssignedCrs(
                        embedded, "EPSG:32719"));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> FlatGeobufCrsSupport.requireConsistentWithAssignedCrs(
                        FlatGeobufCrsSupport.Resolution.unknown(), "EPSG:32719"));
    }

    @Test
    void framedHeaderWithoutCrsStaysUnknownInsteadOfAssumingWgs84() throws Exception {
        FlatBufferBuilder builder = new FlatBufferBuilder(128);
        Header.startHeader(builder);
        int header = Header.endHeader(builder);
        builder.finish(header);
        ByteBuffer raw = builder.dataBuffer();
        ByteBuffer framed = ByteBuffer.allocate(
                org.wololo.flatgeobuf.Constants.MAGIC_BYTES.length
                        + Integer.BYTES + raw.remaining()).order(ByteOrder.LITTLE_ENDIAN);
        framed.put(org.wololo.flatgeobuf.Constants.MAGIC_BYTES);
        framed.putInt(raw.remaining());
        framed.put(raw);
        Path file = tempDir.resolve("unknown.fgb");
        Files.write(file, framed.array());
        assertTrue(FlatGeobufLoader.inspectSourceCrs(file.toFile()).sourceCode().isBlank());
    }

    @Test
    void corruptFramedHeaderCannotYieldPlausibleCrs() throws Exception {
        Path file = tempDir.resolve("truncated.fgb");
        Files.write(file, new byte[] {0x66, 0x67, 0x62});
        assertThrows(java.io.IOException.class,
                () -> FlatGeobufLoader.inspectSourceCrs(file.toFile()));
    }

    @Test
    void schemaWithoutCrsReturnsBlankInsteadOfDefaultEpsg4326() {
        org.geotools.feature.simple.SimpleFeatureTypeBuilder builder =
                new org.geotools.feature.simple.SimpleFeatureTypeBuilder();
        builder.setName("unknown");
        builder.add("the_geom", org.locationtech.jts.geom.Point.class);

        assertTrue(FlatGeobufCrsSupport.schemaCode(builder.buildFeatureType()).isBlank());
    }
}
