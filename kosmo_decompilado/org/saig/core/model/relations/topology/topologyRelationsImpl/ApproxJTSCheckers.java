/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.TopologyException
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;

public class ApproxJTSCheckers {
    public static double AREA_ERROR = 1.0E-7;

    public static boolean contains(Geometry a, Geometry b) {
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return b.difference(a).getArea() < AREA_ERROR;
        }
        return a.contains(b);
    }

    public static boolean crosses(Geometry a, Geometry b) {
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return b.intersection(a).getArea() > AREA_ERROR;
        }
        return a.crosses(b);
    }

    public static boolean notOverlapOfAreas(Geometry a, Geometry b) {
        try {
            return a.intersection(b).getArea() < AREA_ERROR;
        }
        catch (TopologyException e) {
            return a.overlaps(b);
        }
    }

    public static boolean within(Geometry a, Geometry b) {
        if (a.getDimension() > 1 && b.getDimension() > 1) {
            return a.difference(b).getArea() < AREA_ERROR;
        }
        return a.within(b);
    }
}

