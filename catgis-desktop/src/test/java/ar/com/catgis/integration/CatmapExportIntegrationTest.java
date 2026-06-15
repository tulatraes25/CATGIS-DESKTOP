package ar.com.catgis.integration;

import ar.com.catgis.catmap.LayoutExportEngine;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutScaleBar;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CatmapExportIntegrationTest {

    @BeforeAll
    static void prepareFixtures() throws Exception {
        IntegrationFixtureFactory.ensureAllFixtures();
    }

    @Test
    void exportsLayoutToPngAndPdfHeadless() throws Exception {
        LayoutModel model = new LayoutModel();

        LayoutLabel title = new LayoutLabel("title", "CATGIS Integration Layout", 20, 10, 160, 12);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        model.addElement(title);

        LayoutMap map = new LayoutMap("map", 20, 28, 210, 120);
        map.setPreviewImage(createPreviewImage());
        model.addElement(map);

        LayoutLegend legend = new LayoutLegend("legend", 235, 28, 45, 55);
        legend.setTitle("Legend");
        legend.getItems().add(new LayoutLegend.LegendItem("Road", Color.ORANGE, "LineString"));
        legend.getItems().add(new LayoutLegend.LegendItem("Parcel", new Color(210, 230, 210), "Polygon"));
        model.addElement(legend);

        LayoutScaleBar scaleBar = new LayoutScaleBar("scale", 20, 155, 80, 15);
        scaleBar.setUnitLabel("km");
        model.addElement(scaleBar);

        LayoutNorthArrow northArrow = new LayoutNorthArrow("north", 240, 90, 20, 30);
        model.addElement(northArrow);

        assertAllBoundsInsideA4(model);

        Path outputDir = IntegrationFixtureFactory.outputsDir();
        Path png = outputDir.resolve("catmap-integration-layout.png");
        Path pdf = outputDir.resolve("catmap-integration-layout.pdf");

        LayoutExportEngine.exportPng(model, png.toFile(), 96);
        LayoutExportEngine.exportPdf(model, pdf.toFile(), 150);

        assertTrue(Files.exists(png));
        assertTrue(Files.size(png) > 0);
        BufferedImage image = ImageIO.read(png.toFile());
        assertNotNull(image);
        assertTrue(image.getWidth() > 0);
        assertTrue(image.getHeight() > 0);

        assertTrue(Files.exists(pdf));
        assertTrue(Files.size(pdf) > 0);
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            assertEquals(1, document.getNumberOfPages());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("CATGIS Integration Layout"), "El PDF no contiene el título esperado.");
        }
    }

    private static BufferedImage createPreviewImage() {
        BufferedImage preview = new BufferedImage(800, 450, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = preview.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 245, 230));
        g.fillRect(0, 0, preview.getWidth(), preview.getHeight());

        g.setColor(new Color(178, 210, 255));
        g.fillOval(70, 50, 170, 110);

        g.setColor(new Color(210, 235, 210));
        g.fillRect(260, 110, 270, 170);

        g.setColor(new Color(175, 175, 175));
        g.setStroke(new BasicStroke(8f));
        g.drawLine(0, 330, 780, 150);
        g.setStroke(new BasicStroke(4f));
        g.drawLine(110, 0, 260, 450);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Parcel A", 330, 180);
        g.drawString("Lagoon", 115, 115);
        g.dispose();
        return preview;
    }

    private static void assertAllBoundsInsideA4(LayoutModel model) {
        model.getVisibleElementsSortedByZ().forEach(element -> {
            var bounds = element.getBoundsMm();
            assertTrue(bounds.width > 0d, "width inválido para " + element.getId());
            assertTrue(bounds.height > 0d, "height inválido para " + element.getId());
            assertTrue(bounds.x >= 0d, "x inválido para " + element.getId());
            assertTrue(bounds.y >= 0d, "y inválido para " + element.getId());
            assertTrue(bounds.x + bounds.width <= 297d, "elemento fuera de A4 horizontal: " + element.getId());
            assertTrue(bounds.y + bounds.height <= 210d, "elemento fuera de A4 vertical: " + element.getId());
        });
    }
}
