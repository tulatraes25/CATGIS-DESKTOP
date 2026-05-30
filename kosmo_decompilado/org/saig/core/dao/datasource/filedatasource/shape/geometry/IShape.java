/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.Shape;
import java.io.Serializable;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public interface IShape
extends Shape,
Serializable {
    public static final int NULL = 0;
    public static final int POINT2D = 1;
    public static final int LINE = 2;
    public static final int POLYGON = 4;
    public static final int TEXT = 8;
    public static final int MULTI = 16;
    public static final int MULTIPOINT = 32;
    public static final int CIRCLE = 64;
    public static final int ARC = 128;
    public static final int ELLIPSE = 256;
    public static final int Z = 512;
    public static final int SHAPE_TYPE_NULL = 0;
    public static final int SHAPE_TYPE_POINT = 1;
    public static final int SHAPE_TYPE_POLYLINE = 3;
    public static final int SHAPE_TYPE_POLYGON = 5;
    public static final int SHAPE_TYPE_MULTIPOINT = 8;
    public static final int SHAPE_TYPE_POINTZ = 11;
    public static final int SHAPE_TYPE_POLYLINEZ = 13;
    public static final int SHAPE_TYPE_POLYGONZ = 15;
    public static final int SHAPE_TYPE_MULTIPOINTZ = 18;
    public static final int SHAPE_TYPE_POINTM = 21;
    public static final int SHAPE_TYPE_POLYLINEM = 23;
    public static final int SHAPE_TYPE_POLYGONM = 25;
    public static final int SHAPE_TYPE_MULTIPOINTM = 28;
    public static final int SHAPE_TYPE_MULTIPATCH = 31;
    public static final int FIELD_TYPE_BOOLEAN = 0;
    public static final int FIELD_TYPE_INT = 1;
    public static final int FIELD_TYPE_LONGINT = 2;
    public static final int FIELD_TYPE_FLOAT = 3;
    public static final int FIELD_TYPE_DOUBLE = 4;
    public static final int FIELD_TYPE_STRING = 5;
    public static final int FIELD_TYPE_DATE = 6;

    public int getShapeType();

    public IShape cloneShape();

    public SAIGGeneralPath getGeneralPath();

    public void reProject(ICoordTrans var1);

    public boolean isEmpty();
}

