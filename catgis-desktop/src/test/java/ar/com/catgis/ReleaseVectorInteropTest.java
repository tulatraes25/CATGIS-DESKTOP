package ar.com.catgis;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseVectorInteropTest {

    @Test
    void exportsReadableShpGeoJsonKmlAndKmz() throws Exception {
        Path tempDir = Files.createTempDirectory("catgis-release-interop");
        ShapefileData data = ReleaseTestSupport.buildPointData(
                "interop_smoke",
                "EPSG:4326",
                new Coordinate(-68.85, -32.89),
                "Pozo A",
                7
        );
        Layer layer = ReleaseTestSupport.buildVectorLayer("InteropSmoke", tempDir.resolve("source.geojson"), "EPSG:4326");
        layer.setLabelField("name");

        Path shp = tempDir.resolve("interop_smoke.shp");
        Path geojson = tempDir.resolve("interop_smoke.geojson");
        Path kml = tempDir.resolve("interop_smoke.kml");
        Path kmlNoLabel = tempDir.resolve("interop_smoke_nolabel.kml");
        Path kmz = tempDir.resolve("interop_smoke.kmz");

        ExportVectorLayerAction.saveLayerDataToFile(layer, data, shp.toFile(), null, false);
        ExportVectorLayerAction.saveLayerDataToFile(layer, data, geojson.toFile(), null, false);

        Object labelOptions = ReleaseVectorInteropTestHelper.buildKmlOptions(true, "name", true);
        Object noLabelOptions = ReleaseVectorInteropTestHelper.buildKmlOptions(false, null, true);
        ReleaseVectorInteropTestHelper.exportKmlReflective(layer, data, kml, labelOptions);
        ReleaseVectorInteropTestHelper.exportKmlReflective(layer, data, kmlNoLabel, noLabelOptions);
        ReleaseVectorInteropTestHelper.exportKmlReflective(layer, data, kmz, labelOptions);

        ShapefileData shpReloaded = ShapefileLoader.load(shp.toFile());
        ShapefileData geojsonReloaded = GeoJsonLoader.load(geojson.toFile());
        String kmlText = Files.readString(kml, StandardCharsets.UTF_8);
        String kmlNoLabelText = Files.readString(kmlNoLabel, StandardCharsets.UTF_8);

        assertNotNull(shpReloaded);
        assertNotNull(geojsonReloaded);
        assertEquals(1, shpReloaded.getFeatureCount());
        assertEquals(1, geojsonReloaded.getFeatureCount());
        assertTrue(shpReloaded.getSchema().getDescriptor("codigo") != null);
        assertTrue(geojsonReloaded.getSchema().getDescriptor("codigo") != null);
        assertTrue(kmlText.contains("<name>Pozo A</name>"));
        assertTrue(kmlText.contains("<ExtendedData>"));
        assertFalse(kmlNoLabelText.contains("<name>Pozo A</name>"));

        try (ZipFile zipFile = new ZipFile(kmz.toFile())) {
            assertNotNull(zipFile.getEntry("doc.kml"));
        }
    }
}
