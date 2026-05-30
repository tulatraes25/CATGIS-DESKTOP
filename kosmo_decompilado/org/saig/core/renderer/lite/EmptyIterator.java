/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import org.saig.core.renderer.lite.AbstractLiteIterator;

public class EmptyIterator
extends AbstractLiteIterator {
    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void next() {
        throw new IllegalStateException();
    }

    @Override
    public int currentSegment(double[] coords) {
        return 0;
    }
}

