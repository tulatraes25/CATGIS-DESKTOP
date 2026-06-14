package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Visual integrity test for CATMAP layout export with real GIS data.
 * <p>
 * Generates a shapefile (3 polygons) and a GeoTIFF DEM in a temp directory,
 * renders a synthetic map preview with elevation gradient, polygon boundaries,
 * labels, and grid, builds a full CATMAP layout, exports to PNG via
 * LayoutExportEngine, and compares against a golden reference image.
 */
class CatmapVisualTest {

    private static final int DPI = 96;
    private static final CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
    private static final String GOLDEN = "catmap-real-data.png";

    @TempDir
    Path tempDir;

    @Test
    void catmapFullExportMatchesGolden() throws Exception {
        createTestData();

        BufferedImage mapPreview = renderMapPreview();
        LayoutModel model = buildLayout(mapPreview);

        BufferedImage actual = LayoutExportEngine.renderLayout(model, DPI);
        assertNotNull(actual);

        Path goldenPath = goldenPath(GOLDEN);
        if (!Files.exists(goldenPath)) {
            GoldenImageAssert.saveGoldenImage(actual, goldenPath);
            return;
        }

        BufferedImage expected = GoldenImageAssert.loadGoldenImage(
                "ar/com/catgis/catmap/golden/" + GOLDEN);
        GoldenImageAssert.assertMatches(expected, actual, 1.0, 8);
    }

    @Test
    void catmapExportFileNotEmpty() throws Exception {
        createTestData();

        BufferedImage mapPreview = renderMapPreview();
        LayoutModel model = buildLayout(mapPreview);

        File output = tempDir.resolve("catmap_export.png").toFile();
        LayoutExportEngine.exportPng(model, output, DPI);

        assertTrue(output.exists());
        assertTrue(output.length() > 5000, "Export PNG too small: " + output.length());
    }

    private void createTestData() throws Exception {
        GeometryFactory gf = new GeometryFactory();

        // Shapefile
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("zonas");
        tb.setCRS(WGS84);
        tb.add("the_geom", org.locationtech.jts.geom.Polygon.class);
        tb.add("nombre", String.class);
        tb.add("area_ha", Double.class);
        SimpleFeatureType type = tb.buildFeatureType();

        ListFeatureCollection collection = new ListFeatureCollection(type);
        collection.add(feature(type, gf, polygon(gf,
                -58.5, -34.6, -58.4, -34.6, -58.4, -34.5, -58.5, -34.5, -58.5, -34.6),
                "Zona Urbana", 150.0));
        collection.add(feature(type, gf, polygon(gf,
                -58.3, -34.7, -58.1, -34.7, -58.1, -34.5, -58.3, -34.5, -58.3, -34.7),
                "Parque Industrial", 320.0));
        collection.add(feature(type, gf, polygon(gf,
                -58.6, -34.8, -58.2, -34.8, -58.2, -34.7, -58.6, -34.7, -58.6, -34.8),
                "Reserva Natural", 850.0));

        File shpDir = tempDir.resolve("test_data").toFile();
        shpDir.mkdirs();
        File shpFile = new File(shpDir, "zonas.shp");
        ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
        store.createSchema(type);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) store.getFeatureSource("zonas");
        featureStore.addFeatures(collection);
        store.dispose();

