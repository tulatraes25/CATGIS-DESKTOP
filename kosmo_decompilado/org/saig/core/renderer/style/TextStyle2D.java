/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.renderer.style;

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.geotools.resources.Utilities;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.Style2D;
import org.saig.jump.lang.I18N;

public class TextStyle2D
extends Style2D {
    GlyphVector textGlyphVector;
    Shape haloShape;
    String label;
    Font font;
    double rotation;
    boolean pointPlacement = true;
    int perpendicularOffset = 0;
    Style2D graphic;
    double anchorX;
    double anchorY;
    double displacementX;
    double displacementY;
    Paint haloFill;
    Composite haloComposite;
    float haloRadius;
    boolean fontSizeScale;
    boolean isRotationStablished;
    private Paint fill;
    private Composite composite;
    private Rectangle2D textBounds;
    private double lineHeight;
    private double linePlacementYAnchor;

    public TextStyle2D() {
    }

    public TextStyle2D(TextStyle2D t) {
        this.anchorX = t.anchorX;
        this.anchorY = t.anchorY;
        this.composite = t.composite;
        this.displacementX = t.displacementX;
        this.displacementY = t.displacementY;
        this.fill = t.fill;
        this.font = t.font;
        this.graphic = t.graphic;
        this.haloComposite = t.haloComposite;
        this.haloFill = t.haloFill;
        this.haloRadius = t.haloRadius;
        this.haloShape = t.haloShape;
        this.label = t.label;
        this.maxScale = t.maxScale;
        this.minScale = t.minScale;
        this.perpendicularOffset = t.perpendicularOffset;
        this.pointPlacement = t.pointPlacement;
        this.rotation = t.rotation;
        this.textGlyphVector = t.textGlyphVector;
        this.textBounds = t.textBounds;
        this.lineHeight = t.lineHeight;
        this.linePlacementYAnchor = t.linePlacementYAnchor;
    }

    public double getAnchorX() {
        return this.anchorX;
    }

    public double getAnchorY() {
        return this.anchorY;
    }

    public Font getFont() {
        return this.font;
    }

    public Composite getHaloComposite() {
        return this.haloComposite;
    }

    public Paint getHaloFill() {
        return this.haloFill;
    }

    public float getHaloRadius() {
        return this.haloRadius;
    }

    public double getRotation() {
        return this.rotation;
    }

    public GlyphVector getTextGlyphVector(Graphics2D graphics) {
        if (this.textGlyphVector == null) {
            this.textGlyphVector = this.font.createGlyphVector(graphics.getFontRenderContext(), this.label);
        }
        return this.textGlyphVector;
    }

    public GlyphVector getTextGlyphVector(Graphics2D graphics, double factor) {
        GlyphVector textGlyphVector = new Font(this.font.getName(), this.font.getStyle(), (int)((double)this.font.getSize() * factor)).createGlyphVector(graphics.getFontRenderContext(), this.label);
        return textGlyphVector;
    }

    public Shape getHaloShape(Graphics2D graphics) {
        if (this.haloShape == null) {
            GlyphVector gv = this.getTextGlyphVector(graphics);
            this.haloShape = new BasicStroke(2.0f * this.haloRadius, 1, 1).createStrokedShape(gv.getOutline());
        }
        return this.haloShape;
    }

    public Shape getHaloShape(Graphics2D graphics, int factor) {
        if (this.haloShape == null) {
            GlyphVector gv = this.getTextGlyphVector(graphics, factor);
            this.haloShape = new BasicStroke(2.0f * this.haloRadius, 1, 1).createStrokedShape(gv.getOutline());
        }
        return this.haloShape;
    }

    public void setAnchorX(double f) {
        this.anchorX = f;
    }

    public void setAnchorY(double f) {
        this.anchorY = f;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setHaloComposite(Composite composite) {
        this.haloComposite = composite;
    }

    public void setHaloFill(Paint paint) {
        this.haloFill = paint;
    }

    public void setHaloRadius(float f) {
        this.haloRadius = f;
    }

    public void setRotation(double f) {
        this.rotation = f;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getDisplacementX() {
        return this.displacementX;
    }

    public void setDisplacementX(double displacementX) {
        this.displacementX = displacementX;
    }

    public double getDisplacementY() {
        return this.displacementY;
    }

    public void setDisplacementY(double displacementY) {
        this.displacementY = displacementY;
    }

    public Paint getFill() {
        return this.fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public Composite getComposite() {
        return this.composite;
    }

    public void setComposite(Composite composite) {
        this.composite = composite;
    }

    public String toString() {
        return String.valueOf(Utilities.getShortClassName((Object)this)) + "[\"" + this.label + "\"]";
    }

    public boolean isFontSizeScale() {
        return this.fontSizeScale;
    }

    public void setFontSizeScale(boolean fontSizeScale) {
        this.fontSizeScale = fontSizeScale;
    }

    public Font getScaledFont(double scale) {
        if (this.font != null) {
            Font sizedfont = new Font(this.font.getName(), this.font.getStyle(), (int)((double)this.font.getSize() * scale));
            return sizedfont;
        }
        return null;
    }

    public void setPerpendicularOffset(int displace) {
        this.perpendicularOffset = displace;
    }

    public int getPerpendicularOffset() {
        return this.perpendicularOffset;
    }

    public boolean isPointPlacement() {
        return this.pointPlacement;
    }

    public void setPointPlacement(boolean pointPlacement) {
        this.pointPlacement = pointPlacement;
    }

    public void setGraphic(Style2D s) {
        this.graphic = s;
    }

    public Style2D getGraphic() {
        return this.graphic;
    }

    public Rectangle getGraphicDimensions() {
        if (this.graphic instanceof MarkStyle2D) {
            return ((MarkStyle2D)this.graphic).getTransformedShape(0.0f, 0.0f).getBounds();
        }
        if (this.graphic instanceof GraphicStyle2D) {
            BufferedImage i = ((GraphicStyle2D)this.graphic).getImage();
            return new Rectangle(i.getWidth(), i.getHeight());
        }
        throw new RuntimeException(I18N.getString(this.getClass(), "can-not-render-graphic-which-is-not-a-markstyletwod-or-a-graphicstyletwod"));
    }

    public boolean isRotationStablished() {
        return this.isRotationStablished;
    }

    public void setRotationStablished(boolean isStablished) {
        this.isRotationStablished = isStablished;
    }

    public void setTextBounds(Rectangle2D tb) {
        this.textBounds = tb;
    }

    public Rectangle2D getTextBounds() {
        return this.textBounds;
    }

    public void setLineHeight(double lh) {
        this.lineHeight = lh;
    }

    public double getLineHeight() {
        return this.lineHeight;
    }

    public void setLinePlacementYAnchor(double yAnchor) {
        this.linePlacementYAnchor = yAnchor;
    }

    public double getLinePlacementYAnchor() {
        return this.linePlacementYAnchor;
    }
}

