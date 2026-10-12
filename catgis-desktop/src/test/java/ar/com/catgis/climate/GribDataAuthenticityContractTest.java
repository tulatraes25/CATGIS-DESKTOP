package ar.com.catgis.climate;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GribDataAuthenticityContractTest {

    @Test
    void gribMustRemainExplicitlyUnsupportedUntilRealDataIsCertified() {
        assertFalse(GribLoader.isCertified(),
                "GRIB must not be claimed as certified without a real decoder/fixture.");
        assertFalse(NetCdfLoader.supportsGrib(),
                "The GRIB file type must not be advertised as supported prematurely.");
    }

    @Test
    void importerMustNotFabricateRasterSamplesAsScientificData() throws Exception {
        String source = Files.readString(
                Path.of("src", "ar", "com", "catgis", "climate", "GribLoader.java"),
                StandardCharsets.UTF_8);
        assertFalse(source.contains("new BufferedImage("),
                "Legacy GRIB fake checkerboard rendering must never return.");
        assertFalse(source.contains("GridCoverageFactory"),
                "Review authentic GRIB decoding and regression fixtures before enabling coverage creation.");
        assertTrue(source.contains("No se generara ninguna capa simulada."),
                "A user attempting unsupported GRIB import needs explicit feedback.");
    }
}
