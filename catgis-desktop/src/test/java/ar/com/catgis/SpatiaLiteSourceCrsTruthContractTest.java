package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Local spatial table source CRS and file/table identity are independent of
 * the current map's display CRS. No automatic EPSG:4326 assumption is valid.
 */
class SpatiaLiteSourceCrsTruthContractTest {

    @Test
    void selectedTableMustExistAndCannotBeSubstitutedByFirstAvailable() throws Exception {
        String[] tables = {"roads", "survey_utm", "streams"};
        assertEquals("survey_utm", SpatiaLiteLoader.requireRequestedTable(tables, "SURVEY_UTM"));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireRequestedTable(tables, "missing_table"));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireRequestedTable(tables, ""));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireRequestedTable(new String[0], "survey_utm"));
    }

    @Test
    void projectedTableSourceCrsComesFromActualFeatureSchema() throws Exception {
        String detected = SpatiaLiteLoader.requireSourceCrs(schema("EPSG:32719"), "");
        assertTrue(VectorCrsTransformSupport.equivalent("EPSG:32719", detected));
        assertTrue(VectorCrsTransformSupport.equivalent(
                "EPSG:32719",
                SpatiaLiteLoader.requireSourceCrs(schema("EPSG:32719"), detected)));
    }

    @Test
    void contradictoryOrInvalidAssignedCrsFailsBeforeAttachingCoordinates() throws Exception {
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireSourceCrs(schema("EPSG:32719"), "EPSG:4326"));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireSourceCrs(schema("EPSG:32719"), "EPSG:INVALID"));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireSourceCrs(null, ""));
        assertThrows(UnsupportedFormatException.class, () ->
                SpatiaLiteLoader.requireSourceCrs(null, "this-is-not-a-crs"));
    }

    @Test
    void validExplicitManualCrsAndWktRemainSupportedWhenSchemaIsUnreferenced() throws Exception {
        String manual = SpatiaLiteLoader.requireSourceCrs(null, "EPSG:32719");
        assertTrue(VectorCrsTransformSupport.equivalent("EPSG:32719", manual));
        String wkt = CRS.decode("EPSG:32719", true).toWKT();
        String resolved = SpatiaLiteLoader.requireSourceCrs(null, wkt);
        assertTrue(VectorCrsTransformSupport.equivalent("EPSG:32719", resolved));
    }

    @Test
    void realDatabasePathAndTableSourceCrsSurviveProjectSaveAndRepeatedReload() {
        SpatiaLiteLayer original = new SpatiaLiteLayer("Red", "/tmp/roads.sqlite");
        original.setTableName("survey_utm");
        original.setSourceCRS("EPSG:32719");
        original.setResolvedCrs("EPSG:32719");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(original);
        original.setSourceCRS("EPSG:4326"); // Geometry has been projected for display.

        String encoded = SaveProjectAction.buildLayerLine(original);
        Layer decoded = LoadProjectAction.parseLayer(encoded);
        SpatiaLiteLayer restored = assertInstanceOf(SpatiaLiteLayer.class, decoded);
        assertEquals("/tmp/roads.sqlite", restored.getPath());
        assertEquals("survey_utm", restored.getTableName());
        assertEquals("EPSG:32719", restored.getSourceCRS());
        assertEquals("EPSG:32719", restored.getResolvedCrs());

        LocalVectorSourceCrsProvenance.rememberBeforeProjection(restored);
        restored.setSourceCRS("EPSG:4326");
        Layer roundtrip = LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(restored));
        assertEquals("EPSG:32719", roundtrip.getSourceCRS());
        assertEquals("EPSG:4326", restored.getSourceCRS());
    }

    private static SimpleFeatureType schema(String code) throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("survey");
        tb.setCRS(CRS.decode(code, true));
        tb.add("the_geom", Point.class);
        return tb.buildFeatureType();
    }
}
