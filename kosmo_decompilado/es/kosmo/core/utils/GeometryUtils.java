/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.PrecisionModel
 */
package es.kosmo.core.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.geometry.filters.ZValueFilter;
import org.saig.jump.lang.I18N;

public class GeometryUtils {
    public static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 0);

    public static Object[] getMidPointAndAngle(Geometry geom) {
        if (geom == null) {
            return null;
        }
        Coordinate[] coords = geom.getCoordinates();
        double totalLength = geom.getLength();
        double acumLength = 0.0;
        double midLength = totalLength / 2.0;
        Coordinate coordAnchor = null;
        double segmentAngle = 0.0;
        int j = 0;
        while (j < coords.length - 1 && coordAnchor == null) {
            Coordinate currentCoord = coords[j];
            Coordinate nextCoord = coords[j + 1];
            LineSegment lineSegment = new LineSegment(currentCoord, nextCoord);
            double segmentLength = lineSegment.getLength();
            if (segmentLength != 0.0) {
                double currentLength = acumLength + segmentLength;
                if (currentLength < midLength) {
                    acumLength += segmentLength;
                } else {
                    double dist = midLength - acumLength;
                    double fraction = dist / segmentLength;
                    coordAnchor = lineSegment.pointAlong(fraction);
                    segmentAngle = lineSegment.angle();
                }
            }
            ++j;
        }
        if (coordAnchor == null) {
            return null;
        }
        return new Object[]{coordAnchor, segmentAngle};
    }

    public static Coordinate getMidPoint(Geometry geom) {
        Object[] result = GeometryUtils.getMidPointAndAngle(geom);
        if (result == null) {
            return null;
        }
        return (Coordinate)result[0];
    }

    public static Geometry applyZFilter(Geometry geom, boolean is3d) {
        Geometry result = (Geometry)geom.clone();
        if (is3d) {
            result.apply((CoordinateFilter)new ZValueFilter(false));
        } else {
            result.apply((CoordinateFilter)new ZValueFilter(Double.NaN, true));
        }
        result.geometryChanged();
        return result;
    }

    public static Geometry convertToGoodGeometry(FeatureSchema schema, Geometry geom) throws Exception {
        Geometry transfGeom = null;
        switch (schema.getGeometryType()) {
            case 1: {
                if (geom instanceof MultiPoint) {
                    if (geom.getNumGeometries() != 1) break;
                    transfGeom = geom.getGeometryN(0);
                    break;
                }
                transfGeom = geom;
                break;
            }
            case 8: {
                if (geom instanceof Point) {
                    transfGeom = factory.createMultiPoint(((Point)geom).getCoordinates());
                    break;
                }
                transfGeom = geom;
                break;
            }
            case 5: {
                if (geom instanceof MultiPolygon) {
                    if (geom.getNumGeometries() != 1) break;
                    transfGeom = geom.getGeometryN(0);
                    break;
                }
                transfGeom = geom;
                break;
            }
            case 4: {
                if (geom instanceof Polygon) {
                    transfGeom = factory.createMultiPolygon(new Polygon[]{(Polygon)geom});
                    break;
                }
                transfGeom = geom;
                break;
            }
            case 3: {
                if (geom instanceof MultiLineString) {
                    if (geom.getNumGeometries() != 1) break;
                    transfGeom = geom.getGeometryN(0);
                    break;
                }
                transfGeom = geom;
                break;
            }
            case 2: {
                if (geom instanceof LineString) {
                    transfGeom = factory.createMultiLineString(new LineString[]{(LineString)geom});
                    break;
                }
                transfGeom = geom;
                break;
            }
            default: {
                transfGeom = geom;
            }
        }
        if (transfGeom == null) {
            throw new Exception(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.geometry-type-{0}-is-not-supported-by-the-layer", new Object[]{geom.getGeometryType()}));
        }
        return transfGeom;
    }
}

