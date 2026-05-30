/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;

public class StyleUtil {
    public static void paint(Geometry geometry, Graphics2D g, Viewport viewport, boolean renderingFill, Stroke fillStroke, Paint fillPaint, boolean renderingLine, Stroke lineStroke, Color lineColor) throws NoninvertibleTransformException {
        if (geometry instanceof GeometryCollection) {
            StyleUtil.paintGeometryCollection((GeometryCollection)geometry, g, viewport, renderingFill, fillStroke, fillPaint, renderingLine, lineStroke, lineColor);
            return;
        }
        Shape shape = StyleUtil.toShape(geometry, viewport);
        if (!(shape instanceof GeneralPath) && renderingFill) {
            g.setStroke(fillStroke);
            g.setPaint(fillPaint);
            g.fill(shape);
        }
        if (renderingLine) {
            g.setStroke(lineStroke);
            g.setColor(lineColor);
            g.draw(shape);
        }
    }

    private static void paintGeometryCollection(GeometryCollection collection, Graphics2D g, Viewport viewport, boolean renderingFill, Stroke fillStroke, Paint fillPaint, boolean renderingLine, Stroke lineStroke, Color lineColor) throws NoninvertibleTransformException {
        int i = 0;
        while (i < collection.getNumGeometries()) {
            StyleUtil.paint(collection.getGeometryN(i), g, viewport, renderingFill, fillStroke, fillPaint, renderingLine, lineStroke, lineColor);
            ++i;
        }
    }

    private static Shape toShape(Geometry geometry, Viewport viewport) throws NoninvertibleTransformException {
        Geometry actualGeometry;
        Envelope bufferedEnvelope = EnvelopeUtil.bufferByFraction(viewport.getEnvelopeInModelCoordinates(), 0.05);
        if (!bufferedEnvelope.contains((actualGeometry = geometry).getEnvelopeInternal())) {
            try {
                actualGeometry = EnvelopeUtil.toGeometry(bufferedEnvelope).intersection(actualGeometry);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return viewport.getJava2DConverter().toShape(actualGeometry);
    }
}

