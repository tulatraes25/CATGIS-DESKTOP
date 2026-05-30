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
import es.kosmo.core.renderer.decorators.impl.SolidArrowMarkerDecorator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.jump.lang.I18N;

public class StartArrowMarkerDecorator
extends SolidArrowMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.StartArrowMarkerDecorator.Starting-arrow");
    public static final Icon ICON = IconLoader.icon("ArrowStartSolid.gif");

    public StartArrowMarkerDecorator() {
        this(Color.BLACK, 10.0, 30.0, false, 0.0, "pixel");
    }

    public StartArrowMarkerDecorator(Color color, double size, double sharpness, boolean isFixedRotation, double rotation, String units) {
        this.name = NAME;
        this.icon = ICON;
        this.setSharpness(sharpness);
        this.setColor(color);
        this.setSize(size);
        this.setFixedRotation(isFixedRotation);
        this.setRotation(rotation);
        this.setUnit(units);
    }

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, IDecoratorPoint2DTransformer viewport, double pixelSize, Unit<Length> viewUnit) throws Exception {
        int i = 0;
        while (i < geometry.getNumGeometries()) {
            Geometry currentGeom = geometry.getGeometryN(i);
            Coordinate coord = currentGeom.getCoordinates()[0];
            ShapePoint2D shp = new ShapePoint2D(viewport.toViewPoint(new Point2D.Double(coord.x, coord.y)));
            AffineTransform affineTransform = new AffineTransform();
            if (!this.isFixedRotation()) {
                Coordinate nextCoord = this.getNextNonEqual(currentGeom);
                LineSegment lineSegment = new LineSegment(coord, nextCoord);
                this.setRotation(-lineSegment.angle() + Math.PI);
            }
            this.draw(graphics, affineTransform, shp, pixelSize, viewUnit);
            ++i;
        }
    }
}

