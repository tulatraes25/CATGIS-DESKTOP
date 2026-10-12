package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LasReaderBinaryContractTest {

    @TempDir
    Path tempDir;

    @Test
    void formatZeroReadsClassificationAtCorrectByteOffset() throws Exception {
        Path file = fixture(0, 1, 1);
        List<LasReader.LasPoint> points = LasReader.readPoints(file.toFile(), 10);

        assertEquals(1, points.size());
        assertEquals(-60d, points.get(0).x(), 0.000001d);
        assertEquals(-50d, points.get(0).y(), 0.000001d);
        assertEquals(305d, points.get(0).z(), 0.000001d);
        assertEquals(7, points.get(0).classification());
        assertEquals(1234d, points.get(0).intensity(), 0.000001d);
        assertEquals(0, points.get(0).red());
        assertEquals(1, LasReader.getPointCount(file.toFile()));
    }

    @Test
    void formatTwoReadsRgbImmediatelyAfterTwentyByteBaseRecord() throws Exception {
        Path file = fixture(2, 1, 1);
        LasReader.LasPoint point = LasReader.readPoints(file.toFile(), 10).get(0);

        assertEquals(7, point.classification());
        assertEquals(51000, point.red());
        assertEquals(1024, point.green());
        assertEquals(65000, point.blue());
    }

    @Test
    void formatThreeSkipsEightByteGpsTimeBeforeRgb() throws Exception {
        Path file = fixture(3, 1, 1);
        LasReader.LasPoint point = LasReader.readPoints(file.toFile(), 10).get(0);

        assertEquals(7, point.classification());
        assertEquals(51000, point.red());
        assertEquals(1024, point.green());
        assertEquals(65000, point.blue());
    }

    @Test
    void formatOneSkipsGpsTimeAndKeepsPointAlignment() throws Exception {
        Path file = fixture(1, 2, 2);
        List<LasReader.LasPoint> points = LasReader.readPoints(file.toFile(), 10);

        assertEquals(2, points.size());
        assertEquals(-60d, points.get(0).x(), 0.000001d);
        assertEquals(-59.99d, points.get(1).x(), 0.000001d);
        assertEquals(7, points.get(1).classification());
    }

    @Test
    void truncatedPointDataMustFailRatherThanReturnPartialSuccess() throws Exception {
        Path file = fixture(2, 2, 1);
        assertThrows(IOException.class, () -> LasReader.readPoints(file.toFile(), 2));
    }

    @Test
    void unsupportedAndShortRecordsAreExplicitlyRejected() throws Exception {
        Path file = fixture(2, 1, 1);
        byte[] bytes = Files.readAllBytes(file);

        bytes[104] = 6;
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));

        bytes[104] = 2;
        bytes[105] = 25;
        bytes[106] = 0;
        Files.write(file, bytes);
        assertThrows(IOException.class, () -> LasReader.readHeader(file.toFile()));
    }

    private Path fixture(int format, int declaredPoints, int storedPoints) throws Exception {
        int recordLength = switch (format) {
            case 0 -> 20;
            case 1 -> 28;
            case 2 -> 26;
            case 3 -> 34;
            default -> throw new IllegalArgumentException("Unsupported fixture format");
        };
        int headerSize = 227;
        ByteBuffer bytes = ByteBuffer.allocate(headerSize + (recordLength * storedPoints))
                .order(ByteOrder.LITTLE_ENDIAN);

        bytes.put(new byte[]{'L', 'A', 'S', 'F'});
        bytes.position(24);
        bytes.put((byte) 1);
        bytes.put((byte) 2);
        bytes.position(94);
        bytes.putShort((short) headerSize);
        bytes.putInt(headerSize);
        bytes.position(104);
        bytes.put((byte) format);
        bytes.putShort((short) recordLength);
        bytes.putInt(declaredPoints);

        bytes.position(131);
        bytes.putDouble(0.01d);
        bytes.putDouble(0.01d);
        bytes.putDouble(0.1d);
        bytes.putDouble(-70d);
        bytes.putDouble(-70d);
        bytes.putDouble(5d);

        bytes.position(179);
        bytes.putDouble(-59.99d);
        bytes.putDouble(-60d);
        bytes.putDouble(-50d);
        bytes.putDouble(-50d);
        bytes.putDouble(305d);
        bytes.putDouble(305d);

        bytes.position(headerSize);
        for (int i = 0; i < storedPoints; i++) {
            bytes.putInt(1000 + i);
            bytes.putInt(2000);
            bytes.putInt(3000);
            bytes.putShort((short) 1234);
            bytes.put((byte) 0x11); // packed LAS 1.x return/flag information
            bytes.put((byte) 7);    // classification immediately follows packed flags
            bytes.put((byte) -3);   // scan angle
            bytes.put((byte) 2);    // user data
            bytes.putShort((short) 77);

            if (format == 1 || format == 3) {
                bytes.putDouble(123.5d);
            }
            if (format == 2 || format == 3) {
                bytes.putShort((short) 51000);
                bytes.putShort((short) 1024);
                bytes.putShort((short) 65000);
            }
        }

        Path file = tempDir.resolve("format-" + format + ".las");
        Files.write(file, bytes.array());
        return file;
    }
}
