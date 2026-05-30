/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import es.kosmo.core.renderer.decorators.impl.CircleMarkerDecorator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.jump.lang.I18N;

public class MidSegmentCircleMarkerDecorator
extends CircleMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.MidSegmentCircleMarkerDecorator.Mid-segment-circle");
    public static final Icon ICON = IconLoader.icon("CircleMid.gif");

    public MidSegmentCircleMarkerDecorator() {
        this(Color.BLACK, 10.0, false, 2.0, "pixel");
    }

    public MidSegmentCircleMarkerDecorator(Color color, double size, boolean filled, double borderSize, String units) {
        this.name = NAME;
        this.icon = ICON;
        this.setColor(color);
        this.setSize(size);
        this.setFilled(filled);
        this.setBorderSize(borderSize);
        this.setUnit(units);
    }

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, IDecoratorPoint2DTransformer viewport, double pixelSize, Unit<Length> viewUnit) throws Exception {
        int i = 0;
        while (i < geometry.getNumGeometries()) {
            Geometry currentGeom = geometry.getGeometryN(i);
            Coordinate[] coords = currentGeom.getCoordinates();
            AffineTransform affineTransform = new AffineTransform();
            int j = 0;
            while (j < coords.length - 1) {
                Coordinate currentCoord = coords[j];
                Coordinate nextCoord = coords[j + 1];
                LineSegment lineSegment = new LineSegment(currentCoord, nextCoord);
                Coordinate coord = lineSegment.midPoint();
                ShapePoint2D shp = new ShapePoint2D(viewport.toViewPoint(new Point2D.Double(coord.x, coord.y)));
                this.draw(graphics, affineTransform, shp, pixelSize, viewUnit);
                ++j;
            }
            ++i;
        }
    }
}

