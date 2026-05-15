package ar.com.catgis;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class CatmapLayoutItem {

    public enum Kind {
        TEXT,
        IMAGE,
        RECTANGLE,
        ELLIPSE,
        LINE
    }

    public enum HorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    private final String id;
    private Kind kind;
    private String label;
    private String text;
    private String imagePath;
    private int x;
    private int y;
    private int width;
    private int height;
    private int strokeArgb;
    private int fillArgb;
    private int textArgb;
    private float lineWidth;
    private int fontSize;
    private boolean bold;
    private boolean italic;
    private HorizontalAlign align;
    private boolean visible;
    private boolean locked;

    public CatmapLayoutItem(Kind kind) {
        this(UUID.randomUUID().toString(), kind);
    }

    private CatmapLayoutItem(String id, Kind kind) {
        this.id = id != null && !id.isBlank() ? id : UUID.randomUUID().toString();
        this.kind = kind != null ? kind : Kind.TEXT;
        this.align = HorizontalAlign.LEFT;
        applyDefaults();
    }

    public CatmapLayoutItem(CatmapLayoutItem other) {
        this.id = other != null ? other.id : UUID.randomUUID().toString();
        this.kind = other != null ? other.kind : Kind.TEXT;
        this.label = other != null ? other.label : "";
        this.text = other != null ? other.text : "";
        this.imagePath = other != null ? other.imagePath : "";
        this.x = other != null ? other.x : 120;
        this.y = other != null ? other.y : 120;
        this.width = other != null ? other.width : 180;
        this.height = other != null ? other.height : 80;
        this.strokeArgb = other != null ? other.strokeArgb : new Color(37, 99, 235).getRGB();
        this.fillArgb = other != null ? other.fillArgb : new Color(255, 255, 255, 0).getRGB();
        this.textArgb = other != null ? other.textArgb : new Color(30, 41, 59).getRGB();
        this.lineWidth = other != null ? other.lineWidth : 2.2f;
        this.fontSize = other != null ? other.fontSize : 18;
        this.bold = other != null && other.bold;
        this.italic = other != null && other.italic;
        this.align = other != null && other.align != null ? other.align : HorizontalAlign.LEFT;
        this.visible = other == null || other.visible;
        this.locked = other != null && other.locked;
    }

    private void applyDefaults() {
        x = 120;
        y = 120;
        width = 200;
        height = 80;
        strokeArgb = new Color(37, 99, 235).getRGB();
        fillArgb = new Color(255, 255, 255, 0).getRGB();
        textArgb = new Color(30, 41, 59).getRGB();
        lineWidth = 2.4f;
        fontSize = 18;
        bold = false;
        italic = false;
        visible = true;
        locked = false;

        switch (kind) {
            case TEXT -> {
                label = "Texto";
                text = "Texto CATMAP";
                width = 240;
                height = 72;
                fillArgb = new Color(255, 255, 255, 0).getRGB();
            }
            case IMAGE -> {
                label = "Imagen";
                text = "";
                imagePath = "";
                width = 220;
                height = 150;
            }
            case RECTANGLE -> {
                label = "Rectangulo";
                text = "";
                width = 180;
                height = 90;
                strokeArgb = new Color(59, 130, 246).getRGB();
                fillArgb = new Color(96, 165, 250, 56).getRGB();
            }
            case ELLIPSE -> {
                label = "Elipse";
                text = "";
                width = 180;
                height = 90;
                strokeArgb = new Color(16, 185, 129).getRGB();
                fillArgb = new Color(110, 231, 183, 64).getRGB();
            }
            case LINE -> {
                label = "Linea";
                text = "";
                width = 180;
                height = 90;
                strokeArgb = new Color(234, 88, 12).getRGB();
                fillArgb = new Color(255, 255, 255, 0).getRGB();
                lineWidth = 3f;
            }
        }
    }

    public static CatmapLayoutItem decode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String[] parts = text.split("~", -1);
            if (parts.length < 17) {
                return null;
            }
            CatmapLayoutItem item = new CatmapLayoutItem(parts[0], Kind.valueOf(parts[1]));
            item.label = decodeText(parts[2]);
            item.text = decodeText(parts[3]);
            item.imagePath = decodeText(parts[4]);
            item.x = Integer.parseInt(parts[5]);
            item.y = Integer.parseInt(parts[6]);
            item.width = Integer.parseInt(parts[7]);
            item.height = Integer.parseInt(parts[8]);
            item.strokeArgb = Integer.parseInt(parts[9]);
            item.fillArgb = Integer.parseInt(parts[10]);
            item.textArgb = Integer.parseInt(parts[11]);
            item.lineWidth = Float.parseFloat(parts[12]);
            item.fontSize = Integer.parseInt(parts[13]);
            item.bold = Boolean.parseBoolean(parts[14]);
            item.italic = Boolean.parseBoolean(parts[15]);
            item.align = HorizontalAlign.valueOf(parts[16]);
            item.visible = parts.length >= 18 ? Boolean.parseBoolean(parts[17]) : true;
            item.locked = parts.length >= 19 && Boolean.parseBoolean(parts[18]);
            return item;
        } catch (Exception ex) {
            return null;
        }
    }

    public String encode() {
        return String.join("~",
                safe(id),
                safe(kind != null ? kind.name() : Kind.TEXT.name()),
                encodeText(label),
                encodeText(text),
                encodeText(imagePath),
                String.valueOf(x),
                String.valueOf(y),
                String.valueOf(width),
                String.valueOf(height),
                String.valueOf(strokeArgb),
                String.valueOf(fillArgb),
                String.valueOf(textArgb),
                String.valueOf(lineWidth),
                String.valueOf(fontSize),
                String.valueOf(bold),
                String.valueOf(italic),
                safe(align != null ? align.name() : HorizontalAlign.LEFT.name()),
                String.valueOf(visible),
                String.valueOf(locked)
        );
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

    private static String safe(String value) {
        return value != null ? value : "";
    }

    public String getId() {
        return id;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind != null ? kind : Kind.TEXT;
    }

    public String getLabel() {
        return label != null ? label : "";
    }

    public void setLabel(String label) {
        this.label = label != null ? label.trim() : "";
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    public String getImagePath() {
        return imagePath != null ? imagePath : "";
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath.trim() : "";
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(20, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(20, height);
    }

    public Color getStrokeColor() {
        return new Color(strokeArgb, true);
    }

    public void setStrokeColor(Color color) {
        this.strokeArgb = (color != null ? color : new Color(37, 99, 235)).getRGB();
    }

    public Color getFillColor() {
        return new Color(fillArgb, true);
    }

    public void setFillColor(Color color) {
        this.fillArgb = (color != null ? color : new Color(255, 255, 255, 0)).getRGB();
    }

    public Color getTextColor() {
        return new Color(textArgb, true);
    }

    public void setTextColor(Color color) {
        this.textArgb = (color != null ? color : new Color(30, 41, 59)).getRGB();
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(1f, lineWidth);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = Math.max(8, fontSize);
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public HorizontalAlign getAlign() {
        return align != null ? align : HorizontalAlign.LEFT;
    }

    public void setAlign(HorizontalAlign align) {
        this.align = align != null ? align : HorizontalAlign.LEFT;
    }

    @Override
    public String toString() {
        String base = switch (kind) {
            case TEXT -> "Texto";
            case IMAGE -> "Imagen";
            case RECTANGLE -> "Rectangulo";
            case ELLIPSE -> "Elipse";
            case LINE -> "Linea";
        };
        String caption = !getLabel().isBlank() ? getLabel() : base;
        return base + " | " + caption;
    }
}
