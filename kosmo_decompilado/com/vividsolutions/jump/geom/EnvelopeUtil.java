/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.cresques.cts.ICoordTrans
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.util.MathUtil;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.cresques.cts.ICoordTrans;

public class EnvelopeUtil {
    private static GeometryFactory factory = new GeometryFactory();

    public static Envelope expand(Envelope env, double distance) {
        if (distance < 0.0) {
            double minSize = 2.0 * -distance;
            if (env.getWidth() < minSize) {
                return new Envelope();
            }
            if (env.getHeight() < minSize) {
                return new Envelope();
            }
        }
        return new Envelope(env.getMinX() - distance, env.getMaxX() + distance, env.getMinY() - distance, env.getMaxY() + distance);
    }

    public static void translate(Envelope e, Coordinate displacement) {
        if (e.isNull()) {
            return;
        }
        e.init(e.getMinX() + displacement.x, e.getMaxX() + displacement.x, e.getMinY() + displacement.y, e.getMaxY() + displacement.y);
    }

    public static Envelope bufferByFraction(Envelope originalEnvelope, double extentFraction) {
        if (originalEnvelope == null) {
            return null;
        }
        Envelope bufferedEnvelope = new Envelope(originalEnvelope);
        double averageExtent = (bufferedEnvelope.getWidth() + bufferedEnvelope.getHeight()) / 2.0;
        double buffer = averageExtent * extentFraction;
        if (averageExtent == 0.0) {
            buffer = 10.0;
        }
        bufferedEnvelope.expandToInclude(bufferedEnvelope.getMaxX() + buffer, bufferedEnvelope.getMaxY() + buffer);
        bufferedEnvelope.expandToInclude(bufferedEnvelope.getMinX() - buffer, bufferedEnvelope.getMinY() - buffer);
        return bufferedEnvelope;
    }

    public static Coordinate centre(Envelope e) {
        return new Coordinate(MathUtil.avg(e.getMinX(), e.getMaxX()), MathUtil.avg(e.getMinY(), e.getMaxY()));
    }

    public static Geometry toGeometry(Envelope envelope) {
        if (envelope.getWidth() == 0.0 && envelope.getHeight() == 0.0) {
            return factory.createPoint(new Coordinate(envelope.getMinX(), envelope.getMinY()));
        }
        if (envelope.getWidth() == 0.0 || envelope.getHeight() == 0.0) {
            return factory.createLineString(new Coordinate[]{new Coordinate(envelope.getMinX(), envelope.getMinY()), new Coordinate(envelope.getMaxX(), envelope.getMaxY())});
        }
        return factory.createPolygon(factory.createLinearRing(new Coordinate[]{new Coordinate(envelope.getMinX(), envelope.getMinY()), new Coordinate(envelope.getMinX(), envelope.getMaxY()), new Coordinate(envelope.getMaxX(), envelope.getMaxY()), new Coordinate(envelope.getMaxX(), envelope.getMinY()), new Coordinate(envelope.getMinX(), envelope.getMinY())}), null);
    }

    public static Envelope getTransformedEnvelope(Envelope originalEnvelope, ICoordTrans coordTrans) {
        Envelope transformedEnvelope = null;
        if (coordTrans == null) {
            transformedEnvelope = originalEnvelope;
        } else {
            Rectangle2D.Double layerView = new Rectangle2D.Double(originalEnvelope.getMinX(), originalEnvelope.getMinY(), originalEnvelope.getWidth(), originalEnvelope.getHeight());
            Rectangle2D transformedView = coordTrans.convert((Rectangle2D)layerView);
            transformedEnvelope = new Envelope(transformedView.getMinX(), transformedView.getMaxX(), transformedView.getMinY(), transformedView.getMaxY());
        }
        return transformedEnvelope;
    }

    public static List<Envelope> divide(Envelope obj, double width, double height) {
        int in = (int)Math.ceil(obj.getWidth() / width);
        int jn = (int)Math.ceil(obj.getHeight() / height);
        double totalWidth = (double)in * width;
        double totalHeight = (double)jn * height;
        double spx = (obj.getMaxX() + obj.getMinX()) / 2.0 - totalWidth / 2.0;
        double spy = (obj.getMinY() + obj.getMaxY()) / 2.0 - totalHeight / 2.0;
        ArrayList<Envelope> envelopes = new ArrayList<Envelope>();
        int j = 0;
        while (j < jn) {
            double pspx = spx;
            int i = 0;
            while (i < in) {
                envelopes.add(new Envelope(pspx, pspx + width, spy, spy + height));
                pspx += height;
                ++i;
            }
            spy += height;
            ++j;
        }
        return envelopes;
    }
}

