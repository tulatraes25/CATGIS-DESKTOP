/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.gvsig.crs.ogr.OGRSpatialReference;
import org.gvsig.crs.ogr.crsgdalException;
import org.saig.jump.lang.I18N;

public class CrsOgrException
extends Exception {
    private long ORGSpace;

    public CrsOgrException(OGRSpatialReference ORGS) throws crsgdalException {
        this.ORGSpace = ORGS.getPtr();
    }

    public String getStrError() {
        return I18N.getString("org.gvsig.crs.ogr.CrsOgrException.an-error-has-been-produced-exporting-the-proj4-string");
    }
}

