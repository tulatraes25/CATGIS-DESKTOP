/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import org.saig.core.dao.datasource.filedatasource.shape.InvalidShapefileException;

public interface ShapeHandler {
    public int getShapeType();

    public Geometry read(EndianDataInputStream var1, GeometryFactory var2, int var3) throws IOException, InvalidShapefileException;

    public void write(Geometry var1, EndianDataOutputStream var2) throws IOException;

    public int getLength(Geometry var1);
}

