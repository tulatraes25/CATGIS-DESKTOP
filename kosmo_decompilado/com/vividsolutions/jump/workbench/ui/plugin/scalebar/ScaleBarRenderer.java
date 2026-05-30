/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.IncrementChooser;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.RoundQuantity;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.saig.core.util.ScaleManager;
import org.saig.core.util.UnitsManager;

public class ScaleBarRenderer
extends SimpleRenderer {
    public static final String CONTENT_ID = "SCALE_BAR";
    protected static final int BAR_HEIGHT = 10;
    protected static final Color FILL2 = new Color(255, 204, 204);
    protected static final Color FILL1 = Color.white;
    protected static final int HORIZONTAL_MARGIN = 3;
    protected static final int IDEAL_INCREMENT = 75;
    protected static final Color LINE_COLOR = Color.black;
    protected static final int TEXT_BOTTOM_MARGIN = 1;
    protected static final int UNIT_TEXT_BOTTOM_MARGIN = 1;
    protected static final Color TEXT_COLOR = Color.black;
    protected static final Color UNIT_TEXT_COLOR = Color.blue;
    protected static final int VERTICAL_MARGIN = 3;
    protected static final String ENABLED_KEY = ScaleBarRenderer.class + " - ENABLED";
    protected static final int INCREMENT_COUNT = 5;
    protected Font FONT = new Font("Dialog", 0, 10);
    protected Font UNIT_FONT = new Font("Dialog", 1, 11);

    public ScaleBarRenderer(LayerViewPanel panel) {
        super(CONTENT_ID, panel, 1.0);
    }

    public static boolean isEnabled(LayerViewPanel panel) {
        return panel.getBlackboard().get(ENABLED_KEY, false);
    }

    public static void setEnabled(boolean enabled, LayerViewPanel panel) {
        panel.getBlackboard().put(ENABLED_KEY, enabled);
    }

    @Override
    protected void paint(Graphics2D g) {
        this.paint(g, this.panel.getViewport().getScale());
    }

    public void paint(Graphics2D g, double scale) {
        if (!ScaleBarRenderer.isEnabled(this.panel)) {
            return;
        }
        Envelope envelope = this.panel.getViewport().getEnvelopeInModelCoordinates();
        Collection<Unit<Length>> units = null;
        switch (UnitsManager.getUnitsSystem(this.panel.getMapLengthUnit())) {
            case 1: {
                units = UnitsManager.getNonSILengthUnits();
                break;
            }
            case 2: {
                units = UnitsManager.getSILengthUnits();
            }
        }
        RoundQuantity increment = null;
        if (!this.panel.getProjection().isProjected()) {
            double newScale = ScaleManager.getInstance().generateScaleValue(envelope.getMaxX(), envelope.getMinX(), this.panel.getWidth(), this.panel.getProjection(), this.panel.getMapLengthUnit());
            increment = new IncrementChooser().chooseGoodIncrement(units, newScale / 75.0, this.panel.getMapLengthUnit());
            Envelope env = this.panel.getViewport().getEnvelopeInModelCoordinates();
            double distance = MeasureTool.distanceWorld(new Point2D.Double(env.getMinX(), env.getMinY()), new Point2D.Double(env.getMaxX(), env.getMinY()), this.panel.getProjection());
            this.paintIncrements(increment, 5, g, (double)this.panel.getWidth() / distance);
        } else {
            increment = new IncrementChooser().chooseGoodIncrement(units, 75.0 / scale, this.panel.getMapLengthUnit());
            this.paintIncrements(increment, 5, g, scale);
        }
    }

    protected int barBottom() {
        return this.panel.getHeight() - 3;
    }

    protected int barTop() {
        return this.barBottom() - 10;
    }

    protected TextLayout createTextLayout(String text, Font font, Graphics2D g) {
        return new TextLayout(text, font, g.getFontRenderContext());
    }

    protected void paintIncrement(int i, RoundQuantity increment, int incrementCount, Graphics2D g, double scale) {
        Rectangle2D.Double shape = new Rectangle2D.Double(this.x(i, increment, incrementCount, scale), this.barTop(), this.x(i + 1, increment, incrementCount, scale) - this.x(i, increment, incrementCount, scale), this.barBottom() - this.barTop());
        g.setColor(i % 2 == 0 ? FILL1 : FILL2);
        g.fill(shape);
        g.setColor(LINE_COLOR);
        g.draw(shape);
    }

    protected void paintIncrements(RoundQuantity increment, int incrementCount, Graphics2D g, double scale) {
        int i = 0;
        while (i < incrementCount) {
            this.paintIncrement(i, increment, incrementCount, g, scale);
            this.paintLabel(i, increment, incrementCount, g, scale);
            ++i;
        }
    }

    protected void paintLabel(int i, RoundQuantity increment, int incrementCount, Graphics2D g, double scale) {
        String text = new RoundQuantity(increment.getMantissa() * (i + 1), increment.getExponent(), increment.getUnit(), this.panel.getMapLengthUnit()).getAmountString();
        Font font = this.FONT;
        g.setColor(TEXT_COLOR);
        int textBottomMargin = 1;
        if (i == incrementCount - 1) {
            text = increment.getUnit().toString();
            font = this.UNIT_FONT;
            g.setColor(UNIT_TEXT_COLOR);
            textBottomMargin = 1;
        }
        TextLayout layout = this.createTextLayout(text, font, g);
        double center = MathUtil.avg(this.x(i, increment, incrementCount, scale), this.x(i + 1, increment, incrementCount, scale));
        layout.draw(g, (float)(center - (double)(layout.getAdvance() / 2.0f)), this.barBottom() - textBottomMargin);
    }

    protected double x(int i, RoundQuantity increment, int incrementCount, double scale) {
        return 3.0 + (double)i * increment.getModelValue() * scale;
    }
}

