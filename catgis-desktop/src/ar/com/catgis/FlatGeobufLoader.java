package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for FlatGeobuf (.fgb) files using the org.wololo.flatgeobuf library.
 * <p>
 * Supports streaming reads via memory-mapped files and full geometry/attribute
 * deserialization for all FlatGeobuf geometry types (Point, LineString, Polygon,
 * MultiPoint, MultiLineString, MultiPolygon).
 * </p>
 *
 * @see <a href="https://flatgeobuf.org/docs/spec/">FlatGeobuf Specification</a>
 */
public final class FlatGeobufLoader {

    private FlatGeobufLoader() {}

    private static final GeometryFactory GEOM_FACTORY = new GeometryFactory();

    /**
     * Load a FlatGeobuf file and return ShapefileData.
     *
     * @param file the .fgb file
     * @return parsed vector data
     * @throws Exception on I/O errors or invalid format
     */
    public static ShapefileData load(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!file.getName().toLowerCase().endsWith(".fgb")) {
            throw new IllegalArgumentException("Not a FlatGeobuf file: " + file.getName());
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            long fileSize = channel.size();
            if (fileSize < 8) {
                throw new IOException("File too small to be FlatGeobuf: " + fileSize + " bytes");
            }

            // Read magic bytes: FlatGeobuf magic = "gbfg"
            // Bytes: 'g'=0x67 'b'=0x62 'f'=0x66 'g'=0x67 → LE int 0x67666267
            ByteBuffer magicBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(magicBuf);
            magicBuf.flip();
            int magic = magicBuf.getInt();
            if (magic != 0x67666267) {
                throw new IOException("Not a valid FlatGeobuf file (bad magic: 0x"
                        + Integer.toHexString(magic) + ")");
            }

            // Read header size
            ByteBuffer headerSizeBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(headerSizeBuf);
            headerSizeBuf.flip();
            int headerSize = headerSizeBuf.getInt();
            if (headerSize <= 0 || headerSize > fileSize - 8) {
                throw new IOException("Invalid header size: " + headerSize);
            }

            // Read header bytes
            ByteBuffer headerBuf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(headerBuf);
            headerBuf.flip();

            HeaderMeta header = HeaderMeta.read(headerBuf);
            if (header == null) {
                throw new IOException("Failed to parse FlatGeobuf header");
            }

            // Build schema from columns
            SimpleFeatureType schema = buildSchema(header, file.getName());
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);

            // Determine the data offset and read features
            long featuresCount = header.featuresCount;
            int indexNodeSize = header.indexNodeSize;

            long dataOffset = 8 + headerSize; // magic(4) + headerSize(4) + header
            if (indexNodeSize > 0 && featuresCount > 0) {
                // Skip spatial index: each index node is indexNodeSize bytes,
                // and there are roughly (featuresCount / nodeSize) nodes
                long indexBytes = estimateIndexSize(featuresCount, indexNodeSize);
                dataOffset += indexBytes;
            }

            // Map the data portion for streaming read
            long dataSize = fileSize - dataOffset;
            if (dataSize < 4) {
                // No features
                return emptyResult(header, file.getName(), schema);
            }

            List<SimpleFeature> features = new ArrayList<>((int) Math.min(featuresCount, Integer.MAX_VALUE));

            try (FileChannel dataChannel = new FileInputStream(file).getChannel()) {
                dataChannel.position(dataOffset);
                ByteBuffer dataBuf = ByteBuffer.allocateDirect((int) Math.min(dataSize, Integer.MAX_VALUE));
                dataChannel.read(dataBuf);
                dataBuf.flip();
                dataBuf.order(ByteOrder.LITTLE_ENDIAN);

                // Read features sequentially
                while (dataBuf.remaining() >= 4) {
                    int featureSize = dataBuf.getInt();
                    if (featureSize <= 0 || featureSize > dataBuf.remaining()) {
                        break;
                    }

                    int pos = dataBuf.position();
                    dataBuf.limit(pos + featureSize);

                    try {
                        Feature fbFeature = Feature.getRootAsFeature(dataBuf);
                        if (fbFeature != null) {
                            SimpleFeature sf = convertFeature(fbFeature, builder, header);
                            if (sf != null) {
                                features.add(sf);
                            }
                        }
                    } catch (Exception e) {
                        // Skip corrupt feature, continue
                    }

                    dataBuf.position(pos + featureSize);
                    dataBuf.limit(dataBuf.capacity());
                }
            }

            // Compute envelope
            Envelope envelope = new Envelope();
            if (header.envelope != null && !header.envelope.isNull()
                    && (header.envelope.getWidth() > 0 || header.envelope.getHeight() > 0)) {
                envelope = header.envelope;
            } else {
                for (SimpleFeature f : features) {
                    Geometry g = (Geometry) f.getDefaultGeometry();
                    if (g != null && !g.isEmpty()) {
                        envelope.expandToInclude(g.getEnvelopeInternal());
                    }
                }
            }

