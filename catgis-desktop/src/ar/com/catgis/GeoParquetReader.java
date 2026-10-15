package ar.com.catgis;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.parquet.format.ColumnChunk;
import org.apache.parquet.format.ColumnMetaData;
import org.apache.parquet.format.FieldRepetitionType;
import org.apache.parquet.format.FileMetaData;
import org.apache.parquet.format.KeyValue;
import org.apache.parquet.format.RowGroup;
import org.apache.parquet.format.SchemaElement;
import org.apache.parquet.format.Type;
import org.apache.parquet.format.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only GeoParquet 1.0/1.1 WKB metadata inspection, NOT layer ingestion.
 * Apache Parquet decodes Thrift; Jackson decodes bounded, strict JSON.
 * Counts, extents and types are declarations from the file, not sampled facts.
 */
public final class GeoParquetReader {
    static final int MAX_FOOTER_BYTES = 4 * 1024 * 1024;
    static final int MAX_GEO_JSON_CHARS = 256 * 1024;
    private static final int MAX_SCHEMA_FIELDS = 4096;
    private static final int MAX_ROW_GROUPS = 10000;
    private static final Set<String> TYPES = Set.of("Point", "LineString", "Polygon",
            "MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection");
    private static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(32)
                    .maxStringLength(MAX_GEO_JSON_CHARS).maxNumberLength(64).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private GeoParquetReader() {}

    public enum CrsState { DEFAULT_CRS84, EXPLICIT_UNKNOWN, PROJJSON_DECLARED }

    /** rawJson retains optional declarations such as epoch and covering, without interpreting them. */
    public record GeometryColumn(String encoding, List<String> geometryTypes, CrsState crsState,
                                 String crs, double[] bbox, String rawJson) {
        public GeometryColumn {
            geometryTypes = List.copyOf(geometryTypes);
            bbox = bbox == null ? null : bbox.clone();
        }
        @Override public double[] bbox() { return bbox == null ? null : bbox.clone(); }
    }

