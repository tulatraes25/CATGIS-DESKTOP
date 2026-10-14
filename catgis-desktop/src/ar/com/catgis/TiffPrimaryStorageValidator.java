package ar.com.catgis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Validates the actual primary TIFF IFD strip/tile storage pointers before WCS
 * publication. ImageIO's normalized image metadata can omit offsets entirely,
 * so inspecting TIFFDirectory created from that metadata is insufficient.
 *
 * Reads only the header, bounded IFD entries, and numeric offsets/counts.
 * Does not decompress image data or follow external filenames.
 */
final class TiffPrimaryStorageValidator {

    private static final int MAX_IFD_ENTRIES = 8192;
    private static final long MAX_DATA_BLOCKS = 2_000_000L;

    private TiffPrimaryStorageValidator() {}

    static void validate(Path path) throws IOException {
        try (FileChannel file = FileChannel.open(path,
                StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            long fileLength = file.size();
            if (fileLength < 8L) {
                throw new IOException("WCS TIFF header too short for primary IFD.");
            }
            ByteBuffer header = readAt(file, 0L,
                    fileLength >= 16L ? 16 : 8, ByteOrder.BIG_ENDIAN, fileLength);
            int first = Byte.toUnsignedInt(header.get(0));
            int second = Byte.toUnsignedInt(header.get(1));
            ByteOrder order;
            if (first == 'I' && second == 'I') {
                order = ByteOrder.LITTLE_ENDIAN;
            } else if (first == 'M' && second == 'M') {
                order = ByteOrder.BIG_ENDIAN;
            } else {
                throw new IOException("WCS TIFF has invalid byte order marker.");
            }
            header.order(order);
            int signature = Short.toUnsignedInt(header.getShort(2));
            boolean bigTiff = signature == 43;
            if (signature != 42 && !bigTiff) {
                throw new IOException("WCS TIFF has unknown IFD format.");
            }
            if (bigTiff && (fileLength < 16L
                    || Short.toUnsignedInt(header.getShort(4)) != 8
                    || Short.toUnsignedInt(header.getShort(6)) != 0)) {
                throw new IOException("WCS BigTIFF header is inconsistent.");
            }
            long ifdOffset = bigTiff
                    ? header.getLong(8) : Integer.toUnsignedLong(header.getInt(4));
            int countBytes = bigTiff ? 8 : 2;
            ByteBuffer ifdCount = readAt(file, ifdOffset, countBytes, order, fileLength);
            long entries = bigTiff ? ifdCount.getLong()
                    : Short.toUnsignedInt(ifdCount.getShort());
            if (entries <= 0 || entries > MAX_IFD_ENTRIES) {
                throw new IOException("WCS TIFF primary IFD entry count outside safe bounds.");
            }
            int entryBytes = bigTiff ? 20 : 12;
            long entriesStart = ifdOffset + countBytes;
            if (entriesStart < ifdOffset || entriesStart > fileLength
                    || entries > (fileLength - entriesStart) / entryBytes) {
                throw new IOException("WCS TIFF primary IFD exceeds staged file.");
            }

            NumericField stripOffsets = null;
            NumericField stripBytes = null;
            NumericField tileOffsets = null;
            NumericField tileBytes = null;
            for (int i = 0; i < entries; i++) {
                long entryPosition = entriesStart + (long) i * entryBytes;
                ByteBuffer entry = readAt(file, entryPosition,
                        entryBytes, order, fileLength);
                int tag = Short.toUnsignedInt(entry.getShort(0));
                if (tag != 273 && tag != 279 && tag != 324 && tag != 325) {
                    continue;
                }
                NumericField field = numericField(entry, entryPosition,
                        bigTiff, fileLength);
                switch (tag) {
                    case 273 -> {
                        if (stripOffsets != null) throw new IOException("Duplicate TIFF strip offsets.");
                        stripOffsets = field;
                    }
                    case 279 -> {
                        if (stripBytes != null) throw new IOException("Duplicate TIFF strip sizes.");
                        stripBytes = field;
                    }
                    case 324 -> {
                        if (tileOffsets != null) throw new IOException("Duplicate TIFF tile offsets.");
                        tileOffsets = field;
                    }
                    case 325 -> {
                        if (tileBytes != null) throw new IOException("Duplicate TIFF tile sizes.");
                        tileBytes = field;
                    }
                    default -> throw new IOException("Unexpected TIFF storage tag.");
                }
            }

            boolean hasStrips = stripOffsets != null || stripBytes != null;
            boolean hasTiles = tileOffsets != null || tileBytes != null;
            if (hasStrips == hasTiles) {
                throw new IOException("WCS TIFF must declare exactly one strip or tile data layout.");
            }
            NumericField offsets = hasStrips ? stripOffsets : tileOffsets;
            NumericField lengths = hasStrips ? stripBytes : tileBytes;
            if (offsets == null || lengths == null || offsets.count != lengths.count) {
                throw new IOException("WCS TIFF storage offset/count arrays do not match.");
            }
            for (long i = 0L; i < offsets.count; i++) {
                long start = fieldValue(file, offsets, i, order, fileLength);
                long length = fieldValue(file, lengths, i, order, fileLength);
                if (start < 8L || length <= 0L || start > fileLength
                        || length > fileLength - start) {
                    throw new IOException("WCS TIFF strip/tile block extends beyond the raster.");
                }
            }
        }
    }

    private static NumericField numericField(ByteBuffer entry, long entryPosition,
                                             boolean bigTiff, long fileLength)
            throws IOException {
        int type = Short.toUnsignedInt(entry.getShort(2));
        int width = switch (type) {
            case 3 -> 2;   // TIFF_SHORT
            case 4 -> 4;   // TIFF_LONG
            case 16 -> 8;  // BigTIFF_LONG8
            default -> throw new IOException("Unsupported TIFF storage offset/count type.");
        };
        long count = bigTiff ? entry.getLong(4)
                : Integer.toUnsignedLong(entry.getInt(4));
        if (count <= 0L || count > MAX_DATA_BLOCKS) {
            throw new IOException("WCS TIFF data block count outside safety budget.");
        }
        int valueOffset = bigTiff ? 12 : 8;
        long bytes = count * width;
        long position;
        if (bytes <= (bigTiff ? 8L : 4L)) {
            position = entryPosition + valueOffset;
        } else {
            position = bigTiff ? entry.getLong(valueOffset)
                    : Integer.toUnsignedLong(entry.getInt(valueOffset));
        }
        if (position < 0L || position > fileLength
                || bytes > fileLength - position) {
            throw new IOException("WCS TIFF offset/count field points outside raster.");
        }
        return new NumericField(count, width, position);
    }

    private static long fieldValue(FileChannel file, NumericField field, long index,
                                   ByteOrder order, long fileLength) throws IOException {
        long position = field.position + index * field.width;
        ByteBuffer bytes = readAt(file, position, field.width, order, fileLength);
        return switch (field.width) {
            case 2 -> Short.toUnsignedInt(bytes.getShort());
            case 4 -> Integer.toUnsignedLong(bytes.getInt());
            case 8 -> bytes.getLong(); // Negative signed long means unrepresentable unsigned64.
            default -> throw new IOException("Unexpected TIFF offset numeric width.");
        };
    }

    private static ByteBuffer readAt(FileChannel file, long offset, int length,
                                     ByteOrder order, long fileLength) throws IOException {
        if (offset < 0L || length <= 0 || offset > fileLength - length) {
            throw new IOException("TIFF IFD byte range outside the staged file.");
        }
        ByteBuffer data = ByteBuffer.allocate(length).order(order);
        long current = offset;
        while (data.hasRemaining()) {
            int count = file.read(data, current);
            if (count <= 0) {
                throw new IOException("Unexpected EOF while reading TIFF IFD.");
            }
            current += count;
        }
        data.flip();
        return data;
    }

    private record NumericField(long count, int width, long position) {}
}
