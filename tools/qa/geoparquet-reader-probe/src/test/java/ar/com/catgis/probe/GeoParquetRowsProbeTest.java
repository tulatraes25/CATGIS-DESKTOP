package ar.com.catgis.probe;

import ar.com.catgis.GeoParquetReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GeoParquetRowsProbeTest {
    @TempDir Path temporary;

    private static Path file(String name) {
        String root = System.getenv("CATGIS_GEOPARQUET_FIXTURES");
        assertNotNull(root, "Independent Arrow fixtures are required; never skip.");
        Path path = Path.of(root).resolve(name);
        assertTrue(Files.isRegularFile(path), "Missing real fixture: " + name);
        return path;
    }

    @Test
    void pointRowsPreserveActualCoordinatesUnicodeNullsAndRowGroups() throws Exception {
        var scan = GeoParquetRowsProbe.read(file("points.parquet"));
        assertEquals(3, scan.rows().size());
        assertEquals(2, scan.rowGroups());
        assertEquals("ubicaci\u00f3n", scan.metadata().geometryColumn());
        Point north = (Point) scan.rows().get(0).get("ubicaci\u00f3n");
        assertEquals(-67.5, north.getX(), 1e-12);
        assertEquals(-45.8, north.getY(), 1e-12);
        assertEquals("\u00e1rea norte", scan.rows().get(0).get("nombre"));
        assertEquals(1.25, scan.rows().get(0).get("valor"));
        assertNull(scan.rows().get(1).get("ubicaci\u00f3n"));
        assertNull(scan.rows().get(1).get("nombre"));
        assertNull(scan.rows().get(1).get("valor"));
        Point south = (Point) scan.rows().get(2).get("ubicaci\u00f3n");
        assertEquals(-67.4, south.getX(), 1e-12);
        assertEquals(-45.7, south.getY(), 1e-12);
        assertEquals(-7.5, scan.rows().get(2).get("valor"));
    }

    @Test
    void projectedPolygonsKeepExactAreasCoordinatesAndIntegerNullAttributes() throws Exception {
        var scan = GeoParquetRowsProbe.read(file("polygons.parquet"));
        assertEquals(2, scan.rows().size());
        Geometry first = (Geometry) scan.rows().get(0).get("outline");
        Geometry second = (Geometry) scan.rows().get(1).get("outline");
        assertEquals("Polygon", first.getGeometryType());
        assertEquals(10000, first.getArea(), 1e-9);
        assertEquals(30000, second.getArea(), 1e-9);
        assertEquals(1000, first.getEnvelopeInternal().getMinX(), 1e-9);
        assertEquals(2500, second.getEnvelopeInternal().getMaxY(), 1e-9);
        assertEquals("Zona \u00f1", scan.rows().get(0).get("nombre"));
        assertEquals(10L, scan.rows().get(0).get("parcelas"));
        assertNull(scan.rows().get(1).get("parcelas"));
        var crs = scan.metadata().geometryColumns().get("outline");
        assertEquals(GeoParquetReader.CrsState.PROJJSON_DECLARED, crs.crsState());
        assertEquals(3857, new ObjectMapper().readTree(crs.crs()).path("id").path("code").intValue());
    }

    @Test
    void defaultCrsAndExplicitUnknownRemainDistinctWithoutInventingAnSrid() throws Exception {
        var known = GeoParquetRowsProbe.read(file("points.parquet"));
        var unknown = GeoParquetRowsProbe.read(file("unknown.parquet"));
        assertEquals(GeoParquetReader.CrsState.DEFAULT_CRS84,
                known.metadata().geometryColumns().get("ubicaci\u00f3n").crsState());
        assertEquals(GeoParquetReader.CrsState.EXPLICIT_UNKNOWN,
                unknown.metadata().geometryColumns().get("shape").crsState());
        Point point = (Point) unknown.rows().get(0).get("shape");
        assertEquals(3, point.getX());
        assertEquals(4, point.getY());
        assertEquals(0, point.getSRID(), "The probe does not resolve a CRS or assign a fake EPSG code.");
        assertNull(unknown.rows().get(0).get("value"));
    }

    @Test
    void bothGeometryColumnsAreMaterializedAndPrimaryChoiceIsPreserved() throws Exception {
        var scan = GeoParquetRowsProbe.read(file("multiple.parquet"));
        assertEquals("boundary", scan.metadata().geometryColumn());
        assertEquals(1, scan.rows().size());
        assertInstanceOf(Point.class, scan.rows().get(0).get("center"));
        assertEquals(0.5, ((Point) scan.rows().get(0).get("center")).getX());
        assertEquals(1, ((Geometry) scan.rows().get(0).get("boundary")).getArea());
    }

    @Test
    void genuineEmptyArrowFileYieldsNoInventedFeatures() throws Exception {
        var scan = GeoParquetRowsProbe.read(file("empty.parquet"));
        assertTrue(scan.rows().isEmpty());
        assertEquals(0, scan.metadata().rowCount());
        assertEquals(List.of("shape", "nombre"), scan.metadata().columns());
    }

    @Test
    void threeCodecsPreservePrimitiveTypesLargeIntegersNegativeZeroAndNulls() throws Exception {
        for (String codec : List.of("snappy", "gzip", "uncompressed")) {
            var scan = GeoParquetRowsProbe.read(file("typed-" + codec + ".parquet"));
            assertEquals(3, scan.rows().size());
            Map<String, Object> row = scan.rows().get(0);
            assertEquals(9007199254740993L, assertInstanceOf(Long.class, row.get("counter")));
            assertEquals(Integer.MIN_VALUE, assertInstanceOf(Integer.class, row.get("small")));
            assertEquals(true, assertInstanceOf(Boolean.class, row.get("enabled")));
            assertEquals(1.25f, assertInstanceOf(Float.class, row.get("level")));
            assertEquals(Double.doubleToRawLongBits(-0.0),
                    Double.doubleToRawLongBits(assertInstanceOf(Double.class, row.get("amplitude"))));
            assertEquals("\u00f1\u00e1rea", row.get("label"));
            assertNull(scan.rows().get(1).get("counter"));
            assertNull(scan.rows().get(1).get("enabled"));
            assertNull(scan.rows().get(1).get("level"));
            assertNull(scan.rows().get(1).get("shape"));
            assertEquals(-7L, scan.rows().get(2).get("counter"));
            assertThrows(UnsupportedOperationException.class, () -> row.put("counter", 0));
        }
    }

    @Test
    void malformedMetadataAndTruncationStillFailThroughTheActualM1Reader() {
        for (String name : List.of("plain.parquet", "missing-column.parquet", "unsupported-encoding.parquet",
                "bad-geo-json.parquet", "truncated.parquet")) {
            assertThrows(IOException.class, () -> GeoParquetRowsProbe.read(file(name)), name);
        }
    }

    @Test
    void unsupportedNestedAttributesAndCodecsAreExplicitlyRejected() {
        var nested = assertThrows(IOException.class, () -> GeoParquetRowsProbe.read(file("nested.parquet")));
        assertTrue(nested.getMessage().contains("Nested/repeated"), nested.toString());
        var codec = assertThrows(IOException.class, () -> GeoParquetRowsProbe.read(file("zstd.parquet")));
        assertTrue(codec.getMessage().contains("Codec outside"), codec.toString());
    }

    @Test
    void badWkbTrailingBytesAndDamagedPageCannotReturnAPartialSuccessfulScan() throws Exception {
        assertEquals(3, GeoParquetRowsProbe.read(file("checksummed.parquet")).rows().size());
        for (String name : List.of("bad-wkb.parquet", "trailing-wkb.parquet", "bad-page.parquet")) {
            assertThrows(IOException.class, () -> GeoParquetRowsProbe.read(file(name)), name);
        }
    }

    @Test
    void cancellationAndRowBudgetFailWithoutProducingAScan() {
        assertThrows(InterruptedIOException.class,
                () -> GeoParquetRowsProbe.read(file("points.parquet"), () -> true, 100));
        AtomicInteger checks = new AtomicInteger();
        assertThrows(InterruptedIOException.class,
                () -> GeoParquetRowsProbe.read(file("points.parquet"), () -> checks.incrementAndGet() > 3, 100));
        assertThrows(IOException.class,
                () -> GeoParquetRowsProbe.read(file("points.parquet"), () -> false, 1));
        assertThrows(IOException.class,
                () -> GeoParquetRowsProbe.read(file("points.parquet"), () -> false, -1));
    }

    @Test
    void repeatedSuccessAndFailureReleaseHandlesAndPreserveExactFileBytes() throws Exception {
        Path source = temporary.resolve("rows with spaces \u00f1.parquet");
        Files.copy(file("points.parquet"), source);
        byte[] original = Files.readAllBytes(source);
        for (int i = 0; i < 15; i++) { assertEquals(3, GeoParquetRowsProbe.read(source).rows().size()); }
        assertArrayEquals(original, Files.readAllBytes(source));
        Path moved = temporary.resolve("moved.parquet");
        Files.move(source, moved);
        Files.delete(moved);
        Files.copy(file("bad-wkb.parquet"), source);
        byte[] bad = Files.readAllBytes(source);
        assertThrows(IOException.class, () -> GeoParquetRowsProbe.read(source));
        assertArrayEquals(bad, Files.readAllBytes(source));
        Files.move(source, moved);
        Files.delete(moved);
    }

    @SuppressWarnings("removal")
    @Test
    void localReadNeedsNoJavaSocketConnectionOrExternalService() throws Exception {
        // JDK17-only diagnostic guard; not a security boundary for arbitrary native code.
        SecurityManager previous = System.getSecurityManager();
        AtomicInteger requests = new AtomicInteger();
        SecurityManager denyConnections = new SecurityManager() {
            @Override public void checkPermission(Permission permission) {}
            @Override public void checkConnect(String host, int port) {
                requests.incrementAndGet();
                throw new SecurityException("Network access forbidden during row probe.");
            }
            @Override public void checkConnect(String host, int port, Object context) { checkConnect(host, port); }
        };
        try {
            System.setSecurityManager(denyConnections);
            assertEquals(3, GeoParquetRowsProbe.read(file("points.parquet")).rows().size());
            assertEquals(0, requests.get());
        } finally {
            System.setSecurityManager(previous);
        }
    }

    @Test
    void probeRunsOnJava17AndCannotLoadTheDesktopApplication() {
        assertEquals("17", System.getProperty("java.specification.version"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("ar.com.catgis.Main"));
    }

    @AfterAll
    static void recordBoundedJvmDiagnostic() {
        long peaks = ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .mapToLong(pool -> pool.getPeakUsage().getUsed()).sum();
        System.out.println("PROBE_JVM=" + System.getProperty("java.version")
                + " OS=" + System.getProperty("os.name") + " ARCH=" + System.getProperty("os.arch"));
        System.out.println("PROBE_MAX_HEAP_BYTES=" + Runtime.getRuntime().maxMemory()
                + " SUM_HEAP_POOL_PEAKS_BYTES=" + peaks + " (diagnostic, not process RSS)");
    }
}
