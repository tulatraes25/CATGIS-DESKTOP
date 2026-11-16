package ar.com.catgis.catmap;

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
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic golden-master scenes (A0.2 characterisation basis) and a stable
 * pixel hash used by the regression contracts. Scenes are pure layout models so
 * they do not depend on MapPanel or the composer.
 */
public final class GoldenMaster {

    /** A named deterministic scene. */
    public record Scene(String name, LayoutModel model) {}

    /** Hash of a rendered scene at a DPI. */
    public record Entry(String scene, int dpi, int width, int height, String sha256) {}

    private GoldenMaster() {}

    /** Deterministic scene catalogue used by the golden-master contracts. */
    public static List<Scene> scenes() {
        List<Scene> scenes = new ArrayList<>();
        scenes.add(new Scene("a4_label", a4Label()));
        scenes.add(new Scene("a4_pipeline", a4Pipeline()));
        scenes.add(new Scene("a3_portrait", a3Portrait()));
        scenes.add(new Scene("a4_map_preview", a4MapPreview()));
        return scenes;
    }

    private static LayoutModel a4Label() {
        LayoutModel model = new LayoutModel();
        LayoutLabel title = new LayoutLabel("t", "Golden CATGIS", 20, 20, 200, 24);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setColor(new Color(0x1A2434));
        model.addElement(title);
        model.addElement(new LayoutRectangle("r", 20, 60, 120, 60));
        model.addElement(new LayoutLine("l", 20, 140, 240, 0));
        LayoutScaleBar bar = new LayoutScaleBar("s", 20, 160, 160, 24);
        bar.setMapScaleDenominator(25000);
        model.addElement(bar);
        return model;
    }

    private static LayoutModel a4Pipeline() {
        LayoutModel model = new LayoutModel();
        LayoutLabel title = new LayoutLabel("t", "Pipeline A0.2", 20, 16, 240, 20);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        model.addElement(title);
        LayoutLegend legend = new LayoutLegend("lg", 20, 50, 120, 70);
        legend.setTitle("Referencias");
        legend.getItems().add(new LayoutLegend.LegendItem("Ruta", Color.ORANGE, "LineString"));
        legend.getItems().add(new LayoutLegend.LegendItem("Zona", new Color(255, 200, 200), "Polygon"));
        model.addElement(legend);
        model.addElement(new LayoutNorthArrow("n", 230, 50, 30, 40));
        LayoutScaleBar bar = new LayoutScaleBar("s", 20, 160, 160, 24);
        bar.setMapScaleDenominator(50000);
        model.addElement(bar);
        return model;
    }

    private static LayoutModel a3Portrait() {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT));
        LayoutLabel title = new LayoutLabel("t", "A3 vertical", 20, 20, 200, 24);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        model.addElement(title);
        model.addElement(new LayoutEllipse("e", 20, 70, 120, 60));
        model.addElement(new LayoutRectangle("r", 170, 70, 80, 60));
        return model;
    }

    private static LayoutModel a4MapPreview() {
        LayoutModel model = new LayoutModel();
        LayoutMap map = new LayoutMap("m", 15, 30, 267, 130);
        map.setPreviewImage(syntheticMapPreview(200, 100));
        model.addElement(map);
        LayoutLabel title = new LayoutLabel("t", "Mapa de referencia", 15, 8, 220, 18);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        model.addElement(title);
        LayoutLegend legend = new LayoutLegend("lg", 15, 168, 150, 30);
        legend.setTitle("Capas");
        legend.getItems().add(new LayoutLegend.LegendItem("Rios", new Color(30, 100, 200), "LineString"));
        model.addElement(legend);
        return model;
    }

    /**
     * Deterministic raster preview (no timers, no random, no environment data):
     * gradient background plus a fixed grid, used instead of a live MapPanel.
     */
    public static BufferedImage syntheticMapPreview(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = 40 + (x * 120) / Math.max(1, width);
                    int green = 90 + (y * 90) / Math.max(1, height);
                    int blue = 110;
                    image.setRGB(x, y, 0xFF000000 | (red << 16) | (green << 8) | blue);
                }
            }
            graphics.setColor(new Color(0xFFF2CC));
            graphics.fillRect(width / 5, height / 4, width / 3, height / 3);
            graphics.setColor(new Color(0x3366CC));
            graphics.drawLine(0, height / 2, width, height / 2);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    /** Render the model at the given DPI and capture its stable hash. */
    public static Entry capture(String scene, LayoutModel model, int dpi) {
        BufferedImage image = LayoutExportService.renderPage(model, model.getCurrentPage(), dpi);
        Entry entry = new Entry(scene, dpi, image.getWidth(), image.getHeight(), sha256(image));
        image.flush();
        return entry;
    }

    /**
     * Stable SHA-256 over dimensions + ARGB pixels. Independent of the PNG
     * encoder (which may add metadata), so it characterises the rendered image.
     */
    public static String sha256(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(intBytes(image.getWidth()));
            digest.update(intBytes(image.getHeight()));
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                    null, 0, image.getWidth());
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(pixels.length * 4);
            for (int pixel : pixels) {
                buffer.putInt(pixel);
            }
            digest.update(buffer.array());
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception unsupported) {
            return "unavailable";
        }
    }

    private static byte[] intBytes(int value) {
        return java.nio.ByteBuffer.allocate(4).putInt(value).array();
    }

    /** Markdown report of the captured masters (documentation artefact). */
    public static String reportMarkdown(List<Entry> entries) {
        StringBuilder text = new StringBuilder();
        text.append("# Golden masters A0.2\n\n");
        text.append("Escenas deterministas de `catmap/GoldenMaster`; hash SHA-256 sobre\n");
        text.append("dimensiones + pixeles ARGB del render (independiente del codificador PNG).\n\n");
        text.append("| Escena | DPI | Tamano | SHA-256 |\n|---|---|---|---|\n");
        for (Entry entry : entries) {
            text.append("| ").append(entry.scene())
                    .append(" | ").append(entry.dpi())
                    .append(" | ").append(entry.width()).append("x").append(entry.height())
                    .append(" | `").append(entry.sha256()).append("` |\n");
        }
        text.append(String.format(Locale.ROOT,
                "\nNota: las fuentes del sistema pueden cambiar los pixeles entre sistemas"
                        + " operativos; el hash es una referencia por plataforma/JDK y la"
                        + " certificacion CI define su propia linea base.%n"));
        return text.toString();
    }
}