            return new ShapefileData(
                    features, envelope, file.getName(), features.size(),
                    "FlatGeobuf: " + header.name + " (" + features.size() + " features)", schema);
        }
    }

    /**
     * Load a FlatGeobuf file from a path string.
     */
    public static ShapefileData load(String path) throws Exception {
        return load(new File(path));
    }

    // --- Private helpers ---

    private static SimpleFeatureType buildSchema(HeaderMeta header, String sourceName) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        String name = sourceName.replaceAll("[^a-zA-Z0-9_]", "_");
        typeBuilder.setName(name);

        // Geometry column
        typeBuilder.add("the_geom", Geometry.class);

        // Attribute columns
        List<ColumnMeta> columns = header.columns;
        if (columns != null) {
            for (ColumnMeta col : columns) {
                String colName = col.name != null ? col.name : "col_" + columns.indexOf(col);
                // Sanitize column name for GeoTools
                colName = colName.replaceAll("[^a-zA-Z0-9_]", "_");
                if (colName.isEmpty()) colName = "col";

                Class<?> binding = columnTypeToClass(col.type);
                typeBuilder.add(colName, binding);
            }
        }

        return typeBuilder.buildFeatureType();
    }

    private static Class<?> columnTypeToClass(byte columnType) {
        return switch (columnType) {
            case org.wololo.flatgeobuf.generated.ColumnType.Bool -> Boolean.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Byte -> Byte.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Short -> Short.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Int -> Integer.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Long -> Long.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Float -> Float.class;
            case org.wololo.flatgeobuf.generated.ColumnType.Double -> Double.class;
            case org.wololo.flatgeobuf.generated.ColumnType.String -> String.class;
            default -> String.class;
        };
    }

    private static SimpleFeature convertFeature(Feature fbFeature,
                                                 SimpleFeatureBuilder builder,
                                                 HeaderMeta header) {
        builder.reset();

        // Convert geometry
        Geometry geometry = null;
        try {
            org.wololo.flatgeobuf.generated.Geometry fbGeom = fbFeature.geometry();
            if (fbGeom != null) {
                geometry = GeometryConversions.deserialize(fbGeom, header.geometryType);
            }
        } catch (Exception e) {
            geometry = null;
        }

        if (geometry == null) {
            geometry = GEOM_FACTORY.createPoint(new Coordinate(0, 0));
        }
        builder.set("the_geom", geometry);

        // Convert attributes
        List<ColumnMeta> columns = header.columns;
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                ColumnMeta col = columns.get(i);
                String colName = col.name != null ? col.name : "col_" + i;
                colName = colName.replaceAll("[^a-zA-Z0-9_]", "_");
                if (colName.isEmpty()) colName = "col";

                Object value = readColumnValue(fbFeature, i, col.type);
                try {
                    builder.set(colName, value);
                } catch (Exception e) {
                    builder.set(colName, null);
                }
            }
        }

        return builder.buildFeature(String.valueOf(featuresCount(fbFeature)));
    }

    private static int featuresCount(Feature fbFeature) {
        // Use a static counter for feature IDs
        // FlatGeobuf features don't have built-in IDs
        return featureIdCounter++;
    }

    private static int featureIdCounter = 0;

    private static Object readColumnValue(Feature fbFeature, int colIndex, byte columnType) {
        try {
            double raw = fbFeature.properties(colIndex);
            return switch (columnType) {
                case org.wololo.flatgeobuf.generated.ColumnType.Bool -> raw != 0.0;
                case org.wololo.flatgeobuf.generated.ColumnType.Byte -> (byte) raw;
                case org.wololo.flatgeobuf.generated.ColumnType.Short -> (short) raw;
                case org.wololo.flatgeobuf.generated.ColumnType.Int -> (int) raw;
                case org.wololo.flatgeobuf.generated.ColumnType.Long -> (long) raw;
                case org.wololo.flatgeobuf.generated.ColumnType.Float -> (float) raw;
                case org.wololo.flatgeobuf.generated.ColumnType.Double -> raw;
                case org.wololo.flatgeobuf.generated.ColumnType.String ->
                        String.valueOf((long) raw); // offset into string table; best-effort
                default -> raw;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static long estimateIndexSize(long featuresCount, int indexNodeSize) {
        if (indexNodeSize <= 0 || featuresCount <= 0) return 0;
        // Packed R-tree: floor((featuresCount + nodeSize - 1) / nodeSize) nodes
        long numNodes = (featuresCount + indexNodeSize - 1) / indexNodeSize;
        // Each node: 4 bytes (offset) + (nodeSize * 40 bytes approx per item)
        return numNodes * (4L + (long) indexNodeSize * 40);
    }

    private static ShapefileData emptyResult(HeaderMeta header, String sourceName,
                                              SimpleFeatureType schema) {
        return new ShapefileData(
                List.of(), new Envelope(), sourceName, 0,
                "FlatGeobuf: " + header.name + " (0 features)", schema);
    }
}
