package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyVectorExportSafetyContractTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @TempDir
    Path directory;

    @Test
    void dxfEnginePublishesThroughValidatedSingleFilePath() throws Exception {
        ShapefileData data = ReleaseTestSupport.buildPointData(
                "dxf_safe", "EPSG:4326",
                new Coordinate(-67.5, -45.8), "A", 1);
        Path target = directory.resolve("safe.dxf");

        DxfExportEngine.export(
                target.toFile(),
                data.getFeatureCollection(),
                "safe",
                Color.BLACK,
                Color.GRAY,
                Color.RED);

        ShapefileData reopened = DxfLoader.load(target.toFile());
        assertTrue(reopened.getFeatureCount() > 0);
    }

    @Test
    void kmlMenuPathReprojectsProjectedCoordinatesToWgs84() throws Exception {
        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem utm19s = CRS.decode("EPSG:32719", true);
        MathTransform toUtm = CRS.findMathTransform(wgs84, utm19s, true);
        Point geographic = GEOMETRY_FACTORY.createPoint(
                new Coordinate(-67.5, -45.8));
        Point projected = (Point) JTS.transform(geographic, toUtm);

        ShapefileData data = ReleaseTestSupport.buildPointData(
                "kml_safe", "EPSG:32719",
                projected.getCoordinate(), "A", 1);
        Layer layer = ReleaseTestSupport.buildVectorLayer(
                "kml_safe", directory.resolve("source.geojson"), "EPSG:32719");
        Path target = directory.resolve("safe.kml");

        assertTrue(KmlExportEngine.exportLayerToFile(
                target.toFile(), layer, data, null));

        ShapefileData reopened = KmlLoader.load(target.toFile());
        Point point = (Point) reopened.getFeatures().get(0).getDefaultGeometry();
        assertEquals(-67.5, point.getX(), 1e-5);
        assertEquals(-45.8, point.getY(), 1e-5);
    }

    @Test
    void legacyKmlWriterWithoutSourceCrsFailsClosed() {
        assertThrows(IOException.class, () -> KmlExportEngine.exportToKml(
                directory.resolve("unsafe.kml").toFile(),
                List.of(),
                "unsafe"));
    }
}
