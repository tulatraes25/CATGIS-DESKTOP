package ar.com.catgis;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class Layer {
    public enum PointSymbolStyle {
        CIRCLE("Circulo"),
        SQUARE("Cuadrado"),
        DIAMOND("Rombo"),
        TRIANGLE("Triangulo"),
        TARGET("Objetivo"),
        PIN("Pin"),
        FLAG("Bandera"),
        STAR("Estrella"),
        WELL("Pozo");

        private final String label;

        PointSymbolStyle(String label) {
            this.label = label;
        }

        public static PointSymbolStyle fromValue(String value) {
            if (value != null) {
                for (PointSymbolStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return CIRCLE;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum LineSymbolStyle {
        SOLID("Solida"),
        DASHED("Discontinua"),
        DOTTED("Punteada"),
        DASH_DOT("Trazo y punto");

        private final String label;

        LineSymbolStyle(String label) {
            this.label = label;
        }

        public static LineSymbolStyle fromValue(String value) {
            if (value != null) {
                for (LineSymbolStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return SOLID;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum PolygonFillStyle {
        SOLID("Solido"),
        DIAGONAL_HATCH("Trama diagonal"),
        CROSS_HATCH("Trama cruzada"),
        DOTS("Punteado"),
        OUTLINE_ONLY("Solo contorno");

        private final String label;

        PolygonFillStyle(String label) {
            this.label = label;
        }

        public static PolygonFillStyle fromValue(String value) {
            if (value != null) {
                for (PolygonFillStyle style : values()) {
                    if (style.name().equalsIgnoreCase(value.trim())) {
                        return style;
                    }
                }
            }
            return SOLID;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private String name;
    private String path;
    private String type;
    private boolean visible = true;
    private String sourceName;
    private int featureCount;

    private boolean labelsVisible = false;
    private String labelField;

    private Color fillColor = new Color(120, 170, 255, 120);
    private Color borderColor = Color.BLUE;
    private Color lineColor = Color.RED;
    private float lineWidth = 1.5f;
    private Color pointColor = Color.BLUE;
    private int pointSize = 8;
    private PointSymbolStyle pointSymbolStyle = PointSymbolStyle.CIRCLE;
    private LineSymbolStyle lineSymbolStyle = LineSymbolStyle.SOLID;
    private PolygonFillStyle polygonFillStyle = PolygonFillStyle.SOLID;
    private final CategorizedSymbology lineCategorizedSymbology = new CategorizedSymbology();
    private final CategorizedSymbology polygonCategorizedSymbology = new CategorizedSymbology();

    private String sourceCRS = "";

    private final Map<String, FieldConfig> fieldConfigs = new LinkedHashMap<>();

    public Layer(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public boolean isLabelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(boolean labelsVisible) {
        this.labelsVisible = labelsVisible;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        if (fillColor != null) {
            this.fillColor = fillColor;
        }
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor != null) {
            this.borderColor = borderColor;
        }
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        if (lineColor != null) {
            this.lineColor = lineColor;
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (lineWidth > 0) {
            this.lineWidth = lineWidth;
        }
    }

    public Color getPointColor() {
        return pointColor;
    }

    public void setPointColor(Color pointColor) {
        if (pointColor != null) {
            this.pointColor = pointColor;
        }
    }

    public int getPointSize() {
        return pointSize;
    }

    public void setPointSize(int pointSize) {
        if (pointSize > 0) {
            this.pointSize = pointSize;
        }
    }

    public PointSymbolStyle getPointSymbolStyle() {
        return pointSymbolStyle;
    }

    public void setPointSymbolStyle(PointSymbolStyle pointSymbolStyle) {
        if (pointSymbolStyle != null) {
            this.pointSymbolStyle = pointSymbolStyle;
        }
    }

    public LineSymbolStyle getLineSymbolStyle() {
        return lineSymbolStyle;
    }

    public void setLineSymbolStyle(LineSymbolStyle lineSymbolStyle) {
        if (lineSymbolStyle != null) {
            this.lineSymbolStyle = lineSymbolStyle;
        }
    }

    public PolygonFillStyle getPolygonFillStyle() {
        return polygonFillStyle;
    }

    public void setPolygonFillStyle(PolygonFillStyle polygonFillStyle) {
        if (polygonFillStyle != null) {
            this.polygonFillStyle = polygonFillStyle;
        }
    }

    public CategorizedSymbology getLineCategorizedSymbology() {
        return lineCategorizedSymbology;
    }

    public CategorizedSymbology getPolygonCategorizedSymbology() {
        return polygonCategorizedSymbology;
    }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public void setSourceCRS(String sourceCRS) {
        this.sourceCRS = CRSDefinitions.normalizeCode(sourceCRS);
    }

    public Map<String, FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public FieldConfig getOrCreateFieldConfig(String fieldName, String typeName) {
        FieldConfig config = fieldConfigs.get(fieldName);
        if (config == null) {
            config = new FieldConfig(fieldName, typeName);
            fieldConfigs.put(fieldName, config);
        } else if (typeName != null && !typeName.isBlank()) {
            config.setTypeName(typeName);
        }
        return config;
    }

    @Override
    public String toString() {
        return name + (visible ? "" : " [Oculta]");
    }
}
