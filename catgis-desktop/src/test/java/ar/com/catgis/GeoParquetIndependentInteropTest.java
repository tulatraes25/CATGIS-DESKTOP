package ar.com.catgis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Mandatory separate gate: fixtures are written and reopened by PyArrow, not this Java library. */
@Tag("geoparquet-interop")
class GeoParquetIndependentInteropTest {
    @TempDir Path temp;

    @Test
    void actualPointsWithUnicodeColumnsNullsAndMultipleRowGroupsHaveExactMetadata() throws Exception {
        Path source = fixture("points.parquet");
        byte[] original = Files.readAllBytes(source);
        Path file = temp.resolve("datos con espacios y \u00f1.parquet");
        Files.copy(source, file);
        var metadata = GeoParquetReader.readMetadata(file.toFile());
        assertEquals(3, metadata.rowCount());
        assertEquals("1.1.0", metadata.version());
        assertEquals("ubicaci\u00f3n", metadata.geometryColumn());
        assertEquals("Point", metadata.geometryType());
        assertEquals(List.of("ubicaci\u00f3n", "nombre", "valor"), metadata.columns());
        assertEquals("OGC:CRS84", metadata.crs());
        assertArrayEquals(new double[] {-67.5, -45.8, -67.4, -45.7}, metadata.bbox());
        assertTrue(GeoParquetReader.getSummary(file.toFile()).contains("Filas declaradas: 3"));
        assertArrayEquals(original, Files.readAllBytes(file));
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void actualProjectedPolygonsAndMultipleGeometryColumnsAreNotRelabeledAsPoints() throws Exception {
        var metadata = GeoParquetReader.readMetadata(fixture("polygons.parquet").toFile());
        assertEquals(2, metadata.rowCount());
        assertEquals("1.0.0", metadata.version());
        assertEquals("outline", metadata.geometryColumn());
        assertEquals("Polygon", metadata.geometryType());
        assertEquals(List.of("outline", "nombre", "parcelas"), metadata.columns());
        assertEquals(GeoParquetReader.CrsState.PROJJSON_DECLARED,
                metadata.geometryColumns().get("outline").crsState());
        var crs = new ObjectMapper().readTree(metadata.crs());
        assertEquals("ProjectedCRS", crs.path("type").asText());
        assertEquals("EPSG", crs.path("id").path("authority").asText());
        assertEquals(3857, crs.path("id").path("code").asInt());
        assertArrayEquals(new double[] {1000, 2000, 1300, 2500}, metadata.bbox());

        var multiple = GeoParquetReader.readMetadata(fixture("multiple.parquet").toFile());
        assertEquals(1, multiple.rowCount());
        assertEquals("boundary", multiple.geometryColumn());
        assertEquals(2, multiple.geometryColumns().size());
        assertEquals(List.of("Point"), multiple.geometryColumns().get("center").geometryTypes());
        assertArrayEquals(new double[] {0.5, 0.5, 0.5, 0.5}, multiple.geometryColumns().get("center").bbox());
        assertArrayEquals(new double[] {0, 0, 1, 1}, multiple.bbox());
    }

    @Test
    void realEmptyAndUnknownCrsFilesRetainDistinctUnknownAndDefaultStates() throws Exception {
        var unknown = GeoParquetReader.readMetadata(fixture("unknown.parquet").toFile());
        assertEquals(1, unknown.rowCount());
        assertNull(unknown.crs());
        assertNull(unknown.bbox());
        assertEquals(GeoParquetReader.CrsState.EXPLICIT_UNKNOWN,
                unknown.geometryColumns().get("shape").crsState());
        var empty = GeoParquetReader.readMetadata(fixture("empty.parquet").toFile());
        assertEquals(0, empty.rowCount());
        assertEquals("OGC:CRS84", empty.crs());
        assertNull(empty.bbox());
        assertEquals("No declarados", empty.geometryType());
        assertEquals(List.of("shape", "nombre"), empty.columns());
    }

    @Test
    void ordinaryParquetMalformedGeoUnknownEncodingMissingColumnAndTruncationFail() throws Exception {
        for (String name : List.of("plain.parquet", "missing-column.parquet", "unsupported-encoding.parquet",
                "bad-geo-json.parquet", "truncated.parquet")) {
            Path file = fixture(name);
            byte[] before = Files.readAllBytes(file);
            assertThrows(IOException.class, () -> GeoParquetReader.readMetadata(file.toFile()), name);
            assertFalse(GeoParquetReader.isGeoParquet(file.toFile()), name);
            assertArrayEquals(before, Files.readAllBytes(file));
        }
    }

    private static Path fixture(String name) {
        String root = System.getenv("CATGIS_GEOPARQUET_FIXTURES");
        assertNotNull(root, "Independent real fixtures must be provisioned; never skip this gate.");
        Path file = Path.of(root).resolve(name);
        assertTrue(Files.isRegularFile(file), "Missing independent fixture: " + file);
        return file;
    }
}
