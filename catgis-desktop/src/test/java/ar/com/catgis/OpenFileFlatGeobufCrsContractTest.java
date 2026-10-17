package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Contract for direct file import: a target map CRS is never a substitute for source provenance. */
class OpenFileFlatGeobufCrsContractTest {
    @Test
    void directImportPreservesProjectedHeaderCrs() throws Exception {
        assertEquals("EPSG:32719",
                OpenFileAction.sourceCrsFromFlatGeobufData(data("EPSG:32719")));
    }

    @Test
    void directImportPreservesExplicitGeographicCrs() throws Exception {
        assertEquals("EPSG:4326",
                OpenFileAction.sourceCrsFromFlatGeobufData(data("EPSG:4326")));
    }

    @Test
    void missingSourceCrsRemainsUnknownRatherThanInventingWgs84() throws Exception {
        assertEquals("", OpenFileAction.sourceCrsFromFlatGeobufData(data(null)));
        assertEquals("", OpenFileAction.sourceCrsFromFlatGeobufData(null));
    }

    private static ShapefileData data(String crs) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("fgb-source-crs");
        if (crs != null) {
            builder.setCRS(CRS.decode(crs, true));
        }
        builder.add("the_geom", Point.class);
        SimpleFeatureType schema = builder.buildFeatureType();
        return new ShapefileData(List.of(), new Envelope(), "fixture.fgb", 0, "fixture", schema);
    }
}
