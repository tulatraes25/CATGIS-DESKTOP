package ar.com.catgis;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PointGraphicSymbolSupport {

    private static final Map<String, BufferedImage> IMAGE_CACHE = new ConcurrentHashMap<>();

    private PointGraphicSymbolSupport() {
    }

    public static boolean paintLayerSymbol(Graphics2D g2, Layer layer, int centerX, int centerY, int size) {
        if (layer == null) {
            return false;
        }
        return paintSymbol(g2, layer.getPointGraphicSymbol(), centerX, centerY, size);
    }

    public static boolean paintSymbol(Graphics2D g2, String reference, int centerX, int centerY, int size) {
        BufferedImage image = loadImage(reference, size);
        if (image == null) {
            return false;
        }
        int drawX = centerX - (image.getWidth() / 2);
        int drawY = centerY - (image.getHeight() / 2);
        g2.drawImage(image, drawX, drawY, null);
        return true;
    }

    public static ImageIcon buildPreviewIcon(String reference, int size) {
        BufferedImage image = loadImage(reference, size);
        return image != null ? new ImageIcon(image) : null;
    }

    public static BufferedImage loadImage(String reference, int size) {
        if (reference == null || reference.isBlank() || size <= 0) {
            return null;
        }

        String normalized = reference.trim();
        String key = normalized + "|" + size;
        BufferedImage cached = IMAGE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        try {
            URL sourceUrl = resolveUrl(normalized);
            if (sourceUrl == null) {
                return null;
            }

            String lower = sourceUrl.toString().toLowerCase();
            BufferedImage image;
            if (lower.endsWith(".svg")) {
                image = rasterizeSvg(sourceUrl, size);
            } else {
                BufferedImage raw = ImageIO.read(sourceUrl);
                image = fitImage(raw, size);
            }

            if (image != null) {
                IMAGE_CACHE.put(key, image);
            }
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private static URL resolveUrl(String reference) throws Exception {
        if (PointSymbolCatalog.isCatalogReference(reference)) {
            String resourcePath = PointSymbolCatalog.resolveResourcePath(reference);
            return resourcePath != null ? PointGraphicSymbolSupport.class.getResource(resourcePath) : null;
        }
        if (reference.startsWith("classpath:")) {
            String resourcePath = reference.substring("classpath:".length()).trim();
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            return PointGraphicSymbolSupport.class.getResource(resourcePath);
        }

        File file = new File(reference);
        if (file.exists()) {
            return file.toURI().toURL();
        }
        return null;
    }

    private static BufferedImage rasterizeSvg(URL sourceUrl, int size) throws Exception {
        SVGDocument document = new SVGLoader().load(sourceUrl);
        if (document == null) {
            return null;
        }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            document.render(null, g2, new ViewBox(0, 0, size, size));
        } finally {
            g2.dispose();
        }
        return image;
    }

    private static BufferedImage fitImage(BufferedImage raw, int size) {
        if (raw == null) {
            return null;
        }
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double scale = Math.min(size / (double) raw.getWidth(), size / (double) raw.getHeight());
            int drawW = Math.max(1, (int) Math.round(raw.getWidth() * scale));
            int drawH = Math.max(1, (int) Math.round(raw.getHeight() * scale));
            int drawX = (size - drawW) / 2;
            int drawY = (size - drawH) / 2;
            g2.drawImage(raw, drawX, drawY, drawW, drawH, null);
        } finally {
            g2.dispose();
        }
        return result;
    }
}
