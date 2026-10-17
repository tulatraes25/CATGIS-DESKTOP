package ar.com.catgis.probe;

import ar.com.catgis.GeoParquetReader;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.conf.PlainParquetConfiguration;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.InputStreamInStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Executable adapter experiment, NOT an installed CATGIS layer loader.
 * Apache decodes pages/encodings/compression; JTS decodes WKB. No new binary parser.
 * Deliberately bounded small-file subset. No CRS transform or generic schema claim.
 */
public final class GeoParquetRowsProbe {
    static final long MAX_FILE_BYTES = 4L * 1024 * 1024;
    static final long MAX_ROW_GROUP_BYTES = 8L * 1024 * 1024;
    static final long MAX_DECLARED_BYTES = 32L * 1024 * 1024;
    static final long MAX_ROWS = 10000;
    static final int MAX_VALUE_BYTES = 1024 * 1024;
    private static final Set<CompressionCodecName> CODECS = Set.of(
            CompressionCodecName.UNCOMPRESSED, CompressionCodecName.SNAPPY, CompressionCodecName.GZIP);

    public record Scan(GeoParquetReader.GeoParquetMetadata metadata,
                       List<Map<String, Object>> rows, int rowGroups) {}

    private GeoParquetRowsProbe() {}

    public static Scan read(Path path) throws IOException { return read(path, () -> false, MAX_ROWS); }

