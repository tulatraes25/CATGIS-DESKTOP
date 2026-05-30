/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.proj;

import org.apache.log4j.Logger;
import org.gvsig.crs.proj.CrsProjException;
import org.gvsig.crs.proj.JNIBaseCrs;

public class CrsProj
extends JNIBaseCrs {
    private static final Logger LOGGER = Logger.getLogger(CrsProj.class);

    public CrsProj(String strCrs) {
        try {
            this.createCrs(strCrs);
        }
        catch (CrsProjException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    protected void finalize() {
        this.deleteCrs();
    }
}

