package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Golden image tests for the CATMAP layout export pipeline.
 * <p>
 * Each test builds a {@link LayoutModel}, renders via
 * {@link LayoutExportEngine#renderLayout(LayoutModel, int)} at 96 DPI,
 * and compares against a golden PNG in
 * {@code src/test/resources/ar/com/catgis/catmap/golden/}.
 * <p>
 * First run creates golden images; subsequent runs compare with
 * 1% pixel tolerance and per-channel delta of 8 to absorb
 * JDK/OS anti-aliasing variance.
 */
class GoldenImageTest {

    private static final int DPI = 96;

    @TempDir
    Path tempDir;

    @Test
    void label() throws Exception {
        LayoutModel m = new LayoutModel();
        LayoutLabel l = new LayoutLabel("l1", "Hello CATGIS", 30, 60, 200, 24);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        m.addElement(l);
        assertGolden("label.png", m);
    }

    @Test
    void shapes() throws Exception {
        LayoutModel m = new LayoutModel();
        m.addElement(new LayoutRectangle("r1", 20, 20, 100, 60));
        m.addElement(new LayoutEllipse("e1", 140, 20, 100, 60));
        m.addElement(new LayoutLine("ln1", 20, 100, 240, 100));
        assertGolden("shapes.png", m);
    }

    @Test
    void scaleBar() throws Exception {
        LayoutModel m = new LayoutModel();
        LayoutScaleBar s = new LayoutScaleBar("s1", 20, 20, 200, 40);
        s.setUnitLabel("km");
        m.addElement(s);
        assertGolden("scalebar.png", m);
    }

    @Test
    void northArrow() throws Exception {
        LayoutModel m = new LayoutModel();
        m.addElement(new LayoutNorthArrow("n1", 20, 20, 60, 80));
        assertGolden("north.png", m);
    }

    @Test
    void legend() throws Exception {
        LayoutModel m = new LayoutModel();
        LayoutLegend lg = new LayoutLegend("lg1", 20, 20, 200, 80);
        lg.setTitle("Leyenda");
        lg.getItems().add(new LayoutLegend.LegendItem("Rios", Color.BLUE, "LineString"));
        lg.getItems().add(new LayoutLegend.LegendItem("Bosques", new Color(34, 139, 34), "Polygon"));
        lg.getItems().add(new LayoutLegend.LegendItem("Ciudades", Color.RED, "Point"));
        m.addElement(lg);
        assertGolden("legend.png", m);
    }

    @Test
    void cartouche() throws Exception {
        LayoutModel m = new LayoutModel();
        LayoutCartouche c = new LayoutCartouche("c1", 20, 20, 220, 100);
        c.setField("Proyecto", "Proyecto Ejemplo");
        c.setField("Empresa", "CATGIS");
        c.setField("Cartografo", "C. Tula");
        c.setField("Coord.", "EPSG:32720 — WGS 84 / UTM zone 20S");
        m.addElement(c);
        assertGolden("cartouche.png", m);
    }

    @Test
    void pipeline() throws Exception {
        LayoutModel m = new LayoutModel();

        LayoutLabel title = new LayoutLabel("t1", "Mapa de Prueba", 130, 10, 140, 20);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));

        m.addElement(title);
        m.addElement(new LayoutRectangle("f1", 15, 35, 260, 120));
        m.addElement(new LayoutNorthArrow("n1", 220, 40, 35, 45));

        LayoutScaleBar s = new LayoutScaleBar("s1", 20, 162, 180, 30);
        s.setUnitLabel("m");
        m.addElement(s);

        LayoutLegend lg = new LayoutLegend("lg1", 25, 45, 120, 55);
        lg.setTitle("Referencias");
        lg.getItems().add(new LayoutLegend.LegendItem("Ruta", Color.ORANGE, "LineString"));
        lg.getItems().add(new LayoutLegend.LegendItem("Zona urbana", new Color(255, 200, 200), "Polygon"));
        m.addElement(lg);

        assertGolden("pipeline.png", m);
    }

    // ---- Map with preview image (headless, no MapPanel needed) ----

    @Test
    void mapWithPreviewImage() throws Exception {
        LayoutModel m = new LayoutModel();
        LayoutMap map = new LayoutMap("m1", 20, 20, 250, 130);

        // Create a synthetic preview image simulating rendered map content
        BufferedImage preview = new BufferedImage(500, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = preview.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background (land)
        g.setColor(new Color(245, 245, 220));
        g.fillRect(0, 0, 500, 260);

        // Water body
        g.setColor(new Color(100, 149, 237));
        g.fillOval(50, 30, 150, 100);

        // Roads
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(3f));
        g.drawLine(0, 180, 300, 120);
        g.drawLine(300, 120, 500, 60);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(100, 200, 400, 200);

        // City markers
        g.setColor(Color.RED);
        g.fillOval(345, 112, 10, 10);
        g.fillOval(50, 22, 8, 8);

        // Labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.drawString("Ciudad A", 358, 110);
        g.drawString("Lago", 90, 75);

        g.dispose();

        map.setPreviewImage(preview);
        m.addElement(map);

        assertGolden("map-preview.png", m);
    }

    @Test
    void pipelineWithMapPreview() throws Exception {
        LayoutModel m = new LayoutModel();

        // Title
        LayoutLabel title = new LayoutLabel("t1", "Plancheta Catastral", 110, 8, 160, 18);
        title.setFont(new Font("SansSerif", Font.BOLD, 11));

        // Map frame with preview
        LayoutMap map = new LayoutMap("m1", 15, 30, 260, 120);

        BufferedImage preview = new BufferedImage(520, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = preview.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(248, 248, 240));
        g.fillRect(0, 0, 520, 240);
        // Parcels (simplified)
        g.setColor(new Color(220, 220, 200));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 400; i += 80) {
            for (int j = 0; j < 200; j += 60) {
                g.drawRect(i + 10, j + 10, 70, 50);
            }
        }
        // Main road
        g.setColor(new Color(200, 180, 160));
        g.setStroke(new BasicStroke(4f));
        g.drawLine(0, 120, 520, 120);
        g.drawLine(260, 0, 260, 240);
        g.dispose();

        map.setPreviewImage(preview);
        map.setFrameColor(new Color(40, 40, 40));
        map.setFrameWidth(1.5f);
        m.addElement(map);

        // Legend
        LayoutLegend lg = new LayoutLegend("lg1", 15, 158, 260, 40);
        lg.setTitle("Referencias");
        lg.getItems().add(new LayoutLegend.LegendItem("Parcelas", new Color(220, 220, 200), "Polygon"));
        lg.getItems().add(new LayoutLegend.LegendItem("Ruta principal", new Color(200, 180, 160), "LineString"));
        m.addElement(lg);

        // North arrow
        m.addElement(new LayoutNorthArrow("n1", 230, 35, 35, 50));

        m.addElement(title);
        assertGolden("pipeline-map.png", m);
    }

    // ---- helpers ----

    private void assertGolden(String name, LayoutModel model) throws Exception {
        BufferedImage actual = LayoutExportEngine.renderLayout(model, DPI);
        assertNotNull(actual);

        Path goldenPath = goldenPath(name);
        if (!Files.exists(goldenPath)) {
            GoldenImageAssert.saveGoldenImage(actual, goldenPath);
            return; // golden created
        }

        BufferedImage expected = GoldenImageAssert.loadGoldenImage(
                "ar/com/catgis/catmap/golden/" + name);
        GoldenImageAssert.assertMatches(expected, actual, 1.0, 8);
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
