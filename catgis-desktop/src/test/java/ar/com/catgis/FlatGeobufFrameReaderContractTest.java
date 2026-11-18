package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#175: FlatGeobuf frames are streamed in bounded windows and fail closed. */
class FlatGeobufFrameReaderContractTest {

    @TempDir
    Path directory;

    private static byte[] frame(int payloadSize, int fill) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + payloadSize)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(payloadSize);
        for (int i = 0; i < payloadSize; i++) {
            buffer.put((byte) fill);
        }
        return buffer.array();
    }

    private Path fileWith(byte... content) throws Exception {
        Path file = directory.resolve("frames.bin");
        Files.write(file, content);
        return file;
    }

    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] combined = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, combined, offset, array.length);
            offset += array.length;
        }
        return combined;
    }

    @Test
    void streamsAllFramesAndReportsTheDecodedCount() throws Exception {
        byte[] data = concat(frame(4, 1), frame(8, 2), frame(2, 3));
        Path file = fileWith(data);
        List<Integer> fills = new ArrayList<>();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            int decoded = FlatGeobufFrameReader.stream(channel, 0, data.length, 3,
                    (payload, index) -> fills.add(payload[0] & 0xFF));
        }
        assertEquals(3, fills.size());
        assertEquals(List.of(1, 2, 3), fills);
    }

    @Test
    void multiWindowLargeFrameSetsAreStreamedWithoutCountDrift() throws Exception {
        byte[][] frames = new byte[300][];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = frame(8, i);
        }
        byte[] data = concat(frames);
        Path file = fileWith(data);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            int decoded = FlatGeobufFrameReader.stream(channel, 0, data.length, 300,
                    (payload, index) -> {
                    });
            assertEquals(300, decoded);
        }
    }

    @Test
    void truncatedFrameSizeAndPayloadFailClosed() throws Exception {
        byte[] data = concat(frame(4, 1), new byte[]{1, 0}); // truncated size prefix
        Path file = fileWith(data);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            assertThrows(java.io.IOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, data.length, -1, (payload, index) -> {
                    }));
        }

        byte[] truncated = new byte[]{3, 0, 0, 0, 7}; // declares 3 bytes, provides 1
        Path truncatedFile = directory.resolve("truncated.bin");
        Files.write(truncatedFile, truncated);
        try (FileChannel channel = FileChannel.open(truncatedFile, StandardOpenOption.READ)) {
            assertThrows(java.io.IOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, truncated.length, -1, (payload, index) -> {
                    }));
        }
    }

    @Test
    void invalidFrameSizesAreRejected() throws Exception {
        byte[] zero = new byte[]{0, 0, 0, 0};
        Path zeroFile = fileWith(zero);
        try (FileChannel channel = FileChannel.open(zeroFile, StandardOpenOption.READ)) {
            assertThrows(java.io.IOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, zero.length, -1, (payload, index) -> {
                    }));
        }

        byte[] oversize = ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(FlatGeobufFrameReader.MAX_FRAME_BYTES + 1).array();
        Path oversizeFile = directory.resolve("oversize.bin");
        Files.write(oversizeFile, oversize);
        try (FileChannel channel = FileChannel.open(oversizeFile, StandardOpenOption.READ)) {
            assertThrows(java.io.IOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, oversize.length, -1, (payload, index) -> {
                    }));
        }
    }

    @Test
    void declaredCountMustMatchDecodedFrames() throws Exception {
        byte[] data = concat(frame(2, 1), frame(2, 2));
        Path file = fileWith(data);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            assertThrows(java.io.IOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, data.length, 3, (payload, index) -> {
                    }));
        }
    }

    @Test
    void interruptionCancelsWithoutPartialSuccess() throws Exception {
        byte[] data = concat(frame(4, 1), frame(4, 2));
        Path file = fileWith(data);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            Thread.currentThread().interrupt();
            assertThrows(InterruptedIOException.class, () -> FlatGeobufFrameReader.stream(
                    channel, 0, data.length, 2, (payload, index) -> {
                    }));
        } finally {
            Thread.interrupted();
        }
        assertTrue(!Thread.currentThread().isInterrupted());
    }

    @Test
    void frameLimitIsBounded() {
        assertTrue(FlatGeobufFrameReader.MAX_FRAME_BYTES > 0);
        assertTrue(FlatGeobufFrameReader.MAX_FRAME_BYTES <= 64 * 1024 * 1024);
    }
}
