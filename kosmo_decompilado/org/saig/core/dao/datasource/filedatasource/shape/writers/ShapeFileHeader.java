/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.BigByteBuffer;

public class ShapeFileHeader {
    private static final Logger LOGGER = Logger.getLogger(ShapeFileHeader.class);
    public static final int SHAPE_NULL = 0;
    public static final int SHAPE_POINT = 1;
    public static final int SHAPE_POLYLINE = 3;
    public static final int SHAPE_POLYGON = 5;
    public static final int SHAPE_MULTIPOINT = 8;
    public static final int SHAPE_POINTZ = 11;
    public static final int SHAPE_POLYLINEZ = 13;
    public static final int SHAPE_POLYGONZ = 15;
    public static final int SHAPE_MULTIPOINTZ = 18;
    public static final int SHAPE_POINTM = 21;
    public static final int SHAPE_POLYLINEM = 23;
    public static final int SHAPE_POLYGONM = 25;
    public static final int SHAPE_MULTIPOINTM = 28;
    public static final int SHAPE_MULTIPATCH = 31;
    public int myFileCode = 9994;
    public int myUnused1 = 0;
    public int myUnused2 = 0;
    public int myUnused3 = 0;
    public int myUnused4 = 0;
    public int myUnused5 = 0;
    public int myFileLength = 0;
    public int myVersion = 1000;
    public int myShapeType = 0;
    public double myXmin = 0.0;
    public double myYmin = 0.0;
    public double myXmax = 0.0;
    public double myYmax = 0.0;
    public double myZmin = 0.0;
    public double myZmax = 0.0;
    public double myMmin = 0.0;
    public double myMmax = 0.0;
    private boolean myWarning = true;

    public int getFileCode() {
        return this.myFileCode;
    }

    public int getVersion() {
        return this.myVersion;
    }

    public Rectangle2D.Double getFileExtents() {
        return new Rectangle2D.Double(this.myXmin, this.myYmin, this.myXmax - this.myXmin, this.myYmax - this.myYmin);
    }

    public void setWarnings(boolean inWarning) {
        this.myWarning = inWarning;
    }

    public int getHeaderLength() {
        return 50;
    }

    public int getFileLength() {
        return this.myFileLength;
    }

    public void readHeader(BigByteBuffer in) {
        in.order(ByteOrder.BIG_ENDIAN);
        this.myFileCode = in.getInt();
        if (this.myFileCode != 9994) {
            this.warn("File Code = " + this.myFileCode + " Not equal to 9994");
        }
        this.myUnused1 = in.getInt();
        this.myUnused2 = in.getInt();
        this.myUnused3 = in.getInt();
        this.myUnused4 = in.getInt();
        this.myUnused5 = in.getInt();
        this.myFileLength = in.getInt();
        in.order(ByteOrder.LITTLE_ENDIAN);
        this.myVersion = in.getInt();
        this.myShapeType = in.getInt();
        this.myXmin = in.getDouble();
        this.myYmin = in.getDouble();
        this.myXmax = in.getDouble();
        this.myYmax = in.getDouble();
        this.myZmin = in.getDouble();
        this.myZmax = in.getDouble();
        this.myMmin = in.getDouble();
        this.myMmax = in.getDouble();
    }

    public void write(ByteBuffer out, int type, int numGeoms, int length, double minX, double minY, double maxX, double maxY, double minZ, double maxZ, double minM, double maxM) throws IOException {
        out.order(ByteOrder.BIG_ENDIAN);
        out.putInt(this.myFileCode);
        int i = 0;
        while (i < 5) {
            out.putInt(0);
            ++i;
        }
        out.putInt(length);
        out.order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(this.myVersion);
        out.putInt(type);
        out.putDouble(minX);
        out.putDouble(minY);
        out.putDouble(maxX);
        out.putDouble(maxY);
        out.order(ByteOrder.BIG_ENDIAN);
        i = 0;
        while (i < 8) {
            out.putInt(0);
            ++i;
        }
    }

    private void warn(String inWarn) {
        if (this.myWarning) {
            LOGGER.warn((Object)inWarn);
        }
    }
}

