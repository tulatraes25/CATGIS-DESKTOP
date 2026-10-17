package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.wololo.flatgeobuf.generated.ColumnType;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlatGeobufPropertyDecoderTest {
    @Test
    void typedPropertiesAreIndexedAndNullsStayAbsent() {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        column(wire, 2); variable(wire, "Comodoro".getBytes(StandardCharsets.UTF_8));
        column(wire, 0); i32(wire, -12345);
        byte[] kinds = new byte[]{ColumnType.Int, ColumnType.Bool, ColumnType.String};
        Object[] decoded = FlatGeobufPropertyDecoder.decode(wire.toByteArray(), kinds);
        assertEquals(-12345, decoded[0]);
        assertNull(decoded[1]);
        assertEquals("Comodoro", decoded[2]);
    }

    @Test
    void unsignedNumericsCannotWrapNegativeOrLosePrecision() {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        column(wire, 0); wire.write(255);
        column(wire, 1); column(wire, 65535);
        column(wire, 2); i32(wire, -1);
        column(wire, 3); i64(wire, -1L);
        Object[] decoded = FlatGeobufPropertyDecoder.decode(
                wire.toByteArray(), new byte[]{ColumnType.UByte, ColumnType.UShort,
                        ColumnType.UInt, ColumnType.ULong});
        assertEquals((short) 255, decoded[0]);
        assertEquals(65535, decoded[1]);
        assertEquals(4294967295L, decoded[2]);
        assertEquals(new BigInteger("18446744073709551615"), decoded[3]);
    }

    @Test
    void signedFloatsBooleansAndBinaryRetainTheirTypes() {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        column(wire, 0); wire.write(0xFE);
        column(wire, 1); column(wire, 0xFFFE);
        column(wire, 2); i64(wire, -123456L);
        column(wire, 3); i32(wire, Float.floatToIntBits(1.25f));
        column(wire, 4); i64(wire, Double.doubleToLongBits(-3.5d));
        column(wire, 5); wire.write(1);
        column(wire, 6); variable(wire, new byte[]{0, (byte) 0xFF, 42});
        Object[] decoded = FlatGeobufPropertyDecoder.decode(
                wire.toByteArray(), new byte[]{ColumnType.Byte, ColumnType.Short, ColumnType.Long,
                        ColumnType.Float, ColumnType.Double, ColumnType.Bool, ColumnType.Binary});
        assertEquals((byte) -2, decoded[0]);
        assertEquals((short) -2, decoded[1]);
        assertEquals(-123456L, decoded[2]);
        assertEquals(1.25f, decoded[3]);
        assertEquals(-3.5d, decoded[4]);
        assertEquals(true, decoded[5]);
        assertArrayEquals(new byte[]{0, (byte) 0xFF, 42}, (byte[]) decoded[6]);
    }

    @Test
    void utf8DatetimeAndJsonAreNotCoercedIntoNumbers() {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        column(wire, 0); variable(wire, "niño".getBytes(StandardCharsets.UTF_8));
        column(wire, 1); variable(wire, "2026-10-17T11:30:00Z".getBytes(StandardCharsets.UTF_8));
        column(wire, 2); variable(wire, "{\"safe\":true}".getBytes(StandardCharsets.UTF_8));
        Object[] decoded = FlatGeobufPropertyDecoder.decode(wire.toByteArray(),
                new byte[]{ColumnType.String, ColumnType.DateTime, ColumnType.Json});
        assertEquals("niño", decoded[0]);
        assertEquals("2026-10-17T11:30:00Z", decoded[1]);
        assertEquals("{\"safe\":true}", decoded[2]);
    }

    @Test
    void malformedAndAmbiguousPropertyBytesMustFailClosed() {
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0}, new byte[]{ColumnType.Int}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{1, 0, 1}, new byte[]{ColumnType.Bool}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0, 0, 2}, new byte[]{ColumnType.Bool}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0, 0, 3}, new byte[]{ColumnType.Int}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0, 0, 0, 0, 0, 0, 1}, new byte[]{ColumnType.String}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0, 0, -1, -1, -1, -1},
                        new byte[]{ColumnType.Binary}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{0, 0, 2, 0, 0, 0, (byte) 0xC3, 0},
                        new byte[]{ColumnType.String}));
        ByteArrayOutputStream repeated = new ByteArrayOutputStream();
        column(repeated, 0); repeated.write(1);
        column(repeated, 0); repeated.write(0);
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(repeated.toByteArray(), new byte[]{ColumnType.Bool}));
        assertThrows(IllegalArgumentException.class, () ->
                FlatGeobufPropertyDecoder.decode(new byte[]{}, new byte[]{(byte) 100}));
    }

    @Test
    void columnBindingsPreserveUnsignedAndBinarySemantics() {
        assertEquals(Long.class, FlatGeobufPropertyDecoder.bindingFor(ColumnType.UInt));
        assertEquals(BigInteger.class, FlatGeobufPropertyDecoder.bindingFor(ColumnType.ULong));
        assertEquals(byte[].class, FlatGeobufPropertyDecoder.bindingFor(ColumnType.Binary));
        assertEquals(String.class, FlatGeobufPropertyDecoder.bindingFor(ColumnType.DateTime));
        assertTrue(FlatGeobufPropertyDecoder.decode(new byte[0],
                new byte[]{ColumnType.Bool})[0] == null);
    }

    private static void column(ByteArrayOutputStream out, int value) {
        out.write(value & 255); out.write((value >>> 8) & 255);
    }

    private static void i32(ByteArrayOutputStream out, int value) {
        for (int i = 0; i < 4; i++) out.write((value >>> (8 * i)) & 255);
    }

    private static void i64(ByteArrayOutputStream out, long value) {
        for (int i = 0; i < 8; i++) out.write((int) ((value >>> (8 * i)) & 255));
    }

    private static void variable(ByteArrayOutputStream out, byte[] data) {
        i32(out, data.length); out.writeBytes(data);
    }
}
