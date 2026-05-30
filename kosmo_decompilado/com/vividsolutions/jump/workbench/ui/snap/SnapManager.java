/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SnapManager {
    private List<SnapPolicy> policies = new ArrayList<SnapPolicy>();
    private boolean snapCoordinateFound;
    private Color snapCurrentColor;
    private Geometry specificGeometry = null;
    private static SnapPolicy currentSnapPolicy = null;

    public Geometry getSpecificGeometry() {
        return this.specificGeometry;
    }

    public void setSpecificGeometry(Geometry specificGeometry) {
        this.specificGeometry = specificGeometry;
    }

    public Coordinate snap(LayerViewPanel panel, Coordinate originalCoordinate) {
        double minDistance = Double.MAX_VALUE;
        Coordinate coordTemp = originalCoordinate;
        Color colorTemp = null;
        SnapPolicy tempPolicy = null;
        this.snapCoordinateFound = false;
        Iterator<SnapPolicy> i = this.policies.iterator();
        while (i.hasNext() && minDistance != 0.0) {
            SnapPolicy policy = i.next();
            Coordinate snapCoordinate = policy.snap(panel, originalCoordinate, this.specificGeometry);
            if (snapCoordinate == null) continue;
            this.snapCoordinateFound = true;
            if (!(snapCoordinate.distance(originalCoordinate) < minDistance)) continue;
            minDistance = snapCoordinate.distance(originalCoordinate);
            coordTemp = snapCoordinate;
            colorTemp = policy.getColor();
            tempPolicy = policy;
        }
        if (this.snapCoordinateFound) {
            this.snapCurrentColor = colorTemp;
            currentSnapPolicy = tempPolicy;
        } else {
            currentSnapPolicy = null;
        }
        return coordTemp;
    }

    public void addPolicies(Collection<SnapPolicy> policies) {
        this.policies.addAll(policies);
    }

    public void setNewPolicies(Collection<SnapPolicy> policies) {
        this.policies.clear();
        this.policies.addAll(policies);
    }

    public Color getSnapCurrentColor() {
        return this.snapCurrentColor;
    }

    public boolean wasSnapCoordinateFound() {
        return this.snapCoordinateFound;
    }

    public static SnapPolicy getCurrentSnapPolicy() {
        return currentSnapPolicy;
    }
}

