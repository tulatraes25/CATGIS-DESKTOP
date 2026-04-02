package ar.com.catgis;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class Layer {
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
