package ar.com.catgis;

import com.google.flatbuffers.FlatBufferBuilder;
import org.junit.jupiter.api.Test;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Header;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlatGeobufCrsSupportTest {

    @Test
    void missingOrganizationWithNumericCodeMeansEpsgPerFlatGeobufSpec() {
        FlatGeobufCrsSupport.Resolution result =
                FlatGeobufCrsSupport.resolveMetadata(null, 3857, "", "");

        assertEquals("EPSG:3857", result.sourceCode());
        assertEquals("AUTHORITY", result.provenance());
        assertFalse(result.coordinateReferenceSystem() == null);
    }

    @Test
    void absentMetadataRemainsUnknownAndNeverBecomes4326() {
        FlatGeobufCrsSupport.Resolution result =
                FlatGeobufCrsSupport.resolveMetadata(null, 0, "", "");

        assertTrue(result.sourceCode().isBlank());
        assertEquals("UNKNOWN", result.provenance());
        assertTrue(result.coordinateReferenceSystem() == null);
    }

    @Test
    void actualFlatbufferHeaderCrsIsReadRatherThanInvented() {
        FlatBufferBuilder builder = new FlatBufferBuilder(256);
        int org = builder.createString("EPSG");
        Crs.startCrs(builder);
        Crs.addOrg(builder, org);
        Crs.addCode(builder, 32719);
        int crs = Crs.endCrs(builder);

        Header.startHeader(builder);
        Header.addCrs(builder, crs);
        int header = Header.endHeader(builder);
        builder.finish(header);

        ByteBuffer buffer = builder.dataBuffer();
        FlatGeobufCrsSupport.Resolution result = FlatGeobufCrsSupport.resolve(buffer);

        assertEquals("EPSG:32719", result.sourceCode());
        assertEquals("AUTHORITY", result.provenance());
    }

    @Test
    void schemaWithoutCrsReturnsBlankInsteadOfDefaultEpsg4326() {
        org.geotools.feature.simple.SimpleFeatureTypeBuilder builder =
                new org.geotools.feature.simple.SimpleFeatureTypeBuilder();
        builder.setName("unknown");
        builder.add("the_geom", org.locationtech.jts.geom.Point.class);

        assertTrue(FlatGeobufCrsSupport.schemaCode(builder.buildFeatureType()).isBlank());
    }
}
