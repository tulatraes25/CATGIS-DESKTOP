package ar.com.catgis;

import org.geotools.api.data.Query;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WfsRequestCrsTruthContractTest {

    @Test
    void nullBlankAndWhitespaceRequestsRemainUnspecified() throws Exception {
        OnlineWfsLayer layer = new OnlineWfsLayer("unreferenced");
        for (String raw : new String[]{null, "", "   "}) {
            layer.setRequestCrs(raw);
            assertEquals("", layer.getRequestCrs());
            Query query = WfsFeatureLoader.createFeatureQuery("test:features", layer.getRequestCrs());
            assertNull(query.getCoordinateSystemReproject(),
                    "UNSPECIFIED must not request a fabricated EPSG:4326 reprojection");
        }
        assertEquals("", WfsFeatureLoader.normalizeExplicitRequestCrs(null));
    }

    @Test
    void projectedServiceSchemaSurvivesAnUnspecifiedRequest() throws Exception {
        SimpleFeatureType schema = schemaWithCrs("EPSG:32719");
        assertEquals("EPSG:32719", WfsFeatureLoader.resolveLayerCrs("", schema));
        assertEquals("EPSG:32719", WfsFeatureLoader.resolveLayerCrs(null, schema));
        assertEquals("", WfsFeatureLoader.resolveLayerCrs("", schemaWithoutCrs()));
        assertEquals("", WfsFeatureLoader.resolveLayerCrs("", null));
    }

    @Test
    void explicitRequestIsHonoredAndInvalidAuthorityFails() throws Exception {
        OnlineWfsLayer layer = new OnlineWfsLayer("manual request");
        layer.setRequestCrs(" epsg:32719 ");
        assertEquals("EPSG:32719", layer.getRequestCrs());
        Query query = WfsFeatureLoader.createFeatureQuery("test:features", layer.getRequestCrs());
        assertNotNull(query.getCoordinateSystemReproject());
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:32719", true),
                query.getCoordinateSystemReproject()));
        assertEquals("EPSG:32719", WfsFeatureLoader.resolveLayerCrs(layer.getRequestCrs(), schemaWithoutCrs()));
        assertThrows(Exception.class, () ->
                WfsFeatureLoader.createFeatureQuery("test:features", "EPSG:NOT_A_CRS"));
    }

    @Test
    void explicitWgs84StillRequestsItOnlyWhenSelected() throws Exception {
        OnlineWfsLayer layer = new OnlineWfsLayer("explicit geographic");
        layer.setRequestCrs("EPSG:4326");
        assertEquals("EPSG:4326", layer.getRequestCrs());
        assertNotNull(WfsFeatureLoader.createFeatureQuery("test:features", layer.getRequestCrs())
                .getCoordinateSystemReproject());
    }

    private static SimpleFeatureType schemaWithCrs(String code) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("wfs-test");
        builder.setCRS(CRS.decode(code, true));
        builder.add("geom", Point.class);
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType schemaWithoutCrs() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("wfs-unknown");
        builder.add("geom", Point.class);
        return builder.buildFeatureType();
    }
}
