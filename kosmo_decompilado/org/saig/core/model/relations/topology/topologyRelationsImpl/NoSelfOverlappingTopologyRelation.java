/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class NoSelfOverlappingTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "No self overlapping";
    public static final String NAME = I18N.getString(NoSelfOverlappingTopologyRelation.class, "no-self-overlapping");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return I18N.getString(this.getClass(), "thit-operator-allows-to-check-that-there-are-not-elements-in-one-layer-that-self-overlaps");
    }

    private boolean checkLineString(LineString line) {
        Coordinate[] coords = line.getCoordinates();
        LineSegment segment1 = new LineSegment(coords[0], coords[1]);
        boolean check = false;
        int i = 2;
        while (i < coords.length && !check) {
            LineSegment segment2 = new LineSegment(coords[i - 1], coords[i]);
            check = segment2.overlaps(segment1);
            if (!check) {
                segment1 = (LineSegment)segment2.clone();
            }
            ++i;
        }
        return check;
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        boolean check = true;
        if (!feat.getGeometry().isSimple()) {
            if (feat.getGeometry() instanceof LineString) {
                check = !this.checkLineString((LineString)feat.getGeometry());
            } else if (feat.getGeometry() instanceof MultiLineString) {
                MultiLineString multiLine = (MultiLineString)feat.getGeometry();
                int i = 0;
                while (i < multiLine.getNumGeometries() && !check) {
                    check = !this.checkLineString((LineString)multiLine.getGeometryN(i));
                    ++i;
                }
            }
        }
        return check;
    }

    private class LineSegment
    implements Cloneable {
        private LineString line;

        public LineSegment(Coordinate c1, Coordinate c2) {
            GeometryFactory geomFact = new GeometryFactory();
            this.line = geomFact.createLineString(new Coordinate[]{c1, c2});
        }

        public boolean overlaps(LineSegment other) {
            Point startPoint = other.getLineString().getStartPoint();
            return startPoint.intersects((Geometry)this.line);
        }

        public LineString getLineString() {
            return this.line;
        }

        public Object clone() {
            Coordinate[] coords = this.line.getCoordinates();
            return new LineSegment(coords[0], coords[1]);
        }
    }
}

