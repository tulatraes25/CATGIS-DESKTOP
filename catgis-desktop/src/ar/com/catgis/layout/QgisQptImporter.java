package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QgisQptImporter {

    public static class ImportResult {
        public final List<LayoutElement> imported = new ArrayList<>();
        public final List<String> skipped = new ArrayList<>();
        public double pageWidthMm = 297;
        public double pageHeightMm = 210;
        public String orientation = "landscape";
    }

    public static ImportResult importQpt(File file) throws Exception {
        ImportResult result = new ImportResult();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();
        NodeList composerItems = root.getElementsByTagName("ComposerItem");
        if (composerItems.getLength() == 0) {
            composerItems = root.getElementsByTagName("*");
        }

        // Try to find page size from ComposerMap or page properties
        for (int i = 0; i < composerItems.getLength(); i++) {
            Node node = composerItems.item(i);
            if (!(node instanceof Element)) continue;
            Element el = (Element) node;
            String tag = el.getTagName();
            if (tag.contains("Paper") || tag.contains("Composition") || tag.contains("Layout")) {
                result.pageWidthMm = getDoubleAttr(el, "width", 297);
                result.pageHeightMm = getDoubleAttr(el, "height", 210);
                break;
            }
        }

        for (int i = 0; i < composerItems.getLength(); i++) {
            Node node = composerItems.item(i);
            if (!(node instanceof Element)) continue;
            Element el = (Element) node;
            String tag = el.getTagName();
            String id = getAttr(el, "id", getAttr(el, "uuid", "qpt-" + i));
            double x = getDoubleAttr(el, "x", getDoubleAttr(el, "pageX", 10));
            double y = getDoubleAttr(el, "y", getDoubleAttr(el, "pageY", 10));
            double w = getDoubleAttr(el, "width", 100);
            double h = getDoubleAttr(el, "height", 50);

            if (tag.contains("ComposerLabel") || tag.contains("Label")) {
                String text = getTextContent(el, "labelText", getTextContent(el, "text", "Texto QGIS"));
                Font font = parseFont(el);
                LayoutLabel label = new LayoutLabel(id, text, x, y, w, h);
                if (font != null) label.setFont(font);
                label.setName("QGIS Texto");
                result.imported.add(label);
            } else if (tag.contains("ComposerMap") || tag.contains("Map")) {
                LayoutMap map = new LayoutMap(id, x, y, w, h);
                map.setName("QGIS Mapa");
                result.imported.add(map);
            } else if (tag.contains("ComposerLegend") || tag.contains("Legend")) {
                LayoutLegend legend = new LayoutLegend(id, x, y, w, h);
                legend.setAutoHeight(true);
                legend.setShowBackground(false);
                legend.setName("QGIS Leyenda");
                String title = getTextContent(el, "title", "");
                if (!title.isEmpty()) legend.setTitle(title);
                result.imported.add(legend);
            } else if (tag.contains("ComposerPicture") || tag.contains("Picture")) {
                String path = getAttr(el, "pictureFile", getAttr(el, "imageSource", ""));
                if (!path.isEmpty()) {
                    result.skipped.add("Imagen: " + path + " (path no verificada)");
                }
            } else if (tag.contains("ComposerScaleBar") || tag.contains("ScaleBar")) {
                LayoutScaleBar scale = new LayoutScaleBar(id, x, y, w, h);
                scale.setName("QGIS Escala");
                result.imported.add(scale);
            } else if (tag.contains("ComposerArrow") || tag.contains("NorthArrow") || tag.contains("North")) {
                LayoutNorthArrow north = new LayoutNorthArrow(id, x, y, w, h);
                north.setName("QGIS Norte");
                result.imported.add(north);
            } else if (tag.contains("ComposerShape") || tag.contains("Rectangle")) {
                LayoutRectangle rect = new LayoutRectangle(id, x, y, w, h);
                rect.setName("QGIS Rectangulo");
                result.imported.add(rect);
            } else {
                result.skipped.add(tag + " (no soportado)");
            }
        }
        return result;
    }

    private static double getDoubleAttr(Element el, String name, double def) {
        try { String v = el.getAttribute(name); if (v != null && !v.isEmpty()) return Double.parseDouble(v); } catch (Exception ignored) {}
        try { Node n = el.getElementsByTagName(name).item(0); if (n != null) return Double.parseDouble(n.getTextContent()); } catch (Exception ignored) {}
        return def;
    }

    private static String getAttr(Element el, String name, String def) {
        String v = el.getAttribute(name);
        return (v != null && !v.isEmpty()) ? v : def;
    }

    private static String getTextContent(Element el, String tag, String def) {
        NodeList list = el.getElementsByTagName(tag);
        if (list.getLength() > 0) {
            String txt = list.item(0).getTextContent();
            return (txt != null && !txt.isEmpty()) ? txt : def;
        }
        return def;
    }

    private static Font parseFont(Element el) {
        try {
            String family = getAttr(el, "font", "SansSerif");
            int size = (int) getDoubleAttr(el, "fontSize", 12);
            boolean bold = "true".equalsIgnoreCase(getAttr(el, "bold", "false"));
            int style = bold ? Font.BOLD : Font.PLAIN;
            return new Font(family, style, size);
        } catch (Exception ignored) {}
        return null;
    }
}
