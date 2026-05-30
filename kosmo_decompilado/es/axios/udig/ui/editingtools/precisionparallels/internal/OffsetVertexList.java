/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.PrecisionModel
 */
package es.axios.udig.ui.editingtools.precisionparallels.internal;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.util.ArrayList;

public class OffsetVertexList {
    private static final Coordinate[] COORDINATE_ARRAY_TYPE = new Coordinate[0];
    private ArrayList<Coordinate> ptList = new ArrayList();
    private PrecisionModel precisionModel = null;
    private double minimimVertexDistance = 0.0;
    private boolean inter = false;
    private Coordinate initialCoordinate = null;

    public void setPrecisionModel(PrecisionModel precisionModel) {
        this.precisionModel = precisionModel;
    }

    public void setMinimumVertexDistance(double minimimVertexDistance) {
        this.minimimVertexDistance = minimimVertexDistance;
    }

    public void addPt(Coordinate pt, boolean isIntersectionPt) {
        Coordinate ptToAdd = new Coordinate(pt);
        this.precisionModel.makePrecise(ptToAdd);
        if (this.isDuplicate(ptToAdd)) {
            return;
        }
        if (isIntersectionPt) {
            this.ptList.remove(this.ptList.size() - 1);
            this.ptList.add(ptToAdd);
            this.inter = isIntersectionPt;
            return;
        }
        if (this.inter) {
            this.inter = false;
            return;
        }
        if (this.ptList.isEmpty()) {
            this.initialCoordinate = ptToAdd;
        }
        this.ptList.add(ptToAdd);
    }

    public Coordinate getInitialCoor() {
        return this.initialCoordinate;
    }

    private boolean isDuplicate(Coordinate pt) {
        if (this.ptList.size() < 1) {
            return false;
        }
        Coordinate lastPt = this.ptList.get(this.ptList.size() - 1);
        double ptDist = pt.distance(lastPt);
        return ptDist < this.minimimVertexDistance;
    }

    private Coordinate[] getCoordinates() {
        Coordinate[] coord = this.ptList.toArray(COORDINATE_ARRAY_TYPE);
        return coord;
    }

    public String toString() {
        GeometryFactory fact = new GeometryFactory();
        LineString line = fact.createLineString(this.getCoordinates());
        return line.toString();
    }

    public int size() {
        return this.ptList.size();
    }

    public ArrayList<Coordinate> getList() {
        return this.ptList;
    }
}

