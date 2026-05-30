/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.gvsig.crs.ogr.crsgdalException;

public class JNIBase {
    protected long cPtr;

    static {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("windows")) {
            System.loadLibrary("jgdal092");
        } else {
            System.loadLibrary("jgdal");
        }
    }

    protected native long OGRSpatialReferenceNat() throws crsgdalException;

    protected native void OGRDestroySpatialReferenceNat(long var1);

    protected long createOGRSpatialReference() throws crsgdalException {
        this.cPtr = this.OGRSpatialReferenceNat();
        return this.cPtr;
    }

    protected void deleteOGRSpatialReference() throws crsgdalException {
        if (this.cPtr > 0L) {
            this.OGRDestroySpatialReferenceNat(this.cPtr);
        }
    }

    protected long getPtr() throws crsgdalException {
        return this.cPtr;
    }
}

