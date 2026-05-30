/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.feature.Feature;
import java.util.ArrayList;
import java.util.List;

public class FeatureSegment
extends LineSegment {
    private static final long serialVersionUID = 1L;
    private Feature feature;
    private int index;
    private List<FeatureSegment> matches = null;

    public FeatureSegment(Feature feature, int index, Coordinate p0, Coordinate p1) {
        super(p0, p1);
        this.feature = feature;
        this.index = index;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void addMatch(FeatureSegment match) {
        if (this.matches == null) {
            this.matches = new ArrayList<FeatureSegment>();
        }
        this.matches.add(match);
    }

    public List<FeatureSegment> getMatches() {
        return this.matches;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

