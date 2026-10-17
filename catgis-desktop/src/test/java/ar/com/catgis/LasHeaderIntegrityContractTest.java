package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LasHeaderIntegrityContractTest {
    @TempDir Path folder;

    @Test
    void realVersion14MinimumHeaderAndExtendedCountAccepted() throws Exception {
        Path file = fixture(4, 1, 1);
        LasReader.LasHeader header = LasReader.readHeader(file.toFile());
        assertEquals(4, header.versionMinor());
        assertEquals(375, header.headerSize());
        assertEquals(1, header.pointCount());
        assertEquals(1, LasReader.readPoints(file.toFile(), 1).size());
    }

    @Test
    void versionSpecificHeaderSizeMustBeValid() throws Exception {
        Path file = fixture(4, 1, 1);
        byte[] bytes = Files.readAllBytes(file);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putShort(94, (short) 227);
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));

        Path old = fixture(3, 1, 1);
        byte[] earlier = Files.readAllBytes(old);
        ByteBuffer.wrap(earlier).order(ByteOrder.LITTLE_ENDIAN).putShort(94, (short) 227);
        Files.write(old, earlier);
        assertThrows(IOException.class, () -> LasReader.readHeader(old.toFile()));
    }

    @Test
    void missingPointRecordsAreRejectedEvenWhenOnlyOnePointIsRequested() throws Exception {
        Path file = fixture(2, 4, 1);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));
        assertThrows(IOException.class, () -> LasReader.readPoints(file.toFile(), 1));
    }

    @Test
    void extendedPointCountArithmeticOverflowFailsClosed() throws Exception {
        Path file = fixture(4, Long.MAX_VALUE, 1);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));
    }

    @Test
    void nonFiniteAndNegativeScalesAreNotGeoreferenced() throws Exception {
        Path file = fixture(2, 1, 1);
        byte[] bytes = Files.readAllBytes(file);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(131, Double.NaN);
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));

        bytes = Files.readAllBytes(fixture(2, 1, 1));
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(139, 0d);
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));
    }

    @Test
    void invertedBoundingBoxAndCompressedExtensionAreRejected() throws Exception {
        Path file = fixture(2, 1, 1);
        byte[] bytes = Files.readAllBytes(file);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(187, 999d);
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));
        Path laz = folder.resolve("compressed.laz");
        Files.copy(fixture(2, 1, 1), laz);
        assertThrows(IOException.class, () -> LasReader.readHeader(laz.toFile()));
    }

    private Path fixture(int minor, long declared, int stored) throws IOException {
        int headerSize = minor >= 4 ? 375 : minor == 3 ? 235 : 227;
        int recordSize = 20;
        ByteBuffer bytes = ByteBuffer.allocate(headerSize + recordSize * stored)
                .order(ByteOrder.LITTLE_ENDIAN);
        bytes.put(new byte[]{'L', 'A', 'S', 'F'});
        bytes.put(24, (byte) 1);
        bytes.put(25, (byte) minor);
        bytes.putShort(94, (short) headerSize);
        bytes.putInt(96, headerSize);
        bytes.put(104, (byte) 0);
        bytes.putShort(105, (short) recordSize);
        bytes.putInt(107, minor >= 4 ? 0 : (int) declared);
        bytes.putDouble(131, 0.01d);
        bytes.putDouble(139, 0.01d);
        bytes.putDouble(147, 0.1d);
        bytes.putDouble(155, -70d);
        bytes.putDouble(163, -70d);
        bytes.putDouble(171, 5d);
        bytes.putDouble(179, -59d);
        bytes.putDouble(187, -60d);
        bytes.putDouble(195, -50d);
        bytes.putDouble(203, -51d);
        bytes.putDouble(211, 305d);
        bytes.putDouble(219, 300d);
        if (minor >= 4) {
            bytes.putLong(247, declared);
        }
        Path file = folder.resolve("header-v1-" + minor + ".las");
        Files.write(file, bytes.array());
        return file;
    }
}
