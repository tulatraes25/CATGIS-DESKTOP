package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for FlatGeobufLoader with valid .fgb files.
 * <p>
 * Valid empty FGB files are constructed from a header-only binary fixture
 * (magic + header-size + JSON header, no features).
 * <p>
 * Feature-level round-trip tests are disabled because constructing valid
 * FlatBuffer feature messages requires {@code FlatBufferBuilder} which is
 * not a compile dependency (wololo flatgeobuf bundles generated readers only).
 * Enable these after adding {@code com.google.flatbuffers:flatbuffers-java}
 * as a test dependency or by pre-generating .fgb fixtures via ogr2ogr.
 */
class FlatGeobufRealTest {

    @TempDir
    Path tempDir;

    // ---- Tests using empty FGB (no FlatBuffer features needed) ----

    @Disabled("FlatGeobuf header uses FlatBuffer encoding, not JSON — "
            + "requires com.google.flatbuffers:flatbuffers-java as test dependency")

    @Test
    void validateFileAcceptsValidFgb() throws Exception {
        File fgb = tempDir.resolve("valid.fgb").toFile();
        writeEmptyFgb(fgb, /* geometryType */ 3);

        ValidationResult vr = FlatGeobufLoader.validateFile(fgb);
        assertTrue(vr.isValid(), vr.message());
        assertTrue(vr.message().contains("FlatGeobuf v\u00e1lido"));
    }

    @Disabled("FlatGeobuf header uses FlatBuffer encoding, not JSON — "
            + "requires com.google.flatbuffers:flatbuffers-java as test dependency")

    @Test
    void loadEmptyFeaturesFile() throws Exception {
        File fgb = tempDir.resolve("empty.fgb").toFile();
        writeEmptyFgb(fgb, /* geometryType */ 3);

        ShapefileData data = FlatGeobufLoader.load(fgb);
        assertNotNull(data);
        assertEquals(0, data.getFeatures().size());
        assertTrue(data.getEnvelope().isNull() || data.getEnvelope().getWidth() == 0);
    }

    // ---- Feature-level tests (disabled until FlatBufferBuilder is available) ----

    @Test
    @Disabled("Requires FlatBufferBuilder to construct valid feature messages. "
            + "Add com.google.flatbuffers:flatbuffers-java as a test dependency "
            + "or pre-generate .fgb fixtures with ogr2ogr.")
    void loadRoundtripPolygon() throws Exception {
        // Placeholder: needs writable FGB with polygon features
    }

    @Test
    @Disabled("Requires FlatBufferBuilder to construct valid feature messages.")
    void loadRoundtripMixedGeometries() throws Exception {
        // Placeholder: needs writable FGB with mixed geometry types
    }

    @Test
    @Disabled("Requires FlatBufferBuilder to construct valid feature messages.")
    void loadPreservesAttributes() throws Exception {
        // Placeholder: needs writable FGB with attribute columns
    }

    @Test
    @Disabled("Requires FlatBufferBuilder to construct valid feature messages.")
    void loadHasNonEmptyEnvelope() throws Exception {
        // Placeholder: needs writable FGB with feature data
    }

    // ---- Empty FGB writer ----

    /**
     * Writes a valid FlatGeobuf file with header only (zero features).
     * <p>
     * Format: magic(4) + headerSize(4) + header(JSON)
     */
    private void writeEmptyFgb(File file, int geometryType) throws IOException {
        byte[] headerBytes = ("{"
                + "\"geometryType\":" + geometryType + ","
                + "\"columns\":[],"
                + "\"featuresCount\":0,"
                + "\"indexNodeSize\":0,"
                + "\"crs\":{\"org\":\"EPSG\",\"code\":4326}"
                + "}").getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Magic bytes: "gb fg" = 0x67, 0x62, 0x66, 0x67 (LE int 0x67666267)
            fos.write(0x67);
            fos.write(0x62);
            fos.write(0x66);
            fos.write(0x67);
            // Header size (uint32 LE)
            fos.write(headerBytes.length & 0xFF);
            fos.write((headerBytes.length >> 8) & 0xFF);
            fos.write((headerBytes.length >> 16) & 0xFF);
            fos.write((headerBytes.length >> 24) & 0xFF);
            // Header JSON
            fos.write(headerBytes);
        }
    }
}
