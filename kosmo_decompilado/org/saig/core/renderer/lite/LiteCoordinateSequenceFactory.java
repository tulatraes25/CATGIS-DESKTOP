/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import org.saig.core.renderer.lite.LiteCoordinateSequence;

public class LiteCoordinateSequenceFactory
implements CoordinateSequenceFactory {
    public CoordinateSequence create(Coordinate[] coordinates) {
        return new LiteCoordinateSequence(coordinates);
    }

    public CoordinateSequence create(CoordinateSequence coordSeq) {
        return new LiteCoordinateSequence(coordSeq.toCoordinateArray());
    }

    public CoordinateSequence create(int size, int dimension) {
        return new LiteCoordinateSequence(size, dimension);
    }

    public CoordinateSequence create(double[] points) {
        return new LiteCoordinateSequence(points);
    }
}

