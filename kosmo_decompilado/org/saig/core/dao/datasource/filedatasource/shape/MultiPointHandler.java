/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.MultiPoint
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import org.saig.core.dao.datasource.filedatasource.shape.InvalidShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeHandler;
import org.saig.jump.lang.I18N;

public class MultiPointHandler
implements ShapeHandler {
    protected static GeometryFactory geomFact = new GeometryFactory();
    int myShapeType = -1;

    public MultiPointHandler() {
        this.myShapeType = 8;
    }

    public MultiPointHandler(int type) throws InvalidShapefileException {
        if (type != 8 && type != 18 && type != 28) {
            throw new InvalidShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.MultiPointHandler.expected-type-to-be-8-18-or-28"));
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
            return geomFact.createMultiPoint(null);
        }
        if (shapeType != this.myShapeType) {
            throw new InvalidShapefileException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.MultiPointHandler.expected-type-code-{0}-but-got-{1}", new Object[]{String.valueOf(this.myShapeType), String.valueOf(shapeType)}));
        }
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        actualReadWords += 16;
        int numpoints = file.readIntLE();
        actualReadWords += 2;
        Coordinate[] coords = new Coordinate[numpoints];
        int t = 0;
        while (t < numpoints) {
            double x = file.readDoubleLE();
            double y = file.readDoubleLE();
            actualReadWords += 8;
            coords[t] = new Coordinate(x, y);
            ++t;
        }
        if (this.myShapeType == 18) {
            file.readDoubleLE();
            file.readDoubleLE();
            actualReadWords += 8;
            t = 0;
            while (t < numpoints) {
                double z = file.readDoubleLE();
                actualReadWords += 4;
                coords[t].z = z;
                ++t;
            }
        }
        if (this.myShapeType >= 18 && contentLength >= (fullLength = this.myShapeType == 18 ? 20 + numpoints * 8 + 8 + 4 * numpoints + 8 + 4 * numpoints : 20 + numpoints * 8 + 8 + 4 * numpoints)) {
            file.readDoubleLE();
            file.readDoubleLE();
            actualReadWords += 8;
            int t2 = 0;
            while (t2 < numpoints) {
                file.readDoubleLE();
                actualReadWords += 4;
                ++t2;
            }
        }
        while (actualReadWords < contentLength) {
            file.readShortBE();
            ++actualReadWords;
        }
        return geometryFactory.createMultiPoint(coords);
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

    @Override
    public void write(Geometry geometry, EndianDataOutputStream file) throws IOException {
        MultiPoint mp = (MultiPoint)geometry;
        file.writeIntLE(this.getShapeType());
        Envelope box = mp.getEnvelopeInternal();
        file.writeDoubleLE(box.getMinX());
        file.writeDoubleLE(box.getMinY());
        file.writeDoubleLE(box.getMaxX());
        file.writeDoubleLE(box.getMaxY());
        int numParts = mp.getNumGeometries();
        file.writeIntLE(numParts);
        int t = 0;
        while (t < mp.getNumGeometries()) {
            Coordinate c = mp.getGeometryN(t).getCoordinate();
            file.writeDoubleLE(c.x);
            file.writeDoubleLE(c.y);
            ++t;
        }
        if (this.myShapeType == 18) {
            double[] zExtreame = this.zMinMax((Geometry)mp);
            if (Double.isNaN(zExtreame[0])) {
                file.writeDoubleLE(0.0);
                file.writeDoubleLE(0.0);
            } else {
                file.writeDoubleLE(zExtreame[0]);
                file.writeDoubleLE(zExtreame[1]);
            }
            int t2 = 0;
            while (t2 < mp.getNumGeometries()) {
                Coordinate c = mp.getGeometryN(t2).getCoordinate();
                double z = c.z;
                if (Double.isNaN(z)) {
                    file.writeDoubleLE(0.0);
                } else {
                    file.writeDoubleLE(z);
                }
                ++t2;
            }
        }
        if (this.myShapeType >= 18) {
            file.writeDoubleLE(-1.0E41);
            file.writeDoubleLE(-1.0E41);
            t = 0;
            while (t < mp.getNumGeometries()) {
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
        MultiPoint mp = (MultiPoint)geometry;
        if (this.myShapeType == 8) {
            return mp.getNumGeometries() * 8 + 20;
        }
        if (this.myShapeType == 28) {
            return mp.getNumGeometries() * 8 + 20 + 8 + 4 * mp.getNumGeometries();
        }
        return mp.getNumGeometries() * 8 + 20 + 8 + 4 * mp.getNumGeometries() + 8 + 4 * mp.getNumGeometries();
    }
}

