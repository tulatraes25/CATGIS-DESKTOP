/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.TopologyException
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.TopologyException;
import org.saig.core.filter.BBoxExpression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;

public class BBoxExpressionImpl
extends LiteralExpressionImpl
implements BBoxExpression {
    private GeometryFactory gfac = new GeometryFactory();

    public BBoxExpressionImpl() throws IllegalFilterException {
        this(new Envelope());
    }

    public BBoxExpressionImpl(Envelope env) throws IllegalFilterException {
        this.expressionType = (short)104;
        this.setBounds(env);
    }

    @Override
    public final void setBounds(Envelope env) throws IllegalFilterException {
        Coordinate[] coords = new Coordinate[]{new Coordinate(env.getMinX(), env.getMinY()), new Coordinate(env.getMinX(), env.getMaxY()), new Coordinate(env.getMaxX(), env.getMaxY()), new Coordinate(env.getMaxX(), env.getMinY()), new Coordinate(env.getMinX(), env.getMinY())};
        LinearRing ring = null;
        try {
            ring = this.gfac.createLinearRing(coords);
        }
        catch (TopologyException tex) {
            throw new IllegalFilterException(tex.toString());
        }
        super.setLiteral(this.gfac.createPolygon(ring, null));
    }
}

