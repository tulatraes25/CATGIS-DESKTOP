package ar.com.catgis;

import org.apache.parquet.format.FieldRepetitionType;
import org.apache.parquet.format.KeyValue;
import org.apache.parquet.format.SchemaElement;
import org.apache.parquet.format.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoParquetMetadataContractTest {
    @TempDir Path temp;

    @Test
    void genuineEmptyFooterReportsActualColumnsAndZeroRowsWithoutInventedExtentOrType() throws Exception {
        Path file = GeoParquetTestFixtures.empty(temp.resolve("empty.parquet"));
        var result = GeoParquetReader.readMetadata(file.toFile());
        assertEquals("1.1.0", result.version());
        assertEquals(0, result.rowCount());
        assertEquals(List.of("shape", "nombre"), result.columns());
        assertEquals("shape", result.geometryColumn());
        assertEquals("No declarados", result.geometryType());
        assertNull(result.bbox());
        assertEquals("OGC:CRS84", result.crs());
        assertEquals(GeoParquetReader.CrsState.DEFAULT_CRS84,
                result.geometryColumns().get("shape").crsState());
        assertTrue(GeoParquetReader.getSummary(file.toFile()).contains("No se cargo una capa"));
    }

    @Test
    void explicitNullCrsNeverBecomesDefaultGeographicCrs() throws Exception {
        var result = read(column("\"crs\":null"));
        assertNull(result.crs());
        assertEquals(GeoParquetReader.CrsState.EXPLICIT_UNKNOWN,
                result.geometryColumns().get("shape").crsState());
        String summary = GeoParquetReader.getSummary(temp.resolve("case.parquet").toFile());
        assertTrue(summary.contains("crs=null"));
        assertFalse(summary.contains("EPSG:4326"));
        assertFalse(summary.contains("OGC:CRS84"));
    }

    @Test
    void declaredProjjsonAndOptionalEpochRemainDeclarationsWithoutAutomaticReprojection() throws Exception {
        var result = read(column("\"crs\":{\"type\":\"ProjectedCRS\",\"name\":\"Test declaration\"},"
                + "\"epoch\":2021.5,\"bbox\":[500000,4500000,500100,4500200]"));
        assertEquals(GeoParquetReader.CrsState.PROJJSON_DECLARED,
                result.geometryColumns().get("shape").crsState());
        assertTrue(result.crs().contains("Test declaration"));
        assertTrue(result.geometryColumns().get("shape").rawJson().contains("2021.5"));
        assertArrayEquals(new double[] {500000, 4500000, 500100, 4500200}, result.bbox());
    }

    @Test
    void geographicAntimeridianAndThreeDimensionalDeclaredBoundsArePreserved() throws Exception {
        assertArrayEquals(new double[] {170, -10, -170, 10},
                read(column("\"bbox\":[170,-10,-170,10]")).bbox());
        assertArrayEquals(new double[] {1, 2, 3, 4, 5, 6},
                read(column("\"bbox\":[1,2,3,4,5,6]")).bbox());
    }

    @Test
    void resultsOwnTheirCollectionsAndBoundsAndNeverMutateSourceBytes() throws Exception {
        var result = read(column("\"bbox\":[1,2,3,4]"));
        Path file = temp.resolve("case.parquet");
        byte[] before = Files.readAllBytes(file);
        result.bbox()[0] = 999;
        result.geometryColumns().get("shape").bbox()[1] = 999;
        assertArrayEquals(new double[] {1, 2, 3, 4}, result.bbox());
        assertThrows(UnsupportedOperationException.class, () -> result.columns().add("fake"));
        assertThrows(UnsupportedOperationException.class, () -> result.geometryColumns().clear());
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void allShortOrMagicOnlyContainersAndWrongFooterSignaturesFail() throws Exception {
        for (int size = 0; size < 24; size++) {
            byte[] bytes = new byte[size];
            if (size >= 4) { System.arraycopy(new byte[] {'P', 'A', 'R', '1'}, 0, bytes, 0, 4); }
            Path file = temp.resolve("short.parquet");
            Files.write(file, bytes);
            assertFalse(GeoParquetReader.isGeoParquet(file.toFile()));
            assertThrows(IOException.class, () -> GeoParquetReader.getSummary(file.toFile()));
        }
        Path valid = GeoParquetTestFixtures.empty(temp.resolve("footer.parquet"));
        byte[] bytes = Files.readAllBytes(valid);
        bytes[bytes.length - 1] = 'E';
        Files.write(valid, bytes);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(valid.toFile()));
    }

    @Test
    void footerLengthsAreUnsignedBoundedAndCannotPointBeforeTheHeader() throws Exception {
        for (int length : new int[] {0, -1, 1000000}) {
            Path file = GeoParquetTestFixtures.empty(temp.resolve("length.parquet"));
            byte[] bytes = Files.readAllBytes(file);
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - 8, length);
            Files.write(file, bytes);
            assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(file.toFile()));
        }
        Path sparse = temp.resolve("large-footer.parquet");
        try (RandomAccessFile file = new RandomAccessFile(sparse.toFile(), "rw")) {
            file.writeBytes("PAR1");
            int oversized = GeoParquetReader.MAX_FOOTER_BYTES + 1;
            file.setLength(oversized + 12L);
            file.seek(oversized + 4L);
            file.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(oversized).array());
            file.writeBytes("PAR1");
        }
        IOException failure = assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(sparse.toFile()));
        assertTrue(failure.getMessage().contains("4 MiB"));
    }

    @Test
    void ordinaryParquetAndMalformedOrDuplicateGeoCannotProduceASuccessfulSummary() throws Exception {
        var noGeo = GeoParquetTestFixtures.footer(null);
        Path plain = GeoParquetTestFixtures.write(temp.resolve("plain.parquet"), noGeo);
        assertFalse(GeoParquetReader.isGeoParquet(plain.toFile()));
        for (String geo : List.of("", "null", "[]", "{", GeoParquetTestFixtures.GEO + " {}",
                GeoParquetTestFixtures.GEO.replace("\"version\":", "\"version\":\"1.0.0\",\"version\":"))) {
            assertThrows(IOException.class, () -> read(geo));
        }
        var duplicated = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        duplicated.addToKey_value_metadata(new KeyValue("geo").setValue(GeoParquetTestFixtures.GEO));
        Path file = GeoParquetTestFixtures.write(temp.resolve("duplicate.parquet"), duplicated);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(file.toFile()));
    }

    @Test
    void declaredGeometryMustBeAnExistingRootLevelNonRepeatedBinaryColumn() throws Exception {
        for (String geo : List.of(GeoParquetTestFixtures.GEO.replace("shape", "missing"),
                GeoParquetTestFixtures.GEO.replace("\"primary_column\":\"shape\"", "\"primary_column\":\"missing\""))) {
            assertThrows(IOException.class, () -> read(geo));
        }
        var footer = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        footer.getSchema().get(1).setType(Type.INT32);
        Path wrongType = GeoParquetTestFixtures.write(temp.resolve("type.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(wrongType.toFile()));
        footer.getSchema().get(1).setType(Type.BYTE_ARRAY).setRepetition_type(FieldRepetitionType.REPEATED);
        Path repeated = GeoParquetTestFixtures.write(temp.resolve("repeated.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(repeated.toFile()));
    }

    @Test
    void unsupportedVersionEncodingTypesAndMalformedSpatialFieldsHaveExplicitFailures() throws Exception {
        for (String geo : List.of(GeoParquetTestFixtures.GEO.replace("1.1.0", "9.0.0"),
                GeoParquetTestFixtures.GEO.replace("WKB", "point"),
                GeoParquetTestFixtures.GEO.replace("[]", "[\"Triangle\"]"),
                column("\"crs\":\"EPSG:4326\""), column("\"crs\":{}"), column("\"crs\":42"),
                column("\"bbox\":null"), column("\"bbox\":[1,2,3]"), column("\"bbox\":[1,2,3,1]"),
                column("\"bbox\":[1,2,1e999,4]"), column("\"bbox\":[1,2,\"3\",4]"),
                column("\"orientation\":\"clockwise\""), column("\"edges\":\"invented\""), column("\"epoch\":\"yesterday\""))) {
            assertThrows(IOException.class, () -> read(geo), geo);
        }
        assertEquals("1.0.0", read(GeoParquetTestFixtures.GEO.replace("1.1.0", "1.0.0")).version());
    }

    @Test
    void malformedSchemaAndInconsistentRowCountsAreRejected() throws Exception {
        var footer = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        footer.setNum_rows(1);
        Path mismatch = GeoParquetTestFixtures.write(temp.resolve("rows.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(mismatch.toFile()));
        footer.setNum_rows(-1);
        Path negative = GeoParquetTestFixtures.write(temp.resolve("negative.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(negative.toFile()));
        footer.setNum_rows(0);
        footer.getSchema().get(0).setNum_children(3);
        Path truncated = GeoParquetTestFixtures.write(temp.resolve("schema.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(truncated.toFile()));
        footer.getSchema().get(0).setNum_children(2);
        footer.getSchema().get(2).setName("shape");
        Path duplicate = GeoParquetTestFixtures.write(temp.resolve("duplicate-field.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(duplicate.toFile()));
    }

    @Test
    void jsonSizeNestingAndSchemaFieldBudgetsFailClosed() throws Exception {
        assertThrows(IOException.class, () -> read(column("\"unused\":\"" + "x".repeat(GeoParquetReader.MAX_GEO_JSON_CHARS) + "\"")));
        assertThrows(IOException.class, () -> read(column("\"unused\":" + "[".repeat(40) + "0" + "]".repeat(40))));
        var footer = GeoParquetTestFixtures.footer(GeoParquetTestFixtures.GEO);
        List<SchemaElement> fields = new ArrayList<>(footer.getSchema());
        for (int i = 0; i < 4096; i++) {
            fields.add(new SchemaElement("extra" + i).setType(Type.INT32)
                    .setRepetition_type(FieldRepetitionType.OPTIONAL));
        }
        fields.get(0).setNum_children(fields.size() - 1);
        footer.setSchema(fields);
        Path large = GeoParquetTestFixtures.write(temp.resolve("large-schema.parquet"), footer);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(large.toFile()));
    }

    @Test
    void cancellationPreservesInterruptAndNeverReturnsMetadata() throws Exception {
        Path file = GeoParquetTestFixtures.empty(temp.resolve("cancel.parquet"));
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedIOException.class, () -> GeoParquetReader.readMetadata(file.toFile()));
            assertFalse(GeoParquetReader.isGeoParquet(file.toFile()));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void successfulAndFailedInspectionsCloseAllFileHandles() throws Exception {
        Path file = GeoParquetTestFixtures.empty(temp.resolve("handles.parquet"));
        byte[] before = Files.readAllBytes(file);
        for (int i = 0; i < 40; i++) { assertNotNull(GeoParquetReader.readMetadata(file.toFile())); }
        assertArrayEquals(before, Files.readAllBytes(file));
        Path moved = temp.resolve("moved.parquet");
        Files.move(file, moved);
        Files.write(moved, new byte[] {'P', 'A', 'R', '1'});
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(moved.toFile()));
        Files.delete(moved);
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(null));
        assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(temp.toFile()));
    }

    private GeoParquetReader.GeoParquetMetadata read(String geo) throws IOException {
        return GeoParquetReader.readMetadata(GeoParquetTestFixtures.write(temp.resolve("case.parquet"),
                GeoParquetTestFixtures.footer(geo)).toFile());
    }

    private static String column(String extra) {
        return "{\"version\":\"1.1.0\",\"primary_column\":\"shape\",\"columns\":{\"shape\":{"
                + "\"encoding\":\"WKB\",\"geometry_types\":[]," + extra + "}}}";
    }
}
