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
 * deserialization for all FlatGeobuf geometry types.
 * <p>
 * Validation is strict: corrupt or unsupported files throw
 * {@link UnsupportedFormatException} with a clear user-facing message.
 * Callers should invoke {@link #validateFile(File)} before loading to
 * get a {@link ValidationResult} without throwing.
 *
 * @see <a href="https://flatgeobuf.org/docs/spec/">FlatGeobuf Specification</a>
 */
public final class FlatGeobufLoader {

    private FlatGeobufLoader() {
    }

    private static final GeometryFactory GEOM_FACTORY = new GeometryFactory();

    // FlatGeobuf magic bytes as read by wololo library (legacy)
    private static final int FGB_MAGIC_LEGACY = 0x67666267;
    // Official FlatGeobuf spec magic: f g b 0x03 = LE 0x03626766
    private static final int FGB_MAGIC_SPEC  = 0x03626766;

    // ---- Public API ----

    /**
     * Validate a FlatGeobuf file without loading its full contents.
     *
     * @param file the .fgb file to check
     * @return validation result with status and message
     */
    public static ValidationResult validateFile(File file) {
        if (file == null) {
            return ValidationResult.invalid("Archivo no especificado.");
        }
        if (!file.exists()) {
            return ValidationResult.invalid("El archivo no existe: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase().endsWith(".fgb")) {
            return ValidationResult.invalid(
                    "La extensión del archivo no es .fgb: " + file.getName());
        }
        if (file.length() < 8) {
            return ValidationResult.invalid(
                    "El archivo es demasiado pequeño para ser FlatGeobuf ("
                            + file.length() + " bytes).");
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer magicBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            if (channel.read(magicBuf) < 4) {
                return ValidationResult.invalid("No se pudieron leer los bytes mágicos.");
            }
            magicBuf.flip();
            int magic = magicBuf.getInt();
            if (magic != FGB_MAGIC_LEGACY && magic != FGB_MAGIC_SPEC) {
                return ValidationResult.invalid(
                        "El archivo no es FlatGeobuf válido (magic incorrecto: 0x"
                                + Integer.toHexString(magic) + ").");
            }

            ByteBuffer headerSizeBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            if (channel.read(headerSizeBuf) < 4) {
                return ValidationResult.invalid("No se pudo leer el tamaño del encabezado.");
            }
            headerSizeBuf.flip();
            int headerSize = headerSizeBuf.getInt();
            long fileSize = channel.size();
            if (headerSize <= 0 || headerSize > fileSize - 8) {
                return ValidationResult.invalid(
                        "Tamaño de encabezado inválido: " + headerSize + " bytes.");
            }

            ByteBuffer headerBuf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
            if (channel.read(headerBuf) < headerSize) {
                return ValidationResult.invalid("No se pudo leer el encabezado completo.");
            }
            headerBuf.flip();

            HeaderMeta header = HeaderMeta.read(headerBuf);
            if (header == null) {
                return ValidationResult.invalid("No se pudo interpretar el encabezado FlatGeobuf.");
            }

            String geomType = GeometryType.name((short) header.geometryType);
            long featureCount = header.featuresCount;

            return ValidationResult.valid(
                    "FlatGeobuf válido — geometría: " + geomType
                            + ", features: " + featureCount
                            + ", columnas: " + (header.columns != null ? header.columns.size() : 0));
        } catch (IOException e) {
            CatgisLogger.warn("validateFile I/O error: " + file.getAbsolutePath(), e);
            return ValidationResult.invalid(
                    "Error de lectura al validar el archivo: " + e.getMessage());
        }
    }

    /**
     * Load a FlatGeobuf file and return ShapefileData.
     *
     * @param file the .fgb file
     * @return parsed vector data
     * @throws UnsupportedFormatException if the file is invalid or corrupt
     * @throws IOException                on I/O errors
     */
    public static ShapefileData load(File file) throws UnsupportedFormatException, IOException {
        if (file == null || !file.exists()) {
            throw new UnsupportedFormatException("El archivo no existe: " + file);
        }

        CatgisLogger.debug("FlatGeobufLoader.load: " + file.getAbsolutePath()
                + " (" + file.length() + " bytes)");

        ValidationResult vr = validateFile(file);
        if (!vr.isValid()) {
            throw new UnsupportedFormatException(vr.message());
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            long fileSize = channel.size();

            // Skip magic (already validated)
            ByteBuffer magicBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(magicBuf);

            // Read header size
            ByteBuffer headerSizeBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(headerSizeBuf);
            headerSizeBuf.flip();
            int headerSize = headerSizeBuf.getInt();

            // Read header
            ByteBuffer headerBuf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(headerBuf);
            headerBuf.flip();

            HeaderMeta header = HeaderMeta.read(headerBuf);

            CatgisLogger.debug("FlatGeobuf header: geometry="
                    + GeometryType.name((short) header.geometryType)
                    + ", features=" + header.featuresCount
                    + ", columns=" + (header.columns != null ? header.columns.size() : 0)
                    + ", indexNodeSize=" + header.indexNodeSize);

            SimpleFeatureType schema = buildSchema(header, file.getName());
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);

            long featuresCount = header.featuresCount;
            int indexNodeSize = header.indexNodeSize;

            long dataOffset = 8 + headerSize; // magic(4) + headerSize(4) + header
            if (indexNodeSize > 0 && featuresCount > 0) {
                long indexBytes = estimateIndexSize(featuresCount, indexNodeSize);
                dataOffset += indexBytes;
            }

            long dataSize = fileSize - dataOffset;
            if (dataSize < 4) {
                CatgisLogger.debug("FlatGeobuf: no feature data (dataSize=" + dataSize + ")");
                return emptyResult(header, file.getName(), schema);
            }

            int capacity = (int) Math.min(dataSize, Integer.MAX_VALUE);
            List<SimpleFeature> features = new ArrayList<>(
                    featuresCount > 0 ? (int) Math.min(featuresCount, Integer.MAX_VALUE) : 256);

            int skipped = 0;

            try (FileChannel dataChannel = new FileInputStream(file).getChannel()) {
                dataChannel.position(dataOffset);
                ByteBuffer dataBuf = ByteBuffer.allocateDirect(capacity);
                dataChannel.read(dataBuf);
                dataBuf.flip();
                dataBuf.order(ByteOrder.LITTLE_ENDIAN);

                while (dataBuf.remaining() >= 4) {
                    int featureSize = dataBuf.getInt();
                    if (featureSize <= 0 || featureSize > dataBuf.remaining()) {
                        CatgisLogger.debug("FlatGeobuf: stopping feature read, featureSize="
                                + featureSize + " remaining=" + dataBuf.remaining());
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
                        skipped++;
                        CatgisLogger.warn("FlatGeobuf: corrupt feature #" + (features.size() + skipped)
                                + ", skipping", e);
                    }

                    dataBuf.position(pos + featureSize);
                    dataBuf.limit(dataBuf.capacity());
                }

                if (skipped > 0) {
                    CatgisLogger.warn("FlatGeobuf: skipped " + skipped
                            + " corrupt feature(s) in " + file.getName(), null);
                }
            }

            Envelope envelope = computeEnvelope(header, features);

            CatgisLogger.info("FlatGeobuf loaded: " + file.getName()
                    + " → " + features.size() + " features"
                    + (skipped > 0 ? " (" + skipped + " skipped)" : ""));

            return new ShapefileData(
                    features, envelope, file.getName(), features.size(),
                    "FlatGeobuf: " + header.name + " (" + features.size() + " features)", schema);
        }
    }

    /**
     * Load a FlatGeobuf file from a path string.
     */
    public static ShapefileData load(String path) throws UnsupportedFormatException, IOException {
        return load(new File(path));
    }

    // --- Private helpers ---

    private static Envelope computeEnvelope(HeaderMeta header, List<SimpleFeature> features) {
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
        return envelope;
    }

    private static SimpleFeatureType buildSchema(HeaderMeta header, String sourceName) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        String name = sourceName.replaceAll("[^a-zA-Z0-9_]", "_");
        typeBuilder.setName(name);
        typeBuilder.add("the_geom", Geometry.class);

        List<ColumnMeta> columns = header.columns;
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                ColumnMeta col = columns.get(i);
                String colName = col.name != null ? col.name : "col_" + i;
                colName = colName.replaceAll("[^a-zA-Z0-9_]", "_");
                if (colName.isEmpty()) {
                    colName = "col_" + i;
                }
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
            default -> {
                CatgisLogger.debug("FlatGeobuf: unknown column type " + columnType
                        + ", defaulting to String");
                yield String.class;
            }
        };
    }

    private static SimpleFeature convertFeature(Feature fbFeature,
                                                 SimpleFeatureBuilder builder,
                                                 HeaderMeta header) {
        builder.reset();

        Geometry geometry = null;
        try {
            org.wololo.flatgeobuf.generated.Geometry fbGeom = fbFeature.geometry();
            if (fbGeom != null) {
                geometry = GeometryConversions.deserialize(fbGeom, header.geometryType);
            }
        } catch (Exception e) {
            CatgisLogger.warn("FlatGeobuf: geometry deserialization failed for feature", e);
            throw new RuntimeException("Failed to deserialize geometry", e);
        }

        if (geometry == null) {
            CatgisLogger.debug("FlatGeobuf: null geometry in feature, using empty Point(0,0)");
            geometry = GEOM_FACTORY.createPoint(new Coordinate(0, 0));
        }
        builder.set("the_geom", geometry);

        List<ColumnMeta> columns = header.columns;
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                ColumnMeta col = columns.get(i);
                String colName = col.name != null ? col.name : "col_" + i;
                colName = colName.replaceAll("[^a-zA-Z0-9_]", "_");
                if (colName.isEmpty()) {
                    colName = "col_" + i;
                }

                Object value = readColumnValue(fbFeature, i, col.type);
                try {
                    builder.set(colName, value);
                } catch (Exception e) {
                    CatgisLogger.warn("FlatGeobuf: failed to set column '" + colName
                            + "' (type=" + col.type + ")", e);
                    builder.set(colName, null);
                }
            }
        }

        return builder.buildFeature(String.valueOf(featureIdCounter++));
    }

    private static int featureIdCounter;

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
                        String.valueOf((long) raw);
                default -> raw;
            };
        } catch (Exception e) {
            CatgisLogger.debug("FlatGeobuf: failed to read column " + colIndex
                    + " (type=" + columnType + ")");
            return null;
        }
    }

    private static long estimateIndexSize(long featuresCount, int indexNodeSize) {
        if (indexNodeSize <= 0 || featuresCount <= 0) {
            return 0;
        }
        long numNodes = (featuresCount + indexNodeSize - 1) / indexNodeSize;
        return numNodes * (4L + (long) indexNodeSize * 40);
    }

    private static ShapefileData emptyResult(HeaderMeta header, String sourceName,
                                              SimpleFeatureType schema) {
        CatgisLogger.debug("FlatGeobuf: returning empty result for " + sourceName);
        return new ShapefileData(
                List.of(), new Envelope(), sourceName, 0,
                "FlatGeobuf: " + header.name + " (0 features)", schema);
    }
}
