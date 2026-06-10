package ar.com.catgis;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic LAS LiDAR file reader.
 * Reads point cloud data from LAS format files.
 * NOTE: This is a SIMPLIFIED parser that reads basic header fields and
 * point coordinates. It does NOT handle:
 * - Scale/offset transformations (uses raw coordinate values)
 * - Variable record lengths per point format
 * - Variable header sizes (uses fixed 375 bytes)
 * - LAZ compression (requires LASzip)
 * - Waveform data
 * - RGB color from point formats 2/3
 *
 * For production LiDAR processing, use PDAL or LASlib.
 */
public final class LasReader {

    private LasReader() {}

    public record LasHeader(int versionMajor, int versionMinor, int pointCount,
                             double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ,
                             int pointFormat, int recordLength) {}

    public record LasPoint(double x, double y, double z, int classification, double intensity) {}

    /**
     * Read the LAS file header.
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

            raf.seek(24);
            int versionMajor = raf.read();
            int versionMinor = raf.read();

            raf.seek(96);
            int pointFormat = raf.read();
            int recordLength = readUShort(raf);

            raf.seek(179);
            long pointCount = readUInt(raf);

            raf.seek(177);
            double minX = readDouble(raf);
            double minY = readDouble(raf);
            double maxX = readDouble(raf);
            double maxY = readDouble(raf);
            double minZ = readDouble(raf);
            double maxZ = readDouble(raf);

            return new LasHeader(versionMajor, versionMinor, (int) pointCount,
                    minX, minY, minZ, maxX, maxY, maxZ, pointFormat, recordLength);
        }
    }

    /**
     * Read points from a LAS file.
     * Only reads a subset of points for performance.
     */
    public static List<LasPoint> readPoints(File file, int maxPoints) throws Exception {
        LasHeader header = readHeader(file);
        List<LasPoint> points = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Points start after header (375 bytes for LAS 1.2+)
            long dataOffset = 375;
            long pointsToRead = Math.min(header.pointCount(), maxPoints);

            raf.seek(dataOffset);

            for (long i = 0; i < pointsToRead; i++) {
                try {
                    double x = raf.readDouble();
                    double y = raf.readDouble();
                    double z = raf.readDouble();

                    int intensity = readUShort(raf);
                    raf.readByte(); // return number
                    raf.readByte(); // number of returns
                    int classification = raf.readByte();

                    // Skip remaining bytes based on point format
                    int remaining = header.recordLength() - 26;
                    if (remaining > 0) raf.skipBytes(remaining);

                    points.add(new LasPoint(x, y, z, classification, intensity));
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

    private static int readUShort(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedShort();
    }

    private static long readUInt(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedShort() | ((long) raf.readUnsignedShort() << 16);
    }

    private static double readDouble(RandomAccessFile raf) throws IOException {
        return raf.readDouble();
    }
}
