package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Exercises the actual CATGIS project line serializer and parser.
 * A displayed EPSG:4326 layer may still reference an EPSG:32719 file.
 */
class FlatGeobufProjectCrsRoundTripContractTest {

    @Test
    void projectedMemoryCrsDoesNotOverwriteOriginalOnDiskCrs() {
        Layer original = new VectorLayer("UTM source", "/tmp/field data.fgb");
        original.setSourceCRS("EPSG:32719");
        FlatGeobufSourceCrsProvenance.rememberBeforeProjection(original);

        // The production projection service sets the in-memory layer CRS
        // to the project CRS once the geometry is transformed.
        original.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:4326", original.getSourceCRS());
        FlatGeobufSourceCrsProvenance.rememberBeforeProjection(original);
        assertEquals("EPSG:32719", FlatGeobufSourceCrsProvenance.crsForProjectFile(original),
                "A second projection must never erase original source provenance");

        String line = SaveProjectAction.buildLayerLine(original);
        assertEquals("EPSG:32719", serializedSourceCrs(line));
        Layer reopened = LoadProjectAction.parseLayer(line);
        assertNotNull(reopened);
        assertEquals("EPSG:32719", reopened.getSourceCRS());

        // Reopening reloads the source file, then projects again for display.
        FlatGeobufSourceCrsProvenance.rememberBeforeProjection(reopened);
        reopened.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:32719",
                serializedSourceCrs(SaveProjectAction.buildLayerLine(reopened)));
    }

    @Test
    void nonFlatGeobufLayersKeepExistingProjectSerialization() {
        Layer shape = new VectorLayer("ordinary", "/tmp/source.shp");
        shape.setSourceCRS("EPSG:32719");
        FlatGeobufSourceCrsProvenance.rememberBeforeProjection(shape);
        shape.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:4326",
                serializedSourceCrs(SaveProjectAction.buildLayerLine(shape)));
    }

    @Test
    void explicitManualSourceForCrsFreeFlatGeobufSurvivesSaveAndReopen() {
        Layer manual = new VectorLayer("manually-referenced", "/tmp/crs-free.fgb");
        manual.setSourceCRS("EPSG:22182");
        FlatGeobufSourceCrsProvenance.rememberBeforeProjection(manual);
        manual.setSourceCRS("EPSG:4326");
        Layer reopened = LoadProjectAction.parseLayer(SaveProjectAction.buildLayerLine(manual));
        assertNotNull(reopened);
        assertEquals("EPSG:22182", reopened.getSourceCRS());
    }

    @Test
    void unprojectedLegacyFlatGeobufKeepsItsExistingCrs() {
        Layer file = new VectorLayer("legacy", "/tmp/legacy.fgb");
        file.setSourceCRS("EPSG:32719");
        assertEquals("EPSG:32719",
                serializedSourceCrs(SaveProjectAction.buildLayerLine(file)));
    }

    private static String serializedSourceCrs(String layerLine) {
        return layerLine.split("\\|", -1)[12];
    }
}
