package ar.com.catgis;

import ar.com.catgis.data.raster.RasterCoverageSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#163: explicit normalization is separated from the UI WGS84 default policy. */
class CrsNormalizationSplitContractTest {

    @Test
    void defaultPolicyStillAppliesToUiFacingNormalization() {
        assertEquals("EPSG:4326", CRSDefinitions.normalizeCode(""));
        assertEquals("EPSG:4326", CRSDefinitions.normalizeCode(null));
        assertEquals("EPSG:32719", CRSDefinitions.normalizeCode("32719"));
        assertEquals("EPSG:32719", CRSDefinitions.normalizeCode(" epsg:32719 "));
    }

    @Test
    void explicitNormalizationPreservesMissingCrs() {
        assertEquals("", CRSDefinitions.normalizeExplicit(""));
        assertEquals("", CRSDefinitions.normalizeExplicit(null));
        assertEquals("EPSG:32719", CRSDefinitions.normalizeExplicit("32719"));
        assertEquals("urn:ogc:def:crs:EPSG::4326",
                CRSDefinitions.normalizeExplicit("urn:ogc:def:crs:EPSG::4326"));
    }

    @Test
    void coordinateTransformsDoNotAssumeWgs84ForMissingCrs() {
        assertNull(CoordinateTransformSupport.transformPoint(1.0, 2.0, "", "EPSG:3857"),
                "a missing source CRS must fail instead of being treated as WGS84");
        assertNull(CoordinateTransformSupport.transformPoint(1.0, 2.0, "EPSG:3857", ""),
                "a missing target CRS must fail instead of being treated as WGS84");
        double[] valid = CoordinateTransformSupport.transformPoint(-60.0, -30.0,
                "EPSG:4326", "EPSG:3857");
        assertNotNull(valid, "explicit CRS transform must still work");
        assertTrue(Double.isFinite(valid[0]) && Double.isFinite(valid[1]));
    }

    @Test
    void rasterDeclaredCodeNeverInventsDefault() {
        assertEquals("", RasterCoverageSupport.declaredCode(""));
        assertEquals("", RasterCoverageSupport.declaredCode(null));
        assertEquals("EPSG:32719", RasterCoverageSupport.declaredCode("32719"));
        assertFalse(RasterCoverageSupport.declaredCode(" ").startsWith("EPSG:"));
    }
}
