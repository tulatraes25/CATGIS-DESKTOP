package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutModel;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CATMAP SVG 2 output with a single embedded PNG raster.
 * This is NOT editable vector text, linework or an actual vector map export.
 */
public final class SvgExportEngine {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String IMAGE_PREFIX = "data:image/png;base64,";
    private static final long MAX_EMBEDDED_PNG_BYTES = 64L * 1024L * 1024L;
    private static final long MAX_SVG_BYTES = 128L * 1024L * 1024L;
    private static final double PAGE_WIDTH_MM = 297.0;
    private static final double PAGE_HEIGHT_MM = 210.0;

    private SvgExportEngine() {}

    /**
     * Export layout as a raster image embedded in an SVG wrapper.
     * A unique staged file is parsed and its embedded pixels checked before
     * the previous destination is replaced in a single atomic filesystem move.
     */
    public static void exportSvg(LayoutModel model, File file, int dpi) throws Exception {
        exportSvg(model, file, dpi, CatmapAtomicExport::atomicMove);
    }

    /** Package-private fault injection for exact old-file rollback checks. */
    static void exportSvg(LayoutModel model, File file, int dpi,
                          CatmapAtomicExport.AtomicMover mover) throws Exception {
        BufferedImage raster = LayoutExportEngine.renderLayout(model, dpi);
        CatmapAtomicExport.publish(file,
                staged -> writeRasterSvg(raster, staged),
                staged -> verifyRasterSvg(staged, raster.getWidth(), raster.getHeight()),
                mover);
    }

    private static void writeRasterSvg(BufferedImage raster, Path staged)
            throws IOException {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        if (!ImageIO.write(raster, "png", png)) {
            throw new IOException("CATMAP no dispone de codificador PNG para SVG.");
        }
        byte[] pngBytes = png.toByteArray();
        if (pngBytes.length == 0 || pngBytes.length > MAX_EMBEDDED_PNG_BYTES) {
            throw new IOException("El PNG incrustado del SVG excede el limite seguro.");
        }
        String base64 = Base64.getEncoder().encodeToString(pngBytes);
        // BufferedWriter propagates I/O errors; PrintWriter would suppress many.
        try (Writer writer = Files.newBufferedWriter(staged, StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<svg xmlns=\"" + SVG_NAMESPACE
                    + "\" width=\"" + PAGE_WIDTH_MM + "mm\" height=\""
                    + PAGE_HEIGHT_MM + "mm\" viewBox=\"0 0 "
                    + PAGE_WIDTH_MM + " " + PAGE_HEIGHT_MM + "\">\n");
            writer.write("  <rect width=\"" + PAGE_WIDTH_MM
                    + "\" height=\"" + PAGE_HEIGHT_MM + "\" fill=\"white\"/>\n");
            writer.write("  <image x=\"0\" y=\"0\" width=\""
                    + PAGE_WIDTH_MM + "\" height=\"" + PAGE_HEIGHT_MM
                    + "\" href=\"" + IMAGE_PREFIX);
            writer.write(base64);
            writer.write("\"/>\n</svg>\n");
        }
    }

    /**
     * Confirm secure XML structure and actual embedded PNG pixels.
     * Parsing is restricted to CATMAP's bounded output, with DTD and external
     * entities disabled. This validates a raster SVG, not true vector data.
     */
    static void verifyRasterSvg(Path staged, int expectedWidth, int expectedHeight)
            throws IOException {
        long bytes = Files.size(staged);
        if (bytes == 0 || bytes > MAX_SVG_BYTES) {
            throw new IOException("CATMAP SVG vacio o superior al presupuesto seguro.");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(staged.toFile());
            Element root = document.getDocumentElement();
            if (!SVG_NAMESPACE.equals(root.getNamespaceURI())
                    || !"svg".equals(root.getLocalName())
                    || !((PAGE_WIDTH_MM + "mm").equals(root.getAttribute("width")))
                    || !((PAGE_HEIGHT_MM + "mm").equals(root.getAttribute("height")))
                    || !("0 0 " + PAGE_WIDTH_MM + " " + PAGE_HEIGHT_MM)
                            .equals(root.getAttribute("viewBox"))) {
                throw new IOException("CATMAP SVG tiene dimensiones o raiz inesperadas.");
            }
            // A generated raster wrapper has exactly one background rectangle
            // and one embedded image. Scripts, foreign tags and external
            // drawable content are not part of this contract.
            NodeList children = root.getChildNodes();
            int backgroundCount = 0;
            int imageCount = 0;
            int elementCount = 0;
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                elementCount++;
                if (!SVG_NAMESPACE.equals(child.getNamespaceURI())) {
                    throw new IOException("CATMAP SVG incluye elementos XML externos.");
                }
                if ("rect".equals(child.getLocalName())) {
                    backgroundCount++;
                } else if ("image".equals(child.getLocalName())) {
                    imageCount++;
                } else {
                    throw new IOException("CATMAP SVG incluye elementos no admitidos.");
                }
            }
            if (elementCount != 2 || backgroundCount != 1 || imageCount != 1) {
                throw new IOException("CATMAP SVG tiene estructura inesperada.");
            }
            NodeList images = document.getElementsByTagNameNS(SVG_NAMESPACE, "image");
            if (images.getLength() != 1) {
                throw new IOException("CATMAP SVG debe incluir exactamente un raster PNG.");
            }
            String href = ((Element) images.item(0)).getAttribute("href");
            if (!href.startsWith(IMAGE_PREFIX)) {
                throw new IOException("CATMAP SVG no contiene el PNG incrustado esperado.");
            }
            String encoded = href.substring(IMAGE_PREFIX.length());
            if (encoded.isEmpty() || (long) encoded.length() > MAX_EMBEDDED_PNG_BYTES * 4 / 3 + 8) {
                throw new IOException("CATMAP SVG PNG incrustado excede el presupuesto.");
            }
            byte[] pngBytes = Base64.getDecoder().decode(encoded);
            if (pngBytes.length == 0 || pngBytes.length > MAX_EMBEDDED_PNG_BYTES) {
                throw new IOException("CATMAP SVG PNG incrustado vacio o excesivo.");
            }
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(pngBytes));
            if (decoded == null || decoded.getWidth() != expectedWidth
                    || decoded.getHeight() != expectedHeight) {
                throw new IOException("CATMAP SVG PNG incrustado ilegible o dimensiones invalidas.");
            }
            decoded.flush();
        } catch (IOException io) {
            throw io;
        } catch (Exception invalid) {
            throw new IOException("CATMAP SVG no supera validacion XML/PNG.", invalid);
        }
    }
}
