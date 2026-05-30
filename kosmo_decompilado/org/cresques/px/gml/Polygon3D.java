/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.geo.Point3D
 *  org.cresques.px.gml.Polygon
 */
package org.cresques.px.gml;

import java.awt.geom.Point2D;
import org.cresques.geo.Point3D;
import org.cresques.px.gml.Polygon;

public class Polygon3D
extends Polygon {
    public void add(Point2D pt) {
        Point3D newPoint = new Point3D(pt);
        this.add(newPoint);
    }

    public void add(Point3D pt) {
        ++pointNr;
        if (this.outer) {
            this.outPol.addPoint((Point2D)pt);
        } else {
            this.inPol.addPoint((Point2D)pt);
        }
        this.extent.add((Point2D)pt);
    }

    public Point3D getPoint3D(int i) {
        if (this.outer) {
            return (Point3D)this.outPol.get(i);
        }
        return (Point3D)this.inPol.get(i);
    }
}