    /** Original accessors are retained; absent bbox remains null, never world bounds. */
    public record GeoParquetMetadata(String version, String geometryColumn, String geometryType,
                                     String crs, double[] bbox, long rowCount, List<String> columns,
                                     Map<String, GeometryColumn> geometryColumns) {
        public GeoParquetMetadata {
            bbox = bbox == null ? null : bbox.clone();
            columns = List.copyOf(columns);
            geometryColumns = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(geometryColumns));
        }
        /** Source compatibility for callers constructing the old metadata value. */
        public GeoParquetMetadata(String version, String geometryColumn, String geometryType,
                                  String crs, double[] bbox, long rowCount, List<String> columns) {
            this(version, geometryColumn, geometryType, crs, bbox, rowCount, columns, Map.of());
        }
        @Override public double[] bbox() { return bbox == null ? null : bbox.clone(); }
    }

    public static GeoParquetMetadata readMetadata(File file) throws IOException {
        checkActive();
        if (file == null || !Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            throw invalid("Se requiere un archivo regular local, no un enlace ni un directorio.");
        }
        try (FileChannel input = FileChannel.open(file.toPath(), StandardOpenOption.READ,
                LinkOption.NOFOLLOW_LINKS)) {
            long size = input.size();
            if (size < 12) { throw invalid("Archivo Parquet truncado: faltan cabecera y pie."); }
            byte[] header = read(input, 0, 4);
            byte[] tail = read(input, size - 8, 8);
            if (!magic(header, 0) || !magic(tail, 4)) {
                throw invalid("Firma Parquet incorrecta o pie cifrado no compatible (se requiere PAR1).");
            }
            long length = Integer.toUnsignedLong(ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN).getInt());
            if (length == 0 || length > MAX_FOOTER_BYTES || length > size - 12) {
                throw invalid("Longitud de pie invalida o superior al limite de 4 MiB.");
            }
            long footerStart = size - 8 - length;
            byte[] bytes = read(input, footerStart, (int) length);
            FileMetaData footer;
            ByteArrayInputStream memory = new ByteArrayInputStream(bytes);
            try (InputStream bounded = new FilterInputStream(memory) {
                @Override public int read() throws IOException {
                    checkActive();
                    return super.read();
                }
                @Override public int read(byte[] b, int off, int len) throws IOException {
                    checkActive();
                    return super.read(b, off, len);
                }
            }) {
                // The decoder's transport budget is the ACTUAL footer length, not the file size.
                footer = Util.readFileMetaData(bounded, false, null, null, bytes.length);
                if (memory.available() != 0) { throw invalid("Sobran bytes despues de los metadatos Parquet."); }
            } catch (RuntimeException malformed) {
                throw new IOException("No se pudo decodificar el pie Parquet.", malformed);
            }
            checkActive();
            if (input.size() != size) { throw invalid("El archivo cambio durante la inspeccion."); }
            return inspect(footer, footerStart);
        }
    }

    /** True only for supported, structurally checked metadata; does NOT validate row-page payloads. */
    public static boolean isGeoParquet(File file) {
        try {
            readMetadata(file);
            return true;
        } catch (IOException | RuntimeException failure) {
            return false;
        }
    }

    public static String getSummary(File file) throws IOException {
        GeoParquetMetadata meta = readMetadata(file);
        GeometryColumn primary = meta.geometryColumns().get(meta.geometryColumn());
        String crs = primary.crsState() == CrsState.DEFAULT_CRS84
                ? "OGC:CRS84 (por defecto; X=longitud, Y=latitud)"
                : primary.crsState() == CrsState.EXPLICIT_UNKNOWN
                ? "Desconocido: crs=null declarado en el archivo"
                : "PROJJSON declarado (no interpretado): " + boundedText(primary.crs(), 2000);
        return "=== GeoParquet: metadatos declarados ===\n"
                + "Archivo: " + boundedText(file.getName(), 512) + "\n"
                + "Version: " + meta.version() + "\n"
                + "Columna geometrica primaria: " + meta.geometryColumn() + "\n"
                + "Codificacion: " + primary.encoding() + "\n"
                + "Tipos declarados: " + meta.geometryType() + "\n"
                + "Filas declaradas: " + meta.rowCount() + "\n"
                + "CRS: " + crs + "\n"
                + "Extension declarada: " + (meta.bbox() == null ? "No declarada"
                    : java.util.Arrays.toString(meta.bbox())) + "\n"
                + "Columnas: " + boundedText(String.join(", ", meta.columns()), 4000) + "\n\n"
                + "Solo inspeccion de metadatos. No se cargo una capa ni se validaron las geometrías o paginas de datos.\n";
    }

    private static GeoParquetMetadata inspect(FileMetaData footer, long footerStart) throws IOException {
        if (!footer.isSetVersion() || footer.getVersion() < 1 || footer.getVersion() > 2
                || !footer.isSetNum_rows() || footer.getNum_rows() < 0
                || footer.getSchema() == null || footer.getRow_groups() == null) {
            throw invalid("Pie Parquet incompleto o version no compatible.");
        }
        if (footer.isSetEncryption_algorithm() || footer.isSetFooter_signing_key_metadata()) {
            throw invalid("Metadatos cifrados o firmados requieren un lector con verificacion criptografica.");
        }
        List<SchemaElement> schema = footer.getSchema();
        if (schema.size() < 2 || schema.size() > MAX_SCHEMA_FIELDS) {
            throw invalid("Esquema vacio o superior al limite de campos.");
        }
        SchemaElement root = schema.get(0);
        if (root.isSetType() || !root.isSetNum_children() || root.getNum_children() <= 0) {
            throw invalid("Raiz de esquema Parquet invalida.");
        }
        Map<String, SchemaElement> top = new LinkedHashMap<>();
        Map<List<String>, Type> leaves = new LinkedHashMap<>();
        int[] cursor = {1};
        for (int i = 0; i < root.getNum_children(); i++) {
            checkActive();
            if (cursor[0] >= schema.size()) { throw invalid("Esquema truncado."); }
            SchemaElement field = schema.get(cursor[0]);
            if (top.putIfAbsent(field.getName(), field) != null) { throw invalid("Columnas duplicadas."); }
            schemaField(schema, cursor, List.of(), leaves, 0);
        }
        if (cursor[0] != schema.size()) { throw invalid("Campos huerfanos en el esquema."); }
        validateGroups(footer, leaves, footerStart);
        String geo = null;
        if (footer.getKey_value_metadata() != null) {
            for (KeyValue entry : footer.getKey_value_metadata()) {
                checkActive();
                if ("geo".equals(entry.getKey())) {
                    if (geo != null || !entry.isSetValue()) { throw invalid("Clave geo duplicada o sin valor."); }
                    geo = entry.getValue();
                }
            }
        }
        if (geo == null) { throw invalid("Es Parquet, pero falta la clave geo de GeoParquet 1.x."); }
        if (geo.length() > MAX_GEO_JSON_CHARS) { throw invalid("Metadatos geo demasiado extensos."); }
        JsonNode json = JSON.readTree(geo);
        if (json == null || !json.isObject()) { throw invalid("La clave geo debe contener un objeto JSON."); }
        String version = text(json, "version");
        if (!"1.0.0".equals(version) && !"1.1.0".equals(version)) {
            throw invalid("Version GeoParquet no compatible: " + boundedText(version, 80));
        }
        String primaryName = text(json, "primary_column");
        JsonNode columns = json.get("columns");
        if (columns == null || !columns.isObject() || columns.isEmpty() || columns.size() > MAX_SCHEMA_FIELDS) {
            throw invalid("Faltan las columnas geometricas declaradas.");
        }
        Map<String, GeometryColumn> geometryColumns = new LinkedHashMap<>();
        var entries = columns.fields();
        while (entries.hasNext()) {
            checkActive();
            var entry = entries.next();
            SchemaElement physical = top.get(entry.getKey());
            if (physical == null || physical.getType() != Type.BYTE_ARRAY
                    || physical.getRepetition_type() == FieldRepetitionType.REPEATED) {
                throw invalid("Columna WKB ausente, anidada o con tipo fisico incorrecto: " + boundedText(entry.getKey(), 128));
            }
            // M1 supports unannotated binary WKB. Text/decimal/other logical
            // annotations, including newer Parquet spatial logical types, need
            // an explicit compatibility contract, never silent reinterpretation.
            if (physical.isSetConverted_type() || physical.isSetLogicalType()) {
                throw invalid("Anotacion logica de columna no compatible con esta inspeccion WKB: "
                        + boundedText(entry.getKey(), 128));
            }
            geometryColumns.put(entry.getKey(), geometryColumn(entry.getValue()));
        }
        GeometryColumn primary = geometryColumns.get(primaryName);
        if (primary == null) { throw invalid("La columna primaria no figura entre las columnas geometricas."); }
        checkActive();
        return new GeoParquetMetadata(version, primaryName,
                primary.geometryTypes().isEmpty() ? "No declarados" : String.join(", ", primary.geometryTypes()),
                primary.crs(), primary.bbox(), footer.getNum_rows(), new ArrayList<>(top.keySet()), geometryColumns);
    }

    private static void schemaField(List<SchemaElement> schema, int[] cursor, List<String> parent,
                                    Map<List<String>, Type> leaves, int depth) throws IOException {
        checkActive();
        if (depth > 32 || cursor[0] >= schema.size()) { throw invalid("Esquema truncado o demasiado profundo."); }
        SchemaElement field = schema.get(cursor[0]++);
        if (field.getName() == null || field.getName().isBlank() || field.getName().length() > 1024
                || !field.isSetRepetition_type()) { throw invalid("Campo Parquet sin nombre o repeticion."); }
        List<String> path = new ArrayList<>(parent);
        path.add(field.getName());
        if (field.isSetType()) {
            if (field.isSetNum_children() && field.getNum_children() != 0) { throw invalid("Campo primitivo con hijos."); }
            if (leaves.putIfAbsent(List.copyOf(path), field.getType()) != null) { throw invalid("Ruta de campo duplicada."); }
        } else {
            if (!field.isSetNum_children() || field.getNum_children() <= 0
                    || field.getNum_children() > schema.size() - cursor[0]) { throw invalid("Grupo de esquema invalido."); }
            Set<String> names = new HashSet<>();
            for (int i = 0; i < field.getNum_children(); i++) {
                if (cursor[0] >= schema.size() || !names.add(schema.get(cursor[0]).getName())) {
                    throw invalid("Hijos duplicados o esquema truncado.");
                }
                schemaField(schema, cursor, path, leaves, depth + 1);
            }
        }
    }

    private static void validateGroups(FileMetaData footer, Map<List<String>, Type> leaves,
                                       long footerStart) throws IOException {
        if (footer.getRow_groupsSize() > MAX_ROW_GROUPS) { throw invalid("Demasiados grupos de filas."); }
        long rows = 0;
        for (RowGroup group : footer.getRow_groups()) {
            checkActive();
            if (group.getNum_rows() < 0 || group.getColumns() == null
                    || group.getColumnsSize() != leaves.size() || group.getTotal_byte_size() < 0) {
                throw invalid("Grupo de filas incoherente con el esquema.");
            }
            try { rows = Math.addExact(rows, group.getNum_rows()); }
            catch (ArithmeticException overflow) { throw invalid("Desbordamiento en el recuento de filas."); }
            Set<List<String>> found = new HashSet<>();
            for (ColumnChunk chunk : group.getColumns()) {
                checkActive();
                if (chunk.isSetCrypto_metadata() || chunk.isSetEncrypted_column_metadata()
                        || !chunk.isSetMeta_data() || (chunk.isSetFile_path() && !chunk.getFile_path().isEmpty())) {
                    throw invalid("Columnas cifradas o almacenadas en otros archivos no compatibles.");
                }
                ColumnMetaData column = chunk.getMeta_data();
                List<String> path = column.getPath_in_schema();
                if (path == null || !found.add(path) || leaves.get(path) != column.getType()
                        || column.getNum_values() < 0 || column.getTotal_compressed_size() < 0
                        || column.getTotal_uncompressed_size() < 0) {
                    throw invalid("Metadatos de columna incoherentes.");
                }
                validateColumnExtent(group, column, footerStart);
            }
        }
        if (rows != footer.getNum_rows()) { throw invalid("El total de filas no coincide con los grupos de filas."); }
    }

    private static void validateColumnExtent(RowGroup group, ColumnMetaData column,
                                             long footerStart) throws IOException {
        long dataOffset = column.getData_page_offset();
        // Arrow can emit an empty dictionary but no data page for zero rows.
        // In that case data_page_offset=0 is an ABSENT page, not a byte address.
        boolean absentDataPage = dataOffset == 0 && group.getNum_rows() == 0 && column.getNum_values() == 0;
        long start = dataOffset;
        if (column.isSetDictionary_page_offset()) {
            long dictionaryOffset = column.getDictionary_page_offset();
            if (dictionaryOffset < 4 || dictionaryOffset > footerStart
                    || (!absentDataPage && dictionaryOffset >= dataOffset)) {
                throw invalid("La pagina de diccionario apunta fuera de su columna.");
            }
            start = dictionaryOffset;
        } else if (absentDataPage) {
            if (column.getTotal_compressed_size() != 0 || column.getTotal_uncompressed_size() != 0) {
                throw invalid("La columna declara bytes pero no tiene paginas.");
            }
            return;
        }
        long bytes = column.getTotal_compressed_size();
        if (start < 4 || start > footerStart || bytes > footerStart - start
                || (!absentDataPage && (dataOffset < 4 || dataOffset > footerStart
                    || (bytes > 0 && dataOffset - start >= bytes)
                    || (column.getNum_values() > 0 && bytes == 0)))) {
            throw invalid("La columna apunta fuera del cuerpo del archivo.");
        }
    }

    private static GeometryColumn geometryColumn(JsonNode column) throws IOException {
        if (!column.isObject()) { throw invalid("Metadatos de columna geometrica invalidos."); }
        String encoding = text(column, "encoding");
        if (!"WKB".equals(encoding)) { throw invalid("Codificacion no compatible en esta inspeccion: " + encoding); }
        JsonNode types = column.get("geometry_types");
        if (types == null || !types.isArray() || types.size() > 14) { throw invalid("geometry_types debe ser una lista de tipos."); }
        List<String> names = new ArrayList<>();
        for (JsonNode type : types) {
            String value = type.isTextual() ? type.textValue() : "";
            String base = value.endsWith(" Z") ? value.substring(0, value.length() - 2) : value;
            if (!TYPES.contains(base) || names.contains(value)) { throw invalid("Tipo geometrico invalido o duplicado."); }
            names.add(value);
        }
        CrsState state;
        String crs;
        JsonNode declared = column.get("crs");
        if (declared == null) {
            state = CrsState.DEFAULT_CRS84;
            crs = "OGC:CRS84";
        } else if (declared.isNull()) {
            state = CrsState.EXPLICIT_UNKNOWN;
            crs = null;
        } else {
            if (!declared.isObject() || !text(declared, "type").endsWith("CRS")) {
                throw invalid("crs debe ser null o un objeto PROJJSON de CRS, no un codigo EPSG textual.");
            }
            state = CrsState.PROJJSON_DECLARED;
            crs = declared.toString();
        }
        if (column.has("orientation") && !"counterclockwise".equals(text(column, "orientation"))) {
            throw invalid("Orientacion geometrica no compatible.");
        }
        if (column.has("edges") && !Set.of("planar", "spherical").contains(text(column, "edges"))) {
            throw invalid("Declaracion edges invalida.");
        }
        if (column.has("epoch") && (!column.get("epoch").isNumber() || !Double.isFinite(column.get("epoch").doubleValue()))) {
            throw invalid("Epoca de coordenadas invalida.");
        }
        double[] bbox = null;
        if (column.has("bbox")) {
            JsonNode bounds = column.get("bbox");
            if (!bounds.isArray() || (bounds.size() != 4 && bounds.size() != 6)) { throw invalid("bbox requiere 4 o 6 numeros."); }
            bbox = new double[bounds.size()];
            for (int i = 0; i < bbox.length; i++) {
                if (!bounds.get(i).isNumber() || !Double.isFinite(bounds.get(i).doubleValue())) { throw invalid("bbox no finito."); }
                bbox[i] = bounds.get(i).doubleValue();
            }
            int dimension = bbox.length / 2;
            for (int i = 1; i < dimension; i++) {
                if (bbox[i] > bbox[i + dimension]) { throw invalid("Limites Y/Z invertidos en bbox."); }
            }
            // Geographic antimeridian boxes may legitimately have xmin > xmax.
            if (declared != null && declared.isObject() && "ProjectedCRS".equals(declared.path("type").asText())
                    && bbox[0] > bbox[dimension]) { throw invalid("Limites X proyectados invertidos en bbox."); }
        }
        return new GeometryColumn(encoding, names, state, crs, bbox, column.toString());
    }

    private static String text(JsonNode node, String field) throws IOException {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.textValue().isBlank() || value.textValue().length() > 1024) {
            throw invalid("Campo textual obligatorio invalido: " + field);
        }
        return value.textValue();
    }

    private static byte[] read(FileChannel input, long offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        while (buffer.hasRemaining()) {
            checkActive();
            int count = input.read(buffer, offset + buffer.position());
            if (count < 0) { throw invalid("Archivo truncado durante la lectura."); }
        }
        return buffer.array();
    }

    private static boolean magic(byte[] bytes, int offset) {
        return bytes[offset] == 'P' && bytes[offset + 1] == 'A'
                && bytes[offset + 2] == 'R' && bytes[offset + 3] == '1';
    }

    private static void checkActive() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) { throw new InterruptedIOException("Inspeccion GeoParquet cancelada."); }
    }

    private static IOException invalid(String message) { return new IOException(message); }
    private static String boundedText(String text, int limit) {
        return text.length() <= limit ? text : text.substring(0, limit) + " [texto abreviado]";
    }
}
