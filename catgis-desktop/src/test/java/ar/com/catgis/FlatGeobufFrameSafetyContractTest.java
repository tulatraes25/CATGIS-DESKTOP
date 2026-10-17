package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlatGeobufFrameSafetyContractTest {
    @TempDir Path tempDir;

    @Test
    void oversizedHeaderIsRejectedBeforeAnyHeapAllocation() throws Exception {
        Path file = tempDir.resolve("declared-oversized-header.fgb");
        try (RandomAccessFile f = new RandomAccessFile(file.toFile(), "rw")) {
            f.write(new byte[]{0x67, 0x62, 0x66, 0x67});
            // 4 MiB + 1 as little-endian signed size
            f.write(new byte[]{0x01, 0x00, 0x40, 0x00});
            f.setLength(FlatGeobufLoader.MAX_HEADER_BYTES + 64L);
        }
        ValidationResult result = FlatGeobufLoader.validateFile(file.toFile());
        assertFalse(result.isValid());
        assertTrue(result.message().contains("4 MiB"), result.message());
    }

    @Test
    void shortChannelReadMustNeverCountAsComplete() throws Exception {
        Path file = tempDir.resolve("truncated.bin");
        Files.write(file, new byte[]{1, 2, 3});
        try (FileChannel channel = FileChannel.open(file)) {
            ByteBuffer expectedFour = ByteBuffer.allocate(4);
            assertThrows(IOException.class, () ->
                    FlatGeobufLoader.readFully(channel, expectedFour, "four bytes"));
        }
        try (FileChannel channel = FileChannel.open(file)) {
            ByteBuffer expectedThree = ByteBuffer.allocate(3);
            FlatGeobufLoader.readFully(channel, expectedThree, "three bytes");
            assertEquals(3, expectedThree.position());
            expectedThree.flip();
            assertEquals(1, expectedThree.get());
            assertEquals(2, expectedThree.get());
            assertEquals(3, expectedThree.get());
        }
    }

    @Test
    void malformedFramesCannotSilentlyBecomePartialLayer() {
        for (int badSize : new int[]{-1, 0, 6, Integer.MAX_VALUE}) {
            assertThrows(IOException.class,
                    () -> FlatGeobufLoader.requireFeatureFrameSize(badSize, 5));
        }
        assertThrows(IOException.class,
                () -> FlatGeobufLoader.requireFeatureFrameSize(5, 0));
    }

    @Test
    void sourceMissingGeometryNeverBecomesFictitiousOriginPoint() {
        assertThrows(IllegalArgumentException.class,
                () -> FlatGeobufLoader.requireRealGeometry(null));
        Geometry actual = new GeometryFactory().createPoint(new Coordinate(0, 0));
        assertSame(actual, FlatGeobufLoader.requireRealGeometry(actual));
    }
}
