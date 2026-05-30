/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.feature;

import com.vividsolutions.jump.feature.Feature;

public interface FeatureIterator {
    public Feature next() throws Exception;

    public boolean hasNext() throws Exception;

    public void close();

    public void close(boolean var1);

    public void setIgnoredUpdate(boolean var1);
}

