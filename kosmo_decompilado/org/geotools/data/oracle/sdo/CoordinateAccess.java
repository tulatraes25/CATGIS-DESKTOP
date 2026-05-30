/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 */
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.geom.CoordinateSequence;

public interface CoordinateAccess
extends CoordinateSequence {
    public int getDimension();

    public int getNumAttributes();

    public double getOrdinate(int var1, int var2);

    public Object getAttribute(int var1, int var2);

    public void setOrdinate(int var1, int var2, double var3);

    public void setAttribute(int var1, int var2, Object var3);

    public double[] toOrdinateArray(int var1);

    public Object[] toAttributeArray(int var1);

    public void setOrdinateArray(int var1, double[] var2);

    public void setAttributeArray(int var1, Object var2);

    public double[][] toOrdinateArrays();

    public Object[] toAttributeArrays();

    public void setCoordinateArrays(double[][] var1, Object[] var2);

    public void setAt(int var1, double[] var2, Object[] var3);
}

