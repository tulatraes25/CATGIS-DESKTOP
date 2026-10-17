package ar.com.catgis;

import com.google.flatbuffers.FlatBufferBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.ByteArrayOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlatGeobufHeaderFramingContractTest {
    @TempDir Path directory;

    @Test
    void officialEightByteFgbAndLegacyFourByteBothPassFramedWololoReader()
            throws Exception {
        HeaderMeta original = new HeaderMeta();
        original.name = "tiny";
        original.geometryType = (byte) GeometryType.Point;
        original.featuresCount = 0;
        original.indexNodeSize = 0;
        original.columns = List.of();
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        HeaderMeta.write(original, payload, new FlatBufferBuilder(128));

        // Wololo writes the prefixed FlatBuffer but NOT the outer FGB magic.
        byte[] header = payload.toByteArray();
        assertTrue(header.length > 4);
        for (byte[] magic : new byte[][]{
                {0x66, 0x67, 0x62, 0x03, 0x66, 0x67, 0x62, 0x01},
                {0x67, 0x62, 0x66, 0x67}
        }) {
            Path file = directory.resolve("framed-" + magic.length + ".fgb");
            ByteArrayOutputStream framed = new ByteArrayOutputStream();
            framed.write(magic);
            framed.write(header);
            Files.write(file, framed.toByteArray());

            try (FileChannel channel = FileChannel.open(file)) {
                FlatGeobufLoader.HeaderPrefix prefix =
                        FlatGeobufLoader.readHeaderPrefix(channel);
                HeaderMeta parsed = FlatGeobufLoader.readFramedHeader(channel, prefix);
                assertEquals("tiny", parsed.name);
                assertEquals(0, parsed.featuresCount);
                assertEquals(GeometryType.Point, parsed.geometryType);
                assertEquals(0, parsed.indexNodeSize);
            }
            ValidationResult result = FlatGeobufLoader.validateFile(file.toFile());
            assertTrue(result.isValid(), result.message());
        }
    }

    @Test
    void invalidZeroFilledHeaderCannotBeMisreadAsAnEmptyDataset() throws Exception {
        Path file = directory.resolve("invalid-zero-header.fgb");
        byte[] bytes = new byte[8 + 16];
        bytes[0] = 0x67; bytes[1] = 0x62; bytes[2] = 0x66; bytes[3] = 0x67;
        bytes[4] = 0x10;
        Files.write(file, bytes);
        try (FileChannel channel = FileChannel.open(file)) {
            FlatGeobufLoader.HeaderPrefix prefix =
                    FlatGeobufLoader.readHeaderPrefix(channel);
            assertThrows(java.io.IOException.class,
                    () -> FlatGeobufLoader.readFramedHeader(channel, prefix));
        }
        assertFalse(FlatGeobufLoader.validateFile(file.toFile()).isValid());
        assertThrows(UnsupportedFormatException.class,
                () -> FlatGeobufLoader.load(file.toFile()));
    }
}
