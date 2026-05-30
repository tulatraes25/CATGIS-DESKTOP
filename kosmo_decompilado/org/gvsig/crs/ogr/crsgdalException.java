/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.saig.jump.lang.I18N;

public class crsgdalException
extends Exception {
    public crsgdalException() {
        this.getStrError();
    }

    public String getStrError() {
        return I18N.getString("org.gvsig.crs.ogr.crsgdalException.it-is-not-possible-to-create-the-object");
    }
}

