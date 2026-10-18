package ar.com.catgis.probe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class LocalProbeInputTest {
    @TempDir Path temporary;

    private Path input() throws IOException {
        byte[] data = new byte[32];
        for (int i = 0; i < data.length; i++) { data[i] = (byte) i; }
        return Files.write(temporary.resolve("input bytes.dat"), data);
    }

    @Test
    void directAndSlicedBuffersPreservePositionsAndActualByteRanges() throws Exception {
        try (var stream = new LocalProbeInput(input(), () -> false).newStream()) {
            ByteBuffer direct = ByteBuffer.allocateDirect(16);
            direct.position(3).limit(11);
            stream.readFully(direct);
            assertEquals(11, direct.position());
            for (int i = 0; i < 8; i++) { assertEquals(i, direct.get(3 + i)); }
            ByteBuffer parent = ByteBuffer.allocate(24);
            parent.position(5).limit(20);
            ByteBuffer slice = parent.slice();
            slice.position(2).limit(6);
            stream.readFully(slice);
            assertEquals(6, slice.position());
            for (int i = 0; i < 4; i++) { assertEquals(8 + i, parent.get(7 + i)); }
            assertEquals(12, stream.getPos());
        }
    }

    @Test
    void independentStreamsSeekAndEofDoNotInventZeroBytesOrSharePositions() throws Exception {
        var input = new LocalProbeInput(input(), () -> false);
        try (var first = input.newStream(); var second = input.newStream()) {
            first.seek(31);
            assertEquals(31, first.read());
            assertEquals(-1, first.read());
            assertEquals(0, second.read());
            first.seek(31);
            assertThrows(EOFException.class, () -> first.readFully(new byte[2]));
            assertThrows(IOException.class, () -> first.seek(-1));
            assertThrows(IOException.class, () -> first.seek(33));
        }
    }

    @Test
    void cancellationDuringReadStillClosesTheUnderlyingLocalHandle() throws Exception {
        AtomicBoolean cancelled = new AtomicBoolean();
        Path path = input();
        try (var stream = new LocalProbeInput(path, cancelled::get).newStream()) {
            assertEquals(0, stream.read());
            cancelled.set(true);
            assertThrows(InterruptedIOException.class, () -> stream.read());
        }
        Path moved = temporary.resolve("closed.dat");
        Files.move(path, moved);
        Files.delete(moved);
    }
}
