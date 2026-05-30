/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IDatum
 */
package org.gvsig.crs;

import org.cresques.cts.IDatum;

public class CRSDatum
implements IDatum {
    private double eSemiMajorAxis = 0.0;
    private double eIFlattening = 0.0;

    public CRSDatum(double semiMajorAxis, double flattening) {
        this.eSemiMajorAxis = semiMajorAxis;
        this.eIFlattening = flattening;
    }

    public double getESemiMajorAxis() {
        return this.eSemiMajorAxis;
    }

    public double getEIFlattening() {
        return this.eIFlattening;
    }
}

