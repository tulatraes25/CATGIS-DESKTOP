/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

public interface IModifiableCoordinateTrace {
    public void undoLastCoordinate();

    public void redoLastCoordinate();

    public void reverseCoordinates();

    public void closeTrace();
}

