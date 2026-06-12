package ar.com.catgis;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * LAS LiDAR file reader with scale/offset coordinate transformation.
 * <p>
 * Supports LAS 1.0 through 1.4, point formats 0-3 (with RGB),
 * variable header sizes, and proper coordinate scaling.
 * </p>
 * <p>
 * For production LiDAR processing with LAZ compression or waveform data,
 * use PDAL or LASlib.
 * </p>
 */
public final class LasReader {

    private LasReader() {}

    public record LasHeader(int versionMajor, int versionMinor, long pointCount,
                             double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ,
                             double scaleX, double scaleY, double scaleZ,
                             double offsetX, double offsetY, double offsetZ,
                             int pointFormat, int recordLength, int headerSize) {}

    public record LasPoint(double x, double y, double z, int classification, double intensity,
                            int red, int green, int blue) {
        public LasPoint(double x, double y, double z, int classification, double intensity) {
            this(x, y, z, classification, intensity, 0, 0, 0);
        }
    }

    /**
     * Read the LAS file header with scale/offset support.
     */
    public static LasHeader readHeader(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] sig = new byte[4];
            raf.read(sig);
            String signature = new String(sig);
            if (!signature.equals("LASF")) {
                throw new Exception("Not a valid LAS file (bad signature: " + signature + ")");
            }

            // Version
            raf.seek(24);
            int versionMajor = raf.read();
            int versionMinor = raf.read();

            // Header size (variable starting from LAS 1.1)
            raf.seek(94);
            int headerSize = readUShort(raf);
            if (headerSize <= 0) headerSize = 375; // default for LAS 1.0

            // Offset to point data
            raf.seek(96);
            long offsetToPoints = readUInt(raf);

            // Point format and record length
            raf.seek(104);
            int pointFormat = raf.read() & 0xFF;
            int recordLength = readUShort(raf);

            // Point count (different positions for different versions)
            long pointCount;
            if (versionMajor == 1 && versionMinor >= 4) {
                raf.seek(247);
                pointCount = readULong(raf);
            } else {
                raf.seek(107);
                pointCount = readUInt(raf);
            }

            // Scale factors
            raf.seek(131);
            double scaleX = readDouble(raf);
            double scaleY = readDouble(raf);
            double scaleZ = readDouble(raf);

            // Offsets
            double offsetX = readDouble(raf);
            double offsetY = readDouble(raf);
            double offsetZ = readDouble(raf);

            // Bounds
            raf.seek(179);
            double maxX = readDouble(raf);
            double minX = readDouble(raf);
            double maxY = readDouble(raf);
            double minY = readDouble(raf);
            double maxZ = readDouble(raf);
            double minZ = readDouble(raf);

            return new LasHeader(versionMajor, versionMinor, pointCount,
                    minX, minY, minZ, maxX, maxY, maxZ,
                    scaleX, scaleY, scaleZ, offsetX, offsetY, offsetZ,
                    pointFormat, recordLength, headerSize);
        }
    }

    /**
     * Read points from a LAS file with proper coordinate scaling.
     */
    public static List<LasPoint> readPoints(File file, int maxPoints) throws Exception {
        LasHeader header = readHeader(file);
        return readPoints(file, maxPoints, header);
    }

    /**
     * Read points using an already-parsed header.
     */
    public static List<LasPoint> readPoints(File file, int maxPoints, LasHeader header) throws Exception {
        List<LasPoint> points = new ArrayList<>();
        long pointsToRead = Math.min(header.pointCount(), maxPoints);

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Seek to point data using header offset
            raf.seek(0);
            raf.seek(96);
            long offsetToPoints = readUInt(raf);

            if (offsetToPoints <= 0 || offsetToPoints < header.headerSize()) {
                offsetToPoints = header.headerSize();
            }

            raf.seek(offsetToPoints);

            for (long i = 0; i < pointsToRead; i++) {
                try {
                    // X, Y, Z as int32 (scaled integers)
                    int rawX = readInt(raf);
                    int rawY = readInt(raf);
                    int rawZ = readInt(raf);

                    // Apply scale/offset
                    double x = rawX * header.scaleX() + header.offsetX();
                    double y = rawY * header.scaleY() + header.offsetY();
                    double z = rawZ * header.scaleZ() + header.offsetZ();

                    // Intensity
                    int intensity = readUShort(raf);

                    // Flags
                    int returnNumber = raf.read() & 0x07;
                    int numberOfReturns = raf.read() & 0x07;

                    // Scan direction and edge flags
                    raf.read(); // scan direction flag
                    raf.read(); // edge of flight line

                    // Classification
                    int classification = raf.read() & 0xFF;

                    // Scan angle rank
                    raf.read(); // raw scan angle
                    raf.read(); // user data
                    raf.read(); // point source ID

                    // RGB (formats 2 and 3)
                    int red = 0, green = 0, blue = 0;
                    if (header.pointFormat() == 2 || header.pointFormat() == 3) {
                        red = readUShort(raf);
                        green = readUShort(raf);
                        blue = readUShort(raf);
                    }

                    // Skip remaining bytes based on point format
                    int bytesRead = 20; // base: 3*4 (xyz) + 2 (intensity) + 7 bytes flags
                    if (header.pointFormat() == 2 || header.pointFormat() == 3) {
                        bytesRead += 6; // 3 * 2 bytes RGB
                    }
                    int remaining = header.recordLength() - bytesRead;
                    if (remaining > 0) raf.skipBytes(remaining);

                    points.add(new LasPoint(x, y, z, classification, intensity, red, green, blue));
                } catch (Exception e) {
                    break;
                }
            }
        }

        return points;
    }

    /**
     * Get point count from a LAS file.
     */
    public static long getPointCount(File file) throws Exception {
        return readHeader(file).pointCount();
    }

    /**
     * Get bounding box from a LAS file.
     */
    public static org.locationtech.jts.geom.Envelope getBounds(File file) throws Exception {
        LasHeader h = readHeader(file);
        return new org.locationtech.jts.geom.Envelope(h.minX(), h.minY(), h.maxX(), h.maxY());
    }

    // --- I/O helpers ---

    private static int readUShort(RandomAccessFile raf) throws IOException {
        byte[] b = new byte[2];
        raf.read(b);
        return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8);
    }

    private static int readInt(RandomAccessFile raf) throws IOException {
        byte[] b = new byte[4];
        raf.read(b);
        return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8) | ((b[2] & 0xFF) << 16) | ((b[3] & 0xFF) << 24);
    }

    private static long readUInt(RandomAccessFile raf) throws IOException {
        byte[] b = new byte[4];
        raf.read(b);
        return (b[0] & 0xFFL) | ((b[1] & 0xFFL) << 8) | ((b[2] & 0xFFL) << 16) | ((b[3] & 0xFFL) << 24);
    }

    private static long readULong(RandomAccessFile raf) throws IOException {
        byte[] b = new byte[8];
        raf.read(b);
        return (b[0] & 0xFFL) | ((b[1] & 0xFFL) << 8) | ((b[2] & 0xFFL) << 16) | ((b[3] & 0xFFL) << 24)
                | ((b[4] & 0xFFL) << 32) | ((b[5] & 0xFFL) << 40) | ((b[6] & 0xFFL) << 48) | ((b[7] & 0xFFL) << 56);
    }

    private static double readDouble(RandomAccessFile raf) throws IOException {
        return Double.longBitsToDouble(readULong(raf));
    }
}
