package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Point;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Genuine OS-installed ogr2ogr FlatGeobuf -> CATGIS typed feature round-trip. */
@Tag("native-gdal")
class FlatGeobufNativeGdalPropertiesIntegrationTest {
    @TempDir Path temp;

    @Test
    void genuineIndexedAndSequentialFgbPreserveAllPropertiesAndCoordinates() throws Exception {
        assertEquals("1", System.getenv("CATGIS_NATIVE_GDAL_REQUIRED"),
                "Only a provisioned real-native GDAL job may claim this proof.");
        String nativeBin = System.getenv("CATGIS_GDAL_BIN");
        assertNotNull(nativeBin, "Native GDAL bin must be independently provisioned");
        Path ogr = Path.of(nativeBin, "ogr2ogr").toAbsolutePath().normalize();
        assertTrue(Files.isExecutable(ogr), "Real ogr2ogr required: " + ogr);

        Path input = temp.resolve("typed.geojson");
        Files.writeString(input, """
                {"type":"FeatureCollection","features":[
                {"type":"Feature","properties":{"name":"niño","score":12.5,"count":3},
                 "geometry":{"type":"Point","coordinates":[-70.12,-45.34]}},
                {"type":"Feature","properties":{"name":"segunda","score":-2.25,"count":null},
                 "geometry":{"type":"Point","coordinates":[-70.13,-45.35]}}
                ]}
                """, StandardCharsets.UTF_8);

        for (String index : List.of("YES", "NO")) {
            Path destination = temp.resolve("typed-" + index + ".fgb");
            runNative(List.of(ogr.toString(), "-f", "FlatGeobuf",
                    "-lco", "SPATIAL_INDEX=" + index,
                    destination.toString(), input.toString()), "ogr-" + index + ".log");
            assertTrue(Files.size(destination) > 80, "GDAL must create actual FGB bytes");
            ValidationResult check = FlatGeobufLoader.validateFile(destination.toFile());
            assertTrue(check.isValid(), check.message());
            ShapefileData result = FlatGeobufLoader.load(destination.toFile());
            assertEquals(2, result.getFeatures().size(),
                    "Indexing must never change actual source cardinality");

            SimpleFeature first = result.getFeatures().stream()
                    .filter(f -> "niño".equals(f.getAttribute("name")))
                    .findFirst().orElseThrow();
            SimpleFeature second = result.getFeatures().stream()
                    .filter(f -> "segunda".equals(f.getAttribute("name")))
                    .findFirst().orElseThrow();
            assertEquals(12.5, ((Number) first.getAttribute("score")).doubleValue(), 1e-9);
            assertEquals(3L, ((Number) first.getAttribute("count")).longValue());
            assertEquals(-2.25, ((Number) second.getAttribute("score")).doubleValue(), 1e-9);
            assertNull(second.getAttribute("count"),
                    "Absent optional attribute must not become an invented numeric zero");

            Point point = (Point) first.getDefaultGeometry();
            assertNotNull(point);
            assertEquals(-70.12, point.getX(), 1e-9);
            assertEquals(-45.34, point.getY(), 1e-9);
        }
    }

    private void runNative(List<String> args, String logName) throws Exception {
        Path log = temp.resolve(logName);
        Process command = new ProcessBuilder(args)
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        if (!command.waitFor(90, TimeUnit.SECONDS)) {
            command.destroyForcibly();
            throw new IOException("Native ogr2ogr exceeded 90 seconds.");
        }
        assertEquals(0, command.exitValue(),
                () -> "Real ogr2ogr failed: " + String.join(" ", args)
                        + "\n" + readLog(log));
    }

    private static String readLog(Path log) {
        try {
            return Files.readString(log, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "No se pudo leer el registro de GDAL.";
        }
    }
}
