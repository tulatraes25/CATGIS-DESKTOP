/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.Stroke;
import org.saig.core.filter.Expression;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;

public class DynamicLineStyle2D
extends LineStyle2D {
    protected Feature feature;
    protected LineSymbolizer ls;

    public DynamicLineStyle2D(Feature feature, LineSymbolizer sym) {
        this.feature = feature;
        this.ls = sym;
    }

    @Override
    public Stroke getStroke() {
        org.saig.core.styling.Stroke stroke = this.ls.getStroke();
        if (stroke == null) {
            return null;
        }
        String joinType = this.evaluateExpression(stroke.getLineJoin(), this.feature, "mitre");
        int joinCode = SLDStyleFactory.lookUpJoin(joinType);
        String capType = this.evaluateExpression(stroke.getLineCap(), this.feature, "square");
        int capCode = SLDStyleFactory.lookUpCap(capType);
        float[] dashes = stroke.getDashArray();
        float width = ((Number)stroke.getWidth().getValue(this.feature)).floatValue();
        float dashOffset = ((Number)stroke.getDashOffset().getValue(this.feature)).floatValue();
        if (width <= 1.0f) {
            width = 0.0f;
        }
        BasicStroke stroke2d = dashes != null && dashes.length > 0 ? new BasicStroke(width, capCode, joinCode, 1.0f, dashes, dashOffset) : new BasicStroke(width, capCode, joinCode, 1.0f);
        return stroke2d;
    }

    @Override
    public Composite getContourComposite() {
        org.saig.core.styling.Stroke stroke = this.ls.getStroke();
        if (stroke == null) {
            return null;
        }
        float opacity = ((Number)stroke.getOpacity().getValue(this.feature)).floatValue();
        AlphaComposite composite = AlphaComposite.getInstance(3, opacity);
        return composite;
    }

    @Override
    public Paint getContour() {
        org.saig.core.styling.Stroke stroke = this.ls.getStroke();
        if (stroke == null) {
            return null;
        }
        Paint contourPaint = Color.decode((String)stroke.getColor().getValue(this.feature));
        Graphic gr = stroke.getGraphicFill();
        SLDStyleFactory fac = new SLDStyleFactory();
        if (gr != null) {
            contourPaint = fac.getTexturePaint(gr, this.feature);
        }
        return contourPaint;
    }

    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;
        if (e != null && (result = (String)e.getValue(feature)) == null) {
            result = defaultValue;
        }
        return result;
    }
}

