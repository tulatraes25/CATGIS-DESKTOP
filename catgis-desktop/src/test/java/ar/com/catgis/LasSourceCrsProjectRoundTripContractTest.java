package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LasSourceCrsProjectRoundTripContractTest {
    @TempDir Path directory;

    @Test
    void actualUtmLasFileRetainsSourceCrsAcrossProjectSaveAndReload() throws Exception {
        Path file = onePointLas();
        byte[] before = Files.readAllBytes(file);

        // LAS lacks a certified VLR/EVLR CRS parser, so a human must supply it.
        assertThrows(IllegalStateException.class,
                () -> LayerVectorDataSupport.requireDeclaredLasSourceCrs(""));
        LayerVectorDataSupport.requireDeclaredLasSourceCrs("EPSG:32719");

        LasReader.LasHeader header = LasReader.readHeader(file.toFile());
        assertEquals(1L, header.pointCount());
        LasReader.LasPoint point = LasReader.readPoints(file.toFile(), 1, header).get(0);
        assertEquals(500000d, point.x(), 0.000001d);
        assertEquals(4900000d, point.y(), 0.000001d);

        // Real GeoTools projection—not just metadata relabelling.
        GeometryFactory gf = new GeometryFactory();
        Point projected = (Point) VectorCrsTransformSupport.reprojectGeometry(
                gf.createPoint(new Coordinate(point.x(), point.y())),
                "EPSG:32719", "EPSG:4326");
        assertNotNull(projected);
        assertTrue(projected.getX() < -68d && projected.getX() > -70d);
        assertTrue(projected.getY() < -45d && projected.getY() > -48d);

        Layer layer = new VectorLayer("Survey UTM19S", file.toString());
        layer.setSourceCRS("EPSG:32719");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer);
        layer.setSourceCRS("EPSG:4326"); // In-memory geometry is now projected.
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer);
        assertEquals("EPSG:4326", layer.getSourceCRS());
        assertEquals("EPSG:32719", LocalVectorSourceCrsProvenance.crsForProjectFile(layer));

        String serialized = SaveProjectAction.buildLayerLine(layer);
        assertEquals("EPSG:32719", serialized.split("\\|", -1)[12]);
        Layer reopened = LoadProjectAction.parseLayer(serialized);
        assertNotNull(reopened);
        assertEquals("EPSG:32719", reopened.getSourceCRS());
        assertEquals(file.toString(), reopened.getPath());

        LocalVectorSourceCrsProvenance.rememberBeforeProjection(reopened);
        reopened.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:32719",
                SaveProjectAction.buildLayerLine(reopened).split("\\|", -1)[12]);
        assertEquals(before.length, Files.readAllBytes(file).length);
        org.junit.jupiter.api.Assertions.assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void filePathChangeCannotRetainUnrelatedSourceProvenance() {
        Layer layer = new VectorLayer("survey", "one.las");
        layer.setSourceCRS("EPSG:32719");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer);
        layer.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:32719", LocalVectorSourceCrsProvenance.crsForProjectFile(layer));

        layer.setPath("different.las");
        assertEquals("EPSG:4326", LocalVectorSourceCrsProvenance.crsForProjectFile(layer));
        layer.setSourceCRS("EPSG:22182");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(layer);
        layer.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:22182", LocalVectorSourceCrsProvenance.crsForProjectFile(layer));
    }

    @Test
    void unsupportedFormatsRetainTheirExistingSerializationBehavior() {
        Layer raster = new RasterLayer("raster", "sample.tif");
        raster.setSourceCRS("EPSG:32719");
        LocalVectorSourceCrsProvenance.rememberBeforeProjection(raster);
        raster.setSourceCRS("EPSG:4326");
        assertEquals("EPSG:4326", LocalVectorSourceCrsProvenance.crsForProjectFile(raster));
        assertEquals("", LocalVectorSourceCrsProvenance.crsForProjectFile(null));
    }

    private Path onePointLas() throws Exception {
        int headerSize = 227;
        ByteBuffer fixture = ByteBuffer.allocate(headerSize + 20).order(ByteOrder.LITTLE_ENDIAN);
        fixture.put(new byte[]{'L', 'A', 'S', 'F'});
        fixture.put(24, (byte) 1); // LAS major
        fixture.put(25, (byte) 2); // LAS minor
        fixture.putShort(94, (short) headerSize);
        fixture.putInt(96, headerSize); // Point data begins here
        fixture.put(104, (byte) 0); // Point format 0, uncompressed
        fixture.putShort(105, (short) 20);
        fixture.putInt(107, 1); // Exactly one point, no truncation
        fixture.putDouble(131, 0.01d);
        fixture.putDouble(139, 0.01d);
        fixture.putDouble(147, 0.01d);
        fixture.putDouble(155, 500000d);
        fixture.putDouble(163, 4900000d);
        fixture.putDouble(171, 100d);
        fixture.putDouble(179, 500001d);
        fixture.putDouble(187, 499999d);
        fixture.putDouble(195, 4900001d);
        fixture.putDouble(203, 4899999d);
        fixture.putDouble(211, 101d);
        fixture.putDouble(219, 99d);
        // First 20-byte point record is zero-filled and resolves to offsets.
        Path file = directory.resolve("field survey UTM19S.las");
        Files.write(file, fixture.array());
        return file;
    }
}
