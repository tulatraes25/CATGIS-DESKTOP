/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 */
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import org.geotools.data.oracle.sdo.CoordinateAccess;

public interface CoordinateAccessFactory
extends CoordinateSequenceFactory {
    public CoordinateAccess create(double[][] var1, Object[] var2);

    public int getDimension();

    public int getNumAttributes();
}

