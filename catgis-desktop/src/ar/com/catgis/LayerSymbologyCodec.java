package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public final class LayerSymbologyCodec {

    private LayerSymbologyCodec() {
    }

    public static String encodeCategorizedSymbology(CategorizedSymbology symbology) {
        if (symbology == null || !symbology.isConfigured()) {
            return "";
        }

        StringBuilder rules = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, CategoryStyleRule> entry : symbology.getRules().entrySet()) {
            if (!first) {
                rules.append(';');
            }
            first = false;
            CategoryStyleRule rule = entry.getValue();
            rules.append(encode(entry.getKey())).append('>')
                    .append(colorToText(rule.getPrimaryColor())).append('>')
                    .append(colorToText(rule.getSecondaryColor())).append('>')
                    .append(rule.getLineStyle().name()).append('>')
                    .append(rule.getLineWidth()).append('>')
                    .append(rule.getPolygonFillStyle().name()).append('>')
                    .append(rule.getPointSymbolStyle().name()).append('>')
                    .append(rule.getPointSize());
        }

        return encode(symbology.getFieldName()) + "~"
                + encode(symbology.getLegendTitle()) + "~"
                + encode(symbology.getLegendSubtitle()) + "~"
                + rules;
    }

    public static CategorizedSymbology decodeCategorizedSymbology(String text) {
        CategorizedSymbology symbology = new CategorizedSymbology();
        if (text == null || text.isBlank()) {
            return symbology;
        }

        String[] parts = text.split("~", 4);
        if (parts.length < 4) {
            return symbology;
        }

        symbology.setFieldName(decode(parts[0]));
        symbology.setLegendTitle(decode(parts[1]));
        symbology.setLegendSubtitle(decode(parts[2]));

        if (parts[3].isBlank()) {
            return symbology;
        }

        String[] ruleParts = parts[3].split(";");
        for (String rulePart : ruleParts) {
            String[] fields = rulePart.split(">", -1);
            if (fields.length < 5) {
                continue;
            }
            String value = decode(fields[0]);
            CategoryStyleRule rule = symbology.getOrCreateRule(value);
            Color primary = parseColor(fields[1]);
            Color secondary = parseColor(fields[2]);
            if (primary != null) {
                rule.setPrimaryColor(primary);
            }
            if (secondary != null) {
                rule.setSecondaryColor(secondary);
            }
            rule.setLineStyle(Layer.LineSymbolStyle.fromValue(fields[3]));
            if (fields.length >= 6) {
                try {
                    rule.setLineWidth(Float.parseFloat(fields[4]));
                } catch (Exception ignored) {
                }
                rule.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(fields[5]));
            } else if (fields.length >= 5) {
                rule.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(fields[4]));
            }
            if (fields.length >= 7) {
                rule.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(fields[6]));
            }
            if (fields.length >= 8) {
                try {
                    rule.setPointSize(Integer.parseInt(fields[7]));
                } catch (Exception ignored) {
                }
            }
        }
        return symbology;
    }

    private static String encode(String value) {
        String normalized = value != null ? value : "";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(normalized.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private static String colorToText(Color color) {
        if (color == null) {
            return "";
        }
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }

    private static Color parseColor(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String[] parts = text.split(",");
            if (parts.length < 3) {
                return null;
            }
            int red = Integer.parseInt(parts[0].trim());
            int green = Integer.parseInt(parts[1].trim());
            int blue = Integer.parseInt(parts[2].trim());
            int alpha = parts.length >= 4 ? Integer.parseInt(parts[3].trim()) : 255;
            return new Color(red, green, blue, alpha);
        } catch (Exception ex) {
            return null;
        }
    }
}
