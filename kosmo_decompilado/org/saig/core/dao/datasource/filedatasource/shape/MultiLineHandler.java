/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import org.saig.core.dao.datasource.filedatasource.shape.InvalidShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeHandler;
import org.saig.jump.lang.I18N;

public class MultiLineHandler
implements ShapeHandler {
    protected static GeometryFactory geomFact = new GeometryFactory();
    int myShapeType = -1;

    public MultiLineHandler() {
        this.myShapeType = 3;
    }

    public MultiLineHandler(int type) throws InvalidShapefileException {
        if (type != 3 && type != 13 && type != 23) {
            throw new InvalidShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.MultiLineHandler.expected-type-to-be-3-13-or-23"));
        }
        this.myShapeType = type;
    }

    @Override
    public Geometry read(EndianDataInputStream file, GeometryFactory geometryFactory, int contentLength) throws IOException, InvalidShapefileException {
        int fullLength;
        int actualReadWords = 0;
        int shapeType = file.readIntLE();
        actualReadWords += 2;
        if (shapeType == 0) {
            return geomFact.createMultiLineString(null);
        }
        if (shapeType != this.myShapeType) {
            throw new InvalidShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.MultiLineHandler.file-says-its-type-{0}-but-i-am-expecting-type-{1}", new Object[]{String.valueOf(shapeType), "" + this.myShapeType}));
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
        LineString[] lines = new LineString[numParts];
        Coordinate[] coords = new Coordinate[numPoints];
        int t = 0;
        while (t < numPoints) {
            coords[t] = new Coordinate(file.readDoubleLE(), file.readDoubleLE());
            actualReadWords += 8;
            ++t;
        }
        if (this.myShapeType == 13) {
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
        if (this.myShapeType >= 13 && contentLength >= (fullLength = this.myShapeType == 13 ? 22 + 2 * numParts + numPoints * 8 + 4 + 4 + 4 * numPoints + 4 + 4 + 4 * numPoints : 22 + 2 * numParts + numPoints * 8 + 4 + 4 + 4 * numPoints)) {
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
            file.readShortBE();
            ++actualReadWords;
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
            lines[part] = geometryFactory.createLineString(points);
            ++part;
        }
        if (numParts == 1) {
            return lines[0];
        }
        return geometryFactory.createMultiLineString(lines);
    }

    @Override
    public void write(Geometry geometry, EndianDataOutputStream file) throws IOException {
        MultiLineString multi = (MultiLineString)geometry;
        file.writeIntLE(this.getShapeType());
        Envelope box = multi.getEnvelopeInternal();
        file.writeDoubleLE(box.getMinX());
        file.writeDoubleLE(box.getMinY());
        file.writeDoubleLE(box.getMaxX());
        file.writeDoubleLE(box.getMaxY());
        int numParts = multi.getNumGeometries();
        file.writeIntLE(numParts);
        int npoints = multi.getNumPoints();
        file.writeIntLE(npoints);
        LineString[] lines = new LineString[numParts];
        int idx = 0;
        int i = 0;
        while (i < numParts) {
            lines[i] = (LineString)multi.getGeometryN(i);
            file.writeIntLE(idx);
            idx += lines[i].getNumPoints();
            ++i;
        }
        Coordinate[] coords = multi.getCoordinates();
        int t = 0;
        while (t < npoints) {
            file.writeDoubleLE(coords[t].x);
            file.writeDoubleLE(coords[t].y);
            ++t;
        }
        if (this.myShapeType == 13) {
            double[] zExtreame = this.zMinMax((Geometry)multi);
            if (Double.isNaN(zExtreame[0])) {
                file.writeDoubleLE(0.0);
                file.writeDoubleLE(0.0);
            } else {
                file.writeDoubleLE(zExtreame[0]);
                file.writeDoubleLE(zExtreame[1]);
            }
            int t2 = 0;
            while (t2 < npoints) {
                double z = coords[t2].z;
                if (Double.isNaN(z)) {
                    file.writeDoubleLE(0.0);
                } else {
                    file.writeDoubleLE(z);
                }
                ++t2;
            }
        }
        if (this.myShapeType >= 13) {
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
        MultiLineString multi = (MultiLineString)geometry;
        int numlines = multi.getNumGeometries();
        int numpoints = multi.getNumPoints();
        if (this.myShapeType == 3) {
            return 22 + 2 * numlines + numpoints * 8;
        }
        if (this.myShapeType == 23) {
            return 22 + 2 * numlines + numpoints * 8 + 4 + 4 + 4 * numpoints;
        }
        return 22 + 2 * numlines + numpoints * 8 + 4 + 4 + 4 * numpoints + 4 + 4 + 4 * numpoints;
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

