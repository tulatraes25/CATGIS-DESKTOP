/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.qa;

import org.saig.jump.lang.I18N;

public class ValidationErrorType {
    public static final ValidationErrorType GEOMETRY_CLASS_DISALLOWED = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.geometry-class-not-allowed"));
    public static final ValidationErrorType BASIC_TOPOLOGY_INVALID = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.basic-topology-is-invalid"));
    public static final ValidationErrorType EXTERIOR_RING_CCW = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.polygon-shell-is-oriented-counter-clockwise"));
    public static final ValidationErrorType INTERIOR_RING_CW = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.polygon-hole-is-oriented-clockwise"));
    public static final ValidationErrorType NONSIMPLE_LINESTRING = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.linestring-not-simple"));
    public static final ValidationErrorType SMALL_SEGMENT = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.contains-segment-with-length-below-minimum"));
    public static final ValidationErrorType SMALL_AREA = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.is-contains-polygon-with-area-below-minimum"));
    public static final ValidationErrorType SMALL_ANGLE = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.contains-segments-with-angle-below-minimum"));
    public static final ValidationErrorType POLYGON_HAS_HOLES = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.polygon-has-holes"));
    public static final ValidationErrorType REPEATED_CONSECUTIVE_POINTS = new ValidationErrorType(I18N.getString("com.vividsolutions.jump.qa.ValidationErrorType.consecutive-points-are-the-same"));
    private String message;

    protected ValidationErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return this.getMessage();
    }
}

