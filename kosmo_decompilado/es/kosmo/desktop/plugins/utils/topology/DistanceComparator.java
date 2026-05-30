/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package es.kosmo.desktop.plugins.utils.topology;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import java.util.Comparator;

public class DistanceComparator
implements Comparator<Feature> {
    protected Geometry comparingGeom;

    public DistanceComparator(Geometry geom) {
        this.comparingGeom = (Geometry)geom.clone();
    }

    @Override
    public int compare(Feature a, Feature b) {
        if (a.getGeometry() == null || b.getGeometry() == null) {
            return 0;
        }
        if (a.getGeometry() == null) {
            return 1;
        }
        if (b.getGeometry() == null) {
            return -1;
        }
        Geometry geomA = a.getGeometry();
        Geometry geomB = b.getGeometry();
        Double toADistance = this.comparingGeom.distance(geomA);
        Double toBDistance = this.comparingGeom.distance(geomB);
        return toADistance.compareTo(toBDistance);
    }
}

