/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.ogr;

import org.apache.log4j.Logger;
import org.gvsig.crs.ogr.JNIBase;
import org.gvsig.crs.ogr.crsgdalException;

public class OGRSpatialReference
extends JNIBase {
    private static Logger LOGGER = Logger.getLogger(OGRSpatialReference.class);

    public OGRSpatialReference() {
        try {
            this.createOGRSpatialReference();
        }
        catch (crsgdalException exp) {
            LOGGER.error((Object)(String.valueOf(exp.getStrError()) + " - " + exp));
        }
    }

    @Override
    public void deleteOGRSpatialReference() {
        try {
            super.deleteOGRSpatialReference();
        }
        catch (crsgdalException e) {
            LOGGER.error((Object)(String.valueOf(e.getStrError()) + " - " + e));
        }
    }
}

