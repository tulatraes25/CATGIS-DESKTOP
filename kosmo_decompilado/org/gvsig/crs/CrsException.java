/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs;

public class CrsException
extends Exception {
    private static final long serialVersionUID = 1L;

    public CrsException(Exception e) {
        super(e);
    }

    public CrsException(String msg) {
        super(msg);
    }
}

