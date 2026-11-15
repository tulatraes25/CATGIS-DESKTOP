package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DC-1 integration contract: republishing a real GeoTools shapefile over an
 * existing dataset must replace the published content, keep the dataset
 * readable and leave no staging or backup file behind. Before the fix the
 * previous parts were deleted first, so a failure during the write destroyed the
 * user dataset.
 */
class VectorExportRepublishContractTest {

    @TempDir
    Path directory;

    @Test
    void republishingARealShapefileReplacesTheDatasetAndLeavesNoTemporaries() throws Exception {
        Layer layer = ReleaseTestSupport.buildVectorLayer(
                "RepublishSmoke",
                directory.resolve("source.geojson"),
                "EPSG:4326"
        );
        layer.setLabelField("name");

        ShapefileData firstVersion = ReleaseTestSupport.buildPointData(
                "republish_smoke",
                "EPSG:4326",
                new Coordinate(-68.85, -32.89),
                "Pozo A",
                7
        );
        Path shapefile = directory.resolve("republish_smoke.shp");
        assertTrue(ExportVectorLayerAction.saveLayerDataToFile(layer, firstVersion, shapefile.toFile(), null, false),
                "La primera exportacion debe completarse.");
        assertEquals(1, ShapefileLoader.load(shapefile.toFile()).getFeatureCount());

        ShapefileData secondVersion = ReleaseTestSupport.buildPointData(
                "republish_smoke",
                "EPSG:4326",
                new Coordinate(-68.80, -32.80),
                "Pozo B",
                9
        );
        assertTrue(ExportVectorLayerAction.saveLayerDataToFile(layer, secondVersion, shapefile.toFile(), null, false),
                "La segunda exportacion sobre el dataset existente debe completarse.");

        ShapefileData reloaded = ShapefileLoader.load(shapefile.toFile());
        assertEquals(1, reloaded.getFeatureCount());
        assertTrue(reloaded.getSchema().getDescriptor("codigo") != null,
                "El esquema publicado debe conservar los atributos.");
        assertEquals("Pozo B", reloaded.getFeatures().get(0).getAttribute("name"),
                "El dataset publicado debe ser el de la segunda exportacion.");
        assertEquals(9, ((Number) reloaded.getFeatures().get(0).getAttribute("codigo")).intValue());
        assertNoExportLeftovers();
    }

    private void assertNoExportLeftovers() throws Exception {
        try (Stream<Path> entries = Files.list(directory)) {
            List<String> leftovers = entries
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(".catgis-") || name.contains(".catgis-old-"))
                    .toList();
            assertTrue(leftovers.isEmpty(), "Quedaron temporales de exportacion: " + leftovers);
        }
    }
}
