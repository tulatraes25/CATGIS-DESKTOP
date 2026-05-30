/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class BasicStyle
implements Style {
    private boolean renderingFill = true;
    private boolean renderingLine = true;
    private boolean renderingLinePattern = false;
    private boolean renderingFillPattern = false;
    private Color fillColor = new Color(0, 0, 0, 255);
    private Color lineColor = new Color(0, 0, 0, 255);
    private BasicStroke lineStroke;
    private Stroke fillStroke = new BasicStroke(1.0f);
    private boolean enabled = true;
    private String linePattern = "3";
    private Paint fillPattern = WKTFillPattern.createDiagonalStripePattern(4, 2.0, false, true);

    public BasicStyle(Color fillColor) {
        this.setFillColor(fillColor);
        this.setLineColor(Layer.defaultLineColor(fillColor));
        this.setLineWidth(1);
    }

    public BasicStyle() {
        this(Color.black);
    }

    public boolean isRenderingFillPattern() {
        return this.renderingFillPattern;
    }

    public BasicStyle setRenderingFillPattern(boolean renderingFillPattern) {
        this.renderingFillPattern = renderingFillPattern;
        return this;
    }

    public Paint getFillPattern() {
        return this.fillPattern;
    }

    public BasicStyle setFillPattern(Paint fillPattern) {
        this.fillPattern = fillPattern;
        if (fillPattern instanceof BasicFillPattern) {
            ((BasicFillPattern)fillPattern).setColor(this.fillColor);
        }
        return this;
    }

    public String getLinePattern() {
        return this.linePattern;
    }

    public BasicStyle setLinePattern(String linePattern) {
        this.linePattern = linePattern;
        this.lineStroke = this.createLineStroke(this.lineStroke.getLineWidth());
        return this;
    }

    @Override
    public void initialize(Layer layer) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws NoninvertibleTransformException {
        StyleUtil.paint(f.getGeometry(), g, viewport, this.renderingFill, this.fillStroke, this.renderingFillPattern && this.fillPattern != null ? this.fillPattern : this.fillColor, this.renderingLine, this.lineStroke, this.lineColor);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    public boolean isRenderingFill() {
        return this.renderingFill;
    }

    public boolean isRenderingLine() {
        return this.renderingLine;
    }

    public boolean isRenderingLinePattern() {
        return this.renderingLinePattern;
    }

    public void setRenderingFill(boolean renderingFill) {
        this.renderingFill = renderingFill;
    }

    public void setRenderingLine(boolean renderingLine) {
        this.renderingLine = renderingLine;
    }

    public BasicStyle setRenderingLinePattern(boolean renderingLinePattern) {
        this.renderingLinePattern = renderingLinePattern;
        this.lineStroke = this.createLineStroke(this.lineStroke.getLineWidth());
        return this;
    }

    public void setFillColor(Color fillColor) {
        this.setFillColor(fillColor, this.getAlpha());
    }

    private BasicStyle setFillColor(Color fillColor, int alpha) {
        this.fillColor = GUIUtil.alphaColor(fillColor, alpha);
        if (this.fillPattern instanceof BasicFillPattern) {
            ((BasicFillPattern)this.fillPattern).setColor(this.fillColor);
        }
        return this;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = GUIUtil.alphaColor(lineColor, this.getAlpha());
    }

    public void setLineWidth(int lineWidth) {
        this.lineStroke = this.createLineStroke(lineWidth);
    }

    private BasicStroke createLineStroke(float lineWidth) {
        return this.renderingLinePattern && this.linePattern.trim().length() != 0 && lineWidth > 0.0f ? new BasicStroke(lineWidth, 0, 2, 1.0f, BasicStyle.toArray(this.linePattern, lineWidth), 0.0f) : new BasicStroke(lineWidth, 0, 2);
    }

    public static float[] toArray(String linePattern, float lineWidth) {
        List<String> strings = StringUtil.fromCommaDelimitedString(linePattern);
        float[] array = new float[strings.size()];
        int i = 0;
        while (i < strings.size()) {
            String string = strings.get(i);
            array[i] = Float.parseFloat(string) * lineWidth;
            if (array[i] < 0.0f) {
                throw new IllegalArgumentException(I18N.getString("workbench.ui.renderer.style.BasicStyle.negative-dash-length"));
            }
            ++i;
        }
        return array;
    }

    public int getAlpha() {
        return this.fillColor.getAlpha();
    }

    public Color getFillColor() {
        return GUIUtil.alphaColor(this.fillColor, 255);
    }

    public Color getLineColor() {
        return GUIUtil.alphaColor(this.lineColor, 255);
    }

    public int getLineWidth() {
        return (int)this.lineStroke.getLineWidth();
    }

    public void setAlpha(int alpha) {
        this.setFillColor(this.fillColor, alpha);
        this.lineColor = GUIUtil.alphaColor(this.lineColor, alpha);
    }

    public BasicStroke getLineStroke() {
        return this.lineStroke;
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }
}

