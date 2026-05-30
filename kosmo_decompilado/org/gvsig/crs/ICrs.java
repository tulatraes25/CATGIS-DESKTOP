/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs;

import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.proj.CrsProj;

public interface ICrs
extends IProjection {
    public int getCode();

    public String getWKT();

    public void setTransformationParams(String var1, String var2);

    public String getSourceTransformationParams();

    public String getTargetTransformationParams();

    public CrsWkt getCrsWkt();

    public CrsProj getCrsProj();

    public String getProj4String() throws CrsException;

    public void setTransParam(String var1);

    public String getTransParam();

    public boolean isTransInTarget();

    public void setTransInTarget(boolean var1);
}

