/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import es.kosmo.core.renderer.decorators.AbstractDecorator;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public abstract class VertexMarkerDecorator
extends AbstractDecorator {
    public static final int FONT_BASE_SIZE = 10;
    public static final Font DEFAULT_FONT = new Font("Dialog", 0, 10);
    protected String text;
    protected Font font;

    public void draw(Graphics2D g, AffineTransform affineTransform, ShapePoint2D shp, double pixelSize, Unit<Length> viewUnit) {
        ShapePoint2D p = shp;
        Rectangle2D.Double shape = new Rectangle2D.Double(p.getX() - 1.0, p.getY() - 1.0, 2.0, 2.0);
        g.setColor(this.getColor());
        g.setStroke(new BasicStroke());
        g.fill(shape);
        double sizeInPx = (double)this.getFont().getSize() * this.getTransformationFactor(viewUnit);
        if (!"pixel".equals(this.unit)) {
            sizeInPx *= pixelSize;
        }
        Font derivedFont = this.getFont().deriveFont((float)sizeInPx);
        TextLayout layout = new TextLayout(this.getText(), derivedFont, g.getFontRenderContext());
        layout.draw(g, (float)p.getX() + 2.0f, (float)p.getY() - 2.0f);
    }

    @Override
    public boolean isCompatible(int geomType) {
        return true;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String txt) {
        this.text = txt;
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font selectedFont) {
        this.font = selectedFont;
    }
}

