package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import com.google.flatbuffers.FlatBufferBuilder;
import org.junit.jupiter.api.io.TempDir;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Header;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contract for direct file import: a target map CRS is never a substitute for source provenance. */
class OpenFileFlatGeobufCrsContractTest {
    @TempDir Path folder;

    @Test
    void realFramedFlatGeobufPreservesProjectedHeaderThroughDirectOpenSeam() throws Exception {
        Path source = framedHeaderFile("projected.fgb", 32719);
        assertTrue(FlatGeobufLoader.validateFile(source.toFile()).isValid());
        ShapefileData actual = FlatGeobufLoader.load(source.toFile());
        assertNotNull(actual);
        assertEquals("EPSG:32719", OpenFileAction.sourceCrsFromFlatGeobufData(actual));
    }

    @Test
    void realFramedFlatGeobufWithoutHeaderCrsIsRejectedNonInteractively() throws Exception {
        Path source = framedHeaderFile("unknown.fgb", 0);
        assertTrue(FlatGeobufLoader.validateFile(source.toFile()).isValid());
        ShapefileData actual = FlatGeobufLoader.load(source.toFile());
        assertNotNull(actual);
        assertEquals("", OpenFileAction.sourceCrsFromFlatGeobufData(actual));
        assertFalse(OpenFileAction.openSelectedFile(source.toFile(), "fgb", null, false));
    }

    private Path framedHeaderFile(String filename, int epsgCode) throws Exception {
        FlatBufferBuilder builder = new FlatBufferBuilder(256);
        int crs = 0;
        if (epsgCode > 0) {
            int org = builder.createString("EPSG");
            Crs.startCrs(builder);
            Crs.addOrg(builder, org);
            Crs.addCode(builder, epsgCode);
            crs = Crs.endCrs(builder);
        }
        Header.startHeader(builder);
        if (crs != 0) {
            Header.addCrs(builder, crs);
        }
        int header = Header.endHeader(builder);
        builder.finish(header);
        ByteBuffer raw = builder.dataBuffer();
        ByteBuffer framed = ByteBuffer.allocate(
                org.wololo.flatgeobuf.Constants.MAGIC_BYTES.length
                        + Integer.BYTES + raw.remaining()).order(ByteOrder.LITTLE_ENDIAN);
        framed.put(org.wololo.flatgeobuf.Constants.MAGIC_BYTES);
        framed.putInt(raw.remaining());
        framed.put(raw);
        Path file = folder.resolve(filename);
        Files.write(file, framed.array());
        return file;
    }
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
