package ar.com.catgis.catmap;

import ar.com.catgis.MapPanel;
import ar.com.catgis.catmap.CatmapAtomicExport;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutRenderHost;
import ar.com.catgis.layout.LayoutRenderHosts;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** GeoTIFF export carries the project CRS and the map frame's world extent. */
class GeoTiffLayoutExporterContractTest {

    @TempDir
    Path directory;

    @AfterEach
    void restoreHost() {
        LayoutRenderHosts.reset();
    }

    private static void installProject(String crs) {
        Project project = new Project("Proyecto de prueba");
        project.setProjectCRS(crs);
        LayoutRenderHosts.install(new LayoutRenderHost() {
            @Override
            public Project project() {
                return project;
            }

            @Override
            public MapPanel mapPanel() {
                return null;
            }
        });
    }

    private static LayoutModel modelWithMap() {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.A4_LANDSCAPE);
        LayoutMap map = new LayoutMap("m1", 15, 30, 100, 50);
        map.setOwnExtent(true);
        map.setOwnViewMinX(1000.0);
        map.setOwnViewMinY(2000.0);
        map.setOwnZoomFactor(2.0);
        model.addElement(map);
        return model;
    }

    private void noPartialStages() throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void geoTiffCarriesProjectCrsExtentAndRasterSize() throws Exception {
        installProject("EPSG:32719");
        LayoutModel model = modelWithMap();
        File target = directory.resolve("mapa.tif").toFile();

        GeoTiffLayoutExporter.exportMapFrame(model, target, 96);
        assertTrue(target.isFile());
        assertTrue(Files.size(target.toPath()) > 0);

        int[] expected = CatmapAtomicExport.rasterSize(96, 100.0, 50.0);
        double zoom = 2.0;
        double expectedMinX = 1000.0;
        double expectedMinY = 2000.0;
        double expectedMaxX = 1000.0 + expected[0] * zoom;
        double expectedMaxY = 2000.0 + expected[1] * zoom;

        GeoTiffReader reader = new GeoTiffReader(
                new java.io.ByteArrayInputStream(Files.readAllBytes(target.toPath())));
        GridCoverage2D coverage = null;
        try {
            coverage = reader.read(null);
            assertTrue(org.geotools.referencing.CRS.toSRS(reader.getCoordinateReferenceSystem())
                    .contains("32719"));
            ReferencedEnvelope envelope = new ReferencedEnvelope(
                    coverage.getEnvelope2D(), reader.getCoordinateReferenceSystem());
            assertEquals(expectedMinX, envelope.getMinX(), 0.01);
            assertEquals(expectedMinY, envelope.getMinY(), 0.01);
            assertEquals(expectedMaxX, envelope.getMaxX(), 0.01);
            assertEquals(expectedMaxY, envelope.getMaxY(), 0.01);
            assertEquals(expected[0], coverage.getRenderedImage().getWidth());
            assertEquals(expected[1], coverage.getRenderedImage().getHeight());
        } finally {
            if (coverage != null) {
                coverage.dispose(true);
            }
            reader.dispose();
        }
        noPartialStages();
    }

    @Test
    void existingFileIsReplacedOnlyByAValidGeoTiff() throws Exception {
        installProject("EPSG:32719");
        File target = directory.resolve("reemplazo.tif").toFile();
        Files.writeString(target.toPath(), "anterior", StandardCharsets.UTF_8);

        GeoTiffLayoutExporter.exportMapFrame(modelWithMap(), target, 96);
        byte[] header = new byte[2];
        try (java.io.InputStream input = Files.newInputStream(target.toPath())) {
            assertEquals(2, input.read(header));
        }
        assertTrue(Files.size(target.toPath()) > 12, "GeoTIFF must replace the previous bytes");
        assertTrue((header[0] == 0x49 && header[1] == 0x49)
                        || (header[0] == 0x4D && header[1] == 0x4D),
                "replacement must start with a TIFF header");
    }

    @Test
    void missingMapFrameFailsClosedAndKeepsPreviousFile() throws Exception {
        installProject("EPSG:32719");
        File target = directory.resolve("sin-mapa.tif").toFile();
        Files.writeString(target.toPath(), "anterior", StandardCharsets.UTF_8);

        assertThrows(java.io.IOException.class,
                () -> GeoTiffLayoutExporter.exportMapFrame(new LayoutModel(), target, 96));
        assertEquals("anterior", Files.readString(target.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void missingProjectCrsFailsClosed() {
        Project project = new Project("Sin CRS") {
            @Override
            public String getProjectCRS() {
                return null;
            }
        };
        LayoutRenderHosts.install(new LayoutRenderHost() {
            @Override
            public Project project() {
                return project;
            }

            @Override
            public MapPanel mapPanel() {
                return null;
            }
        });
        File target = directory.resolve("sin-crs.tif").toFile();
        assertThrows(java.io.IOException.class,
                () -> GeoTiffLayoutExporter.exportMapFrame(modelWithMap(), target, 96));
    }

    @Test
    void invalidFrameDimensionsFailClosed() {
        installProject("EPSG:32719");
        LayoutModel model = new LayoutModel();
        LayoutMap map = new LayoutMap("m1", 15, 30, 0, 0);
        map.setOwnExtent(true);
        map.setOwnViewMinX(0);
        map.setOwnViewMinY(0);
        model.addElement(map);
        File target = directory.resolve("marco-invalido.tif").toFile();
        assertThrows(java.io.IOException.class,
                () -> GeoTiffLayoutExporter.exportMapFrame(model, target, 96));
    }
}
