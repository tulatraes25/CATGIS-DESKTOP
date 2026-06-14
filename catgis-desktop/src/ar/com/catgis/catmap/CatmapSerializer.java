package ar.com.catgis.catmap;

import ar.com.catgis.CatgisLogger;

import ar.com.catgis.layout.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Serializer/deserializer for .catmap layout files.
 * Format: simple key-value text format.
 */
public final class CatmapSerializer {

    private CatmapSerializer() {}

    /**
     * Save layout to .catmap file atomically.
     * Writes to a temporary file first, then renames to the target
     * to prevent corruption on crash during write.
     */
    public static void save(LayoutModel model, File file) throws IOException {
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8))) {
            w.write("# CATMAP Layout v1");
            w.newLine();

            // Page settings
            w.write("PAGE_SIZE=A4");
            w.newLine();
            w.write("PAGE_ORIENTATION=LANDSCAPE");
            w.newLine();

            // Elements
            for (LayoutElement el : model.getElements()) {
                w.write(serializeElement(el));
                w.newLine();
            }

            w.write("# End of layout");
            w.newLine();
        }
        // Atomic rename: replace target only after successful write
        if (file.exists()) {
            File backup = new File(file.getParentFile(), file.getName() + ".bak");
            if (backup.exists()) backup.delete();
            if (!file.renameTo(backup)) {
                CatgisLogger.warn("CatmapSerializer: no se pudo crear backup de " + file.getName(), null);
            }
        }
        if (!tmp.renameTo(file)) {
            throw new IOException("No se pudo guardar el layout: " + file.getAbsolutePath());
        }
    }

    /**
     * Load layout from .catmap file.
     */
    public static LayoutModel load(File file) throws IOException {
        LayoutModel model = new LayoutModel();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (trimmed.startsWith("PAGE_")) continue; // Skip page settings for now

                LayoutElement element = parseElement(trimmed);
                if (element != null) {
                    model.addElement(element);
                }
            }
        }

        return model;
    }

    // Public entry for undo/redo in LayoutModel
    public static String serializeElementRaw(LayoutElement el) {
        return serializeElement(el);
    }

    public static LayoutElement parseElementRaw(String line) {
        return parseElement(line);
    }

    private static String serializeElement(LayoutElement el) {
        StringBuilder sb = new StringBuilder();
        sb.append("ELEMENT|");
        sb.append(el.getClass().getSimpleName()).append("|");
        sb.append(el.getId()).append("|");
        sb.append(el.getName()).append("|");
        sb.append(el.getBoundsMm().x).append("|");
        sb.append(el.getBoundsMm().y).append("|");
        sb.append(el.getBoundsMm().width).append("|");
        sb.append(el.getBoundsMm().height).append("|");
        sb.append(el.getZOrder()).append("|");
        sb.append(el.isVisible()).append("|");
        sb.append(el.isLocked());

        // Type-specific properties
        if (el instanceof LayoutLabel label) {
            sb.append("|TEXT=").append(encodeText(label.getText()));
            sb.append("|FONT_FAMILY=").append(label.getFont().getFamily());
            sb.append("|FONT_SIZE=").append(label.getFont().getSize());
            sb.append("|FONT_STYLE=").append(label.getFont().getStyle());
            sb.append("|COLOR=").append(colorToHex(label.getColor()));
        } else if (el instanceof LayoutMap map) {
            sb.append("|SHOW_GRID=").append(map.isShowGrid());
            sb.append("|FRAME_COLOR=").append(colorToHex(map.getFrameColor()));
            sb.append("|FRAME_WIDTH=").append(map.getFrameWidth());
            sb.append("|OWN_EXTENT=").append(map.isOwnExtent());
            if (map.isOwnExtent()) {
                sb.append("|VIEW_MIN_X=").append(map.getOwnViewMinX());
                sb.append("|VIEW_MIN_Y=").append(map.getOwnViewMinY());
                sb.append("|ZOOM_FACTOR=").append(map.getOwnZoomFactor());
            }
        } else if (el instanceof LayoutLegend legend) {
            sb.append("|AUTO_HEIGHT=").append(legend.isAutoHeight());
            sb.append("|SHOW_BACKGROUND=").append(legend.isShowBackground());
            sb.append("|TITLE=").append(encodeText(legend.getTitle()));
        } else if (el instanceof LayoutScaleBar scale) {
            sb.append("|SCALE_DENOMINATOR=").append(scale.getMapScaleDenominator());
            sb.append("|SEGMENTS=").append(scale.getSegments());
        } else if (el instanceof LayoutNorthArrow) {
            // No extra properties needed
        } else if (el instanceof LayoutCartouche cartouche) {
            for (var entry : cartouche.getFields().entrySet()) {
                sb.append("|FIELD_").append(entry.getKey()).append("=").append(encodeText(entry.getValue()));
            }
        } else if (el instanceof LayoutImage image) {
            BufferedImage bi = image.getImage();
            if (bi != null) {
                try {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    ImageIO.write(bi, "png", baos);
                    String encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
                    sb.append("|IMAGE_DATA=").append(encoded);
                } catch (Exception e) {
                    sb.append("|IMAGE_DATA=");
                }
            } else {
                sb.append("|IMAGE_DATA=");
            }
        } else if (el instanceof LayoutTable table) {
            sb.append("|MAX_ROWS=").append(table.getMaxVisibleRows());
        }

        return sb.toString();
    }

    private static LayoutElement parseElement(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) {
            CatgisLogger.warn("CatmapSerializer: linea malformada (partes=" + parts.length
                    + "), omitiendo: " + (line.length() > 80 ? line.substring(0, 80) + "..." : line), null);
            return null;
        }

        String type = parts[1];
        String id = parts[2];
        String name = parts[3];
        double x = parseDouble(parts[4]);
        double y = parseDouble(parts[5]);
        double w = parseDouble(parts[6]);
        double h = parseDouble(parts[7]);
        int zOrder = parseInt(parts[8]);
        boolean visible = parseBoolean(parts[9]);
        boolean locked = parts.length > 10 ? parseBoolean(parts[10]) : false;

        if (w <= 0 || h <= 0) {
            CatgisLogger.warn("CatmapSerializer: elemento '" + id + "' (tipo=" + type
                    + ") tiene tamanio invalido w=" + w + " h=" + h + ", posible corrupcion de archivo", null);
        }

        LayoutElement element = switch (type) {
            case "LayoutLabel" -> {
                LayoutLabel label = new LayoutLabel(id, "", x, y, w, h);
                yield label;
            }
            case "LayoutMap" -> {
                LayoutMap map = new LayoutMap(id, x, y, w, h);
                yield map;
            }
            case "LayoutLegend" -> {
                LayoutLegend legend = new LayoutLegend(id, x, y, w, h);
                legend.setAutoHeight(true);
                yield legend;
            }
            case "LayoutScaleBar" -> {
                LayoutScaleBar scale = new LayoutScaleBar(id, x, y, w, h);
                yield scale;
            }
            case "LayoutNorthArrow" -> {
                LayoutNorthArrow north = new LayoutNorthArrow(id, x, y, w, h);
                yield north;
            }
            case "LayoutCartouche" -> {
                LayoutCartouche cartouche = new LayoutCartouche(id, x, y, w, h);
                yield cartouche;
            }
            case "LayoutRectangle" -> {
                LayoutRectangle rect = new LayoutRectangle(id, x, y, w, h);
                yield rect;
            }
            case "LayoutEllipse" -> {
                LayoutEllipse ellipse = new LayoutEllipse(id, x, y, w, h);
                yield ellipse;
            }
            case "LayoutLine" -> {
                LayoutLine ln = new LayoutLine(id, x, y, w, h);
                yield ln;
            }
            case "LayoutImage" -> {
                LayoutImage img = new LayoutImage(id, null, x, y, w, h);
                yield img;
            }
            case "LayoutTable" -> {
                LayoutTable tbl = new LayoutTable(id, x, y, w, h);
                yield tbl;
            }
            default -> null;
        };

        if (element == null) return null;

        element.setName(name);
        element.setZOrder(zOrder);
        element.setVisible(visible);
        element.setLocked(locked);

        // Parse type-specific properties
        for (int i = 11; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("TEXT=")) {
                if (element instanceof LayoutLabel label) {
                    label.setText(decodeText(part.substring(5)));
                }
            } else if (part.startsWith("FONT_FAMILY=")) {
                if (element instanceof LayoutLabel label) {
                    String family = part.substring(12);
                    label.setFont(new Font(family, label.getFont().getStyle(), label.getFont().getSize()));
                }
            } else if (part.startsWith("FONT_SIZE=")) {
                if (element instanceof LayoutLabel label) {
                    int size = parseInt(part.substring(10));
                    label.setFont(new Font(label.getFont().getFamily(), label.getFont().getStyle(), size));
                }
            } else if (part.startsWith("FONT_STYLE=")) {
                if (element instanceof LayoutLabel label) {
                    int style = parseInt(part.substring(11));
                    label.setFont(new Font(label.getFont().getFamily(), style, label.getFont().getSize()));
                }
            } else if (part.startsWith("COLOR=")) {
                if (element instanceof LayoutLabel label) {
                    label.setColor(parseColor(part.substring(6)));
                }
            } else if (part.startsWith("SHOW_GRID=")) {
                if (element instanceof LayoutMap map) {
                    map.setShowGrid(parseBoolean(part.substring(10)));
                }
            } else if (part.startsWith("FRAME_COLOR=")) {
                if (element instanceof LayoutMap map) {
                    map.setFrameColor(parseColor(part.substring(12)));
                }
            } else if (part.startsWith("FRAME_WIDTH=")) {
                if (element instanceof LayoutMap map) {
                    map.setFrameWidth((float) parseDouble(part.substring(12)));
                }
            } else if (part.startsWith("OWN_EXTENT=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnExtent(parseBoolean(part.substring(11)));
                }
            } else if (part.startsWith("VIEW_MIN_X=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnViewMinX(parseDouble(part.substring(11)));
                }
            } else if (part.startsWith("VIEW_MIN_Y=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnViewMinY(parseDouble(part.substring(11)));
                }
            } else if (part.startsWith("ZOOM_FACTOR=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnZoomFactor(parseDouble(part.substring(12)));
                }
            } else if (part.startsWith("AUTO_HEIGHT=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setAutoHeight(parseBoolean(part.substring(12)));
                }
            } else if (part.startsWith("SHOW_BACKGROUND=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setShowBackground(parseBoolean(part.substring(15)));
                }
            } else if (part.startsWith("TITLE=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setTitle(decodeText(part.substring(6)));
                }
            } else if (part.startsWith("SCALE_DENOMINATOR=")) {
                if (element instanceof LayoutScaleBar scale) {
                    scale.setMapScaleDenominator(parseDouble(part.substring(18)));
                }
            } else if (part.startsWith("SEGMENTS=")) {
                if (element instanceof LayoutScaleBar scale) {
                    scale.setSegments(parseInt(part.substring(9)));
                }
            } else if (part.startsWith("IMAGE_DATA=")) {
                if (element instanceof LayoutImage img) {
                    String data = part.substring(11);
                    if (!data.isEmpty()) {
                        try {
                            byte[] bytes = Base64.getDecoder().decode(data);
                            BufferedImage bi = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                            if (bi != null) img.setImage(bi);
                        } catch (Exception ignored) {
                            CatgisLogger.warn("CatmapSerializer: no se pudo decodificar IMAGE_DATA", null);
                        }
                    }
                }
            } else if (part.startsWith("FIELD_")) {
                if (element instanceof LayoutCartouche cartouche) {
                    int eq = part.indexOf('=');
                    if (eq > 6) {
                        String key = part.substring(6, eq);
                        String value = decodeText(part.substring(eq + 1));
                        cartouche.setField(key, value);
                    }
                }
            }
        }

        return element;
    }

    // --- Utility methods ---

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) {
            CatgisLogger.warn("CatmapSerializer: valor decimal invalido '" + s + "', usando 0", null);
            return 0;
        }
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) {
            CatgisLogger.warn("CatmapSerializer: valor entero invalido '" + s + "', usando 0", null);
            return 0;
        }
    }

    private static boolean parseBoolean(String s) {
        String trimmed = s.trim();
        if ("true".equalsIgnoreCase(trimmed)) return true;
        if ("false".equalsIgnoreCase(trimmed)) return false;
        CatgisLogger.warn("CatmapSerializer: valor booleano invalido '" + s + "', usando false", null);
        return false;
    }

    private static Color parseColor(String s) {
        try {
            String[] parts = s.split(",");
            if (parts.length >= 3) {
                return new Color(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                );
            }
        } catch (Exception ignored) {
            CatgisLogger.warn("CatmapSerializer: color invalido '" + s + "', usando negro", null);
        }
        return Color.BLACK;
    }

    private static String colorToHex(Color c) {
        if (c == null) return "0,0,0";
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    private static String encodeText(String text) {
        if (text == null) return "";
        return text.replace("%", "%25").replace("|", "%7C").replace("\n", "%0A");
    }

    private static String decodeText(String text) {
        if (text == null) return "";
        return text.replace("%7C", "|").replace("%0A", "\n").replace("%25", "%");
    }
}
