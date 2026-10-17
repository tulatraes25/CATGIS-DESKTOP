package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
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
 * Bounded eager import with fail-closed record validation. A streaming or indexed
 * large-dataset reader and fully verified property decoding are separate gates.
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

    // Temporary bounded eager-read containment until indexed lazy viewport support.
    static final int MAX_HEADER_BYTES = 4 * 1024 * 1024;
    static final int MAX_EAGER_DATA_BYTES = 128 * 1024 * 1024;
    static final long MAX_EAGER_FEATURES = 100_000;

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

            HeaderPrefix prefix = readHeaderPrefix(channel);
            int headerSize = prefix.headerSize();
            ByteBuffer headerBuf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
            readFully(channel, headerBuf, "encabezado FlatGeobuf");
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
        } catch (IOException | RuntimeException e) {
            CatgisLogger.warn("FlatGeobuf: error de lectura o encabezado corrupto: "
                    + file.getAbsolutePath(), e);
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

            HeaderPrefix prefix = readHeaderPrefix(channel);
            int headerSize = prefix.headerSize();
            int headerOffset = prefix.headerOffset();
            ByteBuffer headerBuf = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
            readFully(channel, headerBuf, "encabezado FlatGeobuf");
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

            long dataOffset = headerOffset + headerSize;
            if (indexNodeSize > 0 && featuresCount > 0) {
                long indexBytes = estimateIndexSize(featuresCount, indexNodeSize);
                dataOffset += indexBytes;
            }

            long dataSize = fileSize - dataOffset;
            if (featuresCount < 0 || featuresCount > MAX_EAGER_FEATURES) {
                throw new IOException("FlatGeobuf supera el máximo de entidades para lectura en memoria. "
                        + "Se requiere un lector indexado.");
            }
            if (dataOffset < headerOffset || dataSize < 0
                    || dataSize > MAX_EAGER_DATA_BYTES) {
                throw new IOException("FlatGeobuf supera 128 MiB de datos o tiene índice inválido. "
                        + "Se requiere un lector indexado.");
            }
            if (dataSize == 0) {
                if (featuresCount != 0) {
                    throw new IOException("FlatGeobuf truncado: faltan las entidades declaradas.");
                }
                return emptyResult(header, file.getName(), schema);
            }
            if (dataSize < Integer.BYTES) {
                throw new IOException("FlatGeobuf truncado antes del primer tamaño de entidad.");
            }

            int capacity = (int) dataSize;
            List<SimpleFeature> features = new ArrayList<>();

            try (FileInputStream dataFis = new FileInputStream(file);
                 FileChannel dataChannel = dataFis.getChannel()) {
                dataChannel.position(dataOffset);
                ByteBuffer dataBuf = ByteBuffer.allocateDirect(capacity);
                readFully(dataChannel, dataBuf, "datos de entidades FlatGeobuf");
                dataBuf.flip();
                dataBuf.order(ByteOrder.LITTLE_ENDIAN);

                while (dataBuf.remaining() >= 4) {
                    if (features.size() >= MAX_EAGER_FEATURES) {
                        throw new IOException("FlatGeobuf requiere visor indexado: máximo de entidades en RAM.");
                    }
                    int featureSize = dataBuf.getInt();
                    requireFeatureFrameSize(featureSize, dataBuf.remaining());

                    int pos = dataBuf.position();
                    dataBuf.limit(pos + featureSize);

                    try {
                        Feature fbFeature = Feature.getRootAsFeature(dataBuf);
                        if (fbFeature == null) {
                            throw new IllegalStateException("Entidad FlatGeobuf nula.");
                        }
                        features.add(convertFeature(fbFeature, builder, header, features.size()));
                    } catch (RuntimeException invalid) {
                        throw new IOException("Entidad FlatGeobuf corrupta en índice "
                                + features.size() + ": carga completa rechazada.", invalid);
                    }

                    dataBuf.position(pos + featureSize);
                    dataBuf.limit(dataBuf.capacity());
                }

                if (dataBuf.hasRemaining()) {
                    throw new IOException("FlatGeobuf truncado: bytes sobrantes después de entidades.");
                }
                if (featuresCount > 0 && features.size() != featuresCount) {
                    throw new IOException("Conteo FlatGeobuf incongruente: declarado "
                            + featuresCount + " leído " + features.size() + ".");
                }
            }

            Envelope envelope = computeEnvelope(header, features);

            CatgisLogger.info("FlatGeobuf: " + features.size()
                    + " entidades verificadas desde " + file.getName());

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
                                                 HeaderMeta header, int sourceIndex) {
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

        geometry = requireRealGeometry(geometry);
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

        return builder.buildFeature(String.valueOf(sourceIndex));
    }

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

    // The official FlatGeobuf magic is EIGHT bytes: fgb<major>fgb<patch>.
    // A historical four-byte CATGIS legacy marker is kept only for compatibility.
    record HeaderPrefix(int headerSize, int headerOffset) {}

    static HeaderPrefix readHeaderPrefix(FileChannel channel) throws IOException {
        ByteBuffer leading = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        readFully(channel, leading, "firma inicial");
        leading.flip();
        int magic = leading.getInt();
        int headerOffset = 8;
        if (magic == FGB_MAGIC_SPEC) {
            ByteBuffer marker = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            readFully(channel, marker, "firma extendida de ocho bytes");
            marker.flip();
            int trailing = marker.getInt();
            // Low three bytes are 'f', 'g', 'b'; high byte is backward-compatible patch.
            if ((trailing & 0x00ffffff) != 0x00626766) {
                throw new IOException("FlatGeobuf: segunda mitad de firma inválida.");
            }
            headerOffset = 12;
        } else if (magic != FGB_MAGIC_LEGACY) {
            throw new IOException("FlatGeobuf: firma incorrecta.");
        }
        ByteBuffer length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        readFully(channel, length, "tamaño de encabezado");
        length.flip();
        int size = length.getInt();
        if (size <= 0 || size > MAX_HEADER_BYTES || size > channel.size() - headerOffset) {
            throw new IOException("Encabezado FlatGeobuf inválido o excede 4 MiB.");
        }
        return new HeaderPrefix(size, headerOffset);
    }

    static Geometry requireRealGeometry(Geometry geometry) {
        if (geometry == null) {
            throw new IllegalArgumentException(
                    "Geometría FlatGeobuf ausente: prohibido fabricar Point(0,0).");
        }
        return geometry;
    }

    static void requireFeatureFrameSize(int size, int remaining) throws IOException {
        if (size <= 0 || size > remaining) {
            throw new IOException("Entidad FlatGeobuf truncada o tamaño inválido: "
                    + size + " bytes, restantes " + remaining + ".");
        }
    }

    static void readFully(FileChannel channel, ByteBuffer target, String label) throws IOException {
        while (target.hasRemaining()) {
            if (channel.read(target) <= 0) {
                throw new IOException("Lectura FlatGeobuf truncada: " + label);
            }
        }
    }

    static long estimateIndexSize(long featuresCount, int indexNodeSize) throws IOException {
        if (featuresCount <= 0 || indexNodeSize < 2 || indexNodeSize > 65535) {
            throw new IOException("Índice R-tree FlatGeobuf con parámetros inválidos.");
        }
        try {
            long nodes = featuresCount;
            long level = featuresCount;
            while (level > 1) {
                level = 1 + (level - 1) / indexNodeSize;
                nodes = Math.addExact(nodes, level);
            }
            return Math.multiplyExact(nodes, 40L);
        } catch (ArithmeticException overflow) {
            throw new IOException("Índice FlatGeobuf excede el rango de offsets.", overflow);
        }
    }

    private static ShapefileData emptyResult(HeaderMeta header, String sourceName,
                                              SimpleFeatureType schema) {
        CatgisLogger.debug("FlatGeobuf: returning empty result for " + sourceName);
        return new ShapefileData(
                List.of(), new Envelope(), sourceName, 0,
                "FlatGeobuf: " + header.name + " (0 features)", schema);
    }
}
