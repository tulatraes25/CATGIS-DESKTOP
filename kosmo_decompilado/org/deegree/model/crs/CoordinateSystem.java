/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.crs.coordinatesystems.CoordinateSystem
 *  org.deegree.datatypes.QualifiedName
 */
package org.deegree.model.crs;

import java.net.URI;
import org.deegree.datatypes.QualifiedName;

public class CoordinateSystem
extends QualifiedName {
    private static final long serialVersionUID = -170831086069691683L;
    private org.deegree.crs.coordinatesystems.CoordinateSystem realCRS;
    private String requestedID;

    @Deprecated
    CoordinateSystem(String prefix, String localName, URI namespace) {
        super(prefix.toLowerCase(), localName, namespace);
        this.requestedID = localName;
    }

    @Deprecated
    CoordinateSystem(String name, URI namespace) {
        super(name, namespace);
        this.requestedID = name;
    }

    @Deprecated
    protected CoordinateSystem(String name) {
        super(name);
        this.requestedID = name;
    }

    CoordinateSystem(org.deegree.crs.coordinatesystems.CoordinateSystem realCRS, String requestedID) {
        super(realCRS.getIdentifier());
        this.realCRS = realCRS;
        this.requestedID = requestedID;
    }

    @Deprecated
    public String getName() {
        return this.getIdentifier();
    }

    public String getIdentifier() {
        return this.requestedID;
    }

    public String getPrefixedName() {
        return this.getIdentifier();
    }

    @Deprecated
    public String getCode() {
        return this.getLocalName();
    }

    public String getUnits() {
        return this.realCRS.getUnits().toString();
    }

    public int getDimension() {
        return this.realCRS.getDimension();
    }

    public org.deegree.crs.coordinatesystems.CoordinateSystem getCRS() {
        return this.realCRS;
    }

    public boolean equals(Object other) {
        if (other != null && other instanceof CoordinateSystem) {
            CoordinateSystem that = (CoordinateSystem)((Object)other);
            return this.realCRS.equals((Object)that.realCRS);
        }
        return false;
    }

    public void setIdentifier(String id) {
        this.requestedID = id;
    }
}

