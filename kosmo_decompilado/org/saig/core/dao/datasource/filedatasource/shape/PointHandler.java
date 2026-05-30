/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import org.saig.core.dao.datasource.filedatasource.shape.InvalidShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeHandler;
import org.saig.jump.lang.I18N;

public class PointHandler
implements ShapeHandler {
    int Ncoords = 2;
    int myShapeType = -1;

    public PointHandler(int type) throws InvalidShapefileException {
        if (type != 1 && type != 11 && type != 21) {
            throw new InvalidShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.PointHandler.expected-a-type-of-1-11-or-21"));
        }
        this.myShapeType = type;
    }

    public PointHandler() {
        this.myShapeType = 1;
    }

    @Override
    public Geometry read(EndianDataInputStream file, GeometryFactory geometryFactory, int contentLength) throws IOException, InvalidShapefileException {
        int actualReadWords = 0;
        int shapeType = file.readIntLE();
        actualReadWords += 2;
        if (shapeType != this.myShapeType) {
            throw new InvalidShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.PointHandler.handler-shapetype-does-not-match-file-shapetype"));
        }
        double x = file.readDoubleLE();
        double y = file.readDoubleLE();
        double z = Double.NaN;
        actualReadWords += 8;
        if (shapeType == 11) {
            z = file.readDoubleLE();
            actualReadWords += 4;
        }
        if (shapeType >= 11) {
            double m = file.readDoubleLE();
            actualReadWords += 4;
        }
        while (actualReadWords < contentLength) {
            file.readShortBE();
            ++actualReadWords;
        }
        return geometryFactory.createPoint(new Coordinate(x, y, z));
    }

    @Override
    public void write(Geometry geometry, EndianDataOutputStream file) throws IOException {
        file.writeIntLE(this.getShapeType());
        Coordinate c = geometry.getCoordinates()[0];
        file.writeDoubleLE(c.x);
        file.writeDoubleLE(c.y);
        if (this.myShapeType == 11) {
            if (Double.isNaN(c.z)) {
                file.writeDoubleLE(0.0);
            } else {
                file.writeDoubleLE(c.z);
            }
        }
        if (this.myShapeType == 11 || this.myShapeType == 21) {
            file.writeDoubleLE(-1.0E41);
        }
    }

    @Override
    public int getShapeType() {
        return this.myShapeType;
    }

    @Override
    public int getLength(Geometry geometry) {
        if (this.myShapeType == 21) {
            return 10;
        }
        if (this.myShapeType == 11) {
            return 14;
        }
        return 10;
    }
}

