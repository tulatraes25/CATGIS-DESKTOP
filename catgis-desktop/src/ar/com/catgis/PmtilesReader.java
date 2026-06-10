package ar.com.catgis;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * PMTiles reader - modern single-file tile archive format.
 * Spec: https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md
 */
public final class PmtilesReader {

    private PmtilesReader() {}

    public record PmtilesHeader(int version, int numTiles, int zoomMin, int zoomMax,
                                 long dataOffset, int compression, int tileType) {}
    public record TileEntry(int z, int x, int y, long offset, int length, int compression) {}

    public static final int HEADER_SIZE = 127;
    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_GZIP = 1;
    public static final int COMPRESSION_ZSTD = 2;
    public static final int TILE_TYPE_MVT = 1;
    public static final int TILE_TYPE_PNG = 2;
    public static final int TILE_TYPE_JPEG = 3;
    public static final int TILE_TYPE_WEBP = 4;

    /**
     * Read PMTiles header from file.
     */
    public static PmtilesHeader readHeader(File file) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] headerBytes = new byte[HEADER_SIZE];
            raf.readFully(headerBytes);

            ByteBuffer buf = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN);

            // Check magic bytes: 0x4d50 (PM)
            int magic = buf.getInt(0);
            if ((magic & 0xFFFF) != 0x4D50) {
                throw new Exception("Not a valid PMTiles file (bad magic bytes)");
            }

            int version = buf.getInt(4);
            int numTiles = buf.getInt(12);
            int zoomMin = buf.getInt(16);
            int zoomMax = buf.getInt(20);
            long dataOffset = buf.getLong(24);
            int compression = buf.getInt(32);
            int tileType = buf.getInt(36);

            return new PmtilesHeader(version, numTiles, zoomMin, zoomMax,
                    dataOffset, compression, tileType);
        }
    }

    /**
     * Read tile entries from PMTiles directory.
     */
    public static List<TileEntry> readDirectory(File file, PmtilesHeader header) throws Exception {
        List<TileEntry> entries = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Skip header
            raf.seek(HEADER_SIZE);

            // Read directory entries
            byte[] entryBuf = new byte[20]; // Each entry is 20 bytes
            for (int i = 0; i < header.numTiles(); i++) {
                if (raf.read(entryBuf) < 20) break;

                ByteBuffer buf = ByteBuffer.wrap(entryBuf).order(ByteOrder.LITTLE_ENDIAN);
                int z = buf.getInt(0);
                int x = buf.getInt(4);
                int y = buf.getInt(8);
                long offset = buf.getLong(12);
                int length = buf.getInt(16);

                entries.add(new TileEntry(z, x, y, offset, length, header.compression()));
            }
        }
        return entries;
    }

    /**
     * Read a single tile by z/x/y coordinates.
     */
    public static byte[] readTile(File file, List<TileEntry> entries, int z, int x, int y) throws Exception {
        for (TileEntry entry : entries) {
            if (entry.z() == z && entry.x() == x && entry.y() == y) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    raf.seek(entry.offset());
                    byte[] tileData = new byte[entry.length()];
                    raf.readFully(tileData);
                    return tileData;
                }
            }
        }
        return null;
    }

    /**
     * Get statistics about a PMTiles file.
     */
    public static String getStats(File file) throws Exception {
        PmtilesHeader header = readHeader(file);
        List<TileEntry> entries = readDirectory(file, header);

        int[] zoomCounts = new int[header.zoomMax() + 1];
        for (TileEntry e : entries) {
            if (e.z() >= 0 && e.z() < zoomCounts.length) zoomCounts[e.z()]++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== PMTiles Stats ===\n");
        sb.append("Version: ").append(header.version()).append("\n");
        sb.append("Total tiles: ").append(header.numTiles()).append("\n");
        sb.append("Zoom range: ").append(header.zoomMin()).append(" - ").append(header.zoomMax()).append("\n");
        sb.append("File size: ").append(file.length() / 1024).append(" KB\n");
        sb.append("Compression: ").append(compressionName(header.compression())).append("\n");
        sb.append("Tile type: ").append(tileTypeName(header.tileType())).append("\n");
        sb.append("\nTiles per zoom level:\n");
        for (int z = header.zoomMin(); z <= header.zoomMax(); z++) {
            if (zoomCounts[z] > 0) {
                sb.append("  z").append(z).append(": ").append(zoomCounts[z]).append(" tiles\n");
            }
        }
        return sb.toString();
    }

    private static String compressionName(int code) {
        return switch (code) {
            case COMPRESSION_NONE -> "None";
            case COMPRESSION_GZIP -> "GZIP";
            case COMPRESSION_ZSTD -> "ZSTD";
            default -> "Unknown (" + code + ")";
        };
    }

    private static String tileTypeName(int code) {
        return switch (code) {
            case TILE_TYPE_MVT -> "MVT (vector)";
            case TILE_TYPE_PNG -> "PNG";
            case TILE_TYPE_JPEG -> "JPEG";
            case TILE_TYPE_WEBP -> "WebP";
            default -> "Unknown (" + code + ")";
        };
    }
}
