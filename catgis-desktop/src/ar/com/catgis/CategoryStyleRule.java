package ar.com.catgis;

import java.awt.Color;

public class CategoryStyleRule {
    private final String value;
    private Color primaryColor;
    private Color secondaryColor;
    private Layer.LineSymbolStyle lineStyle;
    private float lineWidth;
    private Layer.PolygonFillStyle polygonFillStyle;
    private Layer.PointSymbolStyle pointSymbolStyle;
    private int pointSize;

    public CategoryStyleRule(String value) {
        this.value = value != null ? value : "(sin valor)";
        this.primaryColor = new Color(59, 130, 246);
        this.secondaryColor = new Color(30, 41, 59);
        this.lineStyle = Layer.LineSymbolStyle.SOLID;
        this.lineWidth = 1.5f;
        this.polygonFillStyle = Layer.PolygonFillStyle.SOLID;
        this.pointSymbolStyle = Layer.PointSymbolStyle.CIRCLE;
        this.pointSize = 9;
    }

    public String getValue() {
        return value;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        if (primaryColor != null) {
            this.primaryColor = primaryColor;
        }
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        if (secondaryColor != null) {
            this.secondaryColor = secondaryColor;
        }
    }

    public Layer.LineSymbolStyle getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(Layer.LineSymbolStyle lineStyle) {
        if (lineStyle != null) {
            this.lineStyle = lineStyle;
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (lineWidth > 0f) {
            this.lineWidth = lineWidth;
        }
    }

    public Layer.PolygonFillStyle getPolygonFillStyle() {
        return polygonFillStyle;
    }

    public void setPolygonFillStyle(Layer.PolygonFillStyle polygonFillStyle) {
        if (polygonFillStyle != null) {
            this.polygonFillStyle = polygonFillStyle;
        }
    }

    public Layer.PointSymbolStyle getPointSymbolStyle() {
        return pointSymbolStyle;
    }

    public void setPointSymbolStyle(Layer.PointSymbolStyle pointSymbolStyle) {
        if (pointSymbolStyle != null) {
            this.pointSymbolStyle = pointSymbolStyle;
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
}
