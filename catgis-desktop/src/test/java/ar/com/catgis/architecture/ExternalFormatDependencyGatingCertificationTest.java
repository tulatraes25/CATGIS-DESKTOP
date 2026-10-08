package ar.com.catgis.architecture;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalFormatDependencyGatingCertificationTest {

    private static final Path TEST_ROOT = Path.of("src", "test", "java", "ar", "com", "catgis");
    private static final List<String> REAL_GDAL_TESTS = List.of(
            "GeoPackageRealTest.java",
            "SpatiaLiteRealTest.java",
            "FlatGeobufRealTest.java"
    );

    @Test
    void gdalDependentRealTestsNeverSilentlyPassWhenOgr2ogrIsMissing() throws Exception {
        int explicitAvailabilityAssumptions = 0;

        for (String fileName : REAL_GDAL_TESTS) {
            String source = Files.readString(TEST_ROOT.resolve(fileName), StandardCharsets.UTF_8);

            assertFalse(source.contains("if (!ogr2ogrAvailable()) return"),
                    fileName + " silently returns when ogr2ogr is unavailable");
            assertTrue(source.contains("assumeOgr2ogrAvailable();"),
                    fileName + " must invoke the explicit missing-ogr2ogr skip gate");

            explicitAvailabilityAssumptions++;
        }

        assertEquals(3, explicitAvailabilityAssumptions);
    }

    @Test
    void expectedGdalDependentTestCountRemainsExplicit() throws Exception {
        int testCount = 0;
        for (String fileName : REAL_GDAL_TESTS) {
            String source = Files.readString(TEST_ROOT.resolve(fileName), StandardCharsets.UTF_8);
            testCount += countOccurrences(source, "@Test");
        }
        assertEquals(12, testCount,
                "GDAL-dependent real-test count changed; review skip budget and dependency certification");
    }

    private static int countOccurrences(String source, String token) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
