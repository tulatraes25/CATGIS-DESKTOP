/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import org.saig.core.filter.Expression;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.shape.ExplicitBoundsShape;

public class ShapeMarkFactory
implements MarkFactory {
    private static final String SHAPE_PREFIX = "shape://";
    static Map<String, Shape> shapes = new HashMap<String, Shape>();

    static {
        shapes.put("vertline", new Line2D.Double(0.0, -0.5, 0.0, 0.5));
        shapes.put("horline", new Line2D.Double(-0.5, 0.0, 0.5, 0.0));
        shapes.put("slash", new Line2D.Double(-0.5, -0.5, 0.5, 0.5));
        shapes.put("backslash", new Line2D.Double(-0.5, 0.5, 0.5, -0.5));
        ExplicitBoundsShape dotShape = new ExplicitBoundsShape(new Ellipse2D.Double(-1.0E-6, -1.0E-6, 1.0E-6, 1.0E-6));
        dotShape.setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        shapes.put("dot", dotShape);
        GeneralPath gp = new GeneralPath();
        gp.moveTo(-0.5f, 0.0f);
        gp.lineTo(0.5f, 0.0f);
        gp.moveTo(0.0f, -0.5f);
        gp.lineTo(0.0f, 0.5f);
        shapes.put("plus", gp);
        gp = new GeneralPath();
        gp.moveTo(-0.5f, 0.5f);
        gp.lineTo(0.5f, -0.5f);
        gp.moveTo(-0.5f, -0.5f);
        gp.lineTo(0.5f, 0.5f);
        shapes.put("times", gp);
        gp = new GeneralPath();
        gp.moveTo(-0.5f, 0.2f);
        gp.lineTo(0.0f, 0.0f);
        gp.lineTo(-0.5f, -0.2f);
        ExplicitBoundsShape oarrow = new ExplicitBoundsShape(gp);
        oarrow.setBounds(new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0));
        shapes.put("oarrow", oarrow);
        gp = new GeneralPath();
        gp.moveTo(-0.5f, 0.2f);
        gp.lineTo(0.0f, 0.0f);
        gp.lineTo(-0.5f, -0.2f);
        gp.closePath();
        ExplicitBoundsShape carrow = new ExplicitBoundsShape(gp);
        carrow.setBounds(new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0));
        shapes.put("carrow", carrow);
    }

    @Override
    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception {
        if (symbolUrl == null) {
            return null;
        }
        String wellKnownName = symbolUrl.getValue(feature).toString();
        if (!wellKnownName.startsWith(SHAPE_PREFIX)) {
            return null;
        }
        String name = wellKnownName.substring(SHAPE_PREFIX.length());
        return shapes.get(name);
    }
}

