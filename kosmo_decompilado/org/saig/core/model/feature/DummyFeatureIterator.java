/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.feature;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.model.feature.FeatureIterator;

public class DummyFeatureIterator
implements FeatureIterator {
    @Override
    public void close() {
    }

    @Override
    public void close(boolean isCancel) {
    }

    @Override
    public boolean hasNext() throws Exception {
        return false;
    }

    @Override
    public Feature next() throws Exception {
        return null;
    }

    @Override
    public void setIgnoredUpdate(boolean ignored) {
    }
}

