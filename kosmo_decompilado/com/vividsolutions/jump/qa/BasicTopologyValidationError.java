/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.operation.valid.TopologyValidationError
 */
package com.vividsolutions.jump.qa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.qa.ValidationError;
import com.vividsolutions.jump.qa.ValidationErrorType;

public class BasicTopologyValidationError
extends ValidationError {
    private TopologyValidationError basicTopologyError;

    public BasicTopologyValidationError(TopologyValidationError basicTopologyError, Feature feature) {
        super(ValidationErrorType.BASIC_TOPOLOGY_INVALID, feature);
        this.basicTopologyError = basicTopologyError;
    }

    @Override
    public String getMessage() {
        return this.basicTopologyError.getMessage();
    }

    @Override
    public Coordinate getLocation() {
        return this.basicTopologyError.getCoordinate();
    }
}

