package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutCartouche;
import ar.com.catgis.layout.LayoutEllipse;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutLine;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutRectangle;
import ar.com.catgis.layout.LayoutScaleBar;
import ar.com.catgis.layout.LayoutTable;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vector SVG: text and shapes stay editable; only the map frame is raster. */
class SvgVectorExporterContractTest {

    @TempDir
    Path directory;

    private static LayoutModel vectorModel() {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("lbl", "Hola & <SVG>", 10, 8, 120, 12);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setColor(Color.decode("#1a2434"));
        model.addElement(label);
        model.addElement(new LayoutRectangle("rect", 10, 25, 60, 20));
        LayoutEllipse ellipse = new LayoutEllipse("ellipse", 80, 25, 40, 20);
        ellipse.setFillColor(new Color(0x33AA55));
        ellipse.setBorderColor(Color.BLACK);
        model.addElement(ellipse);
        LayoutLine line = new LayoutLine("line", 10, 60, 100, 0);
        line.setColor(Color.RED);
        line.setLineWidth(1.2f);
        model.addElement(line);
        LayoutScaleBar scaleBar = new LayoutScaleBar("scale", 10, 70, 80, 12);
        scaleBar.setMapScaleDenominator(50000);
        model.addElement(scaleBar);
        LayoutLegend legend = new LayoutLegend("legend", 10, 90, 80, 40);
        legend.setTitle("Referencias");
        legend.getItems().add(new LayoutLegend.LegendItem("Rios", Color.BLUE, "LINE"));
        model.addElement(legend);
        LayoutCartouche cartouche = new LayoutCartouche("cartouche", 100, 90, 90, 40);
        cartouche.setField("Proyecto", "CATGIS");
        model.addElement(cartouche);
        model.addElement(LayoutTable.createFromData("table", 100, 140,
                new String[][]{{"Clase", "Area"}, {"Bosque", "12"}}));
        model.addElement(new LayoutNorthArrow("north", 270, 12, 16, 20));
        return model;
    }

    @Test
    void textAndShapesAreNativeSvgWithoutEmbeddedImages() throws Exception {
        File file = directory.resolve("vector.svg").toFile();
        SvgVectorExporter.exportPage(vectorModel(), file);
        String svg = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        assertTrue(svg.startsWith("<?xml"));
        assertTrue(svg.contains("<text"), "labels must be vector text");
        assertTrue(svg.contains("<ellipse"), "ellipses must be vector");
        assertTrue(svg.contains("<line"), "lines must be vector");
        assertTrue(svg.contains("<polygon"), "north arrow must be vector");
        assertTrue(svg.contains("Hola &amp; &lt;SVG&gt;"), "text must be XML-escaped");
        assertTrue(svg.contains("Referencias"), "legend title must be vector text");
        assertTrue(svg.contains("CATGIS"), "cartouche fields must be vector text");
        assertTrue(!svg.contains("data:image/png"),
                "a page without raster elements must not embed images");

        SvgVectorExporter.verifyVectorSvg(file.toPath(), LayoutPageSpec.A4_LANDSCAPE);
    }

    @Test
    void mapFrameIsTheOnlyEmbeddedRaster() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("lbl", "Titulo", 10, 8, 100, 12));
        LayoutMap map = new LayoutMap("map", 10, 25, 260, 150);
        BufferedImage preview = new BufferedImage(40, 24, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < preview.getHeight(); y++) {
            for (int x = 0; x < preview.getWidth(); x++) {
                preview.setRGB(x, y, (x * 5) << 8 | y);
            }
        }
        map.setPreviewImage(preview);
        model.addElement(map);

        File file = directory.resolve("mapa.svg").toFile();
        SvgVectorExporter.exportPage(model, file);
        String svg = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        assertTrue(svg.contains("<image"));
        assertTrue(svg.contains("href=\"data:image/png;base64,"));
        assertTrue(svg.contains("<text"), "the title stays editable text");
        assertEquals(1, count(svg, "<image"));
        SvgVectorExporter.verifyVectorSvg(file.toPath(), LayoutPageSpec.A4_LANDSCAPE);
    }

    @Test
    void documentSheetDrivesTheSvgRoot() throws Exception {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT));
        model.addElement(new LayoutLabel("lbl", "A3", 10, 10, 100, 12));
        File file = directory.resolve("a3.svg").toFile();
        SvgVectorExporter.exportPage(model, file);
        String svg = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(svg.contains("width=\"297mm\""));
        assertTrue(svg.contains("height=\"420mm\""));
        assertTrue(svg.contains("viewBox=\"0 0 297 420\""));
    }

    @Test
    void exportReplacesAnExistingFileAtomically() throws Exception {
        File file = directory.resolve("reemplazo.svg").toFile();
        Files.writeString(file.toPath(), "basura anterior", StandardCharsets.UTF_8);
        SvgVectorExporter.exportPage(vectorModel(), file);
        String svg = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(svg.startsWith("<?xml"));
        assertTrue(!svg.contains("basura anterior"));
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void validationRejectsNonSvgAndExternalImages() throws Exception {
        File notXml = directory.resolve("roto.svg").toFile();
        Files.writeString(notXml.toPath(), "esto no es XML", StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class,
                () -> SvgVectorExporter.verifyVectorSvg(notXml.toPath(),
                        LayoutPageSpec.A4_LANDSCAPE));

        File empty = directory.resolve("vacio.svg").toFile();
        Files.writeString(empty.toPath(),
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"297mm\""
                        + " height=\"210mm\" viewBox=\"0 0 297 210\"/>",
                StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class,
                () -> SvgVectorExporter.verifyVectorSvg(empty.toPath(),
                        LayoutPageSpec.A4_LANDSCAPE));

        File external = directory.resolve("externo.svg").toFile();
        Files.writeString(external.toPath(),
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"297mm\""
                        + " height=\"210mm\" viewBox=\"0 0 297 210\">"
                        + "<image href=\"https://example.com/x.png\"/>"
                        + "<rect x=\"0\" y=\"0\" width=\"1\" height=\"1\"/>"
                        + "</svg>",
                StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class,
                () -> SvgVectorExporter.verifyVectorSvg(external.toPath(),
                        LayoutPageSpec.A4_LANDSCAPE));
    }

    private static int count(String haystack, String needle) {
        int count = 0;
        int index = 0;
        while ((index = haystack.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
