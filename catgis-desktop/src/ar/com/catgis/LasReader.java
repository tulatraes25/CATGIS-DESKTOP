package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Uncompressed LAS 1.0-1.4 reader for legacy point formats 0-3.
 * LAZ compression and LAS point formats 4-10 require a dedicated decoder.
 */
public final class LasReader {

    private LasReader() {
    }

    public record LasHeader(int versionMajor, int versionMinor, long pointCount,
                            double minX, double minY, double minZ,
                            double maxX, double maxY, double maxZ,
                            double scaleX, double scaleY, double scaleZ,
                            double offsetX, double offsetY, double offsetZ,
                            int pointFormat, int recordLength, int headerSize) {
    }

    public record LasPoint(double x, double y, double z, int classification, double intensity,
                           int red, int green, int blue) {
        public LasPoint(double x, double y, double z, int classification, double intensity) {
            this(x, y, z, classification, intensity, 0, 0, 0);
        }
    }

    public static LasHeader readHeader(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (file.getName().toLowerCase(Locale.ROOT).endsWith(".laz")) {
            throw new IOException("LAZ está comprimido: se requiere un lector PDAL/LAZ certificado.");
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] sig = new byte[4];
            raf.readFully(sig);
            String signature = new String(sig, StandardCharsets.US_ASCII);
            if (!"LASF".equals(signature)) {
                throw new IOException("Not a valid LAS file (bad signature: " + signature + ")");
            }

            raf.seek(24);
            int versionMajor = raf.readUnsignedByte();
            int versionMinor = raf.readUnsignedByte();
            if (versionMajor != 1 || versionMinor > 4) {
                throw new IOException("Unsupported LAS version: " + versionMajor + "." + versionMinor);
            }

            raf.seek(94);
            int headerSize = readUShort(raf);
            int minimumHeaderSize = versionMinor == 4 ? 375
                    : versionMinor == 3 ? 235 : 227;
            if (headerSize < minimumHeaderSize || raf.length() < headerSize) {
                throw new IOException("Invalid or truncated LAS " + versionMajor
                        + "." + versionMinor + " header: expected at least "
                        + minimumHeaderSize + " bytes.");
            }

            raf.seek(96);
            long pointOffset = readUInt(raf);
            if (pointOffset < headerSize || pointOffset > raf.length()) {
                throw new IOException("Invalid LAS point-data offset: " + pointOffset);
            }

            raf.seek(104);
            int pointFormat = raf.readUnsignedByte();
            if (pointFormat > 3) {
                throw new IOException("Unsupported or compressed LAS point format: " + pointFormat
                        + ". Use PDAL or an appropriate decoder.");
            }
            int recordLength = readUShort(raf);
            int minimumRecordLength = switch (pointFormat) {
                case 0 -> 20;
                case 1 -> 28;
                case 2 -> 26;
                case 3 -> 34;
                default -> throw new IOException("Unsupported LAS point format.");
            };
            if (recordLength < minimumRecordLength) {
                throw new IOException("LAS point record is shorter than its declared format: "
                        + recordLength + " < " + minimumRecordLength);
            }

            long legacyPointCount = readUInt(raf);
            long pointCount = legacyPointCount;
            if (versionMinor >= 4) {
                raf.seek(247);
                long extendedPointCount = readULong(raf);
                if (extendedPointCount < 0) {
                    throw new IOException("LAS point count exceeds signed 64-bit range.");
                }
                if (extendedPointCount > 0) {
                    pointCount = extendedPointCount;
                }
            }

            raf.seek(131);
            double scaleX = readDouble(raf);
            double scaleY = readDouble(raf);
            double scaleZ = readDouble(raf);

            double offsetX = readDouble(raf);
            double offsetY = readDouble(raf);
            double offsetZ = readDouble(raf);

            raf.seek(179);
            double maxX = readDouble(raf);
            double minX = readDouble(raf);
            double maxY = readDouble(raf);
            double minY = readDouble(raf);
            double maxZ = readDouble(raf);
            double minZ = readDouble(raf);

            if (!Double.isFinite(scaleX) || !Double.isFinite(scaleY)
                    || !Double.isFinite(scaleZ)
                    || scaleX <= 0 || scaleY <= 0 || scaleZ <= 0
                    || !Double.isFinite(offsetX) || !Double.isFinite(offsetY)
                    || !Double.isFinite(offsetZ)
                    || !Double.isFinite(minX) || !Double.isFinite(maxX)
                    || !Double.isFinite(minY) || !Double.isFinite(maxY)
                    || !Double.isFinite(minZ) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IOException("LAS: escala, offset o extensión inválidos.");
            }
            // Fail closed before any geometry allocation or partial preview:
            // even a caller requesting only a prefix must not silently accept
            // an authoritative header claiming records beyond EOF.
            try {
                long expectedBytes = Math.multiplyExact(pointCount, (long) recordLength);
                long requiredFileBytes = Math.addExact(pointOffset, expectedBytes);
                if (requiredFileBytes > raf.length()) {
                    throw new IOException("LAS truncado: faltan puntos declarados en el encabezado.");
                }
            } catch (ArithmeticException overflow) {
                throw new IOException("LAS: conteo de puntos excede el rango de archivo.", overflow);
            }

            return new LasHeader(versionMajor, versionMinor, pointCount,
                    minX, minY, minZ, maxX, maxY, maxZ,
                    scaleX, scaleY, scaleZ, offsetX, offsetY, offsetZ,
                    pointFormat, recordLength, headerSize);
        }
    }

    public static List<LasPoint> readPoints(File file, int maxPoints) throws IOException {
        LasHeader header = readHeader(file);
        return readPoints(file, maxPoints, header);
    }

    public static List<LasPoint> readPoints(File file, int maxPoints, LasHeader header) throws IOException {
        if (maxPoints < 0) {
            throw new IllegalArgumentException("maxPoints must be nonnegative.");
        }
        if (header == null) {
            throw new IllegalArgumentException("LAS header is required.");
        }
        List<LasPoint> points = new ArrayList<>();
        long pointsToRead = Math.min(header.pointCount(), maxPoints);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(96);
            long pointOffset = readUInt(raf);
            if (pointOffset < header.headerSize() || pointOffset > raf.length()) {
                throw new IOException("Invalid LAS point-data offset: " + pointOffset);
            }
            raf.seek(pointOffset);

            for (long i = 0; i < pointsToRead; i++) {
                // 20-byte base record: XYZ(12), intensity(2), return flags(1),
                // classification(1), scan angle(1), user data(1), source ID(2).
                int rawX = readInt(raf);
                int rawY = readInt(raf);
                int rawZ = readInt(raf);

                double x = rawX * header.scaleX() + header.offsetX();
                double y = rawY * header.scaleY() + header.offsetY();
                double z = rawZ * header.scaleZ() + header.offsetZ();

                int intensity = readUShort(raf);
                raf.readUnsignedByte(); // packed return number/return count/flags
                int classification = raf.readUnsignedByte();
                raf.readByte();         // scan angle rank
                raf.readUnsignedByte(); // user data
                readUShort(raf);        // point source ID

                int bytesRead = 20;
                if (header.pointFormat() == 1 || header.pointFormat() == 3) {
                    readDouble(raf);    // GPS time precedes RGB on point format 3
                    bytesRead += 8;
                }

                int red = 0;
                int green = 0;
                int blue = 0;
                if (header.pointFormat() == 2 || header.pointFormat() == 3) {
                    red = readUShort(raf);
                    green = readUShort(raf);
                    blue = readUShort(raf);
                    bytesRead += 6;
                }

                int remaining = header.recordLength() - bytesRead;
                if (remaining < 0 || raf.skipBytes(remaining) != remaining) {
                    throw new IOException("Invalid or truncated LAS point record at index " + i);
                }
                points.add(new LasPoint(x, y, z, classification, intensity, red, green, blue));
            }
        }
        return points;
    }

    public static long getPointCount(File file) throws IOException {
        return readHeader(file).pointCount();
    }

    public static org.locationtech.jts.geom.Envelope getBounds(File file) throws IOException {
        LasHeader h = readHeader(file);
        return new org.locationtech.jts.geom.Envelope(h.minX(), h.maxX(), h.minY(), h.maxY());
    }

    private static int readUShort(RandomAccessFile raf) throws IOException {
        int lo = raf.readUnsignedByte();
        int hi = raf.readUnsignedByte();
        return lo | (hi << 8);
    }

    private static int readInt(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedByte()
                | (raf.readUnsignedByte() << 8)
                | (raf.readUnsignedByte() << 16)
                | (raf.readUnsignedByte() << 24);
    }

    private static long readUInt(RandomAccessFile raf) throws IOException {
        return (raf.readUnsignedByte() & 0xFFL)
                | ((raf.readUnsignedByte() & 0xFFL) << 8)
                | ((raf.readUnsignedByte() & 0xFFL) << 16)
                | ((raf.readUnsignedByte() & 0xFFL) << 24);
    }

    private static long readULong(RandomAccessFile raf) throws IOException {
        return (raf.readUnsignedByte() & 0xFFL)
                | ((raf.readUnsignedByte() & 0xFFL) << 8)
                | ((raf.readUnsignedByte() & 0xFFL) << 16)
                | ((raf.readUnsignedByte() & 0xFFL) << 24)
                | ((raf.readUnsignedByte() & 0xFFL) << 32)
                | ((raf.readUnsignedByte() & 0xFFL) << 40)
                | ((raf.readUnsignedByte() & 0xFFL) << 48)
                | ((raf.readUnsignedByte() & 0xFFL) << 56);
    }

    private static double readDouble(RandomAccessFile raf) throws IOException {
        return Double.longBitsToDouble(readULong(raf));
    }
}
