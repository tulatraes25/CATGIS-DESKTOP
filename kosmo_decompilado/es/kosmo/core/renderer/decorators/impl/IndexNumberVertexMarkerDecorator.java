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
import es.kosmo.core.renderer.decorators.impl.VertexMarkerDecorator;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.jump.lang.I18N;

public class IndexNumberVertexMarkerDecorator
extends VertexMarkerDecorator {
    public static final String NAME = I18N.getString("es.kosmo.core.renderer.decorators.impl.IndexNumberVertexMarkerDecorator.Vertex-Index");
    public static final Icon ICON = IconLoader.icon("VertexIndexDecorator.gif");

    public IndexNumberVertexMarkerDecorator() {
        this(Color.BLACK, DEFAULT_FONT, "pixel");
    }

    public IndexNumberVertexMarkerDecorator(Color color, Font font, String units) {
        this.name = NAME;
        this.icon = ICON;
        this.setColor(color);
        this.setFont(font);
        this.setUnit(units);
    }

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, IDecoratorPoint2DTransformer viewport, double pixelSize, Unit<Length> viewUnit) throws Exception {
        Coordinate[] coords = geometry.getCoordinates();
        AffineTransform affineTransform = new AffineTransform();
        int i = 0;
        while (i < coords.length) {
            Coordinate currentCoord = coords[i];
            ShapePoint2D shp = new ShapePoint2D(viewport.toViewPoint(new Point2D.Double(currentCoord.x, currentCoord.y)));
            this.setText("" + i);
            this.draw(graphics, affineTransform, shp, pixelSize, viewUnit);
            ++i;
        }
    }
}

