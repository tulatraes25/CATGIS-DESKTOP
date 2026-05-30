/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.LineString
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.saig.core.renderer.lite.LiteCoordinateSequence;

public final class LineIterator2
implements PathIterator {
    private double[] allCoords;
    private AffineTransform at;
    private int currentCoord = 0;
    private int actualCoords;
    private boolean done = false;

    @Override
    public int currentSegment(float[] coords) {
        if (this.currentCoord == 0) {
            coords[0] = (float)this.allCoords[0];
            coords[1] = (float)this.allCoords[1];
            if (this.at != null) {
                this.at.transform(coords, 0, coords, 0, 1);
            }
            return 0;
        }
        coords[0] = (float)this.allCoords[this.currentCoord * 2];
        coords[1] = (float)this.allCoords[this.currentCoord * 2 + 1];
        if (this.at != null) {
            this.at.transform(coords, 0, coords, 0, 1);
        }
        return 1;
    }

    public void init(LineString ls, AffineTransform at) {
        this.at = at == null || at.isIdentity() ? null : at;
        CoordinateSequence coordinates = ls.getCoordinateSequence();
        if (coordinates instanceof LiteCoordinateSequence) {
            this.allCoords = ((LiteCoordinateSequence)coordinates).getArray();
            this.actualCoords = coordinates.size();
        } else {
            this.actualCoords = coordinates.size();
            this.allCoords = new double[this.actualCoords * 2];
            int t = 0;
            while (t < this.actualCoords) {
                this.allCoords[t * 2] = coordinates.getOrdinate(t, 0);
                this.allCoords[t * 2 + 1] = coordinates.getOrdinate(t, 1);
                ++t;
            }
        }
        this.done = false;
        this.currentCoord = 0;
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void next() {
        if (this.currentCoord == this.actualCoords - 1) {
            this.done = true;
        } else {
            ++this.currentCoord;
        }
    }

    @Override
    public int currentSegment(double[] coords) {
        float[] fco = new float[6];
        int result = this.currentSegment(fco);
        coords[0] = fco[0];
        coords[1] = fco[1];
        if (coords.length >= 3) {
            coords[2] = fco[2];
            coords[3] = fco[3];
        }
        return result;
    }
}

