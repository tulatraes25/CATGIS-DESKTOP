package ar.com.catgis.climate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#162: CF grid_mapping, units and packaging without assuming WGS84. */
class NetCdfCfSupportContractTest {

    @Test
    void epsgCodeAttributeWins() {
        var resolution = NetCdfCfSupport.resolveCrs("latitude_longitude", "EPSG:32719",
                "", "degrees_east", "degrees_north");
        assertTrue(resolution.hasCrs());
        assertEquals("EPSG:32719", resolution.crsCode());
        assertEquals("CF epsg_code", resolution.provenance());
    }

    @Test
    void latitudeLongitudeGridMappingMapsToWgs84WithProvenance() {
        var resolution = NetCdfCfSupport.resolveCrs("latitude_longitude", null, null,
                "degrees_east", "degrees_north");
        assertEquals("EPSG:4326", resolution.crsCode());
        assertEquals("CF latitude_longitude", resolution.provenance());
    }

    @Test
    void rotatedGridWithoutCrsAttributesFailsClosed() {
        var resolution = NetCdfCfSupport.resolveCrs("rotated_latitude_longitude", null, null,
                "degrees_east", "degrees_north");
        assertFalse(resolution.hasCrs(), "rotated grids must not be relabeled WGS84");
        assertTrue(resolution.provenance().contains("no se asume WGS84"));
    }

    @Test
    void degreeCoordinatesWithoutGridMappingAreAcceptedExplicitly() {
        var resolution = NetCdfCfSupport.resolveCrs("", null, null,
                "degrees_east", "degrees_north");
        assertEquals("EPSG:4326", resolution.crsCode());
        assertEquals("CF degree coordinates", resolution.provenance());
    }

    @Test
    void missingUnitsAndMappingFailClosed() {
        var resolution = NetCdfCfSupport.resolveCrs(null, null, null, "", "");
        assertFalse(resolution.hasCrs());
        assertTrue(resolution.provenance().contains("no se asume WGS84"));
    }

    @Test
    void radiansAreConvertedToDegrees() {
        assertEquals(180.0, NetCdfCfSupport.toDegrees(Math.PI, "radian"), 1.0e-9);
        assertEquals(45.0, NetCdfCfSupport.toDegrees(45.0, "degrees_east"), 1.0e-9);
    }

    @Test
    void longitudesAreNormalizedToMinus180Plus180() {
        assertEquals(-170.0, NetCdfCfSupport.normalizeLongitude(190.0), 1.0e-9);
        assertEquals(0.0, NetCdfCfSupport.normalizeLongitude(360.0), 1.0e-9);
        assertEquals(-170.0, NetCdfCfSupport.normalizeLongitude(-170.0), 1.0e-9);
        assertEquals(170.0, NetCdfCfSupport.normalizeLongitude(-190.0), 1.0e-9);
    }

    @Test
    void packagingAppliesScaleOffsetAndNoData() {
        var packaging = NetCdfCfSupport.packaging(0.1, 5.0, -9999f, 0f, 40f);
        assertEquals(6.0, packaging.decode(10f), 1.0e-6);
        assertTrue(packaging.isNoData(-9999f), "fill value must be NoData");
        assertTrue(packaging.isNoData(500f), "decoded value above valid_max is NoData");
        assertTrue(packaging.isNoData(Float.NaN));
        assertFalse(packaging.isNoData(100f), "10.0 C decoded inside valid range");
        assertEquals(0.1, packaging.scaleFactor(), 1.0e-9);
    }

    @Test
    void packagingDefaultsAreIdentity() {
        var packaging = NetCdfCfSupport.packaging(null, null, null, null, null);
        assertEquals(1.0, packaging.scaleFactor(), 1.0e-9);
        assertEquals(0.0, packaging.addOffset(), 1.0e-9);
        assertEquals(3.25, packaging.decode(3.25f), 1.0e-6);
        assertFalse(packaging.isNoData(3.25f));
    }
}
