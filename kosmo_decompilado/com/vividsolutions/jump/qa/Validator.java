/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.operation.IsSimpleOp
 *  com.vividsolutions.jts.operation.valid.IsValidOp
 *  com.vividsolutions.jts.operation.valid.RepeatedPointTester
 *  com.vividsolutions.jts.operation.valid.TopologyValidationError
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.qa;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.IsSimpleOp;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.RepeatedPointTester;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.qa.BasicTopologyValidationError;
import com.vividsolutions.jump.qa.ValidationError;
import com.vividsolutions.jump.qa.ValidationErrorType;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class Validator {
    private static final Logger LOGGER = Logger.getLogger(Validator.class);
    private int validatedFeatureCount;
    private boolean checkingBasicTopology = true;
    private boolean checkingPolygonOrientation = false;
    private boolean checkingLineStringsSimple = false;
    private boolean checkingMinSegmentLength = false;
    private boolean checkingMinAngle = false;
    private boolean checkingMinPolygonArea = false;
    private boolean checkingNoRepeatedConsecutivePoints = false;
    private boolean checkingNoHoles = false;
    private double minSegmentLength = 0.0;
    private double minAngle = 0.0;
    private double minPolygonArea = 0.0;
    private Collection<String> disallowedGeometryClassNames;
    private RepeatedPointTester repeatedPointTester = new RepeatedPointTester();

    public Validator() {
        this.disallowedGeometryClassNames = new ArrayList<String>();
    }

    public void setCheckingBasicTopology(boolean checkingBasicTopology) {
        this.checkingBasicTopology = checkingBasicTopology;
    }

    public void setCheckingNoRepeatedConsecutivePoints(boolean checkingNoRepeatedConsecutivePoints) {
        this.checkingNoRepeatedConsecutivePoints = checkingNoRepeatedConsecutivePoints;
    }

    public void setCheckingNoHoles(boolean checkingNoHoles) {
        this.checkingNoHoles = checkingNoHoles;
    }

    public void setCheckingPolygonOrientation(boolean checkingPolygonOrientation) {
        this.checkingPolygonOrientation = checkingPolygonOrientation;
    }

    public void setMinSegmentLength(double minSegmentLength) {
        this.minSegmentLength = minSegmentLength;
    }

    public void setMinAngle(double minAngle) {
        this.minAngle = minAngle;
    }

    public void setMinPolygonArea(double minPolygonArea) {
        this.minPolygonArea = minPolygonArea;
    }

    public void setCheckingLineStringsSimple(boolean checkingLineStringsSimple) {
        this.checkingLineStringsSimple = checkingLineStringsSimple;
    }

    public void setCheckingMinSegmentLength(boolean checkingMinSegmentLength) {
        this.checkingMinSegmentLength = checkingMinSegmentLength;
    }

    public void setCheckingMinAngle(boolean checkingMinAngle) {
        this.checkingMinAngle = checkingMinAngle;
    }

    public void setCheckingMinPolygonArea(boolean checkingMinPolygonArea) {
        this.checkingMinPolygonArea = checkingMinPolygonArea;
    }

    public void setDisallowedGeometryClasses(Collection<Class<? extends Geometry>> disallowedGeometryClasses) {
        this.disallowedGeometryClassNames.clear();
        for (Class<? extends Geometry> c : disallowedGeometryClasses) {
            this.disallowedGeometryClassNames.add(c.getName());
        }
    }

    public List<ValidationError> validate(FeatureIterator itFeatures, int numFeatures, TaskMonitor monitor, String layerName) {
        this.validatedFeatureCount = 0;
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.qa.Validator.validating")) + " '" + layerName + "' ...");
        ArrayList<ValidationError> validationErrors = new ArrayList<ValidationError>();
        int totalFeatures = numFeatures;
        try {
            while (itFeatures.hasNext() && !monitor.isCancelRequested()) {
                Feature feature = itFeatures.next();
                this.validate(feature, validationErrors);
                ++this.validatedFeatureCount;
                monitor.report(this.validatedFeatureCount, totalFeatures, I18N.getString("com.vividsolutions.jump.qa.Validator.features"));
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return validationErrors;
    }

    protected void addIfNotNull(ValidationError error, Collection<ValidationError> collection) {
        if (error == null) {
            return;
        }
        this.addIfNotNull(new ValidationError[]{error}, collection);
    }

    protected void addIfNotNull(ValidationError[] errors, Collection<ValidationError> collection) {
        if (ArrayUtils.isEmpty((Object[])errors)) {
            return;
        }
        collection.addAll(Arrays.asList(errors));
    }

    protected void validate(Feature feature, List<ValidationError> validationErrors) {
        this.addIfNotNull(this.validateGeometryClass(feature), validationErrors);
        if (this.checkingBasicTopology) {
            this.addIfNotNull(this.validateBasicTopology(feature), validationErrors);
        }
        if (this.checkingPolygonOrientation) {
            this.addIfNotNull(this.validatePolygonOrientation(feature), validationErrors);
        }
        if (this.checkingLineStringsSimple) {
            this.addIfNotNull(this.validateLineStringsSimple(feature), validationErrors);
        }
        if (this.checkingMinSegmentLength) {
            this.addIfNotNull(this.validateMinSegmentLength(feature), validationErrors);
        }
        if (this.checkingMinAngle) {
            this.addIfNotNull(this.validateMinAngle(feature), validationErrors);
        }
        if (this.checkingMinPolygonArea) {
            this.addIfNotNull(this.validateMinPolygonArea(feature), validationErrors);
        }
        if (this.checkingNoHoles) {
            this.addIfNotNull(this.validateNoHoles(feature), validationErrors);
        }
        if (this.checkingNoRepeatedConsecutivePoints) {
            this.addIfNotNull(this.validateNoRepeatedConsecutivePoints(feature), validationErrors);
        }
    }

    protected ValidationError[] validateGeometryClass(Feature feature) {
        if (this.disallowedGeometryClassNames.contains(feature.getGeometry().getClass().getName())) {
            return new ValidationError[]{new ValidationError(ValidationErrorType.GEOMETRY_CLASS_DISALLOWED, feature)};
        }
        return null;
    }

    protected ValidationError validateBasicTopology(Feature feature) {
        TopologyValidationError error = new IsValidOp(feature.getGeometry()).getValidationError();
        if (error != null) {
            return new BasicTopologyValidationError(error, feature);
        }
        return null;
    }

    protected ValidationError validateNoRepeatedConsecutivePoints(Feature feature) {
        if (this.repeatedPointTester.hasRepeatedPoint(feature.getGeometry())) {
            return new ValidationError(ValidationErrorType.REPEATED_CONSECUTIVE_POINTS, feature, this.repeatedPointTester.getCoordinate());
        }
        return null;
    }

    protected ValidationError[] validateLineStringsSimple(Feature feature) {
        return this.recursivelyValidate(feature.getGeometry(), feature, new RecursiveValidation(){

            @Override
            public ValidationError[] validate(Geometry g, Feature f) {
                LineString lineString = (LineString)g;
                IsSimpleOp op = new IsSimpleOp((Geometry)lineString);
                if (!op.isSimple()) {
                    return new ValidationError[]{new ValidationError(ValidationErrorType.NONSIMPLE_LINESTRING, f, op.getNonSimpleLocation())};
                }
                return null;
            }

            @Override
            public Class<? extends Geometry> getTargetGeometryClass() {
                return LineString.class;
            }
        });
    }

    protected ValidationError[] validatePolygonOrientation(Feature feature) {
        return this.recursivelyValidate(feature.getGeometry(), feature, new RecursiveValidation(){

            @Override
            public ValidationError[] validate(Geometry g, Feature f) {
                Polygon polygon = (Polygon)g;
                ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
                if (CGAlgorithms.isCCW((Coordinate[])polygon.getExteriorRing().getCoordinates())) {
                    errors.add(new ValidationError(ValidationErrorType.EXTERIOR_RING_CCW, f, (Geometry)polygon));
                }
                int i = 0;
                while (i < polygon.getNumInteriorRing()) {
                    if (!CGAlgorithms.isCCW((Coordinate[])polygon.getInteriorRingN(i).getCoordinates())) {
                        errors.add(new ValidationError(ValidationErrorType.INTERIOR_RING_CW, f, (Geometry)polygon));
                    }
                    ++i;
                }
                return errors.toArray(new ValidationError[errors.size()]);
            }

            @Override
            public Class<? extends Geometry> getTargetGeometryClass() {
                return Polygon.class;
            }
        });
    }

    protected ValidationError[] validateNoHoles(Feature feature) {
        return this.recursivelyValidate(feature.getGeometry(), feature, new RecursiveValidation(){

            @Override
            public ValidationError[] validate(Geometry g, Feature f) {
                Polygon polygon = (Polygon)g;
                if (polygon.getNumInteriorRing() > 0) {
                    ValidationError[] errors = new ValidationError[polygon.getNumInteriorRing()];
                    int i = 0;
                    while (i < polygon.getNumInteriorRing()) {
                        errors[i] = new ValidationError(ValidationErrorType.POLYGON_HAS_HOLES, f, (Geometry)polygon.getInteriorRingN(i).getCentroid());
                        ++i;
                    }
                    return errors;
                }
                return null;
            }

            @Override
            public Class<? extends Geometry> getTargetGeometryClass() {
                return Polygon.class;
            }
        });
    }

    private ValidationError[] recursivelyValidate(Geometry geometry, Feature feature, RecursiveValidation validation) {
        if (geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof GeometryCollection) {
            return this.recursivelyValidateGeometryCollection((GeometryCollection)geometry, feature, validation);
        }
        if (!validation.getTargetGeometryClass().isInstance(geometry)) {
            return null;
        }
        return validation.validate(geometry, feature);
    }

    private ValidationError[] recursivelyValidateGeometryCollection(GeometryCollection gc, Feature feature, RecursiveValidation validation) {
        ArrayList errors = new ArrayList();
        int i = 0;
        while (i < gc.getNumGeometries()) {
            Object[] currentErrors = this.recursivelyValidate(gc.getGeometryN(i), feature, validation);
            if (currentErrors != null) {
                CollectionUtils.addAll(errors, (Object[])currentErrors);
            }
            ++i;
        }
        return errors.toArray(new ValidationError[errors.size()]);
    }

    protected ValidationError[] validateMinSegmentLength(Feature feature) {
        List<Coordinate[]> arrays = CoordinateArrays.toCoordinateArrays(feature.getGeometry(), false);
        ArrayList errors = new ArrayList();
        for (Coordinate[] coordinates : arrays) {
            Object[] currentErrors = this.validateMinSegmentLength(coordinates, feature);
            if (errors == null) continue;
            CollectionUtils.addAll(errors, (Object[])currentErrors);
        }
        return errors.toArray(new ValidationError[errors.size()]);
    }

    protected ValidationError[] validateMinAngle(Feature feature) {
        List<Coordinate[]> arrays = CoordinateArrays.toCoordinateArrays(feature.getGeometry(), false);
        for (Coordinate[] coordinates : arrays) {
            ValidationError[] errors = this.validateMinAngle(coordinates, feature);
            if (errors == null) continue;
            return errors;
        }
        return null;
    }

    protected ValidationError[] validateMinPolygonArea(Feature feature) {
        return this.recursivelyValidate(feature.getGeometry(), feature, new RecursiveValidation(){

            @Override
            public ValidationError[] validate(Geometry g, Feature f) {
                Polygon polygon = (Polygon)g;
                if (polygon.getArea() < Validator.this.minPolygonArea) {
                    return new ValidationError[]{new ValidationError(ValidationErrorType.SMALL_AREA, f, (Geometry)polygon)};
                }
                return null;
            }

            @Override
            public Class<? extends Geometry> getTargetGeometryClass() {
                return Polygon.class;
            }
        });
    }

    private ValidationError[] validateMinSegmentLength(Coordinate[] coordinates, Feature feature) {
        if (coordinates.length < 2) {
            return null;
        }
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        int i = 1;
        while (i < coordinates.length) {
            ValidationError error = this.validateMinSegmentLength(coordinates[i - 1], coordinates[i], feature);
            if (error != null) {
                errors.add(error);
            }
            ++i;
        }
        return errors.toArray(new ValidationError[errors.size()]);
    }

    private ValidationError[] validateMinAngle(Coordinate[] coordinates, Feature feature) {
        if (coordinates.length < 3) {
            return null;
        }
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        boolean closed = coordinates[0].equals((Object)coordinates[coordinates.length - 1]);
        int i = closed ? 1 : 2;
        while (i < coordinates.length) {
            ValidationError error = this.validateMinAngle(i == 1 ? coordinates[coordinates.length - 2] : coordinates[i - 2], coordinates[i - 1], coordinates[i], feature);
            if (error != null) {
                errors.add(error);
            }
            ++i;
        }
        return errors.toArray(new ValidationError[errors.size()]);
    }

    private ValidationError validateMinSegmentLength(Coordinate c1, Coordinate c2, Feature feature) {
        if (c1.distance(c2) < this.minSegmentLength) {
            return new ValidationError(ValidationErrorType.SMALL_SEGMENT, feature, CoordUtil.average(c1, c2));
        }
        return null;
    }

    private ValidationError validateMinAngle(Coordinate c1, Coordinate c2, Coordinate c3, Feature feature) {
        if (Angle.angleBetween(c2, c1, c3) < Angle.toRadians(this.minAngle)) {
            return new ValidationError(ValidationErrorType.SMALL_ANGLE, feature, c2);
        }
        return null;
    }

    private static interface RecursiveValidation {
        public ValidationError[] validate(Geometry var1, Feature var2);

        public Class<? extends Geometry> getTargetGeometryClass();
    }
}

