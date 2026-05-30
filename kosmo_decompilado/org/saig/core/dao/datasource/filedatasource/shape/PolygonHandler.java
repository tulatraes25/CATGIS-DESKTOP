/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.dao.datasource.filedatasource.shape.InvalidShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeHandler;
import org.saig.jump.lang.I18N;

public class PolygonHandler
implements ShapeHandler {
    protected static GeometryFactory geomFact = new GeometryFactory();
    int myShapeType;

    public PolygonHandler() {
        this.myShapeType = 5;
    }

    public PolygonHandler(int type) throws InvalidShapefileException {
        if (type != 5 && type != 15 && type != 25) {
            throw new InvalidShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.PolygonHandler.expected-type-to-be-5-15-or-25"));
        }
        this.myShapeType = type;
    }

    boolean pointInList(Coordinate testPoint, Coordinate[] pointList) {
        int numpoints = Array.getLength(pointList);
        int t = 0;
        while (t < numpoints) {
            Coordinate p = pointList[t];
            if (testPoint.x == p.x && testPoint.y == p.y && (testPoint.z == p.z || testPoint.z != testPoint.z)) {
                return true;
            }
            ++t;
        }
        return false;
    }

    @Override
    public Geometry read(EndianDataInputStream file, GeometryFactory geometryFactory, int contentLength) throws IOException, InvalidShapefileException {
        int fullLength;
        int actualReadWords = 0;
        int shapeType = file.readIntLE();
        actualReadWords += 2;
        if (shapeType == 0) {
            return geomFact.createMultiPolygon(null);
        }
        if (shapeType != this.myShapeType) {
            throw new InvalidShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.PolygonHandler.got-shape-type-{0}-but-was-expecting-{1}", new Object[]{String.valueOf(shapeType), String.valueOf(this.myShapeType)}));
        }
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        actualReadWords += 16;
        int numParts = file.readIntLE();
        int numPoints = file.readIntLE();
        actualReadWords += 4;
        int[] partOffsets = new int[numParts];
        int i = 0;
        while (i < numParts) {
            partOffsets[i] = file.readIntLE();
            actualReadWords += 2;
            ++i;
        }
        ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        Coordinate[] coords = new Coordinate[numPoints];
        int t = 0;
        while (t < numPoints) {
            try {
                coords[t] = new Coordinate(file.readDoubleLE(), file.readDoubleLE());
                actualReadWords += 8;
            }
            catch (IOException e) {
                break;
            }
            ++t;
        }
        if (this.myShapeType == 15) {
            file.readDoubleLE();
            file.readDoubleLE();
            actualReadWords += 8;
            t = 0;
            while (t < numPoints) {
                coords[t].z = file.readDoubleLE();
                actualReadWords += 4;
                ++t;
            }
        }
        if (this.myShapeType >= 15 && contentLength >= (fullLength = this.myShapeType == 15 ? 22 + 2 * numParts + 8 * numPoints + 8 + 4 * numPoints + 8 + 4 * numPoints : 22 + 2 * numParts + 8 * numPoints + 8 + 4 * numPoints)) {
            file.readDoubleLE();
            file.readDoubleLE();
            actualReadWords += 8;
            int t2 = 0;
            while (t2 < numPoints) {
                file.readDoubleLE();
                actualReadWords += 4;
                ++t2;
            }
        }
        while (actualReadWords < contentLength) {
            try {
                file.readShortBE();
                ++actualReadWords;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        int offset = 0;
        int part = 0;
        while (part < numParts) {
            int start = partOffsets[part];
            int finish = part == numParts - 1 ? numPoints : partOffsets[part + 1];
            int length = finish - start;
            Coordinate[] points = new Coordinate[length];
            int i2 = 0;
            while (i2 < length) {
                points[i2] = coords[offset];
                ++offset;
                ++i2;
            }
            LinearRing ring = geometryFactory.createLinearRing(points);
            if (CGAlgorithms.isCCW((Coordinate[])points)) {
                holes.add(ring);
            } else {
                shells.add(ring);
            }
            ++part;
        }
        ArrayList holesForShells = new ArrayList(shells.size());
        int i3 = 0;
        while (i3 < shells.size()) {
            holesForShells.add(new ArrayList());
            ++i3;
        }
        i3 = 0;
        while (i3 < holes.size()) {
            LinearRing testRing = (LinearRing)holes.get(i3);
            LinearRing minShell = null;
            Envelope minEnv = null;
            Envelope testEnv = testRing.getEnvelopeInternal();
            Coordinate testPt = testRing.getCoordinateN(0);
            int j = 0;
            while (j < shells.size()) {
                LinearRing tryRing = (LinearRing)shells.get(j);
                Envelope tryEnv = tryRing.getEnvelopeInternal();
                if (minShell != null) {
                    minEnv = minShell.getEnvelopeInternal();
                }
                boolean isContained = false;
                Coordinate[] coordList = tryRing.getCoordinates();
                if (tryEnv.contains(testEnv) && (CGAlgorithms.isPointInRing((Coordinate)testPt, (Coordinate[])coordList) || this.pointInList(testPt, coordList))) {
                    isContained = true;
                }
                if (isContained && (minShell == null || minEnv.contains(tryEnv))) {
                    minShell = tryRing;
                }
                ++j;
            }
            if (minShell == null) {
                System.out.println(I18N.getString("org.saig.core.dao.datasource.filedatasource.PolygonHandler.polygon-found-with-a-hole-that-is-not-inside-a-shell"));
            } else {
                ((List)holesForShells.get(shells.indexOf(minShell))).add(testRing);
            }
            ++i3;
        }
        Polygon[] polygons = new Polygon[shells.size()];
        int i4 = 0;
        while (i4 < shells.size()) {
            polygons[i4] = geometryFactory.createPolygon((LinearRing)shells.get(i4), ((List)holesForShells.get(i4)).toArray(new LinearRing[0]));
            ++i4;
        }
        if (polygons.length == 1) {
            return polygons[0];
        }
        holesForShells = null;
        shells = null;
        holes = null;
        MultiPolygon result = geometryFactory.createMultiPolygon(polygons);
        return result;
    }

    @Override
    public void write(Geometry geometry, EndianDataOutputStream file) throws IOException {
        MultiPolygon multi;
        if (geometry instanceof MultiPolygon) {
            multi = (MultiPolygon)geometry;
        } else {
            multi = geomFact.createMultiPolygon(new Polygon[]{(Polygon)geometry});
            multi.setSRID(geometry.getSRID());
        }
        file.writeIntLE(this.getShapeType());
        Envelope box = multi.getEnvelopeInternal();
        file.writeDoubleLE(box.getMinX());
        file.writeDoubleLE(box.getMinY());
        file.writeDoubleLE(box.getMaxX());
        file.writeDoubleLE(box.getMaxY());
        int nrings = 0;
        int t = 0;
        while (t < multi.getNumGeometries()) {
            Polygon p = (Polygon)multi.getGeometryN(t);
            nrings = nrings + 1 + p.getNumInteriorRing();
            ++t;
        }
        int u = 0;
        int[] pointsPerRing = new int[nrings];
        int t2 = 0;
        while (t2 < multi.getNumGeometries()) {
            Polygon p = (Polygon)multi.getGeometryN(t2);
            pointsPerRing[u] = p.getExteriorRing().getNumPoints();
            ++u;
            int v = 0;
            while (v < p.getNumInteriorRing()) {
                pointsPerRing[u] = p.getInteriorRingN(v).getNumPoints();
                ++u;
                ++v;
            }
            ++t2;
        }
        int npoints = multi.getNumPoints();
        file.writeIntLE(nrings);
        file.writeIntLE(npoints);
        int count = 0;
        int t3 = 0;
        while (t3 < nrings) {
            file.writeIntLE(count);
            count += pointsPerRing[t3];
            ++t3;
        }
        Coordinate[] coords = multi.getCoordinates();
        int num = Array.getLength(coords);
        int t4 = 0;
        while (t4 < num) {
            file.writeDoubleLE(coords[t4].x);
            file.writeDoubleLE(coords[t4].y);
            ++t4;
        }
        if (this.myShapeType == 15) {
            double[] zExtreame = this.zMinMax((Geometry)multi);
            if (Double.isNaN(zExtreame[0])) {
                file.writeDoubleLE(0.0);
                file.writeDoubleLE(0.0);
            } else {
                file.writeDoubleLE(zExtreame[0]);
                file.writeDoubleLE(zExtreame[1]);
            }
            int t5 = 0;
            while (t5 < npoints) {
                double z = coords[t5].z;
                if (Double.isNaN(z)) {
                    file.writeDoubleLE(0.0);
                } else {
                    file.writeDoubleLE(z);
                }
                ++t5;
            }
        }
        if (this.myShapeType >= 15) {
            file.writeDoubleLE(-1.0E41);
            file.writeDoubleLE(-1.0E41);
            t = 0;
            while (t < npoints) {
                file.writeDoubleLE(-1.0E41);
                ++t;
            }
        }
    }

    @Override
    public int getShapeType() {
        return this.myShapeType;
    }

    @Override
    public int getLength(Geometry geometry) {
        MultiPolygon multi;
        if (geometry instanceof MultiPolygon) {
            multi = (MultiPolygon)geometry;
        } else {
            multi = geomFact.createMultiPolygon(new Polygon[]{(Polygon)geometry});
            multi.setSRID(geometry.getSRID());
        }
        int nrings = 0;
        int t = 0;
        while (t < multi.getNumGeometries()) {
            Polygon p = (Polygon)multi.getGeometryN(t);
            nrings = nrings + 1 + p.getNumInteriorRing();
            ++t;
        }
        int npoints = multi.getNumPoints();
        if (this.myShapeType == 15) {
            return 22 + 2 * nrings + 8 * npoints + 4 * npoints + 8 + 4 * npoints + 8;
        }
        if (this.myShapeType == 25) {
            return 22 + 2 * nrings + 8 * npoints + 4 * npoints + 8;
        }
        return 22 + 2 * nrings + 8 * npoints;
    }

    double[] zMinMax(Geometry g) {
        boolean validZFound = false;
        Coordinate[] cs = g.getCoordinates();
        double[] result = new double[2];
        double zmin = Double.NaN;
        double zmax = Double.NaN;
        int t = 0;
        while (t < cs.length) {
            double z = cs[t].z;
            if (!Double.isNaN(z)) {
                if (validZFound) {
                    if (z < zmin) {
                        zmin = z;
                    }
                    if (z > zmax) {
                        zmax = z;
                    }
                } else {
                    validZFound = true;
                    zmin = z;
                    zmax = z;
                }
            }
            ++t;
        }
        result[0] = zmin;
        result[1] = zmax;
        return result;
    }
}

