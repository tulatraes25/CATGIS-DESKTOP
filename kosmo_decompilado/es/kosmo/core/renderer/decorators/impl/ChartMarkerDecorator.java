/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.PlotRenderingInfo
 *  org.jfree.chart.plot.PlotState
 */
package es.kosmo.core.renderer.decorators.impl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import es.kosmo.core.renderer.decorators.AbstractDecorator;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public abstract class ChartMarkerDecorator
extends AbstractDecorator {
    protected Plot renderPlot;
    protected Plot mapPlot;
    protected Plot outlinePlot;
    protected PlotState plotState;
    protected PlotRenderingInfo plotRenderingInfo;
    protected boolean is3d;
    protected boolean drawOutline;

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, IDecoratorPoint2DTransformer viewport, double pixelSize, Unit<Length> viewUnit) throws Exception {
        Point centroid = geometry.getCentroid();
        ShapePoint2D shp = new ShapePoint2D(viewport.toViewPoint(new Point2D.Double(centroid.getX(), centroid.getY())));
        AffineTransform affineTransform = new AffineTransform();
        this.draw(graphics, affineTransform, shp, pixelSize, viewUnit);
    }

    public void draw(Graphics2D g, AffineTransform affineTransform, ShapePoint2D fp, double pixelSize, Unit<Length> viewUnit) {
        Point2D.Double p = new Point2D.Double(fp.getX(), fp.getY());
        double sizeInPixel = this.getSize() * this.getTransformationFactor(viewUnit);
        if (!"pixel".equals(this.unit)) {
            sizeInPixel *= pixelSize;
        }
        if (this.renderPlot == null) {
            this.renderPlot = this.getMapPlot();
        }
        double halfSize = sizeInPixel * 0.5;
        double minx = ((Point2D)p).getX() - halfSize;
        double miny = ((Point2D)p).getY() - halfSize;
        this.renderPlot.draw(g, (Rectangle2D)new Rectangle2D.Double(minx, miny, sizeInPixel, sizeInPixel), (Point2D)p, this.plotState, this.plotRenderingInfo);
    }

    @Override
    public boolean isCompatible(int geomType) {
        return true;
    }

    protected abstract Plot getMapPlot();

    public boolean is3d() {
        return this.is3d;
    }

    public void set3d(boolean is3d) {
        this.is3d = is3d;
    }

    public boolean isDrawOutline() {
        return this.drawOutline;
    }

    public void setDrawOutline(boolean drawOutline) {
        this.drawOutline = drawOutline;
    }
}