        // Raster DEM
        int w = 40, h = 30;
        float[][] data = new float[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (x - w / 2.0) / (w / 2.0);
                double dy = (y - h / 2.0) / (h / 2.0);
                data[y][x] = (float) (50 + 200 * Math.exp(-(dx * dx + dy * dy) * 3));
            }
        }
        ReferencedEnvelope env = new ReferencedEnvelope(-58.7, -58.0, -34.9, -34.4, WGS84);
        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D coverage = factory.create("dem", data, env);
        File rasterFile = tempDir.resolve("dem_test.tif").toFile();
        GeoTiffWriter writer = new GeoTiffWriter(rasterFile);
        writer.write(coverage, null);
        writer.dispose();
    }

    private static org.locationtech.jts.geom.Polygon polygon(GeometryFactory gf, double... coords) {
        Coordinate[] cs = new Coordinate[coords.length / 2 + 1];
        for (int i = 0; i < coords.length; i += 2) {
            cs[i / 2] = new Coordinate(coords[i], coords[i + 1]);
        }
        cs[cs.length - 1] = cs[0];
        return gf.createPolygon(cs);
    }

    private static SimpleFeature feature(SimpleFeatureType type, GeometryFactory gf,
                                          org.locationtech.jts.geom.Polygon geom, String name, double area) {
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        fb.add(geom);
        fb.add(name);
        fb.add(area);
        return fb.buildFeature(null);
    }

    private BufferedImage renderMapPreview() {
        int w = 500, h = 300;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(240, 245, 240));
        g.fillRect(0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (x - w / 2.0) / (w / 2.0);
                double dy = (y - h / 2.0) / (h / 2.0);
                double elevation = 50 + 200 * Math.exp(-(dx * dx + dy * dy) * 3);
                int v = (int) (180 - elevation * 0.6);
                g.setColor(new Color(v, v + 20, v + 30));
                g.fillRect(x, y, 1, 1);
            }
        }

        g.setStroke(new BasicStroke(1.5f));

        drawZone(g, w, h, -58.5, -34.6, -58.4, -34.6, -58.4, -34.5, -58.5, -34.5,
                new Color(255, 200, 150, 120), new Color(120, 60, 40));
        drawZone(g, w, h, -58.3, -34.7, -58.1, -34.7, -58.1, -34.5, -58.3, -34.5,
                new Color(180, 200, 220, 120), new Color(60, 80, 120));
        drawZone(g, w, h, -58.6, -34.8, -58.2, -34.8, -58.2, -34.7, -58.6, -34.7,
                new Color(150, 210, 150, 120), new Color(40, 120, 40));

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.BLACK);
        g.drawString("Zona Urbana", lonToX(-58.48, w), latToY(-34.56, h));
        g.drawString("Parque Ind.", lonToX(-58.22, w), latToY(-34.62, h));
        g.drawString("Reserva Natural", lonToX(-58.45, w), latToY(-34.76, h));

        g.setColor(new Color(180, 180, 180, 80));
        g.setStroke(new BasicStroke(0.5f));
        for (double lon = -58.6; lon <= -58.1; lon += 0.1) {
            g.drawLine(lonToX(lon, w), 0, lonToX(lon, w), h);
        }
        for (double lat = -34.8; lat <= -34.4; lat += 0.1) {
            g.drawLine(0, latToY(lat, h), w, latToY(lat, h));
        }

        g.dispose();
        return img;
    }

    private void drawZone(Graphics2D g, int w, int h,
                           double x1, double y1, double x2, double y2,
                           double x3, double y3, double x4, double y4,
                           Color fill, Color stroke) {
        int[] xs = {lonToX(x1, w), lonToX(x2, w), lonToX(x3, w), lonToX(x4, w)};
        int[] ys = {latToY(y1, h), latToY(y2, h), latToY(y3, h), latToY(y4, h)};
        g.setColor(fill);
        g.fillPolygon(xs, ys, 4);
        g.setColor(stroke);
        g.drawPolygon(xs, ys, 4);
    }

    private int lonToX(double lon, int w) {
        return (int) ((lon + 58.7) / 0.7 * w);
    }

    private int latToY(double lat, int h) {
        return (int) ((34.4 - lat) / 0.5 * h);
    }

    private LayoutModel buildLayout(BufferedImage mapPreview) {
        LayoutModel model = new LayoutModel();

        LayoutLabel title = new LayoutLabel("t1", "Plancheta Catastral — Zonas de Prueba", 80, 8, 220, 16);
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        model.addElement(title);

        LayoutMap map = new LayoutMap("m1", 15, 28, 250, 130);
        map.setPreviewImage(mapPreview);
        map.setFrameColor(new Color(40, 40, 40));
        map.setFrameWidth(1.5f);
        model.addElement(map);

        LayoutLegend legend = new LayoutLegend("lg1", 15, 165, 250, 35);
        legend.setTitle("Referencias");
        legend.getItems().add(new LayoutLegend.LegendItem("Zona Urbana",
                new Color(255, 180, 130), "Polygon"));
        legend.getItems().add(new LayoutLegend.LegendItem("Parque Industrial",
                new Color(170, 190, 210), "Polygon"));
        legend.getItems().add(new LayoutLegend.LegendItem("Reserva Natural",
                new Color(140, 200, 140), "Polygon"));
        legend.getItems().add(new LayoutLegend.LegendItem("Curva de nivel",
                new Color(140, 140, 140), "LineString"));
        model.addElement(legend);

        LayoutScaleBar scale = new LayoutScaleBar("s1", 15, 203, 180, 25);
        scale.setUnitLabel("km");
        model.addElement(scale);

        LayoutNorthArrow north = new LayoutNorthArrow("n1", 235, 32, 30, 42);
        model.addElement(north);

        LayoutCartouche cartouche = new LayoutCartouche("c1", 220, 165, 80, 40);
        cartouche.setField("Proyecto", "Prueba CATGIS");
        cartouche.setField("Coord.", "WGS 84 / EPSG:4326");
        model.addElement(cartouche);

        return model;
    }

    private static Path goldenPath(String name) {
        Path p = Path.of("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("build.gradle"))) {
            p = p.getParent();
        }
        if (p == null) p = Path.of("").toAbsolutePath();
        return p.resolve("src/test/resources/ar/com/catgis/catmap/golden").resolve(name);
    }
}
