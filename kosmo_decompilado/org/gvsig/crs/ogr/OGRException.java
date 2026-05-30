/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.ogr;

import org.apache.log4j.Logger;
import org.gvsig.crs.ogr.CrsOgrException;
import org.gvsig.crs.ogr.OGRSpatialReference;
import org.gvsig.crs.ogr.crsgdalException;
import org.saig.jump.lang.I18N;

public class OGRException
extends Exception {
    private static final Logger LOGGER = Logger.getLogger(OGRException.class);

    OGRException(String msg) {
        super(msg);
    }

    public OGRException(int n, String msg, OGRSpatialReference ORGSpace) throws CrsOgrException, crsgdalException {
        if (n == 1) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.insufficient-parameters-to-build-the-data")));
        } else if (n == 2) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.insufficient-memory-to-build-the-data")));
        } else if (n == 3) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.unsupported-geometry")));
        } else if (n == 4) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.unsupported-operation")));
        } else if (n == 5) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.some-data-is-corrupted")));
        } else if (n == 6) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.failure")));
        } else if (n == 7) {
            LOGGER.warn((Object)(String.valueOf(msg) + " - " + I18N.getString("org.gvsig.crs.ogr.OGRException.unsupported-coordinate-reference-system")));
        }
        throw new CrsOgrException(ORGSpace);
    }
}

