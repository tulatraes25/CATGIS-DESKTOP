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
