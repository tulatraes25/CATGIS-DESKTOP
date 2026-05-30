/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.math.util.MathUtils
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.text.DecimalFormat;
import org.apache.commons.math.util.MathUtils;
import org.saig.core.util.ScaleManager;

public class LabelScaleBarRenderer
extends ScaleBarRenderer {
    protected Font FONT = new Font("Dialog", 1, 10);
    protected DecimalFormat formateador = new DecimalFormat("###,###");
    protected String displayText;
    protected int numberOfNSD = 0;
    protected double fixedScaleValue = -1.0;

    public LabelScaleBarRenderer(LayerViewPanel panel, String text, int numberOfNonSignificantDigits) {
        super(panel);
        this.displayText = text;
        this.numberOfNSD = numberOfNonSignificantDigits;
    }

    public LabelScaleBarRenderer(LayerViewPanel panel, String text, int numberOfNonSignificantDigits, double scaleValue) {
        this(panel, text, numberOfNonSignificantDigits);
        this.fixedScaleValue = scaleValue;
    }

    @Override
    public void paint(Graphics2D g, double scale) {
        if (!LabelScaleBarRenderer.isEnabled(this.panel)) {
            return;
        }
        this.paintLabel(this.displayText, g, this.generateScaleValue());
    }

    protected double generateScaleValue() {
        Envelope currentEnvelope = this.panel.getViewport().getEnvelopeInModelCoordinates();
        double newScale = this.fixedScaleValue;
        if (this.fixedScaleValue == -1.0) {
            newScale = ScaleManager.getInstance().generateScaleValue(currentEnvelope.getMaxX(), currentEnvelope.getMinX(), this.panel.getWidth(), this.panel.getProjection(), this.panel.getMapLengthUnit());
        }
        double roundedScale = MathUtils.round((double)(newScale / Math.pow(10.0, this.numberOfNSD)), (int)0) * Math.pow(10.0, this.numberOfNSD);
        return roundedScale;
    }

    protected void paintLabel(String text, Graphics2D g, double scale) {
        String labelText = text;
        labelText = String.valueOf(labelText) + "1:" + this.formateador.format(scale);
        Font font = this.FONT;
        g.setColor(TEXT_COLOR);
        int textBottomMargin = 1;
        TextLayout layout = this.createTextLayout(labelText, font, g);
        layout.draw(g, 3.0f, this.barBottom() - textBottomMargin);
    }

    public void setFixedScale(double scale) {
        this.fixedScaleValue = scale;
    }
}

