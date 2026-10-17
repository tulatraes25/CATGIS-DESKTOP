package ar.com.catgis;

import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Strict FlatGeobuf feature property decoder.
 * Each entry is little-endian u16 column-index followed by the declared
 * column's typed wire value; absent entries are null, not numeric zero.
 */
final class FlatGeobufPropertyDecoder {
    static final int MAX_PROPERTY_BYTES = 8 * 1024 * 1024;

    private FlatGeobufPropertyDecoder() {}

    static Object[] decode(Feature feature, List<ColumnMeta> columns) {
        if (feature == null || columns == null) {
            throw new IllegalArgumentException("FlatGeobuf: entidades/columnas ausentes.");
        }
        // Per-feature schema overrides cannot be interpreted against the
        // global header schema without a separate schema reconciliation.
        if (feature.columnsLength() > 0) {
            throw new IllegalArgumentException(
                    "FlatGeobuf: esquema de columnas por entidad aún no compatible.");
        }
        int count = feature.propertiesLength();
        if (count < 0 || count > MAX_PROPERTY_BYTES) {
            throw new IllegalArgumentException("FlatGeobuf: propiedades exceden 8 MiB.");
        }
        byte[] raw = new byte[count];
        for (int i = 0; i < count; i++) {
            raw[i] = (byte) feature.properties(i);
        }
        byte[] types = new byte[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            ColumnMeta column = columns.get(i);
            if (column == null) {
                throw new IllegalArgumentException("FlatGeobuf: metadatos de columna nulos.");
            }
            types[i] = column.type;
        }
        return decode(raw, types);
    }

    static Object[] decode(byte[] properties, byte[] types) {
        if (properties == null || types == null || types.length > 65535
                || properties.length > MAX_PROPERTY_BYTES) {
            throw new IllegalArgumentException("FlatGeobuf: límite de propiedades/columnas inválido.");
        }
        for (byte type : types) {
            bindingFor(type); // Fail even if unsupported column has no value.
        }
        Object[] values = new Object[types.length];
        boolean[] seen = new boolean[types.length];
        ByteBuffer bytes = ByteBuffer.wrap(properties).order(ByteOrder.LITTLE_ENDIAN);
        while (bytes.hasRemaining()) {
            require(bytes, 2, "índice de columna");
            int index = Short.toUnsignedInt(bytes.getShort());
            if (index >= types.length || seen[index]) {
                throw new IllegalArgumentException(
                        "FlatGeobuf: índice de atributo ausente/duplicado: " + index);
            }
            seen[index] = true;
            values[index] = readValue(bytes, types[index]);
        }
        return values;
    }

    static Class<?> bindingFor(int type) {
        return switch (type) {
            case ColumnType.Byte -> Byte.class;
            case ColumnType.UByte -> Short.class;
            case ColumnType.Bool -> Boolean.class;
            case ColumnType.Short -> Short.class;
            case ColumnType.UShort -> Integer.class;
            case ColumnType.Int -> Integer.class;
            case ColumnType.UInt -> Long.class;
            case ColumnType.Long -> Long.class;
            case ColumnType.ULong -> BigInteger.class;
            case ColumnType.Float -> Float.class;
            case ColumnType.Double -> Double.class;
            case ColumnType.String, ColumnType.Json, ColumnType.DateTime -> String.class;
            case ColumnType.Binary -> byte[].class;
            default -> throw new IllegalArgumentException(
                    "FlatGeobuf: tipo de columna no soportado: " + type);
        };
    }

    private static Object readValue(ByteBuffer buffer, byte type) {
        return switch (type) {
            case ColumnType.Byte -> { require(buffer, 1, "Byte"); yield buffer.get(); }
            case ColumnType.UByte -> { require(buffer, 1, "UByte"); yield (short) Byte.toUnsignedInt(buffer.get()); }
            case ColumnType.Bool -> {
                require(buffer, 1, "Bool");
                int value = Byte.toUnsignedInt(buffer.get());
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("FlatGeobuf: booleano inválido.");
                }
                yield value == 1;
            }
            case ColumnType.Short -> { require(buffer, 2, "Short"); yield buffer.getShort(); }
            case ColumnType.UShort -> { require(buffer, 2, "UShort"); yield Short.toUnsignedInt(buffer.getShort()); }
            case ColumnType.Int -> { require(buffer, 4, "Int"); yield buffer.getInt(); }
            case ColumnType.UInt -> { require(buffer, 4, "UInt"); yield Integer.toUnsignedLong(buffer.getInt()); }
            case ColumnType.Long -> { require(buffer, 8, "Long"); yield buffer.getLong(); }
            case ColumnType.ULong -> {
                require(buffer, 8, "ULong");
                yield new BigInteger(Long.toUnsignedString(buffer.getLong()));
            }
            case ColumnType.Float -> { require(buffer, 4, "Float"); yield buffer.getFloat(); }
            case ColumnType.Double -> { require(buffer, 8, "Double"); yield buffer.getDouble(); }
            case ColumnType.String, ColumnType.Json, ColumnType.DateTime -> readUtf8(buffer);
            case ColumnType.Binary -> readVariableBytes(buffer);
            default -> throw new IllegalArgumentException(
                    "FlatGeobuf: atributo de tipo desconocido.");
        };
    }

    private static String readUtf8(ByteBuffer buffer) {
        byte[] value = readVariableBytes(buffer);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(value)).toString();
        } catch (CharacterCodingException badUtf8) {
            throw new IllegalArgumentException("FlatGeobuf: atributo UTF-8 inválido.", badUtf8);
        }
    }

    private static byte[] readVariableBytes(ByteBuffer buffer) {
        require(buffer, 4, "longitud de propiedad");
        int length = buffer.getInt();
        if (length < 0 || length > MAX_PROPERTY_BYTES || length > buffer.remaining()) {
            throw new IllegalArgumentException(
                    "FlatGeobuf: propiedad variable truncada o excede presupuesto.");
        }
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    private static void require(ByteBuffer buffer, int required, String label) {
        if (required > buffer.remaining()) {
            throw new IllegalArgumentException(
                    "FlatGeobuf: propiedad truncada en " + label + ".");
        }
    }
}
