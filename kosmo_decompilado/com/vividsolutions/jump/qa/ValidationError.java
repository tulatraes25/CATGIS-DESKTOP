/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.qa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.InteriorPointFinder;
import com.vividsolutions.jump.qa.ValidationErrorType;

public class ValidationError {
    private static InteriorPointFinder interiorPointFinder = new InteriorPointFinder();
    private ValidationErrorType type;
    private Feature feature;
    private Coordinate location;

    public ValidationError(ValidationErrorType type, Feature feature) {
        this(type, feature, ValidationError.location(feature.getGeometry()));
    }

    public ValidationError(ValidationErrorType type, Feature feature, Coordinate location) {
        this.type = type;
        this.feature = feature;
        this.location = location;
    }

    public ValidationError(ValidationErrorType type, Feature feature, Geometry badPart) {
        this(type, feature, ValidationError.location(badPart));
    }

    public ValidationErrorType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.type.getMessage();
    }

    public Feature getFeature() {
        return this.feature;
    }

    public Coordinate getLocation() {
        return this.location;
    }

    private static Coordinate location(Geometry g) {
        try {
            return interiorPointFinder.findPoint(g);
        }
        catch (Exception ex) {
            return interiorPointFinder.centre(g.getEnvelopeInternal());
        }
    }
}

