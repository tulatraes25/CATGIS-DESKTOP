/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.core.renderer.decorators.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import es.kosmo.core.renderer.decorators.impl.OpenArrowMarkerDecorator;
import es.kosmo.core.utils.GeometryUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.jump.lang.I18N;

public class MidLineOpenArrowMarkerDecorator
extends OpenArrowMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.MidLineOpenArrowMarkerDecorator.Mid-line-open-arrow");
    public static final Icon ICON = IconLoader.icon("ArrowMidOpen.gif");

    public MidLineOpenArrowMarkerDecorator() {
        this(Color.BLACK, 10.0, 30.0, false, 0.0, "pixel");
    }

    public MidLineOpenArrowMarkerDecorator(Color color, double size, double sharpness, boolean isFixedRotation, double rotation, String units) {
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
            Object[] middle = GeometryUtils.getMidPointAndAngle(currentGeom);
            if (middle != null) {
                Coordinate coordAnchor = (Coordinate)middle[0];
                Double segmentAngle = (Double)middle[1];
                Point2D.Double modelPoint = new Point2D.Double(coordAnchor.x, coordAnchor.y);
                Point2D viewPoint = viewport.toViewPoint(modelPoint);
                ShapePoint2D shp = new ShapePoint2D(viewPoint);
                if (!this.isFixedRotation()) {
                    this.setRotation(-segmentAngle.doubleValue() + Math.PI);
                }
                AffineTransform affineTransform = new AffineTransform();
                this.draw(graphics, affineTransform, shp, pixelSize, viewUnit);
            }
            ++i;
        }
    }
}