    static Scan read(Path path, BooleanSupplier cancelled, long maxRows) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(cancelled, "cancelled");
        checkActive(cancelled);
        if (maxRows < 0 || maxRows > MAX_ROWS) { throw new IOException("Invalid probe row budget."); }
        LocalProbeInput input = new LocalProbeInput(path, cancelled);
        GeoParquetReader.GeoParquetMetadata metadata = GeoParquetReader.readMetadata(path.toFile());
        if (metadata.rowCount() > maxRows) { throw new IOException("Declared rows exceed probe budget."); }
        checkActive(cancelled);
        ParquetReadOptions options = ParquetReadOptions.builder(new PlainParquetConfiguration())
                .withMaxAllocationInBytes((int) MAX_ROW_GROUP_BYTES)
                .usePageChecksumVerification(true).useStatsFilter(false).useDictionaryFilter(false)
                .useRecordFilter(false).useColumnIndexFilter(false).useBloomFilter(false).build();
        try (ParquetFileReader reader = ParquetFileReader.open(input, options)) {
            MessageType schema = reader.getFooter().getFileMetaData().getSchema();
            validateSchema(schema, metadata);
            long declaredRows = 0;
            long declaredBytes = 0;
            for (var block : reader.getFooter().getBlocks()) {
                checkActive(cancelled);
                if (block.getRowCount() < 0 || block.getRowCount() > maxRows
                        || block.getTotalByteSize() < 0 || block.getTotalByteSize() > MAX_ROW_GROUP_BYTES) {
                    throw new IOException("Row-group declaration exceeds probe budget.");
                }
                declaredRows = Math.addExact(declaredRows, block.getRowCount());
                declaredBytes = Math.addExact(declaredBytes, block.getTotalByteSize());
                if (declaredRows > maxRows || declaredBytes > MAX_DECLARED_BYTES) {
                    throw new IOException("Aggregate row-group declarations exceed probe budget.");
                }
                for (var column : block.getColumns()) {
                    if (!CODECS.contains(column.getCodec())) {
                        throw new IOException("Codec outside this probe's tested subset: " + column.getCodec());
                    }
                }
            }
            if (declaredRows != metadata.rowCount()) { throw new IOException("Inconsistent row declarations."); }
            List<Map<String, Object>> rows = new ArrayList<>();
            int groups = 0;
            while (true) {
                checkActive(cancelled);
                PageReadStore next = reader.readNextRowGroup();
                if (next == null) { break; }
                try (PageReadStore pages = next) {
                    groups++;
                    if (pages.getRowCount() < 0 || pages.getRowCount() > maxRows - rows.size()) {
                        throw new IOException("Decoded row group exceeds probe row budget.");
                    }
                    var records = new ColumnIOFactory().getColumnIO(schema)
                            .getRecordReader(pages, new GroupRecordConverter(schema));
                    for (long row = 0; row < pages.getRowCount(); row++) {
                        checkActive(cancelled);
                        Group record = records.read();
                        if (record == null) { throw new IOException("Missing declared row."); }
                        Map<String, Object> values = new LinkedHashMap<>();
                        for (int column = 0; column < schema.getFieldCount(); column++) {
                            Type field = schema.getType(column);
                            int count = record.getFieldRepetitionCount(column);
                            if (count > 1) { throw new IOException("Repeated value outside probe contract."); }
                            Object value = count == 0 ? null : readValue(record, column, field,
                                    metadata.geometryColumns().get(field.getName()));
                            values.put(field.getName(), value);
                        }
                        // Map.copyOf rejects genuine null attributes; preserve them explicitly.
                        rows.add(Collections.unmodifiableMap(values));
                    }
                }
            }
            checkActive(cancelled);
            if (rows.size() != metadata.rowCount() || Files.size(path) != input.getLength()) {
                throw new IOException("Incomplete row scan or changed input length.");
            }
            return new Scan(metadata, List.copyOf(rows), groups);
        } catch (RuntimeException failure) {
            // Official codecs also report malformed pages through unchecked decoding
            // exceptions. Preserve the cause; never turn an incomplete scan into success.
            throw new IOException("Parquet row decoding failed; no complete scan was returned.", failure);
        } finally {
            options.getCodecFactory().release();
        }
    }

    private static void validateSchema(MessageType schema, GeoParquetReader.GeoParquetMetadata meta)
            throws IOException {
        if (schema.getFieldCount() == 0 || schema.getFieldCount() > 64
                || !schema.getFields().stream().map(Type::getName).toList().equals(meta.columns())) {
            throw new IOException("Unsupported or inconsistent probe schema.");
        }
        for (Type field : schema.getFields()) {
            if (!field.isPrimitive() || field.isRepetition(Type.Repetition.REPEATED)) {
                throw new IOException("Nested/repeated attributes are not implemented in this probe.");
            }
            PrimitiveType primitive = field.asPrimitiveType();
            var logical = primitive.getLogicalTypeAnnotation();
            boolean geometry = meta.geometryColumns().containsKey(field.getName());
            if (geometry) {
                if (primitive.getPrimitiveTypeName() != PrimitiveType.PrimitiveTypeName.BINARY || logical != null) {
                    throw new IOException("Probe geometry must be unannotated binary WKB.");
                }
            } else if (primitive.getPrimitiveTypeName() == PrimitiveType.PrimitiveTypeName.BINARY) {
                if (!(logical instanceof LogicalTypeAnnotation.StringLogicalTypeAnnotation)) {
                    throw new IOException("Non-geometry binary attribute is not a tested string.");
                }
            } else if (logical != null || !Set.of(PrimitiveType.PrimitiveTypeName.BOOLEAN,
                    PrimitiveType.PrimitiveTypeName.INT32, PrimitiveType.PrimitiveTypeName.INT64,
                    PrimitiveType.PrimitiveTypeName.FLOAT, PrimitiveType.PrimitiveTypeName.DOUBLE)
                    .contains(primitive.getPrimitiveTypeName())) {
                throw new IOException("Logical/numeric attribute outside the tested probe subset.");
            }
        }
    }

    private static Object readValue(Group record, int index, Type field,
                                    GeoParquetReader.GeometryColumn geometryColumn) throws IOException {
        return switch (field.asPrimitiveType().getPrimitiveTypeName()) {
            case BOOLEAN -> record.getBoolean(index, 0);
            case INT32 -> record.getInteger(index, 0);
            case INT64 -> record.getLong(index, 0);
            case FLOAT -> record.getFloat(index, 0);
            case DOUBLE -> record.getDouble(index, 0);
            case BINARY -> {
                var value = record.getBinary(index, 0);
                if (value.length() > MAX_VALUE_BYTES) { throw new IOException("Binary/text value exceeds probe budget."); }
                byte[] bytes = value.getBytes();
                yield geometryColumn == null ? strictUtf8(bytes) : geometry(bytes, geometryColumn);
            }
            default -> throw new IOException("Unsupported probe value.");
        };
    }

    private static String strictUtf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
    }

    private static Geometry geometry(byte[] bytes, GeoParquetReader.GeometryColumn declaration) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        try {
            Geometry geometry = new WKBReader().read(new InputStreamInStream(input));
            if (input.available() != 0 || !geometry.isValid()) {
                throw new IOException("Trailing WKB data or invalid decoded geometry.");
            }
            for (Coordinate coordinate : geometry.getCoordinates()) {
                if (!Double.isFinite(coordinate.x) || !Double.isFinite(coordinate.y)) {
                    throw new IOException("Non-finite geometry coordinates.");
                }
            }
            // This experiment covers 2D fixtures only; no silent 3D type claim.
            if (!declaration.geometryTypes().isEmpty()
                    && !declaration.geometryTypes().contains(geometry.getGeometryType())) {
                throw new IOException("Geometry type differs from the tested declaration.");
            }
            return geometry;
        } catch (ParseException failure) {
            throw new IOException("Invalid WKB geometry; no complete scan was returned.", failure);
        }
    }

    static void checkActive(BooleanSupplier cancelled) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted() || cancelled.getAsBoolean()) {
            throw new InterruptedIOException("GeoParquet row probe cancelled.");
        }
    }
}
