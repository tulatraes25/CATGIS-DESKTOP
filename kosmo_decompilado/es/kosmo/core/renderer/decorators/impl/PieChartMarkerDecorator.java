/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.ICoordTrans
 *  org.jfree.chart.plot.PiePlot
 *  org.jfree.chart.plot.PiePlot3D
 *  org.jfree.chart.plot.Plot
 *  org.jfree.data.general.DefaultPieDataset
 *  org.jfree.data.general.PieDataset
 *  org.jfree.util.Rotation
 */
package es.kosmo.core.renderer.decorators.impl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import es.kosmo.core.renderer.decorators.impl.ChartMarkerDecorator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.cresques.cts.ICoordTrans;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.saig.jump.lang.I18N;

public class PieChartMarkerDecorator
extends ChartMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.PieChartMarkerDecorator.Pie-chart");
    public static final Icon ICON = IconLoader.icon("chart_pie.png");
    protected String[] keys;
    protected float foregroundAlpha;
    protected boolean ignoreZeroValues;
    protected double depthFactor;
    protected double minimumAngleToDraw;
    protected boolean circular;
    protected boolean clockwise;
    protected Color[] chartColors;
    protected DefaultPieDataset dataset;

    public PieChartMarkerDecorator() {
        this(Color.BLACK, 10.0, "pixel");
    }

    public PieChartMarkerDecorator(Color color, double size, String units) {
        this.name = NAME;
        this.icon = ICON;
        this.setColor(color);
        this.setSize(size);
        this.set3d(true);
        this.setDrawOutline(true);
        this.setRotation(30.0);
        this.setUnit(units);
    }

    @Override
    protected Plot getMapPlot() {
        if (this.mapPlot == null) {
            Object myMapPlot;
            try {
                myMapPlot = (PiePlot)this.getOutlinePlot().clone();
            }
            catch (CloneNotSupportedException e) {
                myMapPlot = this.is3d ? new PiePlot3D() : new PiePlot();
            }
            myMapPlot.setBackgroundPaint(null);
            myMapPlot.setOutlinePaint(null);
            myMapPlot.setLabelBackgroundPaint(null);
            myMapPlot.setLabelGenerator(null);
            myMapPlot.setLabelLinksVisible(false);
            int i = 0;
            while (i < this.chartColors.length) {
                myMapPlot.setSectionPaint((Comparable)((Object)this.keys[i]), (Paint)this.chartColors[i]);
                ++i;
            }
            this.mapPlot = myMapPlot;
        }
        return this.mapPlot;
    }

    protected Plot getOutlinePlot() {
        if (this.outlinePlot == null) {
            PiePlot3D myMapPlot = null;
            if (!this.is3d) {
                myMapPlot = new PiePlot3D();
                myMapPlot.setDepthFactor(this.depthFactor);
            } else {
                myMapPlot = new PiePlot();
            }
            myMapPlot.setLabelGap(0.0);
            myMapPlot.setIgnoreZeroValues(this.ignoreZeroValues);
            myMapPlot.setDataset((PieDataset)this.dataset);
            myMapPlot.setDirection(this.clockwise ? Rotation.CLOCKWISE : Rotation.ANTICLOCKWISE);
            myMapPlot.setMinimumArcAngleToDraw(this.minimumAngleToDraw);
            myMapPlot.setCircular(this.circular);
            myMapPlot.setStartAngle(this.getRotation());
            this.outlinePlot = myMapPlot;
        }
        return this.outlinePlot;
    }

    public String[] getKeys() {
        return this.keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setIgnoreZeroValues(boolean ignoreZeroValues) {
        this.ignoreZeroValues = ignoreZeroValues;
    }

    public boolean isIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }

    public boolean isClockwise() {
        return this.clockwise;
    }

    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

    public boolean isCircular() {
        return this.circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    public double getMinimumAngleToDraw() {
        return this.minimumAngleToDraw;
    }

    public void setMinimumAngleToDraw(double minimumAngleToDraw) {
        this.minimumAngleToDraw = minimumAngleToDraw;
    }

    public double getDepthFactor() {
        return this.depthFactor;
    }

    public void setDepthFactor(double depthFactor) {
        this.depthFactor = depthFactor;
    }

    public float getForegroundAlpha() {
        return this.foregroundAlpha;
    }

    public void setForegroundAlpha(float foregroundAlpha) {
        this.foregroundAlpha = foregroundAlpha;
    }

    @Override
    public void paint(Feature f, Graphics2D g, IDecoratorPoint2DTransformer viewport, ICoordTrans ct, double pixelSize, Unit<Length> viewUnit) throws Exception {
        if (f != null && f.getGeometry() != null) {
            this.updateDataset(f);
            super.paint(f, g, viewport, ct, pixelSize, viewUnit);
        }
    }

    protected void updateDataset(Feature f) {
        if (this.dataset == null) {
            this.dataset = new DefaultPieDataset();
        } else {
            this.dataset.clear();
        }
        int i = 0;
        while (this.keys != null && i < this.keys.length) {
            Object value = f.getAttribute(this.keys[i]);
            if (value != null && value instanceof Number) {
                this.dataset.insertValue(i, (Comparable)((Object)this.keys[i]), (Number)value);
            }
            ++i;
        }
    }

    public void setColors(Color[] colors) {
        this.chartColors = colors;
    }

    public Color[] getColors() {
        return this.chartColors;
    }
}

