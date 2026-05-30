/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.cresques.cts.ICoordTrans
 *  org.jfree.chart.axis.CategoryAxis
 *  org.jfree.chart.axis.CategoryAxis3D
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.NumberAxis3D
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.plot.CategoryPlot
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.PlotOrientation
 *  org.jfree.chart.renderer.category.BarRenderer
 *  org.jfree.chart.renderer.category.BarRenderer3D
 *  org.jfree.chart.renderer.category.CategoryItemRenderer
 *  org.jfree.data.category.CategoryDataset
 *  org.jfree.data.category.DefaultCategoryDataset
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.saig.jump.lang.I18N;

public class BarChartMarkerDecorator
extends ChartMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.BarChartMarkerDecorator.Bar-chart");
    public static final Icon ICON = IconLoader.icon("chart_bar.png");
    protected String[] rowKeys;
    protected String[] columnKeys;
    protected String categoryAxisLabel;
    protected String valueAxisLabel;
    protected boolean vertical;
    protected DefaultCategoryDataset dataset;
    protected Color[] barColors;

    public BarChartMarkerDecorator() {
        this(Color.BLACK, 10.0, "pixel");
    }

    public BarChartMarkerDecorator(Color color, double size, String units) {
        this.name = NAME;
        this.icon = ICON;
        this.setColor(color);
        this.setSize(size);
        this.setVertical(true);
        this.set3d(true);
        this.setDrawOutline(true);
        this.setUnit(units);
    }

    @Override
    protected Plot getMapPlot() {
        if (this.mapPlot == null) {
            CategoryPlot myMapPlot = new CategoryPlot();
            try {
                myMapPlot = (CategoryPlot)this.getOutlinePlot().clone();
            }
            catch (CloneNotSupportedException e) {
                myMapPlot = new CategoryPlot();
            }
            myMapPlot.setBackgroundPaint(null);
            myMapPlot.setOutlinePaint(null);
            BarRenderer renderer = (BarRenderer)myMapPlot.getRenderer();
            renderer.setDrawBarOutline(this.drawOutline);
            renderer.setBaseSeriesVisibleInLegend(false);
            int i = 0;
            while (i < this.barColors.length) {
                renderer.setSeriesPaint(i, (Paint)this.barColors[i]);
                ++i;
            }
            this.mapPlot = myMapPlot;
        }
        return this.mapPlot;
    }

    protected Plot getOutlinePlot() {
        if (this.outlinePlot == null) {
            CategoryAxis3D categoryAxis = null;
            NumberAxis3D valueAxis = null;
            BarRenderer3D renderer = null;
            if (this.is3d) {
                renderer = new BarRenderer3D();
                categoryAxis = new CategoryAxis3D(this.categoryAxisLabel);
                valueAxis = new NumberAxis3D(this.valueAxisLabel);
            } else {
                renderer = new BarRenderer();
                categoryAxis = new CategoryAxis(this.categoryAxisLabel);
                valueAxis = new NumberAxis(this.valueAxisLabel);
            }
            CategoryPlot myOutlinePlot = new CategoryPlot((CategoryDataset)this.dataset, (CategoryAxis)categoryAxis, (ValueAxis)valueAxis, (CategoryItemRenderer)renderer);
            myOutlinePlot.setDataset((CategoryDataset)this.dataset);
            myOutlinePlot.setOrientation(this.vertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);
            this.outlinePlot = myOutlinePlot;
        }
        return this.outlinePlot;
    }

    public String[] getRowKeys() {
        return this.rowKeys;
    }

    public void setRowKeys(String[] rowKeys) {
        this.rowKeys = rowKeys;
    }

    public String[] getColumnKeys() {
        return this.columnKeys;
    }

    public void setColumnKeys(String[] columnKeys) {
        this.columnKeys = columnKeys;
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
            this.dataset = new DefaultCategoryDataset();
        } else {
            this.dataset.clear();
        }
        int i = 0;
        while (this.rowKeys != null && i < this.rowKeys.length) {
            this.dataset.addValue((Number)f.getAttribute(this.rowKeys[i]), (Comparable)((Object)this.rowKeys[i]), (Comparable)((Object)this.columnKeys[i]));
            ++i;
        }
    }

    public String getCategoryAxisLabel() {
        return this.categoryAxisLabel;
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        this.categoryAxisLabel = categoryAxisLabel;
    }

    public String getValueAxisLabel() {
        return this.valueAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        this.valueAxisLabel = valueAxisLabel;
    }

    public boolean isVertical() {
        return this.vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public void setColors(Color[] colors) {
        this.barColors = colors;
    }

    public Color[] getColors() {
        return this.barColors;
    }
}

