package ar.com.catgis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CatmapLegendItem {

    private String key;
    private String label;
    private String subtitle;
    private boolean visible = true;

    public CatmapLegendItem() {
    }

    public CatmapLegendItem(String key, String label, String subtitle, boolean visible) {
        this.key = key != null ? key.trim() : "";
        this.label = label != null ? label : "";
        this.subtitle = subtitle != null ? subtitle : "";
        this.visible = visible;
    }

    public CatmapLegendItem(CatmapLegendItem other) {
        this(
                other != null ? other.key : "",
                other != null ? other.label : "",
                other != null ? other.subtitle : "",
                other == null || other.visible
        );
    }

    public static CatmapLegendItem decode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String[] parts = text.split("~", -1);
            if (parts.length < 4) {
                return null;
            }
            return new CatmapLegendItem(
                    decodeText(parts[0]),
                    decodeText(parts[1]),
                    decodeText(parts[2]),
                    Boolean.parseBoolean(parts[3])
            );
        } catch (Exception ex) {
            return null;
        }
    }

    public String encode() {
        return encodeText(getKey())
                + "~" + encodeText(getLabel())
                + "~" + encodeText(getSubtitle())
                + "~" + visible;
    }

    private static String encodeText(String value) {
        String text = value != null ? value : "";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getKey() {
        return key != null ? key : "";
    }

    public void setKey(String key) {
        this.key = key != null ? key.trim() : "";
    }

    public String getLabel() {
        return label != null ? label : "";
    }

    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }

    public String getSubtitle() {
        return subtitle != null ? subtitle : "";
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle != null ? subtitle : "";
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return (visible ? "[Mostrar] " : "[Oculto] ") + (!getLabel().isBlank() ? getLabel() : "Item de leyenda");
    }
}
