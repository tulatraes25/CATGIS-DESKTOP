/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateArrays
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class GeometryEditor {
    private static final Logger LOGGER = Logger.getLogger(GeometryEditor.class);
    private GeometryFactory factory = new GeometryFactory();

    public Geometry edit(Geometry geometry, GeometryEditorOperation operation) {
        if (geometry instanceof GeometryCollection) {
            return this.editGeometryCollection((GeometryCollection)geometry, operation);
        }
        if (geometry instanceof Polygon) {
            return this.editPolygon((Polygon)geometry, operation);
        }
        if (geometry instanceof Point) {
            return operation.edit(geometry);
        }
        if (geometry instanceof LineString) {
            return operation.edit(geometry);
        }
        Assert.shouldNeverReachHere((String)I18N.getString("workbench.ui.GeometryEditor.unsupported-geometry-classes-should-be-caught-in-the-geometryeditoroperation"));
        return null;
    }

    private Polygon editPolygon(Polygon polygon, GeometryEditorOperation operation) {
        Polygon newPolygon = (Polygon)operation.edit((Geometry)polygon);
        if (newPolygon.isEmpty()) {
            return newPolygon;
        }
        LinearRing shell = (LinearRing)this.edit((Geometry)newPolygon.getExteriorRing(), operation);
        if (shell.isEmpty()) {
            return this.factory.createPolygon(null, null);
        }
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        int i = 0;
        while (i < newPolygon.getNumInteriorRing()) {
            LinearRing hole = (LinearRing)this.edit((Geometry)newPolygon.getInteriorRingN(i), operation);
            if (!hole.isEmpty()) {
                holes.add(hole);
            }
            ++i;
        }
        return this.factory.createPolygon(shell, holes.toArray(new LinearRing[0]));
    }

    private GeometryCollection editGeometryCollection(GeometryCollection collection, GeometryEditorOperation operation) {
        GeometryCollection newCollection = (GeometryCollection)operation.edit((Geometry)collection);
        ArrayList<Geometry> geometries = new ArrayList<Geometry>();
        int i = 0;
        while (i < newCollection.getNumGeometries()) {
            Geometry geometry = this.edit(newCollection.getGeometryN(i), operation);
            if (!geometry.isEmpty()) {
                geometries.add(geometry);
            }
            ++i;
        }
        if (newCollection.getClass() == MultiPoint.class) {
            return this.factory.createMultiPoint(geometries.toArray(new Point[0]));
        }
        if (newCollection.getClass() == MultiLineString.class) {
            return this.factory.createMultiLineString(geometries.toArray(new LineString[0]));
        }
        if (newCollection.getClass() == MultiPolygon.class) {
            return this.factory.createMultiPolygon(geometries.toArray(new Polygon[0]));
        }
        return this.factory.createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    public Geometry removeRepeatedPoints(Geometry geometry) {
        if (geometry.isEmpty()) {
            return geometry;
        }
        return this.edit(geometry, new CoordinateOperation(){

            @Override
            public Coordinate[] edit(Coordinate[] coordinates, boolean linearRing) {
                return CoordinateArrays.removeRepeatedPoints((Coordinate[])coordinates);
            }
        });
    }

    public Geometry remove(Geometry g, final Geometry itemToRemove) {
        return this.edit(g, new GeometryEditorOperation(){

            @Override
            public Geometry edit(Geometry geometry) {
                if (geometry == itemToRemove) {
                    return GeometryEditor.this.createNullGeometry(geometry.getClass());
                }
                return geometry;
            }
        });
    }

    private Geometry createNullGeometry(Class geometryClass) {
        if (geometryClass == MultiPolygon.class) {
            return this.factory.createMultiPolygon(null);
        }
        if (geometryClass == MultiLineString.class) {
            return this.factory.createMultiLineString(null);
        }
        if (geometryClass == MultiPoint.class) {
            Coordinate[] coordenadas = null;
            return this.factory.createMultiPoint(coordenadas);
        }
        if (geometryClass == GeometryCollection.class) {
            return this.factory.createGeometryCollection(null);
        }
        if (geometryClass == Polygon.class) {
            return this.factory.createPolygon(null, null);
        }
        if (geometryClass == LinearRing.class) {
            Coordinate[] coordenadas = null;
            return this.factory.createLinearRing(coordenadas);
        }
        if (geometryClass == LineString.class) {
            Coordinate[] coordenadas = null;
            return this.factory.createLineString(coordenadas);
        }
        if (geometryClass == Point.class) {
            Coordinate coord = null;
            return this.factory.createPoint(coord);
        }
        Assert.shouldNeverReachHere();
        return null;
    }

    public Geometry insertVertex(Geometry geometry, Coordinate target, Geometry ignoreSegmentsOutside) {
        LineString closestSegment = null;
        Point targetPoint = this.factory.createPoint(target);
        for (Coordinate[] coordinates : com.vividsolutions.jump.util.CoordinateArrays.toCoordinateArrays(geometry, false)) {
            if (coordinates.length < 2) continue;
            int j = 1;
            while (j < coordinates.length) {
                LineString candidate = this.factory.createLineString(new Coordinate[]{coordinates[j], coordinates[j - 1]});
                if (candidate.intersects(ignoreSegmentsOutside)) {
                    if (closestSegment == null) {
                        closestSegment = candidate;
                    } else if (candidate.distance((Geometry)targetPoint) < closestSegment.distance((Geometry)targetPoint)) {
                        closestSegment = candidate;
                    }
                }
                ++j;
            }
        }
        if (closestSegment == null) {
            return null;
        }
        Coordinate closestPoint = new LineSegment(closestSegment.getCoordinateN(0), closestSegment.getCoordinateN(1)).closestPoint(target);
        if (closestPoint.distance(target) < 1.0E-8) {
            closestPoint = target;
        }
        return this.insertVertex(geometry, closestSegment.getCoordinateN(0), closestSegment.getCoordinateN(1), closestPoint);
    }

    public Geometry insertVertex(Geometry geometry, final Coordinate existing1, final Coordinate existing2, final Coordinate v) {
        if (geometry.isEmpty()) {
            return geometry;
        }
        return this.edit(geometry, new CoordinateOperation(){
            private boolean vertexInserted;
            {
                this.vertexInserted = false;
            }

            @Override
            public Coordinate[] edit(Coordinate[] coordinates, boolean linearRing) {
                if (this.vertexInserted) {
                    return coordinates;
                }
                int i = 1;
                while (i < coordinates.length) {
                    if (coordinates[i - 1].equals((Object)existing1) && coordinates[i].equals((Object)existing2) || coordinates[i - 1].equals((Object)existing2) && coordinates[i].equals((Object)existing1)) {
                        Coordinate[] newCoordinates = new Coordinate[coordinates.length + 1];
                        System.arraycopy(coordinates, 0, newCoordinates, 0, i);
                        newCoordinates[i] = v;
                        System.arraycopy(coordinates, i, newCoordinates, i + 1, coordinates.length - i);
                        this.vertexInserted = true;
                        return newCoordinates;
                    }
                    ++i;
                }
                return coordinates;
            }
        });
    }

    public Geometry deleteVertices(Geometry geometry, final Collection vertices) {
        return this.edit(geometry, new CoordinateOperation(){

            @Override
            public Coordinate[] edit(Coordinate[] coordinates, boolean linearRing) {
                ArrayList<Coordinate> newCoordinates = new ArrayList<Coordinate>(Arrays.asList(coordinates));
                boolean firstCoordinateDeleted = false;
                int j = -1;
                Iterator i = newCoordinates.iterator();
                while (i.hasNext()) {
                    Coordinate c = (Coordinate)i.next();
                    ++j;
                    if (!GeometryEditor.this.containsReference(vertices, c)) continue;
                    i.remove();
                    if (j != 0) continue;
                    firstCoordinateDeleted = true;
                }
                if (linearRing && firstCoordinateDeleted) {
                    newCoordinates.remove(newCoordinates.size() - 1);
                }
                if (linearRing && firstCoordinateDeleted && !newCoordinates.isEmpty() && !newCoordinates.get(0).equals(newCoordinates.get(newCoordinates.size() - 1))) {
                    newCoordinates.add(new Coordinate((Coordinate)newCoordinates.get(0)));
                }
                return newCoordinates.toArray(new Coordinate[0]);
            }
        });
    }

    public Geometry deleteVerticesWithClosingOption(Geometry geometry, final Collection vertices) {
        return this.edit(geometry, new CoordinateOperation(){

            @Override
            public Coordinate[] edit(Coordinate[] coordinates, boolean linearRing) {
                boolean closed = linearRing || coordinates[0].equals2D(coordinates[coordinates.length - 1]);
                ArrayList<Coordinate> newCoordinates = new ArrayList<Coordinate>(Arrays.asList(coordinates));
                boolean firstCoordinateDeleted = false;
                int j = -1;
                Iterator i = newCoordinates.iterator();
                while (i.hasNext()) {
                    Coordinate c = (Coordinate)i.next();
                    ++j;
                    if (!GeometryEditor.this.containsReference(vertices, c)) continue;
                    i.remove();
                    if (j != 0) continue;
                    firstCoordinateDeleted = true;
                }
                if (closed && firstCoordinateDeleted) {
                    newCoordinates.remove(newCoordinates.size() - 1);
                }
                if (closed && firstCoordinateDeleted && !newCoordinates.isEmpty() && !newCoordinates.get(0).equals(newCoordinates.get(newCoordinates.size() - 1))) {
                    newCoordinates.add(new Coordinate((Coordinate)newCoordinates.get(0)));
                }
                return newCoordinates.toArray(new Coordinate[0]);
            }
        });
    }

    public boolean containsReference(Collection collection, Object o) {
        for (Object item : collection) {
            if (item != o) continue;
            return true;
        }
        return false;
    }

    private Coordinate[] atLeastNCoordinatesOrNothing(int n, Coordinate[] c) {
        return c.length >= n ? c : new Coordinate[]{};
    }

    private abstract class CoordinateOperation
    implements GeometryEditorOperation {
        private CoordinateOperation() {
        }

        @Override
        public Geometry edit(Geometry geometry) {
            if (geometry instanceof LinearRing) {
                return GeometryEditor.this.factory.createLinearRing(GeometryEditor.this.atLeastNCoordinatesOrNothing(4, this.edit(geometry.getCoordinates(), true)));
            }
            if (geometry instanceof LineString) {
                return GeometryEditor.this.factory.createLineString(GeometryEditor.this.atLeastNCoordinatesOrNothing(2, this.edit(geometry.getCoordinates(), false)));
            }
            if (geometry instanceof Point) {
                Coordinate[] newCoordinates = this.edit(geometry.getCoordinates(), false);
                Assert.isTrue((newCoordinates.length < 2 ? 1 : 0) != 0);
                return GeometryEditor.this.factory.createPoint(newCoordinates.length > 0 ? newCoordinates[0] : null);
            }
            return geometry;
        }

        public abstract Coordinate[] edit(Coordinate[] var1, boolean var2);
    }

    public static interface GeometryEditorOperation {
        public Geometry edit(Geometry var1);
    }
}

